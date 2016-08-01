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
 * Creates a Config object from the values loaded by a ConfigLoader instance.
 */
public class ConfigAdaptor {

    private final ConfigLoader loader;

    public ConfigAdaptor(ConfigLoader loader) {

        this.loader = loader;
    }

    /**
     * Creates the Config object.
     *
     * @return The Config object.
     */
    public Config process() {

        Config config = new Config();

        config.setContentTypes(loader.getValueListMap("config.content_types").toArray(new Map[0]));

        config.setTvShowsNumberingSchema(loader.getValueString("config.tvshows.numbering_schema", config.getTvShowsNumberingSchema()));
        config.setTvShowsSeasonSchema(loader.getValueString("config.tvshows.season_schema", config.getTvShowsSeasonSchema()));
        config.setWordsSeparator(loader.getValueString("config.words_separator", config.getWordsSeparator()));

        config.setInputFolders(loader.getValueListStrings("input.folders").toArray(new String[0]));
        config.setOutputFolders(loader.getValueListMap("output.folders").toArray(new Map[0]));

        return config;
    }
}
