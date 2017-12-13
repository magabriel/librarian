/*
 * This file is part of the librarian application.
 *
 * Copyright (c) 2017 Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code
 */

package com.mags.librarian

import com.mags.librarian.utils.FileUtils
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

internal class FunctionalTest {

    companion object {
        /** Expected number of files processed OK */
        val EXPECTED_FILES_OK = 21
        /** Expected number of files processed with error */
        val EXPECTED_FILES_ERROR = 3

        lateinit private var executionFolder: File
        lateinit private var executionPath: String
        lateinit private var inputPath: String
        lateinit private var outputPath: String
        lateinit private var expectedPath: String
        lateinit private var inputFoldersPath: String
        lateinit private var logFile: File
        lateinit private var rssFile: File
    }

    @BeforeEach
    fun setUpBeforeEach() {
        // create temp folder for actual results
        executionFolder = File.createTempFile("librarian-test", "")
        executionFolder.delete()
        executionFolder.mkdir()

        // base execution path
        executionPath = executionFolder.absolutePath

        // copy the input folder to the execution folder
        val sourceInputpath = File("src/test/resources/functional/input")
        val inputPathFolder = File(executionPath + "/input")
        sourceInputpath.copyRecursively(inputPathFolder)
        // restore executable bit (copyRecursively does not preserve it)
        inputPathFolder.walkBottomUp().filter { it.isFile && it.extension == "sh" }.forEach {
            it.setExecutable(true)
        }

        // set all the paths
        inputPath = inputPathFolder.absolutePath
        outputPath = File(executionPath).resolve("output").absolutePath
        expectedPath = File("src/test/resources/functional/expected").absolutePath
        inputFoldersPath = inputPathFolder.resolve("inputfolders").absolutePath

        // generated files
        logFile = executionFolder.resolve("ibrarian.log")
        rssFile = executionFolder.resolve("ibrarian.rss")

        // prepare output folder
        val outputdDir = File(outputPath)
        outputdDir.mkdirs()

        // set working dir
        System.setProperty("user.dir", inputPath)
    }

    @Test
    fun main() {
        val args = listOf<String>("-vv",
                                  "--loglevel",
                                  "all",
                                  "--log",
                                  logFile.absolutePath,
                                  "--rss",
                                  rssFile.absolutePath)
        Main.main(args.toTypedArray())

        // get actual output files
        val outputdDir = File(outputPath)
        val outputFiles = outputdDir.walkTopDown().filter { it.isFile }.toList()
        val outputFilePaths = outputFiles.map { it.toRelativeString(outputdDir) }.toList().sorted()

        // and expected files
        val expectedDir = File(expectedPath)
        val expectedFiles = expectedDir.walkTopDown().filter { it.isFile }.toList()
        val expectedFilePaths = expectedFiles.map { it.toRelativeString(expectedDir) }.toList().sorted()

        // get input files (after) to check everything moved
        val inputFoldersDir = File(inputFoldersPath)
        val inputFilesAfter = inputFoldersDir.walkTopDown().filter { it.isFile }.toList()
        val inputFilePathsAfter = inputFilesAfter.map { it.toRelativeString(inputFoldersDir) }.toList().sorted()

        // get input folders' subfolders (after) to check everything moved
        val allInputFolders = inputFoldersDir.listFiles().filter { it.isDirectory }
        val inputSubfoldersAfter = FileUtils.findSubfolders(allInputFolders).map { it.absolutePath }

        assertArrayEquals(expectedFilePaths.toTypedArray(),
                          outputFilePaths.toTypedArray(),
                          "All files moved to output")

        assertArrayEquals(arrayOf(),
                          inputFilePathsAfter.toTypedArray(),
                          "All files removed from input")

        assertArrayEquals(arrayOf(),
                          inputSubfoldersAfter.toTypedArray(),
                          "All subfolders removed from input")

        assertTrue(logFile.exists(), "Log file created")
        assertTrue(rssFile.exists(), "RSS file created")

        // check success and error script invocation
        val logLines = logFile.readLines()
        val countSuccess = logLines.filter { s -> s.contains("SUCCESS:") }.count()

        assertEquals(EXPECTED_FILES_OK, countSuccess, "Success script executed for each file")
        val countError = logLines.filter { s -> s.contains("ERROR:") }.count()
        assertEquals(EXPECTED_FILES_ERROR, countError, "Error script executed for each file")
    }
}
