/*
 * This file is part of the librarian application.
 *
 * Copyright (c) Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package com.mags.librarian.classifier;

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
     * @param sourceFileName
     * @return The classification
     */
    public Classification classify(String sourceFileName) {

        Classification classification = new Classification();

        // check first for tvshows if present
        for (Criterium criterium : criteria) {
            if (criterium.name.equals("tvshows")) {
                // try to match against this tv show criterium
                classification = FileMatcher.matchTVShow(sourceFileName, criterium);
                if (!classification.tvShowName.isEmpty()) {
                    // it is a TV show
                    return classification;
                }
            }
        }

        // other kind of files
        for (Criterium criterium : criteria) {
            if (FileMatcher.matchRegExp(sourceFileName, criterium.regExp)) {
                classification.name = criterium.name;
                return classification;
            }
        }

        return classification;
    }

}
