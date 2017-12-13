/*
 * This file is part of the librarian application.
 *
 * Copyright (c) 2017 Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code
 */

package com.mags.librarian.di

import com.mags.librarian.LogWriter
import com.mags.librarian.options.Options
import dagger.Module
import dagger.Provides
import java.util.logging.Level
import java.util.logging.Logger
import javax.inject.Singleton

@Module
class LogModule {
    @Provides
    @Singleton
    fun provideLogWriter(options: Options): LogWriter {
        val logWriter = LogWriter(java.util.logging.Logger.getLogger(this.javaClass.name))
        logWriter.logFileName = options.logFileName
        logWriter.logLevel = options.logLevel
        when (options.verbosity) {
            Options.Verbosity.NORMAL -> logWriter.consoleLogLevel = Level.INFO
            Options.Verbosity.HIGH   -> logWriter.consoleLogLevel = Level.CONFIG
            Options.Verbosity.NONE   -> logWriter.consoleLogLevel = Level.OFF
        }
        logWriter.start()
        return logWriter
    }
}
