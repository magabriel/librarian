/*
 * This file is part of the librarian application.
 *
 * Copyright (c) 2017 Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code
 */

package com.mags.librarian.config

import java.io.*
import javax.inject.Inject

class ConfigCreator
@Inject constructor() {

    private var onFileAlreadyExistsCallback: ((fileName: String) -> Unit)? = null
    private var onInvalidPathCallback: ((fileName: String) -> Unit)? = null
    private var onIOErrorCallback: ((fileName: String, e: IOException) -> Unit)? = null

    fun onFileAlreadyExists(callback: (fileName: String) -> Unit) {
        onFileAlreadyExistsCallback = callback
    }

    fun onInvalidPath(callback: (fileName: String) -> Unit) {
        onInvalidPathCallback = callback
    }

    fun onIOError(callback: (fileName: String, e: IOException) -> Unit) {
        onIOErrorCallback = callback
    }

    /**
     * Create the default config file from a given template.
     */
    fun createDefault(templateFile: String,
                      fileName: String) {

        try {
            val file = File(fileName)
            if (file.exists()) {
                onFileAlreadyExistsCallback?.invoke(fileName)
                return
            }

            // read default file
            val res = javaClass.getResourceAsStream(templateFile)
            val br = BufferedReader(InputStreamReader(res))
            val lines = br.lines().toArray()

            // write it
            val content = lines.joinToString(System.lineSeparator())
            val bw = BufferedWriter(FileWriter(file.absoluteFile))
            bw.write(content)
            bw.close()
        } catch (e: FileNotFoundException) {
            onInvalidPathCallback?.invoke(fileName)
        } catch (e: IOException) {
            onIOErrorCallback?.invoke(fileName, e)
        }
    }
}