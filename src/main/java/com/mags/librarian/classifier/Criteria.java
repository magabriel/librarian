/*
 * This file is part of the librarian application.
 *
 * Copyright (c) 2016 Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code
 */

package com.mags.librarian.classifier;

import com.mags.librarian.config.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Criteria {

    private final Config config;
    private final List<Criterium> criteriumList = new ArrayList<>();

    public Criteria(Config config) {

        this.config = config;
        processContentClasses();
    }

    public List<Criterium> getCriteriumList() {

        return criteriumList;
    }

    /**
     * Create the list of criterium objects.
     *
     * @return Criteria (list of criterium objects)
     */
    private void processContentClasses() {

        for (Map<String, Map> contentClass : config.contentClasses) {

            Criterium criterium = new Criterium();
            criterium.name = contentClass.keySet().toArray()[0].toString();
            criterium.extensions = extractExtensions(contentClass, criterium.name).toArray(new String[0]);
            criterium.filters = extractFilters(contentClass, criterium.name).toArray(new String[0]);

            criteriumList.add(criterium);
        }
    }

    /**
     * Extract the extensions for the criterium.
     *
     * @param contentClass  The ContentClass being processed
     * @param criteriumName The name of the criterium being processed
     * @return A list of extensions
     */
    private List<String> extractExtensions(Map<String, Map> contentClass, String criteriumName) {

        List<String> extensions = new ArrayList<>();

        if (!contentClass.get(criteriumName).containsKey("extension")) {
            return extensions;
        }

        String extensionName = contentClass.get(criteriumName).get("extension").toString();

        for (Map<String, List<String>> extensionItems : config.extensions) {
            String currentExtensionName = extensionItems.keySet().toArray()[0].toString();
            if (extensionName.equals(currentExtensionName)) {
                // found
                return extensionItems.get(currentExtensionName);
            }
        }

        return extensions;
    }

    /**
     * Extract the filters for the criterium.
     *
     * @param contentClass  The ContentClass being processed
     * @param criteriumName The name of the criterium being processed
     * @return A list of filters
     */
    private List<String> extractFilters(Map<String, Map> contentClass, String criteriumName) {

        List<String> filters = new ArrayList<>();

        if (!contentClass.get(criteriumName).containsKey("filter")) {
            return filters;
        }

        String filterName = contentClass.get(criteriumName).get("filter").toString();

        for (Map<String, List<String>> filterItems : config.filters) {
            String currentFilterName = filterItems.keySet().toArray()[0].toString();
            if (filterName.equals(currentFilterName)) {
                // found
                return filterItems.get(currentFilterName);
            }
        }

        return filters;
    }
}
