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
import dagger.Module
import dagger.Provides
import java.util.logging.Logger
import javax.inject.Singleton

@Module
class LogModule {
    @Provides
    @Singleton
    fun provideLogWriter(): LogWriter {
        return LogWriter(java.util.logging.Logger.getLogger(this.javaClass.name))
    }
}
