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

/**
 * Classifies files based on criteria.
 */
public class Classifier {

    private ArrayList<Criterium> criteria = new ArrayList<>();

    public Classifier() {

    }

    public Classifier(ArrayList<Criterium> criteria) {

        this.criteria = criteria;
    }

    /**
     * Adds a new criterium to the criteria.
     *
     * @param name
     * @param regExp
     */
    public void addCriterium(String name, String regExp) {

        Criterium criterium = new Criterium();
        criterium.name = name;
        criterium.regExp = regExp;

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
            if (FileMatcher.matchRegExp(sourceFile.getName(), criterium.regExp)) {
                classification.name = criterium.name;

                if (classification.name.equals("music")) {
                    // if file is in a subfolder, assume it is an album
                    if (sourceFile.getParent() != null) {
                        if (!sourceFile.getParent().toString().equals(baseFolder.toString())) {
                            classification.albumName = sourceFile.getParentFile().getName();
                        }
                    }
                }

                classification.extension = FileMatcher.getFileExtension(sourceFile.getName());

                return classification;
            }
        }

        return classification;
    }

}
