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

import java.io.File;
import java.util.*;

/**
 * Processes the config file to actually move the files.
 */
class Processor {

    private final Options options;
    private final Config config;
    private Log logger;
    private FeedWriter feedWriter;

    Processor(Options options, Config config, Log logger) {

        this.options = options;
        this.config = config;
        this.logger = logger;
    }

    /**
     * Runs the classification process.
     */
    void run() {

        logger.getLogger().info("Started");

        logOptionsAndConfig();

        if (config.outputFolders.length == 0) {
            logger.getLogger().severe("No output folders set, cannot continue.");
            return;
        }

        feedWriter = new FeedWriter(options.rssFileName, logger);

        process();

        logger.getLogger().info("Finished");
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

        logger.getLogger().config("- Content types: ");
        for (Map contentType : config.contentTypes) {
            String name = contentType.keySet().toArray()[0].toString();
            String regExp = contentType.get(name).toString();
            logger.getLogger().config("    - " + name + " : \"" + regExp + "\"");
        }

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

        // extract classification criteria
        ArrayList<Criterium> criteria = constructCriteria();

        // get a classifier
        Classifier classifier = new Classifier(criteria);

        // get a mover
        Mover mover = new Mover(options, config, logger);

        // classify all input files
        LinkedHashMap<File, File[]> inputFiles = collectInputFiles();
        logger.getLogger().info(String.format("Found %s input files.", inputFiles.size()));

        inputFiles.forEach((File folder, File[] files) -> {

            int count = 0;

            for (File inputFile : files) {
                count++;
                logger.getLogger().info(String.format("Processing file (%s/%s) '%s'.", count++, inputFiles.size(),
                                                      inputFile.getName()));

                Classification fileClassification = classifier.classify(inputFile, folder);

                if (fileClassification.name.isEmpty()) {
                    logger.getLogger().warning("- File class not found, skipping.");
                    continue;
                }

                logger.getLogger().info(String.format("- File class found: '%s'.", fileClassification.name));

                if (fileClassification.name.equals("tvshows")) {
                    logger.getLogger().info(String.format("- TV show: '%s', season %s, episode %s.",
                                                          fileClassification.tvShowName,
                                                          fileClassification.season,
                                                          fileClassification.episode));
                }

                mover.moveToDestination(inputFile, fileClassification);

                // if something done, write it to feed
                if (!mover.getActionPerformed().isEmpty()) {
                    String title = String.format(
                            "File \"%s\" -> \"%s\" (action: %s)",
                            mover.getSummary().inputFilename,
                            mover.getSummary().outputFolder,
                            mover.getSummary().action
                    );

                    if (!fileClassification.tvShowName.isEmpty()) {
                        title = String.format(
                                "Episode \"%s\" of TV show \"%s\" -> \"%s\" (action: %s)",
                                mover.getSummary().inputFilename,
                                fileClassification.tvShowName,
                                mover.getSummary().outputFolder,
                                mover.getSummary().action
                        );

                    }

                    feedWriter.addEntry(title, mover.getActionPerformed());
                }
            }

        });

        feedWriter.writeFeed();
    }

    /**
     * Join all the criterium objects to form the criteria.
     *
     * @return Criteria (list of criterium objects)
     */
    private ArrayList<Criterium> constructCriteria() {

        ArrayList<Criterium> criteria = new ArrayList<>();
        for (Map contentType : config.contentTypes) {
            Criterium criterium = new Criterium();
            criterium.name = contentType.keySet().toArray()[0].toString();
            criterium.regExp = contentType.get(criterium.name).toString();

            criteria.add(criterium);
        }

        return criteria;
    }

    /**
     * Construct a list of files in the input folders.
     *
     * @return List of files.
     */
    private LinkedHashMap<File, File[]> collectInputFiles() {

        LinkedHashMap<File, File[]> collectedFiles = new LinkedHashMap<>();

        for (String inputFolder : config.inputFolders) {

            ArrayList<File> inputFilesInFolder;

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
    private ArrayList<File> collectFiles(File inputFolder) {

        ArrayList<File> inputFiles = new ArrayList<>();

        File[] files = inputFolder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    inputFiles.addAll(collectFiles(file));
                } else {
                    inputFiles.add(file);
                }
            }
        }

        return inputFiles;
    }

}
