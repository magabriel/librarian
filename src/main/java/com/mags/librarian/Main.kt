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
import com.mags.librarian.config.ConfigLoader
import com.mags.librarian.config.ConfigReader
import com.mags.librarian.di.DaggerAppComponent
import com.mags.librarian.event.EventDispatcher
import com.mags.librarian.options.Options
import com.mags.librarian.options.OptionsReader
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.util.logging.Level

object Main {

    private val NAME = getApplicationName()
    private val VERSION = getApplicationVersion()

    private const val COPYRIGHT = "(c) magabriel@gmail.com"

    private const val CONFIG_FILE = "librarian.yml"
    private const val LOG_FILE = "librarian.log"
    private const val RSS_FILE = "librarian.rss"

    private var config = Config()
    private var options = Options()
    private var eventDispatcher = EventDispatcher()

    lateinit var logWriter: LogWriter

    @JvmStatic
    fun main(args: Array<String>) {
        var app = DaggerAppComponent.create()

        // set file defaults
        with(File(System.getProperty("user.dir")).toPath()) {
            options.configFileName = resolve(CONFIG_FILE).toString()
            options.logFileName = resolve(LOG_FILE).toString()
            options.rssFileName = resolve(RSS_FILE).toString()
        }

        // create logWriter but no logging allowed yet
        logWriter = app.getLogWriter()
        logWriter.logFileName = options.logFileName

        readOptions(args)
        processOptions()

        writeMessage("$NAME version $VERSION $COPYRIGHT")

        // start logging
        logWriter.start()

        loadConfig()

        val processor = Processor(options, config, logWriter, eventDispatcher)
        processor.run()
    }

    /**
     * Read and configure the command line options.
     */
    private fun readOptions(args: Array<String>) {

        val optionsReader = object : OptionsReader(args.toList(), defaultOptions = options) {
            override fun onUnknownOption(option: String) {
                writeMessage()
                writeMessage("ERROR: Unknown option \"$option\"")
                showUsage()
            }

            override fun onMissingValue(option: String) {
                writeMessage()
                writeMessage("ERROR: Missing value for option \"$option\"")
                showUsage()
            }

            override fun onInvalidValue(option: String,
                                        value: String) {
                writeMessage()
                writeMessage("ERROR: Invalid value \"$value\" for option \"$option\"")
                showUsage()
            }
        }
        options = optionsReader.process()
    }

    private fun processOptions() {

        if (options.help) {
            showUsage()
        }

        if (options.createConfig) {
            createConfig()
            System.exit(0)
        }

        when (options.verbosity) {
            Options.Verbosity.NORMAL -> logWriter.consoleLogLevel = Level.INFO
            Options.Verbosity.HIGH   -> logWriter.consoleLogLevel = Level.CONFIG
            Options.Verbosity.NONE   -> logWriter.consoleLogLevel = Level.OFF
        }

        logWriter.logFileName = options.logFileName
        logWriter.logLevel = options.logLevel
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
    private fun loadConfig() {

        object : ConfigReader() {
            override fun onFileRead(configuration: Config) {
                config = configuration
            }

            override fun onFileNotFound(fileName: String) {
                logWriter.severe("ERROR: Configuration file '${options.configFileName}' not found.")
                logWriter.severe("HINT: You can generate a default configuration file with the provided command line option.")
                System.exit(1)
            }

            override fun onIncludedFileNotFound(fileName: String) {
                logWriter.severe("ERROR: Included file \"$fileName\" does not exist.")
                System.exit(1)
            }
        }.read(options.configFileName)
    }

    private fun createConfig() {

        val configLoader = ConfigLoader()

        try {
            configLoader.createDefault("/librarian-default.yml", options.configFileName)
            logWriter.info("Default configuration file created as '${options.configFileName}'")

        } catch (e: FileNotFoundException) {
            logWriter.severe("ERROR: Configuration file '${options.configFileName}' could not be created. Check intermediate folders exist.")
            System.exit(1)

        } catch (e: IOException) {
            logWriter.severe("ERROR: Configuration file '${options.configFileName}' could not be created: '${e.message}'")
            System.exit(1)
        }
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
