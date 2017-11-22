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
import java.util.*

class Criteria(private val config: Config) {

    val criteriumList = mutableListOf<Criterium>()

    init {
        processContentClasses()
    }

    /**
     * Create the list of criterium objects.
     *
     * @return Criteria (list of criterium objects)
     */
    private fun processContentClasses() {

        for (contentClass in config.contentClasses) {

            val criterium = Criterium()
            criterium.name = contentClass.keys.toTypedArray()[0].toString()
            criterium.extensions = extractExtensions(contentClass, criterium.name).toTypedArray()
            criterium.filters = extractFilters(contentClass, criterium.name).toTypedArray()

            criteriumList.add(criterium)
        }
    }

    /**
     * Extract the extensions for the criterium.
     *
     * @param contentClass  The ContentClass being processed
     * @param criteriumName The name of the criterium being processed
     * @return A list of extensions
     */
    private fun extractExtensions(contentClass: Map<String, Map<*, *>>,
                                  criteriumName: String): List<String> {

        val extensions = ArrayList<String>()

        if (!contentClass[criteriumName]?.containsKey("extension")!!) {
            return extensions
        }

        val extensionName = contentClass[criteriumName]!!["extension"].toString()

        for (extensionItems in config.extensions) {
            val currentExtensionName = extensionItems.keys.toTypedArray()[0].toString()
            if (extensionName == currentExtensionName) {
                // found
                return extensionItems[currentExtensionName] as List<String>
            }
        }

        return extensions
    }

    /**
     * Extract the filters for the criterium.
     *
     * @param contentClass  The ContentClass being processed
     * @param criteriumName The name of the criterium being processed
     * @return A list of filters
     */
    private fun extractFilters(contentClass: Map<String, Map<*, *>>,
                               criteriumName: String): List<String> {

        val filters = ArrayList<String>()

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
