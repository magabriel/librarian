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
import com.mags.librarian.classifier.FileMatcher;
import com.mags.librarian.config.Config;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Mover {

    private final Config config;
    private final Log logger;
    private final Options options;
    private boolean isDuplicated = false;

    // for testing purposes
    private String actionPerformed = "";
    private Summary summary;

    Mover(Options options, Config config, Log logger) {

        this.options = options;
        this.config = config;
        this.logger = logger;
    }

    /**
     * Move a file to its destination folder depending on its classification.
     *
     * @param inputFile          The input file
     * @param fileClassification The file classification
     */
    void moveToDestination(File inputFile, Classification fileClassification) {

        actionPerformed = "";
        summary = new Summary();

        // find all suitable destinations for this file
        ArrayList<Map> suitableDestinations = findSuitableDestinations(fileClassification);

        if (fileClassification.name.equals("tvshows")) {
            // if it is a tvshow, move it (special treatment)
            moveTvShowToDestination(inputFile, fileClassification, suitableDestinations);

        } else if (fileClassification.name.equals("music")) {
            // if it is a music file, move it (special treatment)
            moveMusicToDestination(inputFile, fileClassification, suitableDestinations);

        } else {
            // move other file
            moveRegularFileToDestination(inputFile, suitableDestinations);
        }

        if (actionPerformed.isEmpty()) {
            if (isDuplicated) {
                processDuplicateFile(inputFile);
                return;
            }
            processErroredFile(inputFile);
        }
    }

    /**
     * Process an unknown file.
     *
     * @param inputFile The file to process.
     */
    public void processUnknownFile(File inputFile) {

        switch (config.unknownFilesAction) {
            case IGNORE:
                logger.getLogger().warning(String.format("- File '%s' ignored (left in place).", inputFile.getName()));
                break;

            case DELETE:
                deleteFile(inputFile);
                break;

            case MOVE:
                if (config.unknownFilesMovePath.isEmpty()) {
                    logger.getLogger().warning(
                            String.format("- Unknown files move path is empty, cannot move file '%s'.",
                                          inputFile.getName()));
                    return;
                }

                moveFile(inputFile, new File(config.unknownFilesMovePath), true);
                break;
        }

    }

    /**
     * Process a duplicate file.
     *
     * @param inputFile The file.
     */
    private void processDuplicateFile(File inputFile) {

        switch (config.duplicateFilesAction) {
            case IGNORE:
                logger.getLogger().warning(String.format("- File '%s' ignored (left in place).", inputFile.getName()));
                break;

            case DELETE:
                deleteFile(inputFile);
                break;

            case MOVE:
                if (config.duplicateFilesMovePath.isEmpty()) {
                    logger.getLogger().warning(
                            String.format("- Duplicate files move path is empty, cannot move file '%s'.",
                                          inputFile.getName()));
                    return;
                }

                moveFile(inputFile, new File(config.duplicateFilesMovePath), true);
                break;
        }

    }

    /**
     * Process an errored file.
     *
     * @param inputFile The file.
     */
    private void processErroredFile(File inputFile) {

        switch (config.errorFilesAction) {
            case IGNORE:
                logger.getLogger().warning(String.format("- File '%s' ignored (left in place).", inputFile.getName()));
                break;

            case DELETE:
                deleteFile(inputFile);
                break;

            case MOVE:
                if (config.errorFilesMovePath.isEmpty()) {
                    logger.getLogger().warning(String.format("- Error files move path is empty, cannot move file '%s'.",
                                                             inputFile.getName()));
                    return;
                }

                moveFile(inputFile, new File(config.errorFilesMovePath), true);
                break;
        }

    }

    /**
     * Delete a file.
     *
     * @param inputFile The file to delete.
     */
    private void deleteFile(File inputFile) {

        try {
            if (!options.dryRun) {
                Files.delete(inputFile.toPath());
            }
            logger.getLogger().fine(String.format("- File '%s' deleted.", inputFile.getName()));
        } catch (IOException e) {
            logger.getLogger().warning(String.format("- Error deleting file: %s", e));
        }

    }

    /**
     * Move a file to a destination folder.
     *
     * @param inputFile         The file to move
     * @param destinationFolder The folder where to move it
     */
    private void moveFile(File inputFile, File destinationFolder, boolean autoRenameIfExisting) {

        if (!destinationFolder.exists()) {
            if (!options.dryRun) {
                destinationFolder.mkdirs();
            }
            logger.getLogger().fine(String.format("- Created folder '%s'.", destinationFolder.getAbsolutePath()));
        }

        try {
            Path destination = destinationFolder.toPath().resolve(inputFile.getName());
            if (Files.exists(destination)) {
                String time = String.valueOf(new Date().getTime());
                destination = destinationFolder.toPath().resolve(inputFile.getName() + '-' + time);
            }

            if (!options.dryRun) {
                Files.move(inputFile.toPath(), destination);
            }
            logger.getLogger().fine(String.format("- File '%s' moved to '%s'.",
                                                  inputFile.getName(),
                                                  destinationFolder));
        } catch (IOException e) {
            logger.getLogger().severe(String.format("- Error moving file: %s", e));
        }

    }

    private String replaceWordsSeparatorsInFileNameFragment(String fileName) {

        String existingSeparator = " ";
        if (fileName.matches(" ")) {
            existingSeparator = " ";
        } else if (fileName.matches("_")) {
            existingSeparator = "_";
        } else if (fileName.matches("\\.")) {
            existingSeparator = ".";
        }

        String newName = fileName.replace(existingSeparator, config.tvShowsWordsSeparatorFile);
        return newName;
    }

    private String replaceWordsSeparatorsInFileName(String fileNameWithoutExtension) {

        return fileNameWithoutExtension.
                replace(" ", config.tvShowsWordsSeparatorFile).
                replace("_", config.tvShowsWordsSeparatorFile).
                replace(".", config.tvShowsWordsSeparatorFile);
    }

    private String replaceWordsSeparatorsInTvShowName(String fileNameWithoutExtension) {

        return fileNameWithoutExtension.
                replace(" ", config.tvShowsWordsSeparatorShow).
                replace("_", config.tvShowsWordsSeparatorShow).
                replace(".", config.tvShowsWordsSeparatorShow);
    }

    /**
     * Find all the possible destinations for a file.
     *
     * @param fileClassification The file classification
     * @return A list of destinations
     */
    private ArrayList<Map> findSuitableDestinations(Classification fileClassification) {

        ArrayList<Map> destinations = new ArrayList<>();

        for (Map outputFolder : config.outputFolders) {
            if (outputFolder.get("contents").equals(fileClassification.name)) {
                logger.getLogger().fine(
                        String.format("- Suitable destination folder found: '%s'.", outputFolder.get("path")));

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
    private void moveRegularFileToDestination(
            File inputFile, ArrayList<Map> suitableDestinations) {

        if (suitableDestinations.isEmpty()) {
            logger.getLogger().warning("- No suitable destination found, skipping.");
            return;
        }

        // use the first suitable destination
        File destinationFolder = new File(suitableDestinations.get(0).get("path").toString());

        moveTheFile(inputFile, destinationFolder);
    }


    /**
     * Moves a music file to one of the suitable destinations.
     *
     * @param inputFile            The input file
     * @param fileClassification   The file classification
     * @param suitableDestinations List of suitable destinations
     */
    private void moveMusicToDestination(
            File inputFile,
            Classification fileClassification,
            ArrayList<Map> suitableDestinations) {

        if (fileClassification.albumName.isEmpty()) {
            // no album, it is just a regular move
            moveRegularFileToDestination(inputFile, suitableDestinations);
            return;
        }

        // use the first suitable destination, adding the album as subfolder
        File albumFolder = Paths.get(suitableDestinations.get(0).get("path").toString(),
                                     fileClassification.albumName).toFile();

        moveTheFile(inputFile, albumFolder);
    }

    /**
     * Moves a TV show file to one of the suitable destinations.
     *
     * @param inputFile            The input file
     * @param fileClassification   The file classification
     * @param suitableDestinations List of suitable destinations
     */
    private void moveTvShowToDestination(
            File inputFile,
            Classification fileClassification,
            ArrayList<Map> suitableDestinations) {

        // find the parent destination folder for that TV show
        File parentDestinationFolder = findParentDestinationFolder(inputFile, fileClassification,
                                                                   suitableDestinations);

        // if no suitable destination found, use the last one as autocreate
        if (parentDestinationFolder == null) {
            String path = suitableDestinations.get(suitableDestinations.size() - 1).get("path").toString();
            parentDestinationFolder = new File(path);

            logger.getLogger().fine(
                    String.format("- No suitable destination folder found, using '%s' as default.", path));
        }

        // apply season and numbering schemas
        String seasonName = applySeasonSchema(fileClassification);
        String tvShowFileName = applyTvShowNumberingSchema(fileClassification);

        // replace separators in TV show name
        fileClassification.tvShowName = replaceWordsSeparatorsInTvShowName(fileClassification.tvShowName);

        // ensure we have a valid folder name for the tv show, wheather preexisting or new
        if (fileClassification.tvShowFolderName.isEmpty()) {
            fileClassification.tvShowFolderName = fileClassification.tvShowName;
        }

        // the real destination folder is a subfolder of the parent found
        File tvShowDestinationFolder = Paths.get(
                parentDestinationFolder.getAbsolutePath(),
                fileClassification.tvShowFolderName,
                seasonName).toFile();

        if (!tvShowDestinationFolder.exists()) {
            if (!options.dryRun) {
                tvShowDestinationFolder.mkdirs();
            }
            logger.getLogger().fine(String.format("- Created folder for TV show/season: '%s'.",
                                                  tvShowDestinationFolder.getAbsolutePath()));
        } else {
            logger.getLogger().fine(String.format("- Using existing folder for TV show/season: '%s'.",
                                                  tvShowDestinationFolder.getAbsolutePath()));
        }

        // move the file
        moveTheFile(inputFile, tvShowDestinationFolder, tvShowFileName);
    }

    /**
     * Find the parent folder of an existing TV show folder.
     *
     * @param inputFile            The input file
     * @param fileClassification   The file classification
     * @param suitableDestinations List of suitable destinations
     * @return The destination folder
     */
    private File findParentDestinationFolder(
            File inputFile,
            Classification fileClassification,
            ArrayList<Map> suitableDestinations) {

        logger.getLogger().fine(String.format("- Find suitable parent destination folder for file \"%s\"", inputFile
                .getName()));

        // NOTE: using a final array[1] because of lambda usage limitations
        final File[] parentDestinationFolder = new File[1];

        suitableDestinations.forEach(destination -> {
            File destinationFolder = new File(destination.get("path").toString());

            logger.getLogger().fine(String.format("- Checking candidate parent destination folder \"%s\"",
                                                  destinationFolder));

            // try finding an existing subfolder for the TV show
            String tvShowSubfolders[] = destinationFolder.list((dir, name) -> {

                // for tvshows output folders, only accept it as a destination if
                // a folder for the TV show already exists
                if (FileMatcher.matchTVShowName(name, fileClassification.tvShowName)) {

                    // save real folder name to avoid creating extra folders on case or separators change
                    fileClassification.tvShowFolderName = name;

                    logger.getLogger().finer(String.format("- Matched folder name \"%s\"", name));
                    return true;
                }

                return false;
            });

            if (tvShowSubfolders != null && tvShowSubfolders.length > 0) {
                // get the first one as new destination
                parentDestinationFolder[0] = destinationFolder;
            }
        });

        if (parentDestinationFolder[0] != null) {
            logger.getLogger().fine(
                    String.format("- Using parent destination folder \"%s\"", parentDestinationFolder[0]));
            return parentDestinationFolder[0];
        }

        logger.getLogger().fine("- Parent destination folder not found");
        return null;
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

        List<String> newName = new ArrayList<>();
        newName.add(replaceWordsSeparatorsInFileNameFragment(classification.tvShowName));
        newName.add(config.tvShowsWordsSeparatorFile);
        newName.add(seasonAndEpisode);

        if (!classification.tvShowRest.isEmpty()) {
            newName.add(config.tvShowsWordsSeparatorFile);
            newName.add(replaceWordsSeparatorsInFileNameFragment(classification.tvShowRest));
        }

        String baseName = String.join("", newName);

        return replaceWordsSeparatorsInFileName(baseName) + "." + classification.extension;
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
     * @param inputFile         The input file
     * @param destinationFolder The destination folder
     * @param newName           The name to give to the copied/moved file
     */
    private void moveTheFile(File inputFile, File destinationFolder, String newName) {

        try {
            if (!destinationFolder.exists()) {
                if (!options.dryRun) {
                    destinationFolder.mkdirs();
                    logger.getLogger().fine(String.format("- Created destination folder '%s'.",
                                                          destinationFolder.getAbsolutePath()));
                }
            }

            if (newName.isEmpty()) {
                newName = inputFile.getName();
            }

            // create summary
            summary.inputFolder = inputFile.getParent();
            summary.inputFilename = inputFile.getName();
            summary.outputFolder = destinationFolder.toString();
            summary.outputFilename = newName;

            if (options.copyOnly) {
                if (!options.dryRun) {
                    Files.copy(inputFile.toPath(), destinationFolder.toPath().resolve(newName));
                }
                actionPerformed = String.format("copied [%s] to [%s] as [%s]", inputFile.getAbsolutePath(),
                                                destinationFolder.getAbsolutePath(), newName);
                logger.getLogger().info(String.format("- File '%s' copied to '%s' as '%s'.", inputFile.getName(),
                                                      destinationFolder.getAbsolutePath(), newName));
                summary.action = "copy";

            } else {
                if (!options.dryRun) {
                    Files.move(inputFile.toPath(), destinationFolder.toPath().resolve(newName));
                }
                actionPerformed = String.format("moved [%s] to [%s] as [%s]", inputFile.getAbsolutePath(),
                                                destinationFolder.getAbsolutePath(), newName);
                logger.getLogger().info(String.format("- File '%s' moved to '%s' as '%s'.", inputFile.getName(),
                                                      destinationFolder.getAbsolutePath(), newName));
                summary.action = "move";
            }

        } catch (FileAlreadyExistsException e) {

            isDuplicated = true;

            String msg = "- Cannot move already existing file '%s' to '%s': %s";
            if (options.copyOnly) {
                msg = "- Cannot copy already existing file '%s' to '%s': %s";
            }
            logger.getLogger().severe(String.format(msg,
                                                    inputFile.getName(),
                                                    destinationFolder.getAbsolutePath(),
                                                    e.toString()));
        } catch (IOException e) {

            String msg = "- Cannot move file '%s' to '%s': %s";
            if (options.copyOnly) {
                msg = "- Cannot copy file '%s' to '%s': %s";
            }
            logger.getLogger().severe(String.format(msg,
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
