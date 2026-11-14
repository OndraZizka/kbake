package org.jbake.maven

import org.apache.commons.io.IOUtils
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.util.zip.ZipInputStream

/**
 * Seeds a new KBake Template into the (non-existing) directory defined by outputDirectory
 */
@Mojo(name = "seed", requiresProject = true, requiresDirectInvocation = true)
class SeedMojo : AbstractMojo() {

    /** The URL that will provide the Seeding zip file to download. */
    @Parameter(
        property = "kbake.seedUrl",
        defaultValue = "https://github.com/jbake-org/jbake-template-bootstrap/zipball/master/",
        required = true
    )
    private var seedUrl: String? = null

    /** Where to unzip to. */
    @Parameter(property = "kbake.outputDirectory", defaultValue = $$"${project.basedir}/src/main/kbake", required = true)
    private lateinit var outputDirectory: File

    /** Overwrite the output dir if exists? Default false. */
    @Parameter(property = "kbake.force", defaultValue = "false")
    private var force: Boolean = false

    @Throws(MojoExecutionException::class)
    override fun execute() {
        if (outputDirectory.exists() && !force)
            throw MojoExecutionException("The outputDirectory ${outputDirectory.name} must *NOT* exist. Invoke with kbake.force as true to disregard")

        try {
            val url = URL(seedUrl)
            val tmpZipFile = File.createTempFile("kbake", ".zip")

            log.info("Downloading KBake template from: $seedUrl to temporary file: ${tmpZipFile.absolutePath}")

            val length = FileOutputStream(tmpZipFile).use { fos -> IOUtils.copy(url.openStream(), fos) }

            log.info("Downloaded $length bytes. Unpacking template to output directory: ${outputDirectory.absolutePath}")

            unpackZip(tmpZipFile)
            log.info("KBake template successfully seeded into: ${outputDirectory.absolutePath}")
        } catch (e: Exception) {
            val message = e.message ?: "unknown download or extraction error"
            log.error("Failed to seed KBake template into '${outputDirectory.absolutePath}': $message")
            log.debug("KBake seed failure details", e)
            throw MojoExecutionException("Failed to seed KBake template into ${outputDirectory.absolutePath}: $message", e)
        }
    }

    @Throws(IOException::class)
    private fun unpackZip(tmpZipFile: File) {
        val zis = ZipInputStream(FileInputStream(tmpZipFile))
        // Get the zipped file list entry.
        var ze = zis.getNextEntry()

        while (ze != null) {
            val fileName = stripLeadingPath(ze.getName())
            if (fileName.isEmpty() || ze.isDirectory) {
                ze = zis.getNextEntry()
                continue
            }
            val newFile = outputDirectory.resolve(fileName)
            newFile.parentFile?.mkdirs()
            FileOutputStream(newFile).use { fos ->
                IOUtils.copy(zis, fos)
            }
            ze = zis.getNextEntry()
        }

        zis.closeEntry()
        zis.close()
    }

    /**
     * Strips the first path segment from a zip entry name.
     *
     * GitHub zipball archives contain a top-level directory (e.g., "user-repo-hash/")
     * that we want to skip when extracting. This function removes that leading segment.
     *
     * Example: "kbake-template-abc123/templates/post.ftl" → "templates/post.ftl"
     *
     * @param name the full path from the zip entry
     * @return the path with the first segment removed, or empty string if only one segment
     */
    private fun stripLeadingPath(name: String): String {
        //val elements = LinkedList(listOf(*name.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())).apply { pop() }
        //return StringUtils.join(elements.iterator(), '/')

        val path = java.nio.file.Path.of(name)
        return if (path.nameCount > 1) path.subpath(1, path.nameCount).toString() else ""
    }
}
