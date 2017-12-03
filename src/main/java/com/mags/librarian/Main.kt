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
    private lateinit var logger: LogWriter
    private lateinit var eventDispatcher: EventDispatcher
    private var systemLogger = java.util.logging.Logger.getLogger(this.javaClass.name)

    @JvmStatic
    fun main(args: Array<String>) {

        // set file defaults
        with(File(System.getProperty("user.dir")).toPath()) {
            options.configFileName = resolve(CONFIG_FILE).toString()
            options.logFileName = resolve(LOG_FILE).toString()
            options.rssFileName = resolve(RSS_FILE).toString()
        }

        // our eventDispatcher
        eventDispatcher = EventDispatcher()

        // create logger but no logging allowed yet
        logger = LogWriter(systemLogger)
        logger.logFileName = options.logFileName

        writeMessage("$NAME version $VERSION $COPYRIGHT")

        readOptions(args)

        // start logging
        logger.start()

        loadConfig()

        val processor = Processor(options, config, logger, eventDispatcher)
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

        if (options.help) {
            showUsage()
        }

        if (options.createConfig) {
            createConfig()
            System.exit(0)
        }

        when (options.verbosity) {
            Options.Verbosity.NORMAL -> logger.consoleLogLevel = Level.INFO
            Options.Verbosity.HIGH   -> logger.consoleLogLevel = Level.CONFIG
            Options.Verbosity.NONE   -> logger.consoleLogLevel = Level.OFF
        }

        logger.logFileName = options.logFileName
        logger.logLevel = options.logLevel
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

        var conf = Config()

        try {
            val reader = ConfigReader()
            conf = reader.read(options.configFileName)

        } catch (e: FileNotFoundException) {
            logger.severe("ERROR: Configuration file '${options.configFileName}' not found.")
            logger.severe("HINT: You can generate a default configuration file with the provided command line option.")
            logger.finer(e.toString())

            System.exit(1)
        }

        config = conf
    }

    private fun createConfig() {

        val configLoader = ConfigLoader()

        try {
            configLoader.createDefault("/librarian-default.yml", options.configFileName)
            logger.info("Default configuration file created as '${options.configFileName}'")

        } catch (e: FileNotFoundException) {
            logger.severe("ERROR: Configuration file '${options.configFileName}' could not be created. Check intermediate folders exist.")
            System.exit(1)

        } catch (e: IOException) {
            logger.severe("ERROR: Configuration file '${options.configFileName}' could not be created: '${e.message}'")
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
