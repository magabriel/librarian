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
import java.nio.file.FileAlreadyExistsException
import java.util.*

/**
 * Loads a configuration YAML file.
 */
class ConfigLoader {
    private val configYaml: Yaml
    /**
     * Return the raw configuration object.
     *
     * @return The raw configuration object.
     */
    var configRaw: Map<String, Any>? = null
        private set
    private val configMap = LinkedHashMap<String, Any>()
    /**
     * Return the flattened configuration object.
     *
     * @return the flattened configuration object.
     */
    internal val configFlat: Map<String, Any>
        get() = configMap

    init {
        configYaml = Yaml()
    }

    /**
     * @param fileName
     * @throws FileNotFoundException
     */
    @Throws(FileNotFoundException::class)
    fun load(fileName: String) {
        // load the config file
        val input = FileInputStream(File(fileName))
        configRaw = this.configYaml.load(input) as Map<String, Any>

        flatten("", configRaw, configMap)
    }

    /**
     * Load a configuration file contents from a string.
     *
     * @param document
     */
    fun loadFromString(document: String) {
        configRaw = this.configYaml.load(document) as Map<String, Any>

        flatten("", configRaw, configMap)
    }

    /**
     * Flatten the config object (recursive hashmap) to a flat hasmap.
     *
     * @param baseKey      The base for the flattened keys.
     * @param recursiveMap The config object being flattened.
     * @param flatMap      The flattened config object.
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

    /**
     * Create the default config file from a given template.
     *
     * @param templateFile
     * @param fileName
     * @throws IOException
     */
    @Throws(IOException::class)
    fun createDefault(templateFile: String,
                      fileName: String) {
        val file = File(fileName)
        if (file.exists()) {
            throw FileAlreadyExistsException(
                    "File already exists. Delete or rename it an try again.")
        }
        // read default file
        val res = javaClass.getResourceAsStream(templateFile)
        val br = BufferedReader(InputStreamReader(res))
//        val lines = br.lines().collect<List<String>, Any>(Collectors.toList())
        val lines = br.lines().toArray()
        // write it
        val content = lines.joinToString(System.lineSeparator())
        val bw = BufferedWriter(FileWriter(file.absoluteFile))
        bw.write(content)
        bw.close()
    }

    /**
     * Retrieve a String value with default.
     *
     * @param key
     * @param defaultValue
     * @return The value of the key or defaultValue if not found.
     */
    @JvmOverloads internal fun getValueString(key: String,
                                              defaultValue: String = ""): String {
        return (this.configMap as java.util.Map<String, Any>).getOrDefault(key,
                                                                           defaultValue) as String
    }

    /**
     * Retrieve an Integer value with default.
     *
     * @param key
     * @param defaultValue
     * @return The value of the key or defaultValue if not found.
     */
    @JvmOverloads internal fun getValueInt(key: String,
                                           defaultValue: Int? = null): Int? {
        return (this.configMap as java.util.Map<String, Any>).getOrDefault(key,
                                                                           defaultValue) as Int?
    }

    /**
     * * Retrieve a Boolean value with default.
     *
     * @param key
     * @return The value of the key or false if not found.
     */
    @JvmOverloads internal fun getValueBoolean(key: String,
                                               defaultValue: Boolean? = false): Boolean? {
        return (this.configMap as java.util.Map<String, Any>).getOrDefault(key,
                                                                           defaultValue) as Boolean
    }

    /**
     * Retrieve a list value (a list of Strings).
     *
     * @param key
     * @return The value of the key as a List, or an empty List if not set.
     */
    internal fun getValueListStrings(key: String): List<String> {
        return getValueListStrings(key, emptyList())
    }

    /**
     * Retrieve a list value (a list of Strings).
     *
     * @param key
     * @return The value of the key as a List, or an empty List if not set.
     */
    internal fun getValueListStrings(key: String,
                                     defaultValue: List<String>): List<String> {
        return (this.configMap as java.util.Map<String, Any>).getOrDefault(key,
                                                                           defaultValue) as List<String>
    }

    /**
     * Retrieve a list value (a list of Maps)
     *
     * @param key
     * @return The value of the key as a List, or an empty List if not set.
     */
    internal fun getValueListMap(key: String): List<Map<String, Any>> {
        return getValueListMap(key, emptyList())
    }

    /**
     * Retrieve a list value (a list of Maps)
     *
     * @param key
     * @return The value of the key as a List, or an empty List if not set.
     */
    internal fun getValueListMap(key: String,
                                 defaultValue: List<Map<String, Any>>): List<Map<String, Any>> {
        return (this.configMap as java.util.Map<String, Any>).getOrDefault(key,
                                                                           defaultValue) as List<Map<String, Any>>
    }
}
/**
 * Retrieve a String value.
 *
 * @param key
 * @return The value of the key or null if not found.
 */
/**
 * Retrieve an Integer value.
 *
 * @param key
 * @return The value of the key or null if not found.
 */
/**
 * * Retrieve a Boolean value with default.
 *
 * @param key
 * @return The value of the key or false if not found.
 */

