/*
 * This file is part of the librarian application.
 *
 * Copyright (c) 2016 Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code
 */

package com.mags.librarian.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

class ConfigTest {
    private val baseConfig: Config
        get() {
            val config = Config()
            // extensions
            val extensions = ArrayList<Map<String, List<String>>>()
            val extensionsItems = LinkedHashMap<String, List<String>>()
            extensionsItems.put("extensions.key.1", listOf("extensions.value.1"))
            extensionsItems.put("extensions.key.2", listOf("extensions.value.2"))
            extensions.add(extensionsItems)
            config.extensions = extensions.toTypedArray<Map<String, List<String>>>()
            // filters
            val filters = ArrayList<Map<String, List<String>>>()
            val filterItems = LinkedHashMap<String, List<String>>()
            filterItems.put("filters.key.1", listOf("filters.value.1"))
            filterItems.put("filters.key.2", listOf("filters.value.2"))
            filters.add(filterItems)
            config.filters = filters.toTypedArray<Map<String, List<String>>>()
            // inputFolders
            config.inputFolders = arrayOf("inputFolders.1", "inputfolders.2")
            // outputFolders not provided
            // tvShowsNumberingSchema
            config.tvShowsNumberingSchema = "tvShowsNumberingSchema.base"
            // tvShowsSeasonSchema not provided
            // tvShowsWordsSeparatorShow
            config.tvShowsWordsSeparatorShow = "tvShowsWordsSeparatorShow.base"
            // executeSuccess
            config.executeSuccess = "executeSuccess.base"
            // executeError not provided
            return config
        }
    private val otherConfig: Config
        get() {
            val config = Config()
            // extensions
            val extensions = ArrayList<Map<String, List<String>>>()
            val extensionsItems = LinkedHashMap<String, List<String>>()
            extensionsItems.put("extensions.key.1", listOf("extensions.value.1b"))
            extensionsItems.put("extensions.key.3", listOf("extensions.value.3"))
            extensions.add(extensionsItems)
            config.extensions = extensions.toTypedArray<Map<String, List<String>>>()
            // filters not provided
            // inputFolders not provided
            // outputFolders
            val outputFolders = ArrayList<Map<String, String>>()
            val outputFoldersItems = LinkedHashMap<String, String>()
            outputFoldersItems.put("outputFolders.key.1", "outputFolders.value.1b")
            outputFoldersItems.put("outputFolders.key.3", "outputFolders.value.3")
            outputFolders.add(outputFoldersItems)
            config.outputFolders = outputFolders.toTypedArray<Map<String, String>>()
            // tvShowsNumberingSchema not provided
            // tvShowsSeasonSchema not provided
            // tvShowsWordsSeparatorShow
            config.tvShowsWordsSeparatorShow = "tvShowsWordsSeparatorShow.other"
            // executeSuccess not provided
            // executeError
            config.executeError = "executeError.other"

            return config
        }
    private val mergedConfig: Config
        get() {
            val config = Config()
            // extensions
            val extensions = ArrayList<Map<String, List<String>>>()
            val extensionsItems = LinkedHashMap<String, List<String>>()
            extensionsItems.put("extensions.key.1", listOf("extensions.value.1b"))
            extensionsItems.put("extensions.key.3", listOf("extensions.value.3"))
            extensions.add(extensionsItems)
            config.extensions = extensions.toTypedArray<Map<String, List<String>>>()
            // filters
            val filters = ArrayList<Map<String, List<String>>>()
            val filterItems = LinkedHashMap<String, List<String>>()
            filterItems.put("filters.key.1", listOf("filters.value.1"))
            filterItems.put("filters.key.2", listOf("filters.value.2"))
            filters.add(filterItems)
            config.filters = filters.toTypedArray<Map<String, List<String>>>()
            // inputFolders
            config.inputFolders = arrayOf("inputFolders.1", "inputfolders.2")
            // outputFolders
            val outputFolders = ArrayList<Map<String, String>>()
            val outputFoldersItems = LinkedHashMap<String, String>()
            outputFoldersItems.put("outputFolders.key.1", "outputFolders.value.1b")
            outputFoldersItems.put("outputFolders.key.3", "outputFolders.value.3")
            outputFolders.add(outputFoldersItems)
            config.outputFolders = outputFolders.toTypedArray<Map<String, String>>()
            // tvShowsNumberingSchema
            config.tvShowsNumberingSchema = "tvShowsNumberingSchema.base"
            // tvShowsSeasonSchema
            config.tvShowsSeasonSchema = Config.DEFAULT_TVSHOWS_SEASON_SCHEMA
            // tvShowsWordsSeparatorShow
            config.tvShowsWordsSeparatorShow = "tvShowsWordsSeparatorShow.other"
            // executeSuccess
            config.executeSuccess = "executeSuccess.base"
            // executeError
            config.executeError = "executeError.other"

            return config
        }

    @Test
    @Throws(Exception::class)
    fun merge() {
        val base = baseConfig
        val other = otherConfig
        val actual = base.merge(other)
        val expected = mergedConfig

        assertEquals(expected.toString(), actual.toString())
    }
}