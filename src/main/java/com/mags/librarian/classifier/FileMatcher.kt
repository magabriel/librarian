/*
 * This file is part of the librarian application.
 *
 * Copyright (c) 2016 Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code
 */

package com.mags.librarian.classifier

import java.util.regex.Pattern

object FileMatcher {

    /**
     * Try to match a TV show name against a filename.
     *
     * @return True if match
     */
    fun matchTVShowName(fileName: String,
                        tvshowName: String): Boolean {
        var tvshowName = tvshowName

        // transform word separators to match any of them
        tvshowName = tvshowName.replace("_", " ")
        tvshowName = tvshowName.replace(".", " ")
        tvshowName = tvshowName.trim { it <= ' ' }
        tvshowName = tvshowName.replace(" ", "[ _\\.]")
        tvshowName = tvshowName.replace("(", "\\(")
        tvshowName = tvshowName.replace(")", "\\)")

        val regExp = Pattern.compile(tvshowName, Pattern.CASE_INSENSITIVE)
        val matcher = regExp.matcher(fileName)

        return matcher.find()
    }

    /**
     * Try to match a TV show name against a filename.
     *
     * @return True if match
     */
    fun matchRegExp(fileName: String,
                    regularExpression: String): Boolean {

        val regExp = Pattern.compile(regularExpression, Pattern.CASE_INSENSITIVE)
        val matcher = regExp.matcher(fileName)

        return matcher.find()
    }

    /**
     * Try to match a filename against a criterium defining a TV show.
     *
     * @return The corresponding Classification object if match, or empty if not
     */
    fun matchTVShow(fileName: String,
                    criterium: Criterium): Classification {

        val classification = Classification()

        // check extension first, if specified
        if (criterium.extensions.isNotEmpty()) {
            if (!criterium.extensions.contains(getFileExtension(fileName))) {
                return classification
            }
        }

        val filenameNoExtension = getFilenameWithoutExtension(fileName)

        // check filters now
        criterium.filters.forEach { filter ->
            val regExp = Pattern.compile(filter, Pattern.CASE_INSENSITIVE)
            val matcher = regExp.matcher(filenameNoExtension)

            if (matcher.find()) {

                classification.name = criterium.name
                classification.fileName = fileName
                classification.baseName = getFilenameWithoutExtension(fileName)
                classification.extension = getFileExtension(fileName)

                try {
                    // replace word separators with spaces in captured TVshow name
                    var tvShowName = matcher.group("name")
                    tvShowName = tvShowName.replace("_", " ")
                    tvShowName = tvShowName.replace("-", " ")
                    tvShowName = tvShowName.replace(".", " ")
                    tvShowName = tvShowName.trim { it <= ' ' }
                    classification.tvShowName = tvShowName
                } catch (e: IllegalArgumentException) {
                }

                try {
                    classification.season = Integer.parseInt(matcher.group("season"))
                } catch (e: IllegalArgumentException) {
                }

                try {
                    classification.episode = Integer.parseInt(matcher.group("episode"))
                } catch (e: IllegalArgumentException) {
                }

                try {
                    // optional
                    if (matcher.group("rest") != null) {
                        // replace word separators with spaces in captured TVshow rest
                        var tvShowRest = matcher.group("rest")
                        tvShowRest = tvShowRest.replace("_", " ")
                        tvShowRest = tvShowRest.replace("-", " ")
                        tvShowRest = tvShowRest.replace(".", " ")
                        tvShowRest = tvShowRest.trim { it <= ' ' }
                        classification.tvShowRest = tvShowRest
                    }
                } catch (e: IllegalArgumentException) {
                }

                return classification
            }
        }

        return classification
    }

    /**
     * Try to match a filename against a criterium.
     *
     * @return The corresponding Classification object if match, or empty if not
     */
    fun matchOtherFiles(fileName: String,
                        criterium: Criterium): Classification {

        val classification = Classification()

        var matchExtensions = false

        // check extension if specified
        if (criterium.extensions.isNotEmpty()) {
            if (criterium.extensions.contains(getFileExtension(fileName))) {
                matchExtensions = true
            }
        }

        val filenameNoExtension = getFilenameWithoutExtension(fileName)

        // check filters if specified
        val matchFilters = criterium.filters
                .map { Pattern.compile(it, Pattern.CASE_INSENSITIVE) }
                .map { it.matcher(filenameNoExtension) }
                .any { it.find() }

        // just one if enough
        if (matchExtensions || matchFilters) {
            classification.name = criterium.name
            classification.fileName = fileName
            classification.baseName = getFilenameWithoutExtension(fileName)
            classification.extension = getFileExtension(fileName)

            return classification
        }

        return classification
    }

    private fun getFileExtension(fileName: String): String {
        var extension = ""
        val i = fileName.lastIndexOf('.')
        if (i > 0) {
            extension = fileName.substring(i + 1)
        }
        return extension
    }

    private fun getFilenameWithoutExtension(fileName: String): String {
        val extension = getFileExtension(fileName)
        if (extension.isEmpty()) {
            return fileName
        }
        return fileName.substring(0, fileName.length - extension.length - 1)
    }
}
