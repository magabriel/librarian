/*
 * This file is part of the librarian application.
 *
 * Copyright (c) Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package com.mags.librarian

import com.mags.librarian.config.Config
import com.mags.librarian.config.ConfigCreator
import com.mags.librarian.config.ConfigReader
import com.mags.librarian.di.ConfigModule
import com.mags.librarian.di.DaggerAppComponent
import com.mags.librarian.di.DaggerConfigurationComponent
import com.mags.librarian.di.OptionsModule
import com.mags.librarian.options.Options
import com.mags.librarian.options.OptionsReader
import java.io.File

object Main {

    private val NAME = getApplicationName()
    private val VERSION = getApplicationVersion()

    private const val COPYRIGHT = "(c) magabriel@gmail.com"

    private const val CONFIG_FILE = "librarian.yml"
    private const val LOG_FILE = "librarian.log"
    private const val RSS_FILE = "librarian.rss"

    private var config = Config()
    private var options = Options()

    lateinit var logWriter: LogWriter

    private lateinit var configCreator: ConfigCreator
    private lateinit var configReader: ConfigReader

    @JvmStatic
    fun main(args: Array<String>) {

        // set file defaults
        with(File(System.getProperty("user.dir")).toPath()) {
            options.configFileName = resolve(CONFIG_FILE).toString()
            options.logFileName = resolve(LOG_FILE).toString()
            options.rssFileName = resolve(RSS_FILE).toString()
        }

        options = readOptions(args)
        writeMessage("$NAME version $VERSION $COPYRIGHT")

        // chicken-and-egg problem: we need a config reader to read config and need config to
        // configure app, so we use a separate component to get a config loader and reader first
        var configComponent = DaggerConfigurationComponent.create()
        configReader = configComponent.getConfigReader()
        configCreator = configComponent.getConfigCreator()

        // config loading errors cannot be logged until the logWriter is configured
        var configErrors = listOf<String>()
        config = loadConfig { errors -> configErrors = errors }

        // now we can configure the app component
        var app = DaggerAppComponent.builder()
                .configModule(ConfigModule(config))
                .optionsModule(OptionsModule(options))
                .build()

        // get the fully configured logWriter
        logWriter = app.getLogWriter()

        processOptions()

        // now we can log config errors
        if (configErrors.isNotEmpty()) {
            configErrors.forEach { logWriter.severe(it) }
            System.exit(1)
        }

        val processor = Processor(options,
                                  config,
                                  logWriter,
                                  app.getEventDispatcher(),
                                  app.getFeedWriter(),
                                  app.getCommand(),
                                  app.getMover())
        processor.run()
    }

    /**
     * Read and configure the command line options.
     */
    private fun readOptions(args: Array<String>): Options {

        val optionsReader = OptionsReader(args.toList(), defaultOptions = options)

        optionsReader.onUnknownOption { option: String ->
            writeMessage()
            writeMessage("ERROR: Unknown option \"$option\"")
            showUsage()
        }

        optionsReader.onMissingValue { option: String ->
            writeMessage()
            writeMessage("ERROR: Missing value for option \"$option\"")
            showUsage()
        }

        optionsReader.onInvalidValue { option: String,
                                       value: String ->
            writeMessage()
            writeMessage("ERROR: Invalid value \"$value\" for option \"$option\"")
            showUsage()
        }

        return optionsReader.process()
    }

    private fun processOptions() {

        if (options.help) {
            showUsage()
        }

        if (options.createConfig) {
            createConfig()
            System.exit(0)
        }
    }

    private fun showUsage() {

        writeMessage()
        writeMessage("Usage: librarian <options>")
        writeMessage()
        writeMessage("options: -h | --help        : Show this help.")
        writeMessage("         --copy             : Copy instead of move the files.")
        writeMessage("         --create-config    : Create a default configuration file in current directory.")
        writeMessage("         --dry-run          : Do not change anything, just tell what would have been done.")
        writeMessage("         --loglevel <level> : Loglevel (ALL, FINEST, FINER, FINE, CONFIG, INFO, WARNING, SEVERE, OFF). Default INFO.")
        writeMessage("         -c --config <file> : Use that config file instead of the one in execution directory.")
        writeMessage("         -l --log <file>    : Write to that log file instead of creating one in the execution directory.")
        writeMessage("         -r --rss<file>     : Write to that rss file instead of the one in execution directory.")
        writeMessage("         -v, -vv            : Verbosity normal (default) or high.")
        writeMessage("         --quiet            : Do not write messages.")

        System.exit(1)
    }

    /**
     * Loads the configuration file.
     */
    private fun loadConfig(errorList: (errors: List<String>) -> Unit): Config {

        var readConfig = Config()
        val errors = mutableListOf<String>()

        configReader.onFileRead { configuration ->
            readConfig = configuration
        }

        configReader.onFileNotFound { fileName ->
            errors.add("ERROR: Configuration file '$fileName' not found.")
            errors.add("HINT: You can generate a default configuration file with the provided command line option.")
        }

        configReader.onIncludedFileNotFound { fileName ->
            errors.add("ERROR: Included file \"$fileName\" does not exist.")
        }

        configReader.read(options.configFileName)

        errorList(errors)

        return readConfig
    }

    private fun createConfig() {

        configCreator.onInvalidPath { fileName ->
            logWriter.severe("ERROR: Configuration file '$fileName' could not be created. Check intermediate folders exist.")
            System.exit(1)
        }

        configCreator.onFileAlreadyExists { fileName ->
            logWriter.severe("File '$fileName' already exists. Delete or rename it an try again.")
            System.exit(1)
        }

        configCreator.onIOError { fileName, _ ->
            logWriter.severe("ERROR: Configuration file '$fileName' could not be created. Check intermediate folders exist.")
            System.exit(1)
        }

        configCreator.createDefault("/librarian-default.yml", options.configFileName)
        logWriter.info("Default configuration file created as '${options.configFileName}'")
    }

    private fun writeMessage(msg: String) {
        if (options.verbosity == Options.Verbosity.NONE) {
            return
        }
        System.err.println(msg)
    }

    private fun writeMessage() {
        if (options.verbosity == Options.Verbosity.NONE) {
            return
        }
        System.err.println()
    }

    private fun getApplicationName(): String {
        if (Main::class.java.`package`.implementationTitle != null) {
            return Main::class.java.`package`.implementationTitle
        }
        return "application.name"
    }

    private fun getApplicationVersion(): String {
        if (Main::class.java.`package`.implementationVersion != null) {
            return Main::class.java.`package`.implementationVersion
        }
        return "?.?.?"
    }
}
