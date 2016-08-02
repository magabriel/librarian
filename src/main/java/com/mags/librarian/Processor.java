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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;

/**
 * Processes the config file to actually move the files.
 */
class Processor {

    private final Options options;
    private final Config config;
    private FeedWriter feedWriter;

    Processor(Options options, Config config) {

        this.options = options;
        this.config = config;
    }

    /**
     * Runs the classification process.
     */
    void run() {

        Log.getLogger().info("Started");

        logOptionsAndConfig();

        if (config.outputFolders.length == 0) {
            Log.getLogger().severe("No output folders set, cannot continue.");
            return;
        }

        feedWriter = new FeedWriter(options.getRssFileName());

        process();

        Log.getLogger().info("Finished");
    }

    /**
     * Write options and configuration values to the log.
     */
    private void logOptionsAndConfig() {

        if (options.getDryRun()) {
            Log.getLogger().log(Level.CONFIG, "- Dry run: true");
        }

        if (options.getCopyOnly()) {
            Log.getLogger().log(Level.CONFIG, "- Copy only: true");
        }

        Log.getLogger().log(Level.CONFIG, "- Content types: ");
        for (Map contentType : config.contentTypes) {
            String name = contentType.keySet().toArray()[0].toString();
            String regExp = contentType.get(name).toString();
            Log.getLogger().log(Level.CONFIG, "    - " + name + " : \"" + regExp + "\"");
        }

        Log.getLogger().log(Level.CONFIG, "- Input folders: ");
        for (String folder : config.inputFolders) {
            Log.getLogger().log(Level.CONFIG, "    - " + folder);
        }

        Log.getLogger().log(Level.CONFIG, "- Output folders: ");
        for (Map folder : config.outputFolders) {
            Log.getLogger().log(Level.CONFIG, "    - " + folder);
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
        Mover mover = new Mover(options, config);

        // classify all input files
        ArrayList<File> inputFiles = collectInputFiles();
        Log.getLogger().info(String.format("Found %s input files.", inputFiles.size()));

        int count = 0;
        for (File inputFile : inputFiles) {
            count++;
            Log.getLogger().info(String.format("Processing file (%s/%s) '%s'.", count++, inputFiles.size(),
                                               inputFile.getName()));

            Classification fileClassification = classifier.classify(inputFile.getName());

            if (fileClassification.name.isEmpty()) {
                Log.getLogger().warning("- File class not found, skipping.");
                continue;
            }

            Log.getLogger().info(String.format("- File class found: '%s'.", fileClassification.name));

            if (fileClassification.name.equals("tvshows")) {
                Log.getLogger().info(String.format("- TV show: '%s', season %s, episode %s.",
                                                   fileClassification.tvshowName,
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

                if (!fileClassification.tvshowName.isEmpty()) {
                    title = String.format(
                            "Episode \"%s\" of TV show \"%s\" -> \"%s\" (action: %s)",
                            mover.getSummary().inputFilename,
                            fileClassification.tvshowName,
                            mover.getSummary().outputFolder,
                            mover.getSummary().action
                    );

                }

                feedWriter.addEntry(title, mover.getActionPerformed());
            }
        }

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
    private ArrayList<File> collectInputFiles() {

        ArrayList<File> inputFiles = new ArrayList<>();

        for (String inputFolder : config.inputFolders) {

            File folder = new File(inputFolder);

            if (!folder.exists()) {
                Log.getLogger().warning(String.format("- Input folder '%s' does not exist.", inputFolder));
                continue;
            }
            Collections.addAll(inputFiles, collectFiles(folder).toArray(new File[0]));
        }

        return inputFiles;
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
