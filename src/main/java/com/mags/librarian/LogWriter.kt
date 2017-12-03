/*
 * This file is part of the librarian application.
 *
 * Copyright (c) 2016 Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code
 */

package com.mags.librarian

import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.*
import java.util.logging.Formatter

/**
 * Configures the logger.
 */
class LogWriter internal constructor(private val logger: Logger) {

    lateinit var logFileName: String
    var logLevel = Level.FINE
    var consoleLogLevel = Level.INFO

    private var consoleHandler: Handler? = null
    private var fileHandler: Handler? = null

    init {
        logger.useParentHandlers = false
    }

    fun start() {
        initLogger()
    }

    fun close() {
        for (handler in logger.handlers) {
            logger.removeHandler(handler)
        }
    }

    /**
     * Initializations.
     */
    private fun initLogger() {
        logger.level = Level.ALL
        /*
         * Create the console handler with reduced info and INFO level
         */
        consoleHandler = ConsoleHandler()
        consoleHandler!!.formatter = object : Formatter() {
            override fun format(record: LogRecord): String {
                return String.format("%s\n", record.message)
            }
        }
        consoleHandler!!.level = consoleLogLevel
        logger.addHandler(consoleHandler!!)

        /*
         * Create the custom file handler
         */
        try {
            if (logFileName == null) {
                // no log file name, no logging
                return
            }
            //FileHandler file name with max size and number of log files limit
            fileHandler = FileHandler(logFileName, true)
            fileHandler!!.formatter = object : Formatter() {
                override fun format(record: LogRecord): String {
                    val dt = SimpleDateFormat("yyyy-MM-dd HH.mm:ss")
                    return "%s [%s] %s\n".format(dt.format(Date()),
                                                 (record.level.toString() + "      ").substring(0,
                                                                                                6),
                                                 record.message)
                }
            }
            fileHandler!!.level = logLevel
            logger.addHandler(fileHandler!!)

        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun severe(s: String?) = logger.severe(s)
    fun warning(s: String?) = logger.warning(s)
    fun info(s: String?) = logger.info(s)
    fun config(s: String?) = logger.config(s)
    fun fine(s: String?) = logger.fine(s)
    fun finer(s: String?) = logger.finer(s)

}
