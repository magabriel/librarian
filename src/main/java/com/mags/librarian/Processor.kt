/*
 * This file is part of the librarian application.
 *
 * Copyright (c) Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package com.mags.librarian

import com.mags.librarian.classifier.Classification
import com.mags.librarian.classifier.Classifier
import com.mags.librarian.classifier.Criteria
import com.mags.librarian.config.Config
import com.mags.librarian.event.*
import com.mags.librarian.options.Options
import com.mags.librarian.utils.FileUtils
import java.io.File
import java.util.*
import javax.inject.Inject

/**
 * Processes the config file to actually move the files.
 */
internal class Processor
@Inject constructor(private val options: Options,
                    private val config: Config,
                    private val logWriter: LogWriter,
                    private val eventDispatcher: EventDispatcher,
                    private var feedWriter: FeedWriter,
                    private var command: Command,
                    private var mover: Mover) {

    /**
     * Runs the classification process.
     */
    fun run() {
        logWriter.fine("Started")
        logOptionsAndConfig()
        if (config.outputFolders.isEmpty()) {
            logWriter.severe("No output folders set, cannot continue.")
            return
        }
        addListeners()
        process()
        logWriter.fine("Finished")
    }

    /**
     * Add all listeners for several events
     */
    private fun addListeners() {
        eventDispatcher.addListener(Event.FILE_PROCESSED, object : Listener<EventData> {
            override fun onEvent(eventData: EventData) {
                val data = eventData as FileProcessedEventData
                val arguments = listOf<String>(data.inputFolder,
                                               data.inputFilename,
                                               data.outputFolder,
                                               data.outputFilename,
                                               data.fileClassification?.name!!,
                                               data.action)
                if (!config.executeSuccess.isEmpty()) {
                    command.execute(File(config.executeSuccess).absolutePath, arguments)
                }
                addMovedToFeed(data.fileClassification, data.actionPerformed)
            }
        })

        eventDispatcher.addListener(Event.FILE_UNKNOWN, object : Listener<EventData> {
            override fun onEvent(eventData: EventData) {
                val data = eventData as FileUnknownEventData
                val arguments = listOf<String>(data.inputFolder,
                                               data.inputFilename,
                                               data.outputFolder,
                                               data.outputFilename,
                                               data.action)
                if (!config.executeError.isEmpty()) {
                    command.execute(File(config.executeError).absolutePath, arguments)
                }
            }
        })

        eventDispatcher.addListener(Event.FILE_ERROR, object : Listener<EventData> {
            override fun onEvent(eventData: EventData) {
                val data = eventData as FileErrorEventData
                val arguments = listOf<String>(data.inputFolder,
                                               data.inputFilename,
                                               data.outputFolder,
                                               data.outputFilename,
                                               data.action)
                if (!config.executeError.isEmpty()) {
                    command.execute(File(config.executeError).absolutePath, arguments)
                }
            }
        })
    }

    /**
     * Write options and configuration values to the logWriter.
     */
    private fun logOptionsAndConfig() {
        if (options.dryRun) {
            logWriter.config("- Dry run: true")
        }

        logWriter.config("- Log level: ${options.logLevel}")
        logWriter.config("- Verbosity: ${options.verbosity}")

        if (options.copyOnly) {
            logWriter.config("- Copy only: true")
        }

        logWriter.config("- Extensions: ")
        config.extensions.forEach { extension ->
            val name = extension.keys.toTypedArray()[0]
            val values = extension[name].toString()
            logWriter.config("    - $name : \"$values\"")
        }

        logWriter.config("- Filters: ")
        config.filters.forEach { filter ->
            filter.forEach { name, filterItems ->
                logWriter.config("    - " + name)
                filterItems.forEach { regExp -> logWriter.config("        - " + regExp) }
            }
        }

        logWriter.config("- Unknown files: ")
        logWriter.config("    - Action: ${config.unknownFilesAction}")
        logWriter.config("    - Move path: ${config.unknownFilesMovePath}")

        logWriter.config("- Error files: ")
        logWriter.config("    - Action: ${config.errorFilesAction}")
        logWriter.config("    - Move path: ${config.errorFilesMovePath}")

        logWriter.config("- Content classes: ")
        config.contentClasses.forEach { contentClass ->
            val name = contentClass.keys.toTypedArray()[0]
            val values = contentClass[name].toString()
            logWriter.config("    - $name : \"$values\"")
        }

        logWriter.config("- TV shows : ")
        logWriter.config("    - Numbering schema: ${config.tvShowsNumberingSchema}")
        logWriter.config("    - Season schema: ${config.tvShowsSeasonSchema}")
        logWriter.config("    - Words separators for show: ${config.tvShowsWordsSeparatorShow}")
        logWriter.config("    - Words separators for file: ${config.tvShowsWordsSeparatorFile}")

        logWriter.config("- Input folders: ")
        for (folder in config.inputFolders) {
            logWriter.config("    - $folder")
        }

        logWriter.config("- Output folders: ")
        for (folder in config.outputFolders) {
            logWriter.config("    - $folder")
        }
    }

    /**
     * Main process.
     */
    private fun process() {
        processInputFiles()
        removeEmptyInputSubfolders()
    }

    /**
     * Process all input files
     */
    private fun processInputFiles() {
        // create criteria from config
        val criteria = Criteria(config)
        // get a classifier for the criteria
        val classifier = Classifier(criteria)
        // classify all input files
        val inputFiles = collectInputFiles()
        // get the total files count
        val totalCount = inputFiles.values.map { it.size }.sum()
        if (totalCount == 0) {
            logWriter.fine("No input files found")
        } else {
            logWriter.info("Found $totalCount input files.")
        }

        var count = 0
        inputFiles.forEach { folder: File, files: Array<File> ->
            files.forEach file@ { inputFile ->
                count++
                logWriter.info("Processing file ($count/$totalCount) '${inputFile.name}'.")
                val fileClassification = classifier.classify(inputFile, folder)

                if (fileClassification.name.isEmpty()) {
                    logWriter.warning("- File class not found for file '${inputFile.name}'.")
                    mover.processUnknownFile(inputFile)
                    return@file
                }

                logWriter.info("- File class found: '${fileClassification.name}'.")

                if (fileClassification.name == "tvshows") {
                    with(fileClassification) {
                        logWriter.info("- TV show: '$tvShowName', season $season, episode $episode.")
                    }
                }
                // perform the actual move
                mover.moveToDestination(inputFile.absoluteFile, fileClassification)
            }
        }
        // only write feed if new entries added
        if (feedWriter.hasEntries()) {
            feedWriter.writeFeed()
        }
    }

    /**
     * Write performed action to feed.
     */
    private fun addMovedToFeed(fileClassification: Classification?,
                               actionPerformed: String?) {
        with(fileClassification!!) {
            var title = "File \"$fileName\" -> \"$name\""
            if (!tvShowName.isEmpty()) {
                title = "Episode \"$fileName\" of TV show \"$tvShowName\" -> \"$name\""
            } else if (!albumName.isEmpty()) {
                title = "Track \"$fileName\" of album \"$albumName\" -> \"$name\""
            }

            feedWriter.addEntry(title, actionPerformed!!)
        }
    }

    /**
     * Construct a list of files in the input folders.
     */
    private fun collectInputFiles(): Map<File, Array<File>> {
        val collectedFiles = LinkedHashMap<File, Array<File>>()

        config.inputFolders.forEach { inputFolder ->
            val inputFilesInFolder: List<File>
            val folder = File(inputFolder).absoluteFile
            if (!folder.exists()) {
                logWriter.warning("- Input folder '$inputFolder' does not exist.")
                return@forEach
            }
            inputFilesInFolder = collectFiles(folder)
            val files = inputFilesInFolder.toTypedArray()
            collectedFiles.put(folder, files)
        }

        return collectedFiles
    }

    /**
     * Construct a list of files in the folder
     */
    private fun collectFiles(inputFolder: File): List<File> {
        val allFiles = mutableListOf<File>()
        val files = inputFolder.listFiles()
        if (files != null) {
            files.forEach { file ->
                if (file.isDirectory) {
                    allFiles.addAll(collectFiles(file))
                } else {
                    allFiles.add(file)
                }
            }
        }
        return allFiles
    }

    private fun removeEmptyInputSubfolders() {
        val remainingInputSubFolders = FileUtils.findSubfoldersTree(config.inputFolders.toList())

        remainingInputSubFolders.forEach { _: File, subfolders: List<File> ->
            subfolders.forEach subfolder@ { subfolder ->
                // note that .delete() only deletes folder if empty
                if (subfolder.delete()) {
                    logWriter.fine("Input empty subfolder '$subfolder' deleted.")
                    return@subfolder
                }

                if (subfolder.exists() && collectFiles(subfolder).isEmpty()) {
                    logWriter.warning("Empty input subfolder '$subfolder' cannot be deleted.")
                }
            }
        }
    }

}
