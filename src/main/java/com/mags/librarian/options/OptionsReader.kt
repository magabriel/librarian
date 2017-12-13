/*
 * This file is part of the librarian application.
 *
 * Copyright (c) 2017 Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code
 */

package com.mags.librarian.options

import java.util.logging.Level

class OptionsReader(val arguments: List<String> = listOf(),
                    val defaultOptions: Options = Options()) {

    private var onUnknownOptionCallback: ((option: String) -> Unit)? = null
    private var onMissingValueCallback: ((option: String) -> Unit)? = null
    private var onInvalidValueCallback: ((option: String, value: String) -> Unit)? = null

    fun onUnknownOption(callback: (option: String) -> Unit) {
        onUnknownOptionCallback = callback
    }

    fun onMissingValue(callback: (option: String) -> Unit) {
        onMissingValueCallback = callback
    }

    fun onInvalidValue(callback: (option: String, value: String) -> Unit) {
        onInvalidValueCallback = callback
    }

    fun process(): Options {

        val options = defaultOptions.copy()

        var i = 0
        while (i <= arguments.lastIndex) {
            val currentArgument = arguments[i]

            when (currentArgument) {
                "--help", "-h"   -> options.help = true
                "--copy"         -> options.copyOnly = true
                "--config", "-c" -> options.configFileName = getValueArgument(arguments,
                                                                               currentArgument,
                                                                               ++i)
                "--log", "-l"     -> options.logFileName = getValueArgument(arguments,
                                                                            currentArgument,
                                                                            ++i)
                "--rss", "-r"     -> options.rssFileName = getValueArgument(arguments,
                                                                            currentArgument,
                                                                            ++i)
                "--dry-run"       -> options.dryRun = true
                "--create-config" -> options.createConfig = true
                "-v"             -> options.verbosity = Options.Verbosity.NORMAL
                "-vv"            -> options.verbosity = Options.Verbosity.HIGH
                "--quiet"        -> options.verbosity = Options.Verbosity.NONE
                "--loglevel"     -> options.logLevel = getLogLevel(arguments,
                                                                    currentArgument,
                                                                    ++i,
                                                                    options.logLevel)

                else             -> onUnknownOptionCallback?.invoke(currentArgument)
            }
            i++
        }

        return options
    }

    private fun getValueArgument(arguments: List<String>,
                                 option: String,
                                 index: Int): String {
        if (index <= arguments.lastIndex) {
            return arguments[index]
        }
        onMissingValueCallback?.invoke(option)
        return ""
    }

    private fun getLogLevel(arguments: List<String>,
                            option: String,
                            index: Int,
                            default: Level): Level {
        val loglevelStr = getValueArgument(arguments, option, index)
        try {
            return Level.parse(loglevelStr.toUpperCase())
        } catch (e: IllegalArgumentException) {
            onInvalidValueCallback?.invoke(option, loglevelStr)
        }
        return default
    }
}