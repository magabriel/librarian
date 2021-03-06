/*
 * This file is part of the librarian application.
 *
 * Copyright (c) Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package com.mags.librarian.config

/**
 * Stores the configuration values.
 */
class Config {

    /*
     * Fields
     */
    var include = ""

    var extensions = arrayOf<Map<String, List<String>>>()
    var filters = arrayOf<Map<String, List<String>>>()
    var contentClasses = arrayOf<Map<String, Map<String, String>>>()
    var unknownFilesAction = DEFAULT_UNKNOWN_FILES_ACTION
    var unknownFilesMovePath = ""
    var duplicateFilesAction = DEFAULT_DUPLICATE_FILES_ACTION
    var duplicateFilesMovePath = ""
    var errorFilesAction = DEFAULT_ERROR_FILES_ACTION
    var errorFilesMovePath = ""

    var inputFolders = arrayOf<String>()
    var outputFolders = arrayOf<Map<String, String>>()
    var tvShowsNumberingSchema = DEFAULT_TVSHOWS_NUMBERING_SCHEMA
    var tvShowsSeasonSchema = DEFAULT_TVSHOWS_SEASON_SCHEMA

    var tvShowsWordsSeparatorShow = DEFAULT_WORDS_SEPARATOR_SHOW
    var tvShowsWordsSeparatorFile = DEFAULT_WORDS_SEPARATOR_FILE

    var executeSuccess = ""
    var executeError = ""

    enum class FilesAction {
        IGNORE, MOVE, DELETE
    }

    companion object {
        const val DEFAULT_TVSHOWS_NUMBERING_SCHEMA = "S{season:2}E{episode:2}"
        const val DEFAULT_TVSHOWS_SEASON_SCHEMA = "Season_{season:2}"
        const val DEFAULT_WORDS_SEPARATOR_SHOW = "_"
        const val DEFAULT_WORDS_SEPARATOR_FILE = "_"

        val DEFAULT_UNKNOWN_FILES_ACTION = FilesAction.IGNORE
        val DEFAULT_ERROR_FILES_ACTION = FilesAction.IGNORE
        val DEFAULT_DUPLICATE_FILES_ACTION = FilesAction.IGNORE
    }

    override fun toString(): String {
        var str = "Config{"
        str += "include='$include'"
        str += ", extensions=${extensions.toList().toString()}"
        str += ", filters=${filters.toList().toString()}"
        str += ", contentClasses=${contentClasses.toList().toString()}"
        str += ", unknownFilesAction=$unknownFilesAction"
        str += ", unknownFilesMovePath=$unknownFilesMovePath"
        str += ", duplicateFilesAction=$duplicateFilesAction"
        str += ", duplicateFilesMovePath=$duplicateFilesMovePath"
        str += ", errorFilesAction=$errorFilesAction"
        str += ", errorFilesMovePath=$errorFilesMovePath"
        str += ", executeSuccess=$executeSuccess"
        str += ", executeError=$executeError"
        str += ", inputFolders=${inputFolders.toList().toString()}"
        str += ", outputFolders=${outputFolders.toList().toString()}"
        str += ", tvShowsNumberingSchema='$tvShowsNumberingSchema'"
        str += ", tvShowsSeasonSchema='$tvShowsSeasonSchema'"
        str += ", tvShowsWordsSeparatorShow='$tvShowsWordsSeparatorShow'"
        str += ", tvShowsWordsSeparatorFile='$tvShowsWordsSeparatorFile'"
        str += '}'

        return str
    }

    /**
     * Merge this object with another one
     *
     * @param otherConfig
     * @return merged config
     */
    fun merge(otherConfig: Config): Config {

        if (otherConfig.extensions.size > 0) {
            extensions = otherConfig.extensions
        }

        if (otherConfig.filters.size > 0) {
            filters = otherConfig.filters
        }

        if (otherConfig.contentClasses.size > 0) {
            contentClasses = otherConfig.contentClasses
        }

        if (otherConfig.unknownFilesAction != DEFAULT_UNKNOWN_FILES_ACTION) {
            unknownFilesAction = otherConfig.unknownFilesAction
        }

        if (!otherConfig.unknownFilesMovePath.isEmpty()) {
            unknownFilesMovePath = otherConfig.unknownFilesMovePath
        }

        if (otherConfig.duplicateFilesAction != DEFAULT_DUPLICATE_FILES_ACTION) {
            duplicateFilesAction = otherConfig.duplicateFilesAction
        }

        if (!otherConfig.duplicateFilesMovePath.isEmpty()) {
            duplicateFilesMovePath = otherConfig.duplicateFilesMovePath
        }

        if (otherConfig.errorFilesAction != DEFAULT_ERROR_FILES_ACTION) {
            errorFilesAction = otherConfig.errorFilesAction
        }

        if (!otherConfig.errorFilesMovePath.isEmpty()) {
            errorFilesMovePath = otherConfig.errorFilesMovePath
        }

        if (!otherConfig.executeSuccess.isEmpty()) {
            executeSuccess = otherConfig.executeSuccess
        }

        if (!otherConfig.executeError.isEmpty()) {
            executeError = otherConfig.executeError
        }

        if (otherConfig.inputFolders.size > 0) {
            inputFolders = otherConfig.inputFolders
        }

        if (otherConfig.outputFolders.size > 0) {
            outputFolders = otherConfig.outputFolders
        }

        if (otherConfig.tvShowsNumberingSchema != DEFAULT_TVSHOWS_NUMBERING_SCHEMA) {
            tvShowsNumberingSchema = otherConfig.tvShowsNumberingSchema
        }

        if (otherConfig.tvShowsSeasonSchema != DEFAULT_TVSHOWS_SEASON_SCHEMA) {
            tvShowsSeasonSchema = otherConfig.tvShowsSeasonSchema
        }

        if (otherConfig.tvShowsWordsSeparatorShow != DEFAULT_WORDS_SEPARATOR_SHOW) {
            tvShowsWordsSeparatorShow = otherConfig.tvShowsWordsSeparatorShow
        }

        if (otherConfig.tvShowsWordsSeparatorFile != DEFAULT_WORDS_SEPARATOR_FILE) {
            tvShowsWordsSeparatorFile = otherConfig.tvShowsWordsSeparatorFile
        }

        return this
    }

}
