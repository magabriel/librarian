/*
 * This file is part of the librarian application.
 *
 * Copyright (c) Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package com.mags.librarian;

import com.mags.librarian.config.Config;
import com.mags.librarian.config.ConfigAdaptor;
import com.mags.librarian.config.ConfigLoader;
import com.mags.librarian.config.ConfigReader;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class ProcessorTest {

    private static File executionFolder;
    private static String executionPath;
    private static String inputPath;
    private static String outputPath;
    private static String expectedPath;
    private static Log logger;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        // create temp folder for actual results
        executionFolder = File.createTempFile("librarian-test", "");
        executionFolder.delete();
        executionFolder.mkdir();

        executionPath = executionFolder.getAbsolutePath();

        inputPath = new File("src/test/resources/functional/input").getAbsolutePath();
        outputPath = new File(executionPath + "/output").getAbsolutePath();
        expectedPath = new File("src/test/resources/functional/expected").getAbsolutePath();

        logger = new Log(executionPath + "/librarian.log");
        logger.setLogLevel(Level.ALL);
        logger.start();

    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void run() throws Exception {

        logger.getLogger().log(Level.INFO, "Starting functional test");

        // load configuration
//        ConfigLoader configLoader = new ConfigLoader();
//        configLoader.load(inputPath + "/librarian.yml");
//
//        ConfigAdaptor adaptor = new ConfigAdaptor(configLoader);
//        Config config = adaptor.process();
//
        ConfigReader reader = new ConfigReader();
        Config config = reader.read(inputPath + "/librarian.yml");

        // prepare output folder
        File outputdDir = new File(outputPath);
        outputdDir.mkdirs();

        // make input folders relative to the test input directory
        ArrayList<String> newFolders = new ArrayList<>();
        for (String folder : config.inputFolders) {
            String newPath = inputPath + "/" + folder;
            newFolders.add(newPath);
        }
        config.inputFolders = newFolders.toArray(new String[0]);

        // make output folders relative to the temp directory
        for (Map folder : config.outputFolders) {
            String newPath = outputPath + "/" + folder.get("path").toString();
            folder.put("path", newPath);
        }

        // set options
        Options options = new Options();
        options.copyOnly = true;
        options.rssFileName = executionPath + "/librarian.rss";

        // execute
        Processor proc = new Processor(options, config, logger);
        proc.run();

        // get actual output files
        ArrayList<File> outputFiles = collectFiles(outputdDir);
        ArrayList<String> outputFilePaths = relativizePaths(outputFiles, outputdDir.getAbsolutePath());
        outputFilePaths.sort(String::compareTo);

        // and expected files
        File expectedDir = new File(expectedPath);
        ArrayList<File> expectedFiles = collectFiles(expectedDir);
        ArrayList<String> expectedFilePaths = relativizePaths(expectedFiles, expectedDir.getAbsolutePath());
        expectedFilePaths.sort(String::compareTo);

        assertArrayEquals("All files moved", expectedFilePaths.toArray(), outputFilePaths.toArray());

        assertTrue("RSS file created", Files.exists(executionFolder.toPath().resolve("librarian.rss")));
        assertTrue("Log file created", Files.exists(executionFolder.toPath().resolve("librarian.log")));

        logger.getLogger().log(Level.INFO, "Ended functional test");
    }

    private ArrayList<String> relativizePaths(ArrayList<File> outputFiles, String absolutePath) {

        ArrayList<String> files = new ArrayList<>();

        outputFiles.forEach(file -> files.add(file.getAbsolutePath().substring(absolutePath.length() + 1)));

        return files;
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