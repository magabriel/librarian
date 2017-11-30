/*
 * This file is part of the librarian application.
 *
 * Copyright (c) Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package com.mags.librarian

import com.mags.librarian.Options.Options
import com.mags.librarian.config.Config
import com.mags.librarian.config.ConfigLoader
import com.mags.librarian.config.ConfigReader
import com.mags.librarian.event.EventDispatcher

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

    private lateinit var configFile: String
    private lateinit var logFile: String
    private lateinit var rssFile: String

    private var config = Config()
    private var options = Options()
    private lateinit var logger: Log
    private lateinit var eventDispatcher: EventDispatcher

    @JvmStatic
    fun main(args: Array<String>) {

        // set file defaults
        with(File(System.getProperty("user.dir")).toPath()) {
            configFile = resolve(CONFIG_FILE).toString()
            logFile = resolve(LOG_FILE).toString()
            rssFile = resolve(RSS_FILE).toString()
        }

        // our eventDispatcher
        eventDispatcher = EventDispatcher()

        // create logger but no logging allowed yet
        logger = Log(logFile)

        writeMessage("$NAME version $VERSION $COPYRIGHT")

        options = readOptions(args)

        // start logging
        logger.start()

        config = loadConfig()

        val processor = Processor(options, config, logger, eventDispatcher)
        processor.run()
    }

    /**
     * Read and configure the command line options.
     */
    private fun readOptions(args: Array<String>): Options {

        val opt = Options()

        var i = 0
        while (i < args.size) {
            val arg = args[i]

            when (arg) {
                "--help", "-h"    -> showUsage()

                "--copy"          -> opt.copyOnly = true

                "--create-config" -> {
                    createConfig()
                    // no point on continuing execution
                    System.exit(0)
                }

                "--dry-run"       -> opt.dryRun = true

                "-v"             -> {
                    opt.verbosity = Options.Verbosity.NORMAL
                    logger.consoleLogLevel = Level.INFO
                }

                "-vv"            -> {
                    opt.verbosity = Options.Verbosity.HIGH
                    logger.consoleLogLevel = Level.CONFIG
                }

                "--quiet"        -> {
                    opt.verbosity = Options.Verbosity.NONE
                    logger.consoleLogLevel = Level.OFF
                }

                "--config", "-c" -> if (i < args.lastIndex) {
                    i++
                    configFile = args[i]
                }

                "--log", "-l"    -> if (i < args.lastIndex) {
                    i++
                    logFile = args[i]
                    try {
                        logger.logFileName = logFile
                        opt.logFileName = logFile
                    } catch (e: IOException) {
                        logger.logger.severe(e.message)
                        System.exit(1)
                    }

                }

                "--rss", "-r"    -> if (i < args.lastIndex) {
                    i++
                    rssFile = args[i]
                    opt.rssFileName = rssFile
                }

                "--loglevel"     -> if (i < args.lastIndex) {
                    i++
                    val level = args[i]

                    try {
                        val logLevel = Level.parse(level.toUpperCase())
                        opt.logLevel = logLevel
                        logger.logLevel = logLevel
                    } catch (e: IllegalArgumentException) {
                        logger.logger.severe(String.format("Invalid log level \"%s\"", level))
                        System.exit(1)
                    }

                }

                else             -> showUsage()
            }
            i++
        }

        return opt
    }

    private fun showUsage() {

        writeMessage()
        writeMessage("Usage: librarian <options>")
        writeMessage()
        writeMessage("Options: -h | --help        : Show this help.")
        writeMessage("         --copy             : Copy instead of move the files.")
        writeMessage("         --create-config    : Create a default configuration file in current directory.")
        writeMessage("         --dry-run          : Do not change anything, just tell what would have been done.")
        writeMessage("         --loglevel <level> : Loglevel (ALL, FINEST, FINER, FINE, CONFIG, INFO, WARNING, " + "SEVERE, OFF). Default INFO.")
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
    private fun loadConfig(): Config {

        var conf = Config()

        try {
            val reader = ConfigReader()
            conf = reader.read(configFile)

        } catch (e: FileNotFoundException) {
            logger.logger.severe("ERROR: Configuration file '${configFile}' not found.")
            logger.logger.severe("HINT: You can generate a default configuration file with the provided command line option.")
            logger.logger.finer(e.toString())

            System.exit(1)
        }

        return conf
    }

    private fun createConfig() {

        val configLoader = ConfigLoader()

        try {
            configLoader.createDefault("/librarian-default.yml", configFile)
            logger.logger.info("Default configuration file created as '$configFile'")

        } catch (e: FileNotFoundException) {
            logger.logger.severe("ERROR: Configuration file '$configFile' could not be created. Check intermediate folders exist.")
            System.exit(1)

        } catch (e: IOException) {
            logger.logger.severe("ERROR: Configuration file '$configFile' could not be created: '${e.message}'")
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
