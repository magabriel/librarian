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
import dagger.Component
import javax.inject.Singleton

@Component(modules = [LogModule::class])
@Singleton
interface AppComponent {
    fun getLogWriter(): LogWriter
}
