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
import com.mags.librarian.config.ConfigReader;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
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

        // base execution path
        executionPath = executionFolder.getAbsolutePath();

        // copy the input folder to the execution folder
        File sourceInputpath = new File("src/test/resources/functional/input");
        File inputPathFolder = new File(executionPath + "/input");
        copyFolder(sourceInputpath.toPath(), inputPathFolder.toPath());

        // set all the paths
        inputPath = inputPathFolder.getAbsolutePath();
        outputPath = new File(executionPath + "/output").getAbsolutePath();
        expectedPath = new File("src/test/resources/functional/expected").getAbsolutePath();

        logger = new Log(executionPath + "/librarian.log");
        logger.setLogLevel(Level.ALL);
        logger.start();

    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {

    }

    /**
     * Copy one folder to another destination.
     *
     * @param sourcePath
     * @param targetPath
     * @throws IOException
     */
    private static void copyFolder(Path sourcePath, Path targetPath) throws IOException {

        Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(
                    final Path dir,
                    final BasicFileAttributes attrs) throws IOException {

                Files.createDirectories(targetPath.resolve(sourcePath.relativize(dir)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(
                    final Path file,
                    final BasicFileAttributes attrs) throws IOException {

                Files.copy(file, targetPath.resolve(sourcePath.relativize(file)));
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @Test
    public void run() throws Exception {

        logger.getLogger().log(Level.INFO, "Starting functional test");

        // read configuration
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

        // make ignore, duplicates and error folders relative to the temp directory
        if (!config.unknownFilesMovePath.isEmpty()) {
            config.unknownFilesMovePath = Paths.get(outputPath, config.unknownFilesMovePath).toString();
        }

        if (!config.duplicateFilesMovePath.isEmpty()) {
            config.duplicateFilesMovePath = Paths.get(outputPath, config.duplicateFilesMovePath).toString();
        }

        if (!config.errorFilesMovePath.isEmpty()) {
            config.errorFilesMovePath = Paths.get(outputPath, config.errorFilesMovePath).toString();
        }

        // set options
        Options options = new Options();
        options.copyOnly = false;
        options.rssFileName = executionPath + "/librarian.rss";

        // execute
        Processor proc = new Processor(options, config, logger);
        proc.run();

        // get actual output files
        List<File> outputFiles = collectFiles(outputdDir);
        List<String> outputFilePaths = relativizePaths(outputFiles, outputdDir.getAbsolutePath());
        outputFilePaths.sort(String::compareTo);

        // and expected files
        File expectedDir = new File(expectedPath);
        List<File> expectedFiles = collectFiles(expectedDir);
        List<String> expectedFilePaths = relativizePaths(expectedFiles, expectedDir.getAbsolutePath());
        expectedFilePaths.sort(String::compareTo);

        // get input files (after) to check everything moved
        File inputFoldersDir = new File(inputPath + "/inputfolders");
        List<File> inputFilesAfter = collectFiles(inputFoldersDir);
        List<String> inputFilePathsAfter = relativizePaths(inputFilesAfter, inputFoldersDir.getAbsolutePath());
        inputFilePathsAfter.sort(String::compareTo);

        // get input folders' subfolders (after) to check everything moved
        List<File> inputSubfoldersAfter = collectSubfoldersOfInputFolders(config.inputFolders);

        assertArrayEquals("All files moved to output", expectedFilePaths.toArray(), outputFilePaths.toArray());
        assertArrayEquals("All files removed from input", new String[0], inputFilePathsAfter.toArray());
        assertArrayEquals("All subfolders removed from input", new String[0], inputSubfoldersAfter.toArray());

        assertTrue("RSS file created", Files.exists(executionFolder.toPath().resolve("librarian.rss")));
        assertTrue("Log file created", Files.exists(executionFolder.toPath().resolve("librarian.log")));

        logger.getLogger().log(Level.INFO, "Ended functional test");
    }

    private ArrayList<String> relativizePaths(List<File> outputFiles, String absolutePath) {

        ArrayList<String> files = new ArrayList<>();

        outputFiles.forEach(file -> files.add(file.getAbsolutePath().substring(absolutePath.length() + 1)));

        return files;
    }

    /**
     * Construct a list of files in the folder
     *
     * @return List of files.
     */
    private List<File> collectFiles(File inputFolder) {

        ArrayList<File> allFiles = new ArrayList<>();

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

    private List<File> collectSubfoldersOfInputFolders(String[] inputFolders) {

        List<File> allSubFolders = new ArrayList<>();

        for (String inputFolder : inputFolders) {
            allSubFolders.addAll(collectFolders(new File(inputFolder)));
        }

        return allSubFolders;
    }

    /**
     * Construct a list of folders in the folder
     *
     * @return List of folders.
     */
    private List<File> collectFolders(File inputFolder) {

        List<File> allFolders = new ArrayList<>();

        File[] files = inputFolder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.isDirectory()) {
                    allFolders.addAll(collectFolders(file));
                } else {
                    allFolders.add(file);
                }
            }
        }

        return allFolders;
    }


}