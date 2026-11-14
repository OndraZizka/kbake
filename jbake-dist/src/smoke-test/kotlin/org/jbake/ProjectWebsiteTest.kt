package org.jbake

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName
import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.time.Duration

/** End-to-end bake of the local OZ.cz website sources, with a small checked-in fallback fixture for CI. */
class ProjectWebsiteTest : StringSpec({

    "should bake every Texy page and preserve the website assets" {
        val sourceRoot = findWebsiteSource()
        val keepOutput = System.getProperty("jbake.website.keepOutput", "false").toBoolean()
        val projectDir = if (keepOutput) {
            buildOutputDir.resolve("website-preview").toFile().apply {
                deleteRecursively()
                mkdirs()
            }
        } else {
            Files.createTempDirectory(buildOutputDir, "jbake-website-test-").toFile()
        }
        val contentDir = projectDir.resolve("content")
        val assetsDir = projectDir.resolve("assets")
        val templatesDir = projectDir.resolve("templates")

        try {
            val pagesDir = sourceRoot.resolve("pages")
            val texyFiles = stageWebsite(sourceRoot, pagesDir, contentDir, assetsDir)
            check(texyFiles.isNotEmpty()) { "No .texy pages found under ${pagesDir.absolutePath}" }

            Files.createDirectories(templatesDir.toPath())
            copyClasspathResource("website-fixture/templates/page.ftl", templatesDir.resolve("page.ftl").toPath())

            val imageName = System.getProperty("jbake.texy.image", "jbake/texy-service:latest")
            GenericContainer(DockerImageName.parse(imageName))
                .withExposedPorts(TEXY_PORT)
                .waitingFor(Wait.forHttp("/").forStatusCode(200))
                .withStartupTimeout(Duration.ofMinutes(2))
                .use { texyService ->
                    texyService.start()
                    val serviceUrl = "http://${texyService.host}:${texyService.getMappedPort(TEXY_PORT)}/texy"
                    projectDir.resolve("jbake.properties").writeText(
                        """
                        content.folder=content
                        asset.folder=assets
                        template.folder=templates
                        destination.folder=output
                        template.page.file=page.ftl
                        default.status=published
                        default.type=page
                        site.host=https://ondra.zizka.cz
                        render.index=false
                        render.archive=false
                        render.feed=false
                        render.sitemap=false
                        render.tags=false
                        render.tagsindex=false
                        render.error404=false
                        texy.service.url=$serviceUrl
                        texy.connection.timeout=10000
                        texy.read.timeout=30000
                        """.trimIndent()
                    )

                    val runner = BinaryRunner(projectDir)
                    val executable = BinaryRunner.jbakeExecutableRelative.absolutePath
                    val process = runner.runWithArguments(executable, "-vv", "-b")
                    withClue(
                        "JBake failed to bake ${texyFiles.size} Texy pages from ${sourceRoot.absolutePath}.\n" +
                            "Process output:\n${runner.processOutput}\nExit code: ${process.exitValue()}"
                    ) {
                        process.exitValue() shouldBe 0
                    }

                    val outputDir = projectDir.resolve("output")
                    val expectedOutputs = texyFiles.map { source -> expectedOutputPath(pagesDir, source) }
                    val missing = expectedOutputs.filterNot { outputDir.resolve(it).isFile }
                    withClue("Missing ${missing.size} of ${expectedOutputs.size} expected rendered pages: ${missing.take(20)}") {
                        missing.size shouldBe 0
                    }

                    val homePage = outputDir.resolve("index.html")
                    check(homePage.isFile) { "The website home page was not generated at ${homePage.absolutePath}" }
                    val homeHtml = homePage.readText()
                    check("Ondra Žižka" in homeHtml) { "The generated home page is missing its site heading." }
                    check("Error rendering Texy content" !in homeHtml) {
                        "Texy failed to render the website home page; check the configured Texy service."
                    }

                    check(outputDir.resolve("static/robots.txt").isFile) { "Static website files were not copied." }
                    check(outputDir.resolve("css/styl.css").isFile) { "The legacy website stylesheet was not copied." }
                    if (sourceRoot.resolve("pages/economy/investing/index.html").isFile) {
                        check(outputDir.resolve("pages/economy/investing/index.html").isFile) {
                            "A pre-rendered HTML page was not preserved as a static asset."
                        }
                    }
                    println("Baked ${expectedOutputs.size} Texy pages from ${sourceRoot.absolutePath} into ${outputDir.absolutePath}")
                }
        } finally {
            if (!keepOutput) projectDir.deleteRecursively()
        }
    }
})

private fun findWebsiteSource(): File {
    val configuredPath = System.getProperty("jbake.website.dir")
        ?: System.getenv("JBAKE_WEBSITE_DIR")
    val candidates = listOfNotNull(
        configuredPath?.let(::File),
        File(System.getProperty("user.home"), "uw/ondra.zizka.cz-web"),
        classpathDirectory("website-fixture")
    )
    return candidates.firstOrNull { it.resolve("pages").isDirectory }
        ?: error("Could not find website sources. Set -Djbake.website.dir or JBAKE_WEBSITE_DIR.")
}

private fun stageWebsite(sourceRoot: File, pagesDir: File, contentDir: File, assetsDir: File): List<File> {
    val texyFiles = mutableListOf<File>()
    Files.walk(pagesDir.toPath()).use { paths ->
        paths.filter { Files.isRegularFile(it) }.sorted().forEach { source ->
            val relative = pagesDir.toPath().relativize(source)
            val target = if (source.fileName.toString().endsWith(".texy")) {
                val targetRelative = if (relative.toString() == "index.texy") relative else Path.of("pages").resolve(relative)
                contentDir.toPath().resolve(targetRelative)
                    .also { texyFiles.add(source.toFile()) }
            } else {
                assetsDir.toPath().resolve("pages").resolve(relative)
            }
            copyFile(source, target)
        }
    }

    copyTree(sourceRoot.resolve("static").toPath(), assetsDir.toPath().resolve("static"))
    copyTree(sourceRoot.resolve("src/main/static").toPath(), assetsDir.toPath().resolve("static"))
    val legacyResources = sourceRoot.toPath().resolve("src/main/java/cz/oz/web/_co/baseLayout")
    copyTree(legacyResources.resolve("css"), assetsDir.toPath().resolve("css"))
    copyTree(legacyResources.resolve("js"), assetsDir.toPath().resolve("js"))
    copyTree(legacyResources.resolve("img"), assetsDir.toPath().resolve("img"))
    return texyFiles
}

private fun expectedOutputPath(pagesDir: File, source: File): String {
    val relative = pagesDir.toPath().relativize(source.toPath())
    if (relative.toString() == "index.texy") return "index.html"
    val parent = relative.parent?.toString()?.replace(File.separatorChar, '/')?.let { "$it/" } ?: ""
    val baseName = relative.fileName.toString().removeSuffix(".texy")
    return "pages/$parent${URLEncoder.encode(baseName, StandardCharsets.UTF_8.name())}.html"
}

private fun copyTree(source: Path, destination: Path) {
    if (!Files.isDirectory(source)) return
    Files.walk(source).use { paths ->
        paths.filter { Files.isRegularFile(it) }.forEach { file ->
            copyFile(file, destination.resolve(source.relativize(file)))
        }
    }
}

private fun copyFile(source: Path, destination: Path) {
    Files.createDirectories(destination.parent)
    Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING)
}

private fun copyClasspathResource(resourceName: String, destination: Path) {
    val resource = ProjectWebsiteTest::class.java.classLoader.getResourceAsStream(resourceName)
        ?: error("Required smoke-test resource '$resourceName' is missing.")
    resource.use { Files.copy(it, destination, StandardCopyOption.REPLACE_EXISTING) }
}

private fun classpathDirectory(resourceName: String): File? = runCatching {
    ProjectWebsiteTest::class.java.classLoader.getResource(resourceName)?.toURI()?.let { Path.of(it).toFile() }
}.getOrNull()

private const val TEXY_PORT = 8080
