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
import javax.inject.Inject

class ConfigReader
@Inject constructor(val configLoader: ConfigLoader) {

    private var onFileReadCallback: ((config: Config) -> Unit)? = null
    private var onFileNotFoundCallback: ((fileName: String) -> Unit)? = null
    private var onIncludedFileNotFoundCallback: ((fileName: String) -> Unit)? = null

    fun onFileRead(callback: (configuration: Config) -> Unit) {
        onFileReadCallback = callback
    }

    fun onFileNotFound(callback: (fileName: String) -> Unit) {
        onFileNotFoundCallback = callback
    }

    fun onIncludedFileNotFound(callback: (fileName: String) -> Unit) {
        onIncludedFileNotFoundCallback = callback
    }

    fun read(configFile: String) {

        configLoader.onFileNotFound { fileName ->
            onFileNotFoundCallback?.invoke(fileName)
        }

        configLoader.load(configFile)
        val adaptor = ConfigAdaptor(configLoader)
        val config = adaptor.process()

        if (!config.include.isEmpty()) {
            val includeFile = File(configFile).parentFile.toPath().resolve(config.include).toFile().toString()
            configLoader.load(includeFile)

            val includedConfig = adaptor.process()
            // merge the base over the included
            val mergedConfig = includedConfig.merge(config)
            onFileReadCallback?.invoke(mergedConfig)
            return
        }

        onFileReadCallback?.invoke(config)
    }
}
