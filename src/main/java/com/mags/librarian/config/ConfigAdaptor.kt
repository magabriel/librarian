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
 * Creates a Config object from the values loaded by a ConfigLoader instance.
 */
class ConfigAdaptor(private val loader: ConfigLoader) {
    /**
     * Creates the Config object.
     *
     * @return The Config object.
     */
    fun process(): Config {
        val config = Config()

        config.include = loader.getValueString("include", "")
        @Suppress("UNCHECKED_CAST")
        config.extensions = loader.getValueListMap(
                "config.extensions").map { it as Map<String, List<String>> }.toTypedArray()
        @Suppress("UNCHECKED_CAST")
        config.filters = loader.getValueListMap(
                "config.filters").map { it as Map<String, List<String>> }.toTypedArray()
        @Suppress("UNCHECKED_CAST")
        config.contentClasses = loader.getValueListMap(
                "config.content_classes").map { it as Map<String, Map<String, String>> }.toTypedArray()

        config.unknownFilesAction = Config.FilesAction.valueOf(
                loader.getValueString("config.errors.unknown_files.action",
                                      config.unknownFilesAction.toString()).toUpperCase())
        config.unknownFilesMovePath = loader.getValueString("config.errors.unknown_files.move_path",
                                                            config.unknownFilesMovePath)

        config.duplicateFilesAction = Config.FilesAction.valueOf(
                loader.getValueString("config.errors.duplicate_files.action",
                                      config.duplicateFilesAction.toString()).toUpperCase())
        config.duplicateFilesMovePath = loader.getValueString(
                "config.errors.duplicate_files.move_path", config.duplicateFilesMovePath)

        config.errorFilesAction = Config.FilesAction.valueOf(
                loader.getValueString("config.errors.error_files.action",
                                      config.errorFilesAction.toString()).toUpperCase())
        config.errorFilesMovePath = loader.getValueString("config.errors.error_files.move_path",
                                                          config.errorFilesMovePath)

        config.tvShowsNumberingSchema = loader.getValueString("config.tvshows.numbering_schema",
                                                              config.tvShowsNumberingSchema)

        config.tvShowsSeasonSchema = loader.getValueString("config.tvshows.season_schema",
                                                           config.tvShowsSeasonSchema)

        config.tvShowsWordsSeparatorShow = loader.getValueString(
                "config.tvshows.words_separator.show", config.tvShowsWordsSeparatorShow)

        config.tvShowsWordsSeparatorFile = loader.getValueString(
                "config.tvshows.words_separator.file", config.tvShowsWordsSeparatorFile)

        config.executeSuccess = loader.getValueString("config.execute.success",
                                                      config.executeSuccess)

        config.executeError = loader.getValueString("config.execute.error", config.executeError)
        config.inputFolders = loader.getValueListStrings("input.folders").toTypedArray()
        @Suppress("UNCHECKED_CAST")
        config.outputFolders = loader.getValueListMap("output.folders").map {
            it as Map<String, String>
        }.toTypedArray()

        return config
    }
}
