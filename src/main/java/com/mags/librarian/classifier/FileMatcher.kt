/*
 * This file is part of the librarian application.
 *
 * Copyright (c) 2016 Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code
 */

package com.mags.librarian.classifier

import java.util.*
import java.util.regex.Pattern

object FileMatcher {

    /**
     * Try to match a TV show name against a filename.
     *
     * @param fileName   The filename
     * @param tvshowName The TV show name
     * @return True if match
     */
    fun matchTVShowName(fileName: String,
                        tvshowName: String): Boolean {
        var tvshowName = tvshowName

        // transform word separators to match any of them
        tvshowName = tvshowName.replace("_", " ").replace(".", " ").trim { it <= ' ' }.replace(" ",
                                                                                               "[ _\\.]").replace(
                "(", "\\(").replace(")", "\\)")

        val regExp = Pattern.compile(tvshowName, Pattern.CASE_INSENSITIVE)
        val matcher = regExp.matcher(fileName)

        return matcher.find()
    }

    /**
     * Try to match a TV show name against a filename.
     *
     * @param fileName          The filename
     * @param regularExpression The regExp to match against
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
     * @param fileName  The filename
     * @param criterium The criteriumm of a TV show
     * @return The corresponding Classification object if match, or empty if not
     */
    fun matchTVShow(fileName: String,
                    criterium: Criterium): Classification {

        val classification = Classification()

        // check extension first, if specified
        if (criterium.extensions.isNotEmpty()) {
            if (!Arrays.asList(*criterium.extensions).contains(getFileExtension(fileName))) {
                return classification
            }
        }

        val filenameNoExtension = getFilenameWithoutExtension(fileName)

        // check filters now
        for (filter in criterium.filters) {

            val regExp = Pattern.compile(filter, Pattern.CASE_INSENSITIVE)
            val matcher = regExp.matcher(filenameNoExtension)

            if (matcher.find()) {

                classification.name = criterium.name
                classification.fileName = fileName
                classification.baseName = getFilenameWithoutExtension(fileName)
                classification.extension = getFileExtension(fileName)

                try {
                    var tvShowName = matcher.group("name").trim { it <= ' ' }

                    // replace word separators with spaces in captured TVshow name
                    tvShowName = matcher.group("name").replace("_", " ").replace("-", " ").replace(
                            ".", " ").trim { it <= ' ' }
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
                        classification.tvShowRest = matcher.group("rest").replace("_", " ").replace(
                                "-", " ").replace(".", " ").trim { it <= ' ' }
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
     * @param fileName  The filename
     * @param criterium The criteriumm
     * @return The corresponding Classification object if match, or empty if not
     */
    fun matchOtherFiles(fileName: String,
                        criterium: Criterium): Classification {

        val classification = Classification()

        var matchExtensions = false
        var matchFilters = false

        // check extension if specified
        if (criterium.extensions.isNotEmpty()) {
            if (Arrays.asList(*criterium.extensions).contains(getFileExtension(fileName))) {
                matchExtensions = true
            }
        }

        val filenameNoExtension = getFilenameWithoutExtension(fileName)

        // check filters if specified
        for (filter in criterium.filters) {

            val regExp = Pattern.compile(filter, Pattern.CASE_INSENSITIVE)
            val matcher = regExp.matcher(filenameNoExtension)

            if (matcher.find()) {
                matchFilters = true
            }
        }

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

        return if (extension.isEmpty()) {
            fileName
        } else fileName.substring(0, fileName.length - extension.length - 1)

    }
}
