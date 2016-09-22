/*
 * This file is part of the librarian application.
 *
 * Copyright (c) Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package com.mags.librarian.config;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Stores the configuration values.
 */
public class Config {

    public enum FilesAction {
        IGNORE,
        MOVE,
        DELETE
    }

    /*
     * Defaults
     */
    final static String DEFAULT_TVSHOWS_NUMBERING_SCHEMA = "S{season:2}E{episode:2}";
    final static String DEFAULT_TVSHOWS_SEASON_SCHEMA = "Season_{season:2}";
    final static String DEFAULT_WORDS_SEPARATOR_SHOW = "_";
    final static String DEFAULT_WORDS_SEPARATOR_FILE = "_";
    final static FilesAction DEFAULT_UNKNOWN_FILES_ACTION = FilesAction.IGNORE;
    final static FilesAction DEFAULT_ERROR_FILES_ACTION = FilesAction.IGNORE;

    /*
     * Fields
     */
    public String include = "";

    public Map<String, List<String>>[] extensions = new Map[]{};
    public Map<String, List<String>>[] filters = new Map[]{};
    public Map<String, Map>[] contentClasses = new Map[]{};
    public FilesAction unknownFilesAction = DEFAULT_UNKNOWN_FILES_ACTION;
    public String unknownFilesMovePath = "";
    public FilesAction errorFilesAction = DEFAULT_UNKNOWN_FILES_ACTION;
    public String errorFilesMovePath = "";

    public String[] inputFolders = new String[]{};
    public Map<String, Map>[] outputFolders = new Map[]{};
    public String tvShowsNumberingSchema = DEFAULT_TVSHOWS_NUMBERING_SCHEMA;
    public String tvShowsSeasonSchema = DEFAULT_TVSHOWS_SEASON_SCHEMA;

    public String tvShowsWordsSeparatorShow = DEFAULT_WORDS_SEPARATOR_SHOW;
    public String tvShowsWordsSeparatorFile = DEFAULT_WORDS_SEPARATOR_FILE;

    @Override
    public String toString() {

        return "Config{" +
                "include='" + include + '\'' +
                ", extensions=" + Arrays.toString(extensions) +
                ", filters=" + Arrays.toString(filters) +
                ", contentClasses=" + Arrays.toString(contentClasses) +
                ", unknownFilesAction=" + unknownFilesAction +
                ", unknownFilesMovePath=" + unknownFilesMovePath +
                ", errorFilesAction=" + errorFilesAction +
                ", errorFilesMovePath=" + errorFilesMovePath +
                ", inputFolders=" + Arrays.toString(inputFolders) +
                ", outputFolders=" + Arrays.toString(outputFolders) +
                ", tvShowsNumberingSchema='" + tvShowsNumberingSchema + '\'' +
                ", tvShowsSeasonSchema='" + tvShowsSeasonSchema + '\'' +
                ", tvShowsWordsSeparatorShow='" + tvShowsWordsSeparatorShow + '\'' +
                ", tvShowsWordsSeparatorFile='" + tvShowsWordsSeparatorFile + '\'' +
                '}';
    }

    /**
     * Merge this object with another one
     *
     * @param otherConfig
     * @return merged config
     */
    public Config merge(Config otherConfig) {

        if (otherConfig.extensions.length > 0) {
            extensions = otherConfig.extensions;
        }

        if (otherConfig.filters.length > 0) {
            filters = otherConfig.filters;
        }

        if (otherConfig.contentClasses.length > 0) {
            contentClasses = otherConfig.contentClasses;
        }

        if (!otherConfig.unknownFilesAction.equals(DEFAULT_UNKNOWN_FILES_ACTION)) {
            unknownFilesAction = otherConfig.unknownFilesAction;
        }

        if (!otherConfig.unknownFilesMovePath.isEmpty()) {
            unknownFilesMovePath = otherConfig.unknownFilesMovePath;
        }

        if (!otherConfig.errorFilesAction.equals(DEFAULT_UNKNOWN_FILES_ACTION)) {
            errorFilesAction = otherConfig.errorFilesAction;
        }

        if (!otherConfig.errorFilesMovePath.isEmpty()) {
            errorFilesMovePath = otherConfig.errorFilesMovePath;
        }

        if (otherConfig.inputFolders.length > 0) {
            inputFolders = otherConfig.inputFolders;
        }

        if (otherConfig.outputFolders.length > 0) {
            outputFolders = otherConfig.outputFolders;
        }

        if (!otherConfig.tvShowsNumberingSchema.equals(DEFAULT_TVSHOWS_NUMBERING_SCHEMA)) {
            tvShowsNumberingSchema = otherConfig.tvShowsNumberingSchema;
        }

        if (!otherConfig.tvShowsSeasonSchema.equals(DEFAULT_TVSHOWS_SEASON_SCHEMA)) {
            tvShowsSeasonSchema = otherConfig.tvShowsSeasonSchema;
        }

        if (!otherConfig.tvShowsWordsSeparatorShow.equals(DEFAULT_WORDS_SEPARATOR_SHOW)) {
            tvShowsWordsSeparatorShow = otherConfig.tvShowsWordsSeparatorShow;
        }

        if (!otherConfig.tvShowsWordsSeparatorFile.equals(DEFAULT_WORDS_SEPARATOR_FILE)) {
            tvShowsWordsSeparatorFile = otherConfig.tvShowsWordsSeparatorFile;
        }

        return this;
    }

}
