/*
 * This file is part of the librarian application.
 *
 * Copyright (c) Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package com.mags.librarian.classifier

import java.io.File

/**
 * Classifies files based on criteria.
 */
class Classifier(private val criteria: Criteria = Criteria()) {

    /**
     * Adds a new criterium to the criteria.
     */
    fun addCriterium(name: String,
                     extensions: List<String>,
                     filters: List<String>) {

        val criterium = Criterium(name, extensions, filters)
        criteria.add(criterium)
    }

    /**
     * Performs the classification.
     *
     * @param sourceFile
     * @return The classification
     */
    fun classify(sourceFile: File,
                 baseFolder: File): Classification {

        var classification = Classification()

        // check first for tvshows if present
        criteria.forEach { criterium ->
            if (criterium.name == "tvshows") {
                // try to match against this tv show criterium
                classification = FileMatcher.matchTVShow(sourceFile.name, criterium)
                if (!classification.tvShowName.isEmpty()) {
                    // it is a TV show
                    return classification
                }
            }
        }

        // other kind of files
        criteria.forEach { criterium ->
            if (criterium.name != "tvshows") {
                classification = FileMatcher.matchOtherFiles(sourceFile.name, criterium)

                // for music files check if in album folder
                if (classification.name == "music") {
                    // if file is in a subfolder, assume it is an album
                    if (sourceFile.parent != null) {
                        if (sourceFile.parent.toString() != baseFolder.toString()) {
                            classification.albumName = sourceFile.parentFile.name
                        }
                    }
                }

                if (!classification.name.isEmpty()) {
                    return classification
                }
            }
        }

        return classification
    }

}
