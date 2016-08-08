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

    final private String DEFAULT_TVSHOWS_NUMBERING_SCHEMA = "S{season:2}E{episode:2}";
    final private String DEFAULT_TVSHOWS_SEASON_SCHEMA = "Season_{season:2}";
    final private String DEFAULT_WORDS_SEPARATOR = "_";

    public Map[] contentTypes = new Map[]{};
    public String[] inputFolders = new String[]{};
    public Map[] outputFolders = new Map[]{};
    public String tvShowsNumberingSchema = DEFAULT_TVSHOWS_NUMBERING_SCHEMA;
    public String tvShowsSeasonSchema = DEFAULT_TVSHOWS_SEASON_SCHEMA;

    public String wordsSeparatorShow = DEFAULT_WORDS_SEPARATOR;
    public String wordsSeparatorFile = DEFAULT_WORDS_SEPARATOR;
}
