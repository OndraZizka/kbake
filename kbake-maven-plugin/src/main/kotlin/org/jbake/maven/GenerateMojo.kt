package org.jbake.maven

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.plugins.annotations.ResolutionScope
import org.apache.maven.project.MavenProject
import org.jbake.app.JBakeException
import org.jbake.app.Oven
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.app.configuration.JBakeConfigurationFactory
import java.io.File

/** Runs KBake on a directory. */
@Mojo(name = "generate", requiresProject = true, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
open class GenerateMojo : AbstractMojo() {

    @Parameter(defaultValue = $$"${project}")
    protected var project: MavenProject? = null

    /** Location of the Output Directory. */
    @Parameter(
        property = "kbake.outputDirectory",
        defaultValue = $$"${project.build.directory}/${project.build.finalName}",
        required = true
    )
    protected var outputDirectory: File? = null

    /** Location of the Output Directory. */
    @Parameter(property = "kbake.inputDirectory", defaultValue = $$"${project.basedir}/src/main/kbake", required = true)
    protected var inputDirectory: File? = null

    /** Breaks the build when `true` and errors occur during baking in KBake oven. */
    @Parameter(property = "kbake.failOnError", defaultValue = "true")
    protected var failOnError: Boolean = false

    /** Set if cache is present or clear. */
    @Parameter(property = "kbake.isClearCache", defaultValue = "false", required = true)
    protected var isClearCache: Boolean = false

    @Throws(MojoExecutionException::class)
    override fun execute() {
        executeInternal()
    }

    @Throws(MojoExecutionException::class)
    protected open fun executeInternal() {
        reRender()
    }

    @Throws(MojoExecutionException::class)
    protected fun reRender() {
        try {
            log.info("Starting KBake generation from: ${inputDirectory!!.path} to: ${outputDirectory!!.path}")

            val oven = Oven(createConfiguration())
            oven.bakeEverything()
            if (failOnError && !oven.errors.isEmpty())
                throw MojoFailureException("Baked with " + oven.errors.size + " errors. Check output above for details!")

            log.info("KBake generation completed successfully")
        }
        catch (e: Exception) {
            val message = e.message ?: "unknown rendering error"
            log.error("KBake generation failed for '${inputDirectory!!.path}': $message")
            log.debug("KBake generation failure details", e)
            throw MojoExecutionException("Failed to generate site from ${inputDirectory!!.path}: $message", e)
        }
    }

    @Throws(JBakeException::class)
    protected fun createConfiguration(): JBakeConfiguration =
        JBakeConfigurationFactory()
            .createDefaultJbakeConfiguration(inputDirectory!!, outputDirectory!!, isClearCache)
            .apply { addConfiguration(project!!.properties) }
}
