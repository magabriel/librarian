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
import dagger.Module
import dagger.Provides
import org.yaml.snakeyaml.Yaml
import javax.inject.Singleton

@Module
class ConfigurationModule {

    @Provides
    @Singleton
    fun provideYaml(): Yaml {
        return Yaml()
    }

    @Provides
    @Singleton
    fun provideConfigLoader(yamlReader: Yaml): ConfigLoader {
        return ConfigLoader(yamlReader)
    }

    @Provides
    @Singleton
    fun provideConfigReader(configLoader: ConfigLoader): ConfigReader {
        return ConfigReader(configLoader)
    }

    @Provides
    @Singleton
    fun provideConfigCreator(): ConfigCreator {
        return ConfigCreator()
    }
}
