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

        // extensions
        List<Map> extensions = new ArrayList<>();
        LinkedHashMap<String, String> extensionsItems = new LinkedHashMap<>();
        extensionsItems.put("extensions.key.1", "extensions.value.1");
        extensionsItems.put("extensions.key.2", "extensions.value.2");
        extensions.add(extensionsItems);
        config.extensions = extensions.toArray(new Map[0]);

        // filters
        List<Map> filters = new ArrayList<>();
        LinkedHashMap<String, String> filterItems= new LinkedHashMap<>();
        filterItems.put("filters.key.1", "filters.value.1");
        filterItems.put("filters.key.2", "filters.value.2");
        filters.add(filterItems);
        config.filters = filters.toArray(new Map[0]);

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

        // executeSuccess
        config.executeSuccess = "executeSuccess.base";

        // executeError not provided

        return config;
    }

    private Config getOtherConfig() {

        Config config = new Config();

        // extensions
        List<Map> extensions = new ArrayList<>();
        LinkedHashMap<String, String> extensionsItems = new LinkedHashMap<>();
        extensionsItems.put("extensions.key.1", "extensions.value.1b");
        extensionsItems.put("extensions.key.3", "extensions.value.3");
        extensions.add(extensionsItems);
        config.extensions = extensions.toArray(new Map[0]);

        // filters not provided

        // inputFolders not provided

        // outputFolders
        List<Map> outputFolders = new ArrayList<>();
        LinkedHashMap<String, String> outputFoldersItems = new LinkedHashMap<>();
        outputFoldersItems.put("outputFolders.key.1", "outputFolders.value.1b");
        outputFoldersItems.put("outputFolders.key.3", "outputFolders.value.3");
        outputFolders.add(outputFoldersItems);
        config.outputFolders = outputFolders.toArray(new Map[0]);

        // tvShowsNumberingSchema not provided

        // tvShowsSeasonSchema not provided

        // tvShowsWordsSeparatorShow
        config.tvShowsWordsSeparatorShow = "tvShowsWordsSeparatorShow.other";

        // executeSuccess not provided

        // executeError
        config.executeError = "executeError.other";

        return config;
    }

    private Config getMergedConfig() {

        Config config = new Config();

        // extensions
        List<Map> extensions = new ArrayList<>();
        LinkedHashMap<String, String> extensionsItems = new LinkedHashMap<>();
        extensionsItems.put("extensions.key.1", "extensions.value.1b");
        extensionsItems.put("extensions.key.3", "extensions.value.3");
        extensions.add(extensionsItems);
        config.extensions = extensions.toArray(new Map[0]);

        // filters
        List<Map> filters = new ArrayList<>();
        LinkedHashMap<String, String> filterItems= new LinkedHashMap<>();
        filterItems.put("filters.key.1", "filters.value.1");
        filterItems.put("filters.key.2", "filters.value.2");
        filters.add(filterItems);
        config.filters = filters.toArray(new Map[0]);

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
        outputFolders.add(outputFoldersItems);
        config.outputFolders = outputFolders.toArray(new Map[0]);

        // tvShowsNumberingSchema
        config.tvShowsNumberingSchema = "tvShowsNumberingSchema.base";

        // tvShowsSeasonSchema
        config.tvShowsSeasonSchema = config.DEFAULT_TVSHOWS_SEASON_SCHEMA;

        // tvShowsWordsSeparatorShow
        config.tvShowsWordsSeparatorShow = "tvShowsWordsSeparatorShow.other";

        // executeSuccess
        config.executeSuccess = "executeSuccess.base";

        // executeError
        config.executeError = "executeError.other";

        return config;
    }
}