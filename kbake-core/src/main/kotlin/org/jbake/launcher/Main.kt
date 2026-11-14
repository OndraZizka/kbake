package org.jbake.launcher

import org.jbake.app.FileUtil
import org.jbake.app.JBakeExitException
import org.jbake.app.SystemExit
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.app.configuration.JBakeConfigurationFactory
import org.jbake.util.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.bridge.SLF4JBridgeHandler
import picocli.CommandLine
import java.io.File
import java.nio.file.Files
import kotlin.system.exitProcess

/**
 * Launcher for KBake.
 */
class Main @JvmOverloads constructor(
    private val baker: Baker = Baker(),
    private val jettyServer: JettyServer = JettyServer(),
    private val watcher: BakeWatcher = BakeWatcher()
) {
    var jBakeConfigurationFactory: JBakeConfigurationFactory = JBakeConfigurationFactory()

    @Throws(JBakeExitException::class)
    fun run(arguments: Array<String>) {
        try {
            SLF4JBridgeHandler.removeHandlersForRootLogger()
            SLF4JBridgeHandler.install()

            val config: JBakeConfiguration

            val args: LaunchOptions = parseArguments(arguments)

            if (args.isInit) ensureInitDirectory(args.getSource())

            // Configure log level based on command line options
            configureLogLevel(args.effectiveLogLevel ?: if (args.debug) "DEBUG" else null)

            config = this.jBakeConfigurationFactory
                .setEncoding(args.propertiesEncoding)
                .let {
                    if (args.isRunServer)
                        it.createJettyJbakeConfiguration(args.getSource(), args.getDestination(), args.getConfig(), args.isClearCache)
                    else
                        it.createDefaultJbakeConfiguration(args.getSource(), args.getDestination(), args.getConfig(), args.isClearCache)
                }

            run(args, config)
        }
        catch (e: JBakeExitException) { throw e }
        catch (mex: CommandLine.MissingParameterException) {
            log.error(mex.message)
            throw JBakeExitException(SystemExit.CONFIG_ERROR, mex.message ?: mex.javaClass.simpleName, mex)
        }
        catch (e: Throwable) {
            val message = e.message?.takeIf { it.isNotBlank() }
                ?: "An unexpected error occurred. Run again with --debug for diagnostic details."
            throw JBakeExitException(SystemExit.ERROR, message, e)
        }
    }

    /**
     * Configures log level programmatically using Logback.
     * @param level The log level string (OFF, ERROR, WARN, INFO, DEBUG, TRACE) or null for default
     */
    private fun configureLogLevel(level: String?) {
        if (level == null) return

        try {
            // Use Logback API directly to set log level
            val loggerContext = LoggerFactory.getILoggerFactory()
            if (loggerContext is ch.qos.logback.classic.LoggerContext) {
                val rootLogger = loggerContext.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME)
                val logLevel = ch.qos.logback.classic.Level.toLevel(level.uppercase(), ch.qos.logback.classic.Level.WARN)
                rootLogger.level = logLevel
                log.debug { "Log level set to: $logLevel." }
            }
        } catch (e: Exception) {
            // Logback not available or other issue - silently ignore
            System.err.println("Warning: Could not configure log level: ${e.message}")
        }
    }

    fun run(launchOptions: LaunchOptions, config: JBakeConfiguration) {
        println("KBake " + config.jbakeVersion + " (" + config.buildTimeStamp + " " + config.abbreviatedGitHash + "#) [https://github.com/OndraZizka/kbake]")
        println()

        if (launchOptions.isHelpNeeded) {
            printUsage(launchOptions)
            return
        }

        if (launchOptions.isListConfig) {
            val printer = ConfigurationPrinter(config, System.out)
            printer.print()
            return
        }

        if (launchOptions.isBake)
            baker.bake(config)

        if (launchOptions.isInit)
            initStructure(launchOptions.template, config)

        if (launchOptions.isRunServer) {
            watcher.start(config)

            // TODO: Short term fix until bake, server, init commands no longer share underlying values (such as source/dest).
            @Suppress("SENSELESS_COMPARISON")
            val serverPath = when {
                !launchOptions.isBake -> config.destinationDir // Use the default destination directory.
                launchOptions.getDestination() != null -> launchOptions.getDestination() // Use the destination provided via the commandline.
                launchOptions.getSource().path != "." -> launchOptions.getSource() // Use the source directory provided via the commandline.
                else -> config.destinationDir // Use the default DESTINATION_FOLDER value.
            }
            runServer(serverPath, config)
        }
    }

    private fun parseArguments(args: Array<String>): LaunchOptions {
        return CommandLine.populateCommand(LaunchOptions(), *args)
    }

    private fun ensureInitDirectory(sourceDir: File) {
        if (sourceDir.exists()) return
        try {
            Files.createDirectories(sourceDir.toPath())
        } catch (e: Exception) {
            throw JBakeExitException(
                SystemExit.INIT_ERROR,
                "Cannot create site directory '${sourceDir.absolutePath}': ${e.message ?: "permission denied"}",
                e
            )
        }
    }

    private fun printUsage(options: Any) {
        val cli = CommandLine(options)
        cli.usageHelpLongOptionsMaxWidth = 28
        cli.usage(System.out)
    }

    private fun runServer(path: File, configuration: JBakeConfiguration) {
        jettyServer.run(path.path, configuration)
    }

    private fun initStructure(type: String, config: JBakeConfiguration) {
        val init = Init(config)
        try {
            val templateDir = FileUtil.templateDirForTestsOrForRuntime
            val outputDir = config.sourceDir ?: File(".")

            init.run(outputDir, templateDir, type)
            println("Base dir structure successfully created.")
        } catch (e: Exception) {
            val msg = "Failed to initialise structure: " + e.message
            throw JBakeExitException(SystemExit.INIT_ERROR, msg, e)
        }
    }


    companion object {
        private const val USAGE_PREFIX = "Usage: kbake"
        private const val ALT_USAGE_PREFIX = "   or  kbake"

        /** Runs the app with the given arguments. */
        @JvmStatic
        fun main(args: Array<String>) {
            val debugRequested = args.any { it == "--debug" || it == "-d" || it.matches(Regex("-v{3,}")) }
            try {
                Main().run(args)
            } catch (e: JBakeExitException) {
                System.err.println("Error: ${e.message ?: "KBake could not complete the requested operation."}")
                if (debugRequested) {
                    e.printStackTrace(System.err)
                } else {
                    System.err.println("Run again with --debug for diagnostic details.")
                }
                log.error(e.message)
                if (e.cause is CommandLine.MissingParameterException) printUsage()
                exitProcess(e.getExit())
            }
            catch (e: Throwable) {
                System.err.println("Error: ${e.message ?: "KBake could not complete the requested operation."}")
                if (debugRequested) e.printStackTrace(System.err)
                else System.err.println("Run again with --debug for diagnostic details.")
                log.error(e.message)
                exitProcess(SystemExit.ERROR.errorNumber)
            }
        }

        fun printUsage() = CommandLine.usage(LaunchOptions(), System.out)

        private val log: Logger = LoggerFactory.getLogger("Main.kt")
    }
}
