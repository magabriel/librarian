/*
 * This file is part of the librarian application.
 *
 * Copyright (c) 2016 Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code
 */

package com.mags.librarian.classifier

import com.mags.librarian.config.Config

class Criteria(private val config: Config) {

    val criteriumList = mutableListOf<Criterium>()

    init {
        processContentClasses()
    }

    /**
     * Create the list of criterium objects.
     */
    private fun processContentClasses() {

        for (contentClass in config.contentClasses) {
            val name = contentClass.keys.toTypedArray()[0]
            val criterium = Criterium(name,
                                      extractExtensions(contentClass, name),
                                      extractFilters(contentClass, name))

            criteriumList.add(criterium)
        }
    }

    /**
     * Extract the extensions for the criterium.
     */
    private fun extractExtensions(contentClass: Map<String, Map<*, *>>,
                                  criteriumName: String): List<String> {

        val extensions = listOf<String>()

        if (!contentClass[criteriumName]?.containsKey("extension")!!) {
            return extensions
        }

        val extensionName = contentClass[criteriumName]!!["extension"].toString()

        for (extensionItems in config.extensions) {
            val currentExtensionName = extensionItems.keys.toTypedArray()[0]
            if (extensionName == currentExtensionName) {
                // found
                return extensionItems[currentExtensionName] as List<String>
            }
        }

        return extensions
    }

    /**
     * Extract the filters for the criterium.
     */
    private fun extractFilters(contentClass: Map<String, Map<*, *>>,
                               criteriumName: String): List<String> {

        val filters = listOf<String>()

        if (!contentClass[criteriumName]?.containsKey("filter")!!) {
            return filters
        }

        val filterName = contentClass[criteriumName]!!["filter"].toString()

        for (filterItems in config.filters) {
            val currentFilterName = filterItems.keys.toTypedArray()[0].toString()
            if (filterName == currentFilterName) {
                // found
                return filterItems[currentFilterName] as List<String>
            }
        }

        return filters
    }
}
