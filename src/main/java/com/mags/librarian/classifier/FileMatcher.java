/*
 * This file is part of the librarian application.
 *
 * Copyright (c) 2016 Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code
 */

package com.mags.librarian.classifier;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileMatcher {

    /**
     * Try to match a TV show name against a filename.
     *
     * @param fileName   The filename
     * @param tvshowName The TV show name
     * @return True if match
     */
    public static boolean matchTVShowName(String fileName, String tvshowName) {

        // transform word separators to match any of them
        tvshowName = tvshowName
                .replace("_", " ")
                .replace(".", " ")
                .trim()
                .replace(" ", "[ _\\.]");

        Pattern regExp = Pattern.compile(tvshowName, Pattern.CASE_INSENSITIVE);
        Matcher matcher = regExp.matcher(fileName);

        return matcher.find();
    }

    /**
     * Try to match a TV show name against a filename.
     *
     * @param fileName          The filename
     * @param regularExpression The regExp to match against
     * @return True if match
     */
    public static boolean matchRegExp(String fileName, String regularExpression) {

        Pattern regExp = Pattern.compile(regularExpression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = regExp.matcher(fileName);

        return matcher.find();
    }

    /**
     * Try to match a filename against a criterium defining a TV show.
     *
     * @param fileName  The filename
     * @param criterium The criteriumm of a TV show
     * @return The corresponding Classification object if match, or empty if not
     */
    public static Classification matchTVShow(String fileName, Criterium criterium) {

        Pattern regExp = Pattern.compile(criterium.regExp, Pattern.CASE_INSENSITIVE);
        Matcher matcher = regExp.matcher(fileName);

        Classification classification = new Classification();

        if (matcher.find()) {

            classification.name = criterium.name;

            try {
                // replace word separators with dots in captured TVshow name
                String tvShowName = matcher.group("name")
                        .replace("_", " ")
                        .replace(".", " ")
                        .trim()
                        .replace(" ", ".");
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

        return classification;
    }
}
