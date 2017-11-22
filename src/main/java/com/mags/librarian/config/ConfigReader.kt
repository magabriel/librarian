/*
 * This file is part of the librarian application.
 *
 * Copyright (c) 2016 Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code
 */

package com.mags.librarian.config

import java.io.File
import java.io.FileNotFoundException

class ConfigReader {
    @Throws(FileNotFoundException::class)
    fun read(configFile: String): Config {
        val configLoader = ConfigLoader()
        configLoader.load(configFile)
        val adaptor = ConfigAdaptor(configLoader)
        val config = adaptor.process()

        if (!config.include.isEmpty()) {
            val includeFile = File(configFile).parentFile.toPath().resolve(config.include).toFile()
            if (!includeFile.exists()) {
                throw FileNotFoundException(
                        "Included file \"${includeFile.toString()}\" does not exist.")
            }

            configLoader.load(includeFile.toString())
            val includedConfig = adaptor.process()
            // merge the base over the included
            return includedConfig.merge(config)
        }

        return config
    }
}
