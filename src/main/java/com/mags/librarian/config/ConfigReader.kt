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

abstract class ConfigReader {

    abstract fun onFileRead(configuration: Config)
    abstract fun onFileNotFound(fileName: String)
    abstract fun onIncludedFileNotFound(fileName: String)

    fun read(configFile: String) {
        val configLoader = ConfigLoader()
        try {
            configLoader.load(configFile)
        } catch (e: FileNotFoundException) {
            onFileNotFound(configFile)
            return
        }

        val adaptor = ConfigAdaptor(configLoader)
        val config = adaptor.process()

        if (!config.include.isEmpty()) {
            val includeFile = File(configFile).parentFile.toPath().resolve(config.include).toFile().toString()
            try {
                configLoader.load(includeFile)
            } catch (e: FileNotFoundException) {
                onIncludedFileNotFound(includeFile)
                return
            }
            val includedConfig = adaptor.process()
            // merge the base over the included
            val mergedConfig = includedConfig.merge(config)
            onFileRead(mergedConfig)
            return
        }

        onFileRead(config)
    }
}
