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

class Criteria(private val config: Config? = null) : MutableCollection<Criterium> {

    val criteriumList = mutableListOf<Criterium>()

    override val size: Int
        get() = criteriumList.size

    override fun contains(element: Criterium): Boolean {
        return criteriumList.contains(element)
    }

    override fun containsAll(elements: Collection<Criterium>): Boolean {
        return criteriumList.containsAll(elements)
    }

    override fun isEmpty(): Boolean {
        return criteriumList.isEmpty()
    }

    override fun add(element: Criterium): Boolean {
        return criteriumList.add(element)
    }

    override fun addAll(elements: Collection<Criterium>): Boolean {
        return criteriumList.addAll(elements)
    }

    override fun clear() {
        return criteriumList.clear()
    }

    override fun iterator(): MutableIterator<Criterium> {
        return criteriumList.iterator()
    }

    override fun remove(element: Criterium): Boolean {
        return criteriumList.remove(element)
    }

    override fun removeAll(elements: Collection<Criterium>): Boolean {
        return criteriumList.removeAll(elements)
    }

    override fun retainAll(elements: Collection<Criterium>): Boolean {
        return retainAll(elements)
    }

    init {
        processContentClasses()
    }

    /**
     * Create the list of criterium objects.
     */
    private fun processContentClasses() {

        config?.contentClasses?.forEach { contentClass ->
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

        config?.extensions?.forEach { extensionItems ->
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

        config?.filters?.forEach { filterItems ->
            val currentFilterName = filterItems.keys.toTypedArray()[0].toString()
            if (filterName == currentFilterName) {
                // found
                return filterItems[currentFilterName] as List<String>
            }
        }

        return filters
    }
}
