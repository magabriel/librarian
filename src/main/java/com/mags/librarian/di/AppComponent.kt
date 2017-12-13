/*
 * This file is part of the librarian application.
 *
 * Copyright (c) 2017 Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code
 */

package com.mags.librarian.di

import com.mags.librarian.Command
import com.mags.librarian.FeedWriter
import com.mags.librarian.LogWriter
import com.mags.librarian.Mover
import com.mags.librarian.config.Config
import com.mags.librarian.event.EventDispatcher
import com.mags.librarian.options.Options
import dagger.Component
import javax.inject.Singleton

@Component(modules = [
    ConfigModule::class,
    OptionsModule::class,
    FeedWriterModule::class,
    LogModule::class,
    EventDispatcherModule::class
])
@Singleton
interface AppComponent {
    fun getConfig(): Config
    fun getOptions(): Options
    fun getLogWriter(): LogWriter
    fun getFeedWriter(): FeedWriter
    fun getCommand(): Command
    fun getMover(): Mover
    fun getEventDispatcher(): EventDispatcher
}
