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

        config.include = loader.getValueString("include", "");

        config.extensions = loader.getValueListMap("config.extensions").toArray(new Map[0]);
        config.filters = loader.getValueListMap("config.filters").toArray(new Map[0]);
        config.contentClasses = loader.getValueListMap("config.content_classes").toArray(new Map[0]);

        config.unknownFilesAction = Config.FilesAction.valueOf(
                loader.getValueString("config.unknown_files.action",
                                      config.unknownFilesAction.toString()).toUpperCase());
        config.unknownFilesMovePath =
                loader.getValueString("config.unknown_files.move_path",
                                                            config.unknownFilesMovePath);

        config.tvShowsNumberingSchema =
                loader.getValueString("config.tvshows.numbering_schema",
                                      config.tvShowsNumberingSchema);

        config.tvShowsSeasonSchema =
                loader.getValueString("config.tvshows.season_schema",
                                      config.tvShowsSeasonSchema);

        config.tvShowsWordsSeparatorShow =
                loader.getValueString("config.tvshows.words_separator.show",
                                      config.tvShowsWordsSeparatorShow);

        config.tvShowsWordsSeparatorFile =
                loader.getValueString("config.tvshows.words_separator.file",
                                      config.tvShowsWordsSeparatorFile);

        config.inputFolders = loader.getValueListStrings("input.folders").toArray(new String[0]);
        config.outputFolders = loader.getValueListMap("output.folders").toArray(new Map[0]);

        return config;
    }
}
