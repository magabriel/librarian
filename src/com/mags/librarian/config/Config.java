/*
 * This file is part of the librarian application.
 *
 * Copyright (c) Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package com.mags.librarian.config;

import java.util.Map;

/**
 * Stores the configuration values.
 */
public class Config {

    final private String DEFAULT_TVSHOWS_NUMBERING_SCHEMA =  "S{season:2}E{episode:2}";
    final private String DEFAULT_TVSHOWS_SEASON_SCHEMA = "Season_{season:2}";
    final private String DEFAULT_WORDS_SEPARATOR = "_";

    private Map[] contentTypes = new Map[]{};
    private String[] inputFolders = new String[]{};
    private Map[] outputFolders = new Map[]{};
    private String tvShowsNumberingSchema = DEFAULT_TVSHOWS_NUMBERING_SCHEMA;
    private String tvShowsSeasonSchema = DEFAULT_TVSHOWS_SEASON_SCHEMA;
    private String wordsSeparator = DEFAULT_WORDS_SEPARATOR;

    public String[] getInputFolders() {
        return inputFolders;
    }

    public void setInputFolders(String[] inputFolders) {
        this.inputFolders = inputFolders;
    }

    public Map[] getOutputFolders() {
        return outputFolders;
    }

    public void setOutputFolders(Map[] outputFolders) {
        this.outputFolders = outputFolders;
    }

    public Map[] getContentTypes() {
        return contentTypes;
    }

    public void setContentTypes(Map[] contentTypes) {
        this.contentTypes = contentTypes;
    }

    public String getTvShowsNumberingSchema() {

        return tvShowsNumberingSchema;
    }

    public void setTvShowsNumberingSchema(String tvShowsNumberingSchema) {

        this.tvShowsNumberingSchema = tvShowsNumberingSchema;
    }

    public String getTvShowsSeasonSchema() {

        return tvShowsSeasonSchema;
    }

    public void setTvShowsSeasonSchema(String tvShowsSeasonSchema) {

        this.tvShowsSeasonSchema = tvShowsSeasonSchema;
    }

    public String getWordsSeparator() {

        return wordsSeparator;
    }

    public void setWordsSeparator(String wordsSeparator) {

        this.wordsSeparator = wordsSeparator;
    }
}
