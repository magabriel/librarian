/*
 * This file is part of the librarian application.
 *
 * Copyright (c) Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package com.mags.librarian.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Stores the configuration values.
 */
public class Config {

    final private String DEFAULT_TVSHOWS_NUMBERING_SCHEMA = "S{season:2}E{episode:2}";
    final private String DEFAULT_TVSHOWS_SEASON_SCHEMA = "Season_{season:2}";
    final private String DEFAULT_WORDS_SEPARATOR = "_";

    public String include = "";

    public Map[] contentTypes = new Map[]{};
    public String[] inputFolders = new String[]{};
    public Map[] outputFolders = new Map[]{};
    public String tvShowsNumberingSchema = DEFAULT_TVSHOWS_NUMBERING_SCHEMA;
    public String tvShowsSeasonSchema = DEFAULT_TVSHOWS_SEASON_SCHEMA;

    public String tvShowsWordsSeparatorShow = DEFAULT_WORDS_SEPARATOR;
    public String tvShowsWordsSeparatorFile = DEFAULT_WORDS_SEPARATOR;

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

        if (!otherConfig.tvShowsNumberingSchema.isEmpty()) {
            tvShowsNumberingSchema = otherConfig.tvShowsNumberingSchema;
        }

        if (!otherConfig.tvShowsSeasonSchema.isEmpty()) {
            tvShowsSeasonSchema = otherConfig.tvShowsSeasonSchema;
        }

        if (!otherConfig.tvShowsWordsSeparatorShow.isEmpty()) {
            tvShowsWordsSeparatorShow = otherConfig.tvShowsWordsSeparatorShow;
        }

        if (!otherConfig.tvShowsWordsSeparatorFile.isEmpty()) {
            tvShowsWordsSeparatorFile = otherConfig.tvShowsWordsSeparatorFile;
        }

        return this;
    }

}
