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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;

import static org.junit.Assert.assertArrayEquals;

public class ProcessorTest {

    static File executionFolder;
    static String executionPath;
    static String inputPath;
    static String outputPath;
    static String expectedPath;

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

        Log.setLogFileName(executionPath + "/librarian.log");
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        //executionFolder.delete();
    }

    @Test
    public void run() throws Exception {

        Log.getLogger().log(Level.INFO, "Starting functional test");

        // load configuration
        ConfigLoader configLoader = new ConfigLoader();
        configLoader.load(inputPath + "/librarian.yml");

        ConfigAdaptor adaptor = new ConfigAdaptor(configLoader);
        Config config = adaptor.process();

        // prepare output folder
        File outputdDir = new File(outputPath);
        outputdDir.mkdirs();

        // make input folders relative to the test input directory
        ArrayList<String> newFolders = new ArrayList<String>();
        for (String folder : config.getInputFolders()) {
            String newPath = inputPath + "/" + folder;
            newFolders.add(newPath);
        }
        config.setInputFolders(newFolders.toArray(new String[0]));

        // make output folders relative to the temp directory
        for (Map folder : config.getOutputFolders()) {
            String newPath = outputPath + "/" + folder.get("path").toString();
            folder.put("path", newPath);
        }

        // set options
        Options options = new Options();
        options.setCopyOnly(true);

        // execute
        Processor proc = new Processor(options, config);
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

        assertArrayEquals(expectedFilePaths.toArray(), outputFilePaths.toArray());

        Log.getLogger().log(Level.INFO, "Ended functional test");
    }

    private ArrayList<String> relativizePaths(ArrayList<File> outputFiles, String absolutePath) {

        ArrayList<String> files = new ArrayList<String>();

        outputFiles.forEach(file -> {
            files.add(file.getAbsolutePath().substring(absolutePath.length() + 1));
        });

        return files;
    }

    /**
     * Construct a list of files in the folder
     *
     * @return List of files.
     */
    private ArrayList<File> collectFiles(File inputFolder) {

        ArrayList<File> inputFiles = new ArrayList<File>();

        for (File file : inputFolder.listFiles()) {
            if (file.isDirectory()) {
                inputFiles.addAll(collectFiles(file));
            } else {
                inputFiles.add(file);
            }
        }

        return inputFiles;
    }
}