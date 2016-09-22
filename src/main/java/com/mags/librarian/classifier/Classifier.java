/*
 * This file is part of the librarian application.
 *
 * Copyright (c) Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package com.mags.librarian.classifier;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Classifies files based on criteria.
 */
public class Classifier {

    private List<Criterium> criteria = new ArrayList<>();

    public Classifier() {

    }

    public Classifier(List<Criterium> criteria) {

        this.criteria = criteria;
    }

    /**
     * Adds a new criterium to the criteria.
     *
     * @param name
     * @param extensions
     * @param filters
     */
    public void addCriterium(String name, String[] extensions, String[] filters) {

        Criterium criterium = new Criterium();
        criterium.name = name;
        criterium.extensions = extensions;
        criterium.filters = filters;

        criteria.add(criterium);
    }

    /**
     * Performs the classification.
     *
     * @param sourceFile
     * @return The classification
     */
    public Classification classify(File sourceFile, File baseFolder) {

        Classification classification = new Classification();

        // check first for tvshows if present
        for (Criterium criterium : criteria) {
            if (criterium.name.equals("tvshows")) {
                // try to match against this tv show criterium
                classification = FileMatcher.matchTVShow(sourceFile.getName(), criterium);
                if (!classification.tvShowName.isEmpty()) {
                    // it is a TV show
                    return classification;
                }
            }
        }

        // other kind of files
        for (Criterium criterium : criteria) {
            if (!criterium.name.equals("tvshows")) {
                classification = FileMatcher.matchOtherFiles(sourceFile.getName(), criterium);

                // for music files check if in album folder
                if (classification.name.equals("music")) {
                    // if file is in a subfolder, assume it is an album
                    if (sourceFile.getParent() != null) {
                        if (!sourceFile.getParent().toString().equals(baseFolder.toString())) {
                            classification.albumName = sourceFile.getParentFile().getName();
                        }
                    }
                }

                if (!classification.name.isEmpty()) {
                    return classification;
                }
            }
        }

        return classification;
    }

}
