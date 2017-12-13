/*
 * This file is part of the librarian application.
 *
 * Copyright (c) 2017 Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code
 */

package com.mags.librarian.di

import com.mags.librarian.config.ConfigCreator
import com.mags.librarian.config.ConfigLoader
import com.mags.librarian.config.ConfigReader
import dagger.Component
import javax.inject.Singleton

@Component(modules = [
    ConfigurationModule::class
])
@Singleton
interface ConfigurationComponent {
    fun getConfigReader(): ConfigReader
    fun getConfigLoader(): ConfigLoader
    fun getConfigCreator(): ConfigCreator
}
