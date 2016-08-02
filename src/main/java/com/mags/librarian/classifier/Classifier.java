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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        Classification classification = checkForTVshow(sourceFileName);

        if (!classification.tvshowName.isEmpty()) {
            // it is a TV show
            return classification;
        }

        // other kind of files
        return checkMeetsCriteria(sourceFileName);
    }

    /**
     * Check if a file is a TV show.
     *
     * @param sourceFileName
     * @return True if it is a TV show.
     */
    private Classification checkForTVshow(String sourceFileName) {

        Classification classification = new Classification();

        // check first for tvshows if present
        for (Criterium criterium : criteria) {
            if (criterium.name.equals("tvshows")) {

                Pattern regExp = Pattern.compile(criterium.regExp, Pattern.CASE_INSENSITIVE);
                Matcher matcher = regExp.matcher(sourceFileName);

                if (matcher.find()) {
                    classification.name = criterium.name;

                    try {
                        // replace word separators with dots in captured TVshow name
                        String tvShowName = matcher.group("name").
                                replace("_", " ").
                                replace(".", " ").
                                trim().
                                replace(" ", ".");
                        classification.tvshowName = tvShowName;
                    } catch (IllegalArgumentException e) {
                    }

                    try {
                        classification.season = Integer.parseInt(matcher.group("season"));
                    } catch (IllegalArgumentException e) {
                    }

                    try {
                        classification.episode = Integer.parseInt(matcher.group("episode"));
                    } catch (IllegalArgumentException e) {
                    }

                    try {
                        classification.tvshowRest = matcher.group("rest");
                    } catch (IllegalArgumentException e) {
                    }

                    return classification;
                }
            }
        }

        return classification;
    }

    /**
     * Checks is the file meets the criteria.
     *
     * @param sourceFileName
     * @return The classification with the name of the criterium met.
     */
    private Classification checkMeetsCriteria(String sourceFileName) {

        Classification classification = new Classification();

        for (Criterium criterium : criteria) {
            Pattern regExp = Pattern.compile(criterium.regExp);
            Matcher matcher = regExp.matcher(sourceFileName);
            if (matcher.find()) {
                classification.name = criterium.name;
                break;
            }
        }

        return classification;
    }

}
