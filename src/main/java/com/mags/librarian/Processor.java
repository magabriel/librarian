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
import com.mags.librarian.classifier.Classifier;
import com.mags.librarian.classifier.Criterium;
import com.mags.librarian.config.Config;
import com.mags.librarian.event.*;

import java.io.File;
import java.util.*;
import java.util.stream.Stream;

/**
 * Processes the config file to actually move the files.
 */
class Processor {

    private final Options options;
    private final Config config;
    private Log logger;
    private FeedWriter feedWriter;
    private EventDispatcher eventDispatcher;
    private Command command;

    Processor(Options options, Config config, Log logger, EventDispatcher eventDispatcher) {

        this.options = options;
        this.config = config;
        this.logger = logger;
        this.eventDispatcher = eventDispatcher;
    }

    /**
     * Runs the classification process.
     */
    void run() {

        logger.getLogger().fine("Started");

        logOptionsAndConfig();

        if (config.outputFolders.length == 0) {
            logger.getLogger().severe("No output folders set, cannot continue.");
            return;
        }

        feedWriter = new FeedWriter(options.rssFileName, logger);

        command = new Command(logger);

        addListeners();

        process();

        logger.getLogger().fine("Finished");
    }

    /**
     * Add all listeners for several events
     */
    private void addListeners() {

        eventDispatcher.addListener(Event.FILE_PROCESSED, eventData -> {

            FileProcessedEventData data = (FileProcessedEventData) eventData;

            List<String> arguments = Arrays.asList(
                    data.inputFolder,
                    data.inputFilename,
                    data.outputFolder,
                    data.outputFilename,
                    data.fileClassification.name,
                    data.action
            );

            if (!config.executeSuccess.isEmpty()) {
                command.execute(config.executeSuccess, arguments);
            }

            addMovedToFeed(data.fileClassification, data.actionPerformed);
        });

        eventDispatcher.addListener(Event.FILE_UNKNOWN, eventData -> {

            FileUnknownEventData data = (FileUnknownEventData) eventData;

            List<String> arguments = Arrays.asList(
                    data.inputFolder,
                    data.inputFilename,
                    data.outputFolder,
                    data.outputFilename,
                    data.action
            );

            if (!config.executeError.isEmpty()) {
                command.execute(config.executeError, arguments);
            }
        });

        eventDispatcher.addListener(Event.FILE_ERROR, eventData -> {

            FileErrorEventData data = (FileErrorEventData) eventData;

            List<String> arguments = Arrays.asList(
                    data.inputFolder,
                    data.inputFilename,
                    data.outputFolder,
                    data.outputFilename,
                    data.action
            );

            if (!config.executeError.isEmpty()) {
                command.execute(config.executeError, arguments);
            }
        });
    }

    /**
     * Write options and configuration values to the logger.
     */
    private void logOptionsAndConfig() {

        if (options.dryRun) {
            logger.getLogger().config("- Dry run: true");
        }

        logger.getLogger().config(String.format("- Log level: %s", options.logLevel));
        logger.getLogger().config(String.format("- Verbosity: %s", options.verbosity));

        if (options.copyOnly) {
            logger.getLogger().config("- Copy only: true");
        }

        logger.getLogger().config("- Extensions: ");
        for (Map extension : config.extensions) {
            String name = extension.keySet().toArray()[0].toString();
            String values = extension.get(name).toString();
            logger.getLogger().config("    - " + name + " : \"" + values + "\"");
        }

        logger.getLogger().config("- Filters: ");
        for (Map filter : config.filters) {
            filter.forEach((name, filterItems) -> {
                logger.getLogger().config("    - " + name);
                for (String regExp : (List<String>) filterItems) {
                    logger.getLogger().config("        - " + regExp);
                }
            });
        }

        logger.getLogger().config("- Unknown files: ");
        logger.getLogger().config(String.format("    - Action: %s", config.unknownFilesAction));
        logger.getLogger().config(String.format("    - Move path: %s", config.unknownFilesMovePath));

        logger.getLogger().config("- Error files: ");
        logger.getLogger().config(String.format("    - Action: %s", config.errorFilesAction));
        logger.getLogger().config(String.format("    - Move path: %s", config.errorFilesMovePath));

        logger.getLogger().config("- Content classes: ");
        for (Map contentClass : config.contentClasses) {
            String name = contentClass.keySet().toArray()[0].toString();
            String values = contentClass.get(name).toString();
            logger.getLogger().config("    - " + name + " : \"" + values + "\"");
        }

        logger.getLogger().config("- TV shows : ");
        logger.getLogger().config(String.format("    - Numbering schema: %s", config.tvShowsNumberingSchema));
        logger.getLogger().config(String.format("    - Season schema: %s", config.tvShowsSeasonSchema));
        logger.getLogger().config(
                String.format("    - Words separators for show: %s", config.tvShowsWordsSeparatorShow));
        logger.getLogger().config(
                String.format("    - Words separators for file: %s", config.tvShowsWordsSeparatorFile));

        logger.getLogger().config("- Input folders: ");
        for (String folder : config.inputFolders) {
            logger.getLogger().config("    - " + folder);
        }

        logger.getLogger().config("- Output folders: ");
        for (Map folder : config.outputFolders) {
            logger.getLogger().config("    - " + folder);
        }
    }

    /**
     * Main process.
     */
    private void process() {

        processInputFiles();

        removeEmptyInputSubfolders();
    }

    /**
     * Process all inputf files
     */
    private void processInputFiles() {

        // extract classification criteria
        List<Criterium> criteria = constructCriteria();

        // get a classifier
        Classifier classifier = new Classifier(criteria);

        // get a mover
        Mover mover = new Mover(options, config, logger, eventDispatcher);

        // classify all input files
        Map<File, File[]> inputFiles = collectInputFiles();

        // create an aux flat stream to get the total count
        Long totalCount = ((Stream) inputFiles.values().stream().flatMap(Stream::of)).count();
        if (totalCount == 0) {
            logger.getLogger().fine("No input files found");
        } else {
            logger.getLogger().info(String.format("Found %s input files.", totalCount));
        }

        // using array for lambda limitations
        final int[] count = {0};

        inputFiles.forEach((File folder, File[] files) -> {

            for (File inputFile : files) {
                count[0]++;
                logger.getLogger().info(
                        String.format("Processing file (%s/%s) '%s'.", count[0], totalCount, inputFile.getName()));

                Classification fileClassification = classifier.classify(inputFile, folder);

                if (fileClassification.name.isEmpty()) {
                    logger.getLogger().warning(
                            String.format("- File class not found for file '%s'.", inputFile.getName()));

                    mover.processUnknownFile(inputFile);
                    continue;
                }

                logger.getLogger().info(String.format("- File class found: '%s'.", fileClassification.name));

                if (fileClassification.name.equals("tvshows")) {
                    logger.getLogger().info(String.format("- TV show: '%s', season %s, episode %s.",
                                                          fileClassification.tvShowName,
                                                          fileClassification.season,
                                                          fileClassification.episode));
                }

                // perform the actual move
                mover.moveToDestination(inputFile, fileClassification);
            }

        });

        // only write feed if new entries added
        if (feedWriter.hasEntries()) {
            feedWriter.writeFeed();
        }
    }

    /**
     * Write performed action to feed.
     *
     * @param fileClassification
     */
    private void addMovedToFeed(Classification fileClassification, String actionPerformed) {

        String title = String.format(
                "File \"%s\" -> \"%s\"",
                fileClassification.fileName,
                fileClassification.name
        );

        if (!fileClassification.tvShowName.isEmpty()) {
            title = String.format(
                    "Episode \"%s\" of TV show \"%s\" -> \"%s\"",
                    fileClassification.fileName,
                    fileClassification.tvShowName,
                    fileClassification.name
            );

        } else if (!fileClassification.albumName.isEmpty()) {
            title = String.format(
                    "Track \"%s\" of album \"%s\" -> \"%s\"",
                    fileClassification.fileName,
                    fileClassification.albumName,
                    fileClassification.name
            );
        }

        feedWriter.addEntry(title, actionPerformed);
    }

    /**
     * Join all the criterium objects to form the criteria.
     *
     * @return Criteria (list of criterium objects)
     */
    private List<Criterium> constructCriteria() {

        List<Criterium> criteria = new ArrayList<>();

        for (Map<String, Map> contentClass : config.contentClasses) {
            Criterium criterium = new Criterium();
            criterium.name = contentClass.keySet().toArray()[0].toString();

            if (contentClass.get(criterium.name).containsKey("extension")) {
                String extensionName = contentClass.get(criterium.name).get("extension").toString();
                for (Map<String, List<String>> extensionItems : config.extensions) {
                    String currentExtensionName = extensionItems.keySet().toArray()[0].toString();
                    if (extensionName.equals(currentExtensionName)) {
                        criterium.extensions = extensionItems.get(currentExtensionName).toArray(new String[0]);
                    }
                }
            }

            if (contentClass.get(criterium.name).containsKey("filter")) {
                String filterName = contentClass.get(criterium.name).get("filter").toString();
                for (Map<String, List<String>> filterItems : config.filters) {
                    String currentFilterName = filterItems.keySet().toArray()[0].toString();
                    if (filterName.equals(currentFilterName)) {
                        criterium.filters = filterItems.get(currentFilterName).toArray(new String[0]);
                    }
                }
            }

            criteria.add(criterium);
        }

        return criteria;
    }

    /**
     * Construct a list of files in the input folders.
     *
     * @return List of files.
     */
    private Map<File, File[]> collectInputFiles() {

        Map<File, File[]> collectedFiles = new LinkedHashMap<>();

        for (String inputFolder : config.inputFolders) {

            List<File> inputFilesInFolder;

            File folder = new File(inputFolder);

            if (!folder.exists()) {
                logger.getLogger().warning(String.format("- Input folder '%s' does not exist.", inputFolder));
                continue;
            }

            inputFilesInFolder = collectFiles(folder);
            File[] files = inputFilesInFolder.toArray(new File[0]);

            collectedFiles.put(folder, files);
        }

        return collectedFiles;
    }

    /**
     * Construct a list of files in the folder
     *
     * @return List of files.
     */
    private List<File> collectFiles(File inputFolder) {

        List<File> allFiles = new ArrayList<>();

        File[] files = inputFolder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    allFiles.addAll(collectFiles(file));
                } else {
                    allFiles.add(file);
                }
            }
        }

        return allFiles;
    }

    private void removeEmptyInputSubfolders() {

        Map<File, File[]> remainingInputSubFolders = collectInputSubfolders();

        remainingInputSubFolders.forEach((File folder, File[] subfolders) -> {

            for (File subfolder : subfolders) {

                // note that .delete() only deletes folder if empty
                if (subfolder.delete()) {
                    logger.getLogger().fine(String.format("Input empty subfolder '%s' deleted.", subfolder));
                    continue;
                }

                if (subfolder.exists() && collectFiles(subfolder).isEmpty()) {
                    logger.getLogger().warning(String.format("Empty input subfolder '%s' cannot be deleted.",
                                                             subfolder));
                }

            }

        });
    }

    /**
     * Construct a list of subfolders in the input folders.
     *
     * @return List of subfolders.
     */
    private Map<File, File[]> collectInputSubfolders() {

        Map<File, File[]> collectedFolders = new LinkedHashMap<>();

        for (String inputFolder : config.inputFolders) {

            List<File> subfoldersInFolder;

            File folder = new File(inputFolder);

            if (!folder.exists()) {
                continue;
            }

            subfoldersInFolder = collectFolders(folder);
            File[] folders = subfoldersInFolder.toArray(new File[0]);

            collectedFolders.put(folder, folders);
        }

        return collectedFolders;
    }

    /**
     * Construct a list of folders in the folder
     *
     * @return List of folders.
     */
    private ArrayList<File> collectFolders(File inputFolder) {

        ArrayList<File> allFolders = new ArrayList<>();

        File[] files = inputFolder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.isDirectory()) {
                    allFolders.addAll(collectFiles(file));
                } else {
                    allFolders.add(file);
                }
            }
        }

        return allFolders;
    }


    public Log getLogger() {

        return logger;
    }
}
