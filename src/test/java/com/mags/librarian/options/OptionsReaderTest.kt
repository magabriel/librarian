/*
 * This file is part of the librarian application.
 *
 * Copyright (c) 2017 Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code
 */

package com.mags.librarian.options

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.logging.Level

internal class OptionsReaderTest {

    @Test
    fun unknownOption() {
        var called = 0
        val optionsReader = OptionsReader(listOf("badoption"))
        optionsReader.onUnknownOption { option: String ->
            assertEquals("badoption", option)
            called++
        }
        optionsReader.process()
        assertEquals(1, called)
    }

    @Test
    fun optionHelp() {
        var options: Options

        options = executeReader(listOf("--help"))
        assertTrue(options.help)

        options = executeReader(listOf("-h"))
        assertTrue(options.help)
    }

    @Test
    fun optionCopy() {
        val options = executeReader(listOf("--copy"))
        assertTrue(options.copyOnly)
    }

    @Test
    fun optionCreateConfig() {
        val options = executeReader(listOf("--create-config"))
        assertTrue(options.createConfig)
    }

    @Test
    fun optionDryRun() {
        val options = executeReader(listOf("--dry-run"))
        assertTrue(options.dryRun)
    }

    @Test
    fun optionVerbosity() {
        var options: Options

        options = executeReader(listOf("--quiet"))
        assertEquals(Options.Verbosity.NONE, options.verbosity)

        options = executeReader(listOf("-v"))
        assertEquals(Options.Verbosity.NORMAL, options.verbosity)

        options = executeReader(listOf("-vv"))
        assertEquals(Options.Verbosity.HIGH, options.verbosity)
    }

    @Test
    fun optionConfig() {
        var options: Options

        options = executeReader(listOf("--config", "my/config/file/name"))
        assertEquals("my/config/file/name", options.configFileName)

        options = executeReader(listOf("-c", "my/config/file/name"))
        assertEquals("my/config/file/name", options.configFileName)
    }

    @Test
    fun optionLog() {
        var options: Options

        options = executeReader(listOf("--log", "my/log/file/name"))
        assertEquals("my/log/file/name", options.logFileName)

        options = executeReader(listOf("-l", "my/log/file/name"))
        assertEquals("my/log/file/name", options.logFileName)
    }

    @Test
    fun optionRss() {
        var options: Options

        options = executeReader(listOf("--rss", "my/rss/file/name"))
        assertEquals("my/rss/file/name", options.rssFileName)

        options = executeReader(listOf("-r", "my/rss/file/name"))
        assertEquals("my/rss/file/name", options.rssFileName)
    }

    @Test
    fun optionLogLevel() {
        var options = executeReader(listOf("--loglevel", "all"))
        assertEquals(Level.ALL, options.logLevel)
    }

    @Test
    fun severalOptions() {
        val expected = Options(verbosity = Options.Verbosity.NONE,
                               copyOnly = true,
                               configFileName = "/my/config",
                               logLevel = Level.WARNING)

        var options = executeReader(listOf("--quiet",
                                           "--copy",
                                           "-c",
                                           "/my/config",
                                           "--loglevel",
                                           "warning"))
        assertEquals(expected, options)
    }

    @Test
    fun optionLogLevelNotValid() {
        var called = 0
        val optionsReader = OptionsReader(listOf("--loglevel", "notvalid"))
        optionsReader.onInvalidValue { option: String,
                                       value: String ->
            assertEquals("--loglevel", option)
            assertEquals("notvalid", value)
            called++
        }
        optionsReader.process()
        assertEquals(1, called)
    }

    private fun executeReader(arguments: List<String>): Options {
        val optionsReader = OptionsReader(arguments)
        optionsReader.onUnknownOption { option: String ->
            fail<String>("Unknown option \"$option\"")
        }
        optionsReader.onMissingValue { option: String ->
            fail<String>("Missing value for option \"$option\"")
        }
        optionsReader.onInvalidValue { option: String,
                                       value: String ->
            fail<String>("Invalid value \"$value\" for option \"$option\"")
        }
        return optionsReader.process()
    }

}