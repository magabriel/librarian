/*
 * This file is part of the librarian application.
 *
 * Copyright (c) Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package com.mags.librarian

import com.mags.librarian.config.ConfigReader
import com.mags.librarian.event.EventDispatcher
import com.mags.librarian.options.Options
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.File
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.ArrayList
import java.util.logging.Level
import kotlin.Comparator

class ProcessorTest {
    @Test
    @Throws(Exception::class)
    fun run() {
        logWriter!!.info("Starting functional test")
        // read configuration
        val reader = ConfigReader()
        val config = reader.read(inputPath!! + "/librarian.yml")
        // prepare output folder
        val outputdDir = File(outputPath!!)
        outputdDir.mkdirs()
        // make input folders relative to the test input directory
        val newInputFolders = ArrayList<String>()
        for (folder in config.inputFolders) {
            val newPath = inputPath + "/" + folder
            newInputFolders.add(newPath)
        }
        config.inputFolders = newInputFolders.toTypedArray()
        // make output folders relative to the temp directory
        val newOutputFolders = mutableListOf<Map<String, String>>()
        for (folder in config.outputFolders) {
            val newPath = outputPath + "/" + folder["path"].toString()
            val newFolder = mutableMapOf<String, String>()
            newFolder.put("path", newPath)
            newFolder.put("contents", folder["contents"].toString())
            newOutputFolders.add(newFolder)
        }
        config.outputFolders = newOutputFolders.toTypedArray()
        // make ignore, duplicates and error folders relative to the temp directory
        if (!config.unknownFilesMovePath.isEmpty()) {
            config.unknownFilesMovePath = Paths.get(outputPath!!,
                                                    config.unknownFilesMovePath).toString()
        }

        if (!config.duplicateFilesMovePath.isEmpty()) {
            config.duplicateFilesMovePath = Paths.get(outputPath!!,
                                                      config.duplicateFilesMovePath).toString()
        }

        if (!config.errorFilesMovePath.isEmpty()) {
            config.errorFilesMovePath = Paths.get(outputPath!!,
                                                  config.errorFilesMovePath).toString()
        }
        // make exec paths relative to the input directory
        if (!config.executeSuccess.isEmpty()) {
            config.executeSuccess = Paths.get(inputPath!!, config.executeSuccess).toString()
        }

        if (!config.executeError.isEmpty()) {
            config.executeError = Paths.get(inputPath!!, config.executeError).toString()
        }
        // set options
        val options = Options()
        options.copyOnly = false
        options.rssFileName = executionPath!! + "/librarian.rss"
        // execute
        val proc = Processor(options, config, logWriter!!, eventDispatcher!!)
        proc.run()
        // get actual output files
        val outputFiles = collectFiles(outputdDir)
        val outputFilePaths = relativizePaths(outputFiles, outputdDir.absolutePath)
        outputFilePaths.sortWith(Comparator { obj, anotherString -> obj.compareTo(anotherString) })
        // and expected files
        val expectedDir = File(expectedPath!!)
        val expectedFiles = collectFiles(expectedDir)
        val expectedFilePaths = relativizePaths(expectedFiles, expectedDir.absolutePath)
        expectedFilePaths.sortWith(Comparator { obj, anotherString ->
            obj.compareTo(anotherString)
        })
        // get input files (after) to check everything moved
        val inputFoldersDir = File(inputPath!! + "/inputfolders")
        val inputFilesAfter = collectFiles(inputFoldersDir)
        val inputFilePathsAfter = relativizePaths(inputFilesAfter, inputFoldersDir.absolutePath)
        inputFilePathsAfter.sortWith(Comparator { obj, anotherString ->
            obj.compareTo(anotherString)
        })
        // get input folders' subfolders (after) to check everything moved
        val inputSubfoldersAfter = collectSubfoldersOfInputFolders(config.inputFolders)

        // "All files moved to output"
        assertArrayEquals(expectedFilePaths.toTypedArray(), outputFilePaths.toTypedArray())
        // "All files removed from input"
        assertArrayEquals(arrayOfNulls<String>(0), inputFilePathsAfter.toTypedArray())
        // "All subfolders removed from input"
        assertArrayEquals(arrayOfNulls<String>(0), inputSubfoldersAfter.toTypedArray())

        // "RSS file created"
        assertTrue(Files.exists(executionFolder!!.toPath().resolve("librarian.rss")))
        // "Log file created"
        assertTrue(Files.exists(executionFolder!!.toPath().resolve("librarian.log")))
        // check success and error script invocation
        val logLines = Files.readAllLines(Paths.get(logFilename))
        val countSuccess = logLines.stream().filter { s ->
            s.toString().contains("SUCCESS:")
        }.count()
        // ("Success script executed for each file"
        assertEquals(EXPECTED_FILES_OK.toLong(), countSuccess)
        val countError = logLines.stream().filter { s -> s.toString().contains("ERROR:") }.count()
        // "Error script executed for each file"
        assertEquals(EXPECTED_FILES_ERROR.toLong(), countError)

        logWriter!!.info("Ended functional test")
    }

    private fun relativizePaths(outputFiles: List<File>,
                                absolutePath: String): ArrayList<String> {
        val files = ArrayList<String>()

        outputFiles.forEach { file ->
            files.add(file.absolutePath.substring(absolutePath.length + 1))
        }

        return files
    }

    /**
     * Construct a list of files in the folder
     *
     * @return List of files.
     */
    private fun collectFiles(inputFolder: File): List<File> {
        val allFiles = ArrayList<File>()
        val files = inputFolder.listFiles()
        if (files != null) {
            for (file in files) {
                if (file.isDirectory) {
                    allFiles.addAll(collectFiles(file))
                } else {
                    allFiles.add(file)
                }
            }
        }

        return allFiles
    }

    private fun collectSubfoldersOfInputFolders(inputFolders: Array<String>): List<File> {
        val allSubFolders = ArrayList<File>()

        for (inputFolder in inputFolders) {
            allSubFolders.addAll(collectFolders(File(inputFolder)))
        }

        return allSubFolders
    }

    /**
     * Construct a list of folders in the folder
     *
     * @return List of folders.
     */
    private fun collectFolders(inputFolder: File): List<File> {
        val allFolders = ArrayList<File>()
        val files = inputFolder.listFiles()
        if (files != null) {
            for (file in files) {
                if (!file.isDirectory) {
                    allFolders.addAll(collectFolders(file))
                } else {
                    allFolders.add(file)
                }
            }
        }

        return allFolders
    }

    companion object {
        /**
         * Expected number of files processed OK
         */
        val EXPECTED_FILES_OK = 21
        /**
         * Expected number of files processed with error
         */
        val EXPECTED_FILES_ERROR = 3
        private var executionFolder: File? = null
        private var executionPath: String? = null
        private var inputPath: String? = null
        private var outputPath: String? = null
        private var expectedPath: String? = null
        private var logFilename: String? = null
        private var logWriter: LogWriter? = null
        private var eventDispatcher: EventDispatcher? = null

        @BeforeAll
        @Throws(Exception::class)
        @JvmStatic
        fun setUpBeforeClass() {
            // create temp folder for actual results
            executionFolder = File.createTempFile("librarian-test", "")
            executionFolder!!.delete()
            executionFolder!!.mkdir()
            // base execution path
            executionPath = executionFolder!!.absolutePath
            // copy the input folder to the execution folder
            val sourceInputpath = File("src/test/resources/functional/input")
            val inputPathFolder = File(executionPath!! + "/input")
            copyFolder(sourceInputpath.toPath(), inputPathFolder.toPath())
            // set all the paths
            inputPath = inputPathFolder.absolutePath
            outputPath = File(executionPath!! + "/output").absolutePath
            expectedPath = File("src/test/resources/functional/expected").absolutePath

            logFilename = executionPath!! + "/librarian.log"
            logWriter = LogWriter(java.util.logging.Logger.getLogger(this.javaClass.name))
            logWriter!!.logFileName = executionPath!! + "/librarian.log"
            logWriter!!.logLevel = Level.ALL
            logWriter!!.start()

            eventDispatcher = EventDispatcher()
        }

        @AfterAll
        @Throws(Exception::class)
        @JvmStatic
        fun tearDownAfterClass() {

        }

        /**
         * Copy one folder to another destination.
         *
         * @param sourcePath
         * @param targetPath
         * @throws IOException
         */
        @Throws(IOException::class) private fun copyFolder(sourcePath: Path,
                                                           targetPath: Path) {
            Files.walkFileTree(sourcePath, object : SimpleFileVisitor<Path>() {
                @Throws(IOException::class) override fun preVisitDirectory(dir: Path,
                                                                           attrs: BasicFileAttributes): FileVisitResult {
                    Files.createDirectories(targetPath.resolve(sourcePath.relativize(dir)))
                    return FileVisitResult.CONTINUE
                }

                @Throws(IOException::class) override fun visitFile(file: Path,
                                                                   attrs: BasicFileAttributes): FileVisitResult {
                    Files.copy(file, targetPath.resolve(sourcePath.relativize(file)))
                    return FileVisitResult.CONTINUE
                }
            })
        }
    }

}