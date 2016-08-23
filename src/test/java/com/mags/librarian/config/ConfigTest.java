/*
 * This file is part of the librarian application.
 *
 * Copyright (c) 2016 Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code
 */

package com.mags.librarian.config;

import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ConfigTest {

    @Test
    public void merge() throws Exception {

        Config base = getBaseConfig();
        Config other = getOtherConfig();

        Config actual = base.merge(other);

        Config expected = getMergedConfig();

        assertEquals(expected.toString(), actual.toString());
    }

    private Config getBaseConfig() {

        Config config = new Config();

        // contentTypes
        List<Map> contentTypes = new ArrayList<>();
        LinkedHashMap<String, String> contenTypesItems = new LinkedHashMap<>();
        contenTypesItems.put("contenTypes.key.1", "contenTypes.value.1");
        contenTypesItems.put("contenTypes.key.2", "contenTypes.value.2");
        contentTypes.add(contenTypesItems);
        config.contentTypes = contentTypes.toArray(new Map[0]);

        // inputFolders
        config.inputFolders = new String[]{
                "inputFolders.1",
                "inputfolders.2"
        };

        // outputFolders not provided

        // tvShowsNumberingSchema
        config.tvShowsNumberingSchema = "tvShowsNumberingSchema.base";

        // tvShowsSeasonSchema not provided

        // tvShowsWordsSeparatorShow
        config.tvShowsWordsSeparatorShow = "tvShowsWordsSeparatorShow.base";

        return config;
    }

    private Config getOtherConfig() {

        Config config = new Config();

        // contentTypes
        List<Map> contentTypes = new ArrayList<>();
        LinkedHashMap<String, String> contenTypesItems = new LinkedHashMap<>();
        contenTypesItems.put("contenTypes.key.1", "contenTypes.value.1b");
        contenTypesItems.put("contenTypes.key.3", "contenTypes.value.3");
        contentTypes.add(contenTypesItems);
        config.contentTypes = contentTypes.toArray(new Map[0]);

        // inputFolders not provided

        // outputFolders
        List<Map> outputFolders = new ArrayList<>();
        LinkedHashMap<String, String> outputFoldersItems = new LinkedHashMap<>();
        outputFoldersItems.put("outputFolders.key.1", "outputFolders.value.1b");
        outputFoldersItems.put("outputFolders.key.3", "outputFolders.value.3");
        contentTypes.add(outputFoldersItems);
        config.outputFolders = outputFolders.toArray(new Map[0]);

        // tvShowsNumberingSchema not provided

        // tvShowsSeasonSchema not provided

        // tvShowsWordsSeparatorShow
        config.tvShowsWordsSeparatorShow = "tvShowsWordsSeparatorShow.other";

        return config;
    }

    private Config getMergedConfig() {

        Config config = new Config();

        // contentTypes
        List<Map> contentTypes = new ArrayList<>();
        LinkedHashMap<String, String> contenTypesItems = new LinkedHashMap<>();
        contenTypesItems.put("contenTypes.key.1", "contenTypes.value.1b");
        contenTypesItems.put("contenTypes.key.3", "contenTypes.value.3");
        contentTypes.add(contenTypesItems);
        config.contentTypes = contentTypes.toArray(new Map[0]);

        // inputFolders
        config.inputFolders = new String[]{
                "inputFolders.1",
                "inputfolders.2"
        };

        // outputFolders
        List<Map> outputFolders = new ArrayList<>();
        LinkedHashMap<String, String> outputFoldersItems = new LinkedHashMap<>();
        outputFoldersItems.put("outputFolders.key.1", "outputFolders.value.1b");
        outputFoldersItems.put("outputFolders.key.3", "outputFolders.value.3");
        contentTypes.add(outputFoldersItems);
        config.outputFolders = outputFolders.toArray(new Map[0]);

        // tvShowsNumberingSchema
        config.tvShowsNumberingSchema = "tvShowsNumberingSchema.base";

        // tvShowsSeasonSchema
        config.tvShowsSeasonSchema = config.DEFAULT_TVSHOWS_SEASON_SCHEMA;

        // tvShowsWordsSeparatorShow
        config.tvShowsWordsSeparatorShow = "tvShowsWordsSeparatorShow.other";

        return config;
    }
}