/*
 * This file is part of the librarian application.
 *
 * Copyright (c) Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package com.mags.librarian;

import com.mags.librarian.classifier.Classification;
import com.mags.librarian.config.Config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Mover {

    private final Config config;
    private final Options options;

    // for testing purposes
    private String actionPerformed = "";
    private Summary summary;

    Mover(Options options, Config config) {

        this.options = options;
        this.config = config;
    }

    /**
     * Move a file to its destination folder.
     *
     * @param inputFile The input file
     * @param fileClassification The file classification
     */
    void moveToDestination(File inputFile, Classification fileClassification) {

        actionPerformed = "";
        summary = new Summary();

        // find all suitable destinations for this file
        ArrayList<Map> suitableDestinations = findDestinations(fileClassification);

        // if it is a tvshow, move it (special treatment)
        if (fileClassification.name.equals("tvshows")) {
            moveTvShowToDestination(inputFile, fileClassification, suitableDestinations);
            return;
        }

        // move other file
        moveFileToDestination(inputFile, suitableDestinations);
    }

    private String replaceWordsSeparatorsInFileName(String fileName) {

        String fileNameWithoutExtension = getFilenameWithoutExtension(fileName);

        String newName = replaceWordsSeparators(fileNameWithoutExtension);

        return newName + "." + getFileExtension(fileName);
    }

    private String replaceWordsSeparators(String fileNameWithoutExtension) {

        return fileNameWithoutExtension.
                replace(" ", config.wordsSeparator).
                replace("_", config.wordsSeparator).
                replace(".", config.wordsSeparator);
    }

    private String getFileExtension(String fileName) {

        String extension = "";

        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i + 1);
        }

        return extension;
    }

    private String getFilenameWithoutExtension(String fileName) {

        String extension = getFileExtension(fileName);

        if (extension.isEmpty()) {
            return fileName;
        }

        return fileName.substring(0, fileName.length() - extension.length() - 1);

    }

    /**
     * Find all the possible destinations for a file.
     *
     * @param fileClassification The file classification
     * @return A list of destinations
     */
    private ArrayList<Map> findDestinations(Classification fileClassification) {

        ArrayList<Map> destinations = new ArrayList<>();

        for (Map outputFolder : config.outputFolders) {
            if (outputFolder.get("contents").equals(fileClassification.name)) {
                Log.getLogger().fine(String.format("- Output folder found: '%s'.", outputFolder.get("path")));

                destinations.add(outputFolder);
            }
        }

        return destinations;
    }

    /**
     * Move a file to the first of the suitable destinations.
     *
     * @param inputFile
     * @param suitableDestinations
     */
    private void moveFileToDestination(
            File inputFile, ArrayList<Map> suitableDestinations) {

        if (suitableDestinations.isEmpty()) {
            Log.getLogger().warning("- No suitable destination found, skipping.");
            return;
        }

        // use the first suitable destination
        File destinationFolder = new File(suitableDestinations.get(0).get("path").toString());

        moveTheFile(inputFile, destinationFolder);
    }

    /**
     * Moves a TV show file to one of the suitable destinations.
     *
     * @param inputFile The input file
     * @param fileClassification The file classification
     * @param suitableDestinations List of suitable destinations
     */
    private void moveTvShowToDestination(
            File inputFile,
            Classification fileClassification,
            ArrayList<Map> suitableDestinations) {

        // NOTE: using a final array[1] because of lambda usage limitations
        final File[] parentDestinationFolder = new File[1];

        suitableDestinations.forEach(destination -> {
            File destinationFolder = new File(destination.get("path").toString());

            // try finding an existing subfolder for the TV show
            String tvShowSubfolders[] = destinationFolder.list((dir, name) -> {

                // for tvshows output folders, only accept it as a destination if
                // a folder for the TV show already exists, or "autocreate" option
                // is set
                if (inputFile.getName().matches(fileClassification.tvshowName)) {

                    return true;
                } else if (destination.containsKey("autocreate") && (Boolean) destination.get("autocreate")) {
                    return true;
                }

                return false;
            });

            if (tvShowSubfolders != null && tvShowSubfolders.length > 0) {
                // get the first one as new destination
                parentDestinationFolder[0] = destinationFolder;
            }
        });

        // if no suitable destination found, use the last one as autocreate
        if (parentDestinationFolder[0] == null) {
            String path = suitableDestinations.get(suitableDestinations.size() - 1).get("path").toString();
            parentDestinationFolder[0] = new File(path);
        }

        // apply season and numbering schemas
        String seasonName = applySeasonSchema(fileClassification);
        String tvShowFileName = applyTvShowNumberingSchema(fileClassification);

        // replace separators in TV show name and season
        fileClassification.tvshowName = replaceWordsSeparators(fileClassification.tvshowName);

        // the real destination folder is a subfolder of the parent found
        File tvShowDestinationFolder = Paths.get(
                parentDestinationFolder[0].getAbsolutePath(),
                fileClassification.tvshowName,
                seasonName).toFile();


        if (!tvShowDestinationFolder.exists()) {
            tvShowDestinationFolder.mkdir();
            Log.getLogger().fine(String.format("- Created folder for TV show: '%s'.",
                                               tvShowDestinationFolder.getAbsolutePath()));
        } else {
            Log.getLogger().fine(String.format("- Using existing folder for TV show: '%s'.",
                                               tvShowDestinationFolder.getAbsolutePath()));
        }

        // move the file
        moveTheFile(inputFile, tvShowDestinationFolder, tvShowFileName);
    }

    private String applySeasonSchema(Classification classification) {

        // default season name
        String seasonName = String.format(config.tvShowsSeasonSchema, classification.season);

        seasonName = replaceTag(seasonName, "season", classification.season);

        return seasonName;
    }

    private String applyTvShowNumberingSchema(Classification classification) {

        // default
        String seasonAndEpisode = config.tvShowsNumberingSchema;

        seasonAndEpisode = replaceTag(seasonAndEpisode, "season", classification.season);
        seasonAndEpisode = replaceTag(seasonAndEpisode, "episode", classification.episode);

        return String.format("%s%s%s%s",
                             classification.tvshowName,
                             config.wordsSeparator,
                             seasonAndEpisode,
                             classification.tvshowRest);
    }

    /**
     * Replaces a tag ("{tagname:length}") by a numeric value.
     *
     * @param inputString
     * @param tag
     * @param value
     * @return
     */
    private String replaceTag(String inputString, String tag, Integer value) {

        String replacedString = inputString;

        Pattern regExp = Pattern.compile(String.format("(?<pre>)(?<tag>\\{%s(?::(?<number>[0-9]+))\\})(?<post>)", tag));
        Matcher matcher = regExp.matcher(inputString);
        if (matcher.find()) {
            Integer number = Integer.parseInt(matcher.group("number"));

            String formatted = String.format("%0" + number + "d", value);
            replacedString = matcher.replaceAll("${pre}" + formatted + "${post}");

        }

        return replacedString;
    }

    /**
     * Do the actual file move.
     *
     * @param inputFile
     * @param destinationFolder
     */
    private void moveTheFile(File inputFile, File destinationFolder) {

        moveTheFile(inputFile, destinationFolder, "");
    }

    /**
     * Do the actual file move.
     *
     * @param inputFile
     * @param destinationFolder
     * @param newName
     */
    private void moveTheFile(File inputFile, File destinationFolder, String newName) {

        try {
            if (!options.getDryRun()) {
                destinationFolder.mkdirs();
            }

            if (newName.isEmpty()) {
                newName = inputFile.getName();
            }

            // replace words separator in name
            newName = replaceWordsSeparatorsInFileName(newName);

            // create summary
            summary.inputFolder = inputFile.getParent();
            summary.inputFilename = inputFile.getName();
            summary.outputFolder = destinationFolder.toString();
            summary.outputFilename = newName;

            if (options.getCopyOnly()) {
                if (!options.getDryRun()) {
                    Files.copy(inputFile.toPath(), destinationFolder.toPath().resolve(newName));
                }
                actionPerformed = String.format("copied [%s] to [%s] as [%s]", inputFile.getAbsolutePath(),
                                                destinationFolder.getAbsolutePath(), newName);
                Log.getLogger().info(String.format("- File '%s' copied to '%s' as '%s'.", inputFile.getName(),
                                                   destinationFolder.getAbsolutePath(), newName));
                summary.action = "copy";

            } else {
                if (!options.getDryRun()) {
                    Files.move(inputFile.toPath(), destinationFolder.toPath().resolve(newName));
                }
                actionPerformed = String.format("moved [%s] to [%s] as [%s]", inputFile.getAbsolutePath(),
                                                destinationFolder.getAbsolutePath(), newName);
                Log.getLogger().info(String.format("- File '%s' moved to '%s' as '%s'.", inputFile.getName(),
                                                   destinationFolder.getAbsolutePath(), newName));
                summary.action = "move";
            }

        } catch (IOException e) {
            String msg = "- Cannot move file '%s' to '%s': %s";
            if (options.getCopyOnly()) {
                msg = "- Cannot move file '%s' to '%s': %s";
            }

            Log.getLogger().severe(String.format(msg,
                                                 inputFile.getName(),
                                                 destinationFolder.getAbsolutePath(),
                                                 e.toString()));
        }
    }

    String getActionPerformed() {

        return actionPerformed;
    }

    Summary getSummary() {

        return summary;
    }

    class Summary {

        String inputFolder;
        String inputFilename;
        String outputFolder;
        String outputFilename;
        String action;
    }
}
