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

        String filenameNoExtension = getFilenameWithoutExtension(fileName);

        Pattern regExp = Pattern.compile(criterium.regExp, Pattern.CASE_INSENSITIVE);
        Matcher matcher = regExp.matcher(filenameNoExtension);

        Classification classification = new Classification();

        if (matcher.find()) {

            classification.name = criterium.name;
            classification.fileName = fileName;
            classification.baseName = getFilenameWithoutExtension(fileName);
            classification.extension = getFileExtension(fileName);

            try {
                String tvShowName = matcher.group("name").trim();

                // replace word separators with spaces in captured TVshow name
                tvShowName = matcher.group("name")
                        .replace("_", " ")
                        .replace("-", " ")
                        .replace(".", " ")
                        .trim();
                classification.tvShowName = tvShowName;
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
                // replace word separators with spaces in captured TVshow rest
                classification.tvShowRest = matcher.group("rest")
                        .replace("_", " ")
                        .replace("-", " ")
                        .replace(".", " ")
                        .trim();
            } catch (IllegalArgumentException e) {
            }

            return classification;
        }

        return classification;
    }

    public static String getFileExtension(String fileName) {

        String extension = "";

        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i + 1);
        }

        return extension;
    }

    public static String getFilenameWithoutExtension(String fileName) {

        String extension = getFileExtension(fileName);

        if (extension.isEmpty()) {
            return fileName;
        }

        return fileName.substring(0, fileName.length() - extension.length() - 1);
    }
}
