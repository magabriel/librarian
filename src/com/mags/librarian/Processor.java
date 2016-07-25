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
import java.util.Map;
import java.util.logging.Level;

public class Processor {

    private final Options options;
    private final Config config;

    public Processor(Options options, Config config) {

        this.options = options;
        this.config = config;
    }

    /**
     * Runs the classification process.
     */
    public void run() {

        Log.getLogger().info("Started");

        logOptionsAndConfig();

        if (config.getOutputFolders().length == 0) {
            Log.getLogger().severe("No output folders set, cannot continue.");
            return;
        }

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
        for (Map contentType : config.getContentTypes()) {
            String name = contentType.keySet().toArray()[0].toString();
            String regExp = contentType.get(name).toString();
            Log.getLogger().log(Level.CONFIG, "    - " + name + " : \"" + regExp + "\"");
        }

        Log.getLogger().log(Level.CONFIG, "- Input folders: ");
        for (String folder : config.getInputFolders()) {
            Log.getLogger().log(Level.CONFIG, "    - " + folder);
        }

        Log.getLogger().log(Level.CONFIG, "- Output folders: ");
        for (Map folder : config.getOutputFolders()) {
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

            if (fileClassification.getName().isEmpty()) {
                Log.getLogger().warning("- File class not found, skipping.");
                continue;
            }

            Log.getLogger().info(String.format("- File class found: '%s'.", fileClassification.getName()));

            if (fileClassification.getName().equals("tvshows")) {
                Log.getLogger().info(String.format("- TV show: '%s', season %s, episode %s.",
                                                   fileClassification.getTvshowName(),
                                                   fileClassification.getSeason(),
                                                   fileClassification.getEpisode()));
            }

            mover.moveToDestination(inputFile, fileClassification);
        }
    }

    /**
     * Join all the criterium objects to form the criteria.
     *
     * @return Criteria (list of criterium objects)
     */
    private ArrayList<Criterium> constructCriteria() {

        ArrayList<Criterium> criteria = new ArrayList<Criterium>();
        for (Map contentType : config.getContentTypes()) {
            Criterium criterium = new Criterium();
            criterium.setName(contentType.keySet().toArray()[0].toString());
            criterium.setRegExp(contentType.get(criterium.getName()).toString());

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

        ArrayList<File> inputFiles = new ArrayList<File>();

        for (String inputFolder : config.getInputFolders()) {
            File folder = new File(inputFolder);
            for (File file : folder.listFiles()) {
                inputFiles.add(file);
            }
        }

        return inputFiles;
    }

}
