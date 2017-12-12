/*
 * This file is part of the librarian application.
 *
 * Copyright (c) Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package com.mags.librarian.config

import org.yaml.snakeyaml.Yaml
import java.io.*
import java.util.*
import javax.inject.Inject

/**
 * Loads a configuration YAML file.
 */
class ConfigLoader
@Inject constructor(private val configYaml: Yaml) {

    private var onFileNotFoundCallback: ((fileName: String) -> Unit)? = null
    private var onFileAlreadyExistsCallback: ((fileName: String) -> Unit)? = null
    private var onInvalidPathCallback: ((fileName: String) -> Unit)? = null
    private var onIOErrorCallback: ((fileName: String, e: IOException) -> Unit)? = null

    /**
     * Return the raw configuration object.
     */
    var configRaw: Map<String, Any>? = null
        private set
    private val configMap = LinkedHashMap<String, Any>()
    /**
     * Return the flattened configuration object.
     */
    internal val configFlat: Map<String, Any>
        get() = configMap

    fun load(fileName: String) {
        try {
            // load the config file
            val input = FileInputStream(File(fileName))
            configRaw = this.configYaml.load(input) as Map<String, Any>
            flatten("", configRaw, configMap)
        } catch (e: FileNotFoundException) {
            onFileNotFoundCallback?.invoke(fileName)
        }
    }

    /**
     * Load a configuration file contents from a string.
     */
    fun loadFromString(document: String) {
        configRaw = this.configYaml.load(document) as Map<String, Any>

        flatten("", configRaw, configMap)
    }

    /**
     * Flatten the config object (recursive hashmap) to a flat hasmap.
     */
    private fun flatten(baseKey: String,
                        recursiveMap: Map<String, Any>?,
                        flatMap: MutableMap<String, Any>) {
        recursiveMap!!.forEach { key, value ->
            var flatKey = key
            if (!baseKey.isEmpty()) {
                flatKey = baseKey + "." + key
            }

            if (value.javaClass.name == "java.util.LinkedHashMap") {
                flatten(flatKey, value as Map<String, Any>, flatMap)
            } else {
                flatMap.put(flatKey, value)
            }
        }
    }

    fun onFileNotFound(callback: (fileName: String) -> Unit) {
        onFileNotFoundCallback = callback
    }

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
    @Throws(IOException::class)
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

    /**
     * Retrieve a String value with default.
     */
    @JvmOverloads internal fun getValueString(key: String,
                                              defaultValue: String = ""): String {
        return (this.configMap as java.util.Map<String, Any>).getOrDefault(key,
                                                                           defaultValue) as String
    }

    /**
     * Retrieve an Integer value with default.
     */
    @JvmOverloads internal fun getValueInt(key: String,
                                           defaultValue: Int? = null): Int? {
        return (this.configMap as java.util.Map<String, Any>).getOrDefault(key,
                                                                           defaultValue) as Int?
    }

    /**
     * * Retrieve a Boolean value with default.
     */
    @JvmOverloads internal fun getValueBoolean(key: String,
                                               defaultValue: Boolean? = false): Boolean? {
        return (this.configMap as java.util.Map<String, Any>).getOrDefault(key,
                                                                           defaultValue) as Boolean
    }

    /**
     * Retrieve a list value (a list of Strings).
     */
    internal fun getValueListStrings(key: String): List<String> {
        return getValueListStrings(key, emptyList())
    }

    /**
     * Retrieve a list value (a list of Strings).
     */
    internal fun getValueListStrings(key: String,
                                     defaultValue: List<String>): List<String> {
        return (this.configMap as java.util.Map<String, Any>).getOrDefault(key,
                                                                           defaultValue) as List<String>
    }

    /**
     * Retrieve a list value (a list of Maps)
     */
    internal fun getValueListMap(key: String): List<Map<String, Any>> {
        return getValueListMap(key, emptyList())
    }

    /**
     * Retrieve a list value (a list of Maps)
     */
    internal fun getValueListMap(key: String,
                                 defaultValue: List<Map<String, Any>>): List<Map<String, Any>> {
        return (this.configMap as java.util.Map<String, Any>).getOrDefault(key,
                                                                           defaultValue) as List<Map<String, Any>>
    }
}

