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
import java.io.File
import java.util.*

/**
 * Processes the config file to actually move the files.
 */
internal class Processor(private val options: Options,
                         private val config: Config,
                         private val logger: LogWriter,
                         private val eventDispatcher: EventDispatcher) {
    private var feedWriter: FeedWriter? = null
    private var command: Command? = null

    /**
     * Runs the classification process.
     */
    fun run() {
        logger.fine("Started")
        logOptionsAndConfig()
        if (config.outputFolders.isEmpty()) {
            logger.severe("No output folders set, cannot continue.")
            return
        }
        feedWriter = FeedWriter(options.rssFileName, logger)
        command = Command(logger)
        addListeners()
        process()
        logger.fine("Finished")
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
                    command!!.execute(config.executeSuccess, arguments)
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
                    command!!.execute(config.executeError, arguments)
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
                    command!!.execute(config.executeError, arguments)
                }
            }
        })
    }

    /**
     * Write options and configuration values to the logger.
     */
    private fun logOptionsAndConfig() {
        if (options.dryRun) {
            logger.config("- Dry run: true")
        }

        logger.config("- Log level: ${options.logLevel}")
        logger.config("- Verbosity: ${options.verbosity}")

        if (options.copyOnly) {
            logger.config("- Copy only: true")
        }

        logger.config("- Extensions: ")
        for (extension in config.extensions) {
            val name = extension.keys.toTypedArray()[0]
            val values = extension[name].toString()
            logger.config("    - $name : \"$values\"")
        }

        logger.config("- Filters: ")
        for (filter in config.filters) {
            filter.forEach { name, filterItems ->
                logger.config("    - " + name)
                for (regExp in filterItems) {
                    logger.config("        - " + regExp)
                }
            }
        }

        logger.config("- Unknown files: ")
        logger.config("    - Action: ${config.unknownFilesAction}")
        logger.config("    - Move path: ${config.unknownFilesMovePath}")

        logger.config("- Error files: ")
        logger.config("    - Action: ${config.errorFilesAction}")
        logger.config("    - Move path: ${config.errorFilesMovePath}")

        logger.config("- Content classes: ")
        for (contentClass in config.contentClasses) {
            val name = contentClass.keys.toTypedArray()[0]
            val values = contentClass[name].toString()
            logger.config("    - $name : \"$values\"")
        }

        logger.config("- TV shows : ")
        logger.config("    - Numbering schema: ${config.tvShowsNumberingSchema}")
        logger.config("    - Season schema: ${config.tvShowsSeasonSchema}")
        logger.config("    - Words separators for show: ${config.tvShowsWordsSeparatorShow}")
        logger.config("    - Words separators for file: ${config.tvShowsWordsSeparatorFile}")

        logger.config("- Input folders: ")
        for (folder in config.inputFolders) {
            logger.config("    - $folder")
        }

        logger.config("- Output folders: ")
        for (folder in config.outputFolders) {
            logger.config("    - $folder")
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
     * Process all inputf files
     */
    private fun processInputFiles() {
        // create criteria from config
        val criteria = Criteria(config)
        val criteriumList = criteria.criteriumList
        // get a classifier for the criterium list
        val classifier = Classifier(criteriumList)
        // get a mover
        val mover = Mover(options, config, logger, eventDispatcher)
        // classify all input files
        val inputFiles = collectInputFiles()
        // get the total files count
        val totalCount = inputFiles.values.map { it.size }.sum()
        if (totalCount == 0) {
            logger.fine("No input files found")
        } else {
            logger.info("Found $totalCount input files.")
        }

        var count = 0
        inputFiles.forEach { folder: File, files: Array<File> ->
            for (inputFile in files) {
                count++
                logger.info("Processing file ($count/$totalCount) '${inputFile.name}'.")
                val fileClassification = classifier.classify(inputFile, folder)

                if (fileClassification.name.isEmpty()) {
                    logger.warning("- File class not found for file '${inputFile.name}'.")
                    mover.processUnknownFile(inputFile)
                    continue
                }

                logger.info("- File class found: '${fileClassification.name}'.")

                if (fileClassification.name == "tvshows") {
                    with(fileClassification) {
                        logger.info("- TV show: '$tvShowName', season $season, episode $episode.")
                    }
                }
                // perform the actual move
                mover.moveToDestination(inputFile, fileClassification)
            }
        }
        // only write feed if new entries added
        if (feedWriter!!.hasEntries()) {
            feedWriter!!.writeFeed()
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

            feedWriter!!.addEntry(title, actionPerformed!!)
        }
    }

    /**
     * Construct a list of files in the input folders.
     */
    private fun collectInputFiles(): Map<File, Array<File>> {
        val collectedFiles = LinkedHashMap<File, Array<File>>()

        for (inputFolder in config.inputFolders) {
            val inputFilesInFolder: List<File>
            val folder = File(inputFolder)
            if (!folder.exists()) {
                logger.warning("- Input folder '$inputFolder' does not exist.")
                continue
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

    private fun removeEmptyInputSubfolders() {
        val remainingInputSubFolders = collectInputSubfolders()

        remainingInputSubFolders.forEach { folder: File, subfolders: Array<File> ->
            for (subfolder in subfolders) {
                // note that .delete() only deletes folder if empty
                if (subfolder.delete()) {
                    logger.fine("Input empty subfolder '$subfolder' deleted.")
                    continue
                }

                if (subfolder.exists() && collectFiles(subfolder).isEmpty()) {
                    logger.warning("Empty input subfolder '$subfolder' cannot be deleted.")
                }

            }
        }
    }

    /**
     * Construct a list of subfolders in the input folders.
     */
    private fun collectInputSubfolders(): Map<File, Array<File>> {
        val collectedFolders = LinkedHashMap<File, Array<File>>()

        for (inputFolder in config.inputFolders) {
            val subfoldersInFolder: List<File>
            val folder = File(inputFolder)

            if (!folder.exists()) {
                continue
            }

            subfoldersInFolder = collectFolders(folder)
            val folders = subfoldersInFolder.toTypedArray()

            collectedFolders.put(folder, folders)
        }
        return collectedFolders
    }

    /**
     * Construct a list of folders in the folder
     */
    private fun collectFolders(inputFolder: File): List<File> {
        val allFolders = mutableListOf<File>()
        val files = inputFolder.listFiles()
        if (files != null) {
            for (file in files) {
                if (!file.isDirectory) {
                    allFolders.addAll(collectFiles(file))
                } else {
                    allFolders.add(file)
                }
            }
        }
        return allFolders
    }
}
