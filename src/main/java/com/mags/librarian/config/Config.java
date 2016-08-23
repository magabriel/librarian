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
import java.util.Map;

/**
 * Stores the configuration values.
 */
public class Config {

    final public static String DEFAULT_TVSHOWS_NUMBERING_SCHEMA = "S{season:2}E{episode:2}";
    final public static String DEFAULT_TVSHOWS_SEASON_SCHEMA = "Season_{season:2}";
    final public static String DEFAULT_WORDS_SEPARATOR_SHOW = "_";
    final public static String DEFAULT_WORDS_SEPARATOR_FILE = "_";

    public String include = "";

    public Map[] contentTypes = new Map[]{};
    public String[] inputFolders = new String[]{};
    public Map[] outputFolders = new Map[]{};
    public String tvShowsNumberingSchema = DEFAULT_TVSHOWS_NUMBERING_SCHEMA;
    public String tvShowsSeasonSchema = DEFAULT_TVSHOWS_SEASON_SCHEMA;

    public String tvShowsWordsSeparatorShow = DEFAULT_WORDS_SEPARATOR_SHOW;
    public String tvShowsWordsSeparatorFile = DEFAULT_WORDS_SEPARATOR_FILE;

    @Override
    public String toString() {

        return "Config{" +
                "include='" + include + '\'' +
                ", contentTypes=" + Arrays.toString(contentTypes) +
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

        if (otherConfig.contentTypes.length > 0) {
            contentTypes = otherConfig.contentTypes;
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
