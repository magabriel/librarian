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
import com.mags.librarian.classifier.FileMatcher
import com.mags.librarian.config.Config
import com.mags.librarian.event.*
import java.io.File
import java.io.IOException
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.regex.Pattern

internal class Mover(private val options: Options,
                     private val config: Config,
                     private val logger: Log,
                     private val eventDispatcher: EventDispatcher) {
    private var isDuplicated = false

    // for testing purposes
    var actionPerformed = ""
        private set
    var summary: Summary? = null
        private set

    /**
     * Move a file to its destination folder depending on its classification.
     *
     * @param inputFile          The input file
     * @param fileClassification The file classification
     */
    fun moveToDestination(inputFile: File,
                          fileClassification: Classification) {

        actionPerformed = ""
        summary = Summary()

        // find all suitable destinations for this file
        val suitableDestinations = findSuitableDestinations(fileClassification)

        if (fileClassification.name == "tvshows") {
            // if it is a tvshow, move it (special treatment)
            moveTvShowToDestination(inputFile, fileClassification, suitableDestinations)

        } else if (fileClassification.name == "music") {
            // if it is a music file, move it (special treatment)
            moveMusicToDestination(inputFile, fileClassification, suitableDestinations)

        } else {
            // move other file
            moveRegularFileToDestination(inputFile, suitableDestinations)
        }

        if (!actionPerformed.isEmpty()) {
            // notify listener
            val eventData = FileProcessedEventData()
            eventData.inputFolder = summary!!.inputFolder
            eventData.inputFilename = summary!!.inputFilename
            eventData.outputFolder = summary!!.outputFolder
            eventData.outputFilename = summary!!.outputFilename
            eventData.fileClassification = fileClassification
            eventData.action = summary!!.action
            eventData.actionPerformed = actionPerformed
            eventDispatcher.fireEvent(Event.FILE_PROCESSED, eventData)
            return
        }

        if (isDuplicated) {
            processDuplicateFile(inputFile)
            return
        }

        processErroredFile(inputFile)
    }

    /**
     * Process an unknown file.
     *
     * @param inputFile The file to process.
     */
    fun processUnknownFile(inputFile: File) {

        actionPerformed = ""
        summary = Summary()

        summary!!.inputFilename = inputFile.name
        summary!!.inputFolder = inputFile.parent

        when (config.unknownFilesAction) {
            Config.FilesAction.IGNORE -> {
                logger.logger.warning(
                        String.format("- File '%s' ignored (left in place).", inputFile.name))
                summary!!.action = "ignore"
            }

            Config.FilesAction.DELETE -> {
                deleteFile(inputFile)
                summary!!.action = "delete"
            }

            Config.FilesAction.MOVE   -> {
                if (config.unknownFilesMovePath.isEmpty()) {
                    logger.logger.warning(String.format(
                            "- Unknown files move path is empty, cannot move file '%s'.",
                            inputFile.name))
                    return
                }

                moveFile(inputFile, File(config.unknownFilesMovePath), true)
                summary!!.outputFolder = config.unknownFilesMovePath
                summary!!.action = "move"
            }
        }

        // notify listeners
        val eventData = FileUnknownEventData()
        eventData.inputFolder = summary!!.inputFolder
        eventData.inputFilename = summary!!.inputFilename
        eventData.outputFolder = summary!!.outputFolder
        eventData.outputFilename = summary!!.outputFilename
        eventData.action = summary!!.action
        eventDispatcher.fireEvent(Event.FILE_UNKNOWN, eventData)
    }

    /**
     * Process a duplicate file.
     *
     * @param inputFile The file.
     */
    private fun processDuplicateFile(inputFile: File) {

        actionPerformed = ""
        summary = Summary()

        summary!!.inputFilename = inputFile.name
        summary!!.inputFolder = inputFile.parent

        when (config.duplicateFilesAction) {
            Config.FilesAction.IGNORE -> {
                logger.logger.warning(
                        String.format("- File '%s' ignored (left in place).", inputFile.name))
                summary!!.action = "ignore"
            }

            Config.FilesAction.DELETE -> {
                deleteFile(inputFile)
                summary!!.action = "delete"
            }

            Config.FilesAction.MOVE   -> {
                if (config.duplicateFilesMovePath.isEmpty()) {
                    logger.logger.warning(String.format(
                            "- Duplicate files move path is empty, cannot move file '%s'.",
                            inputFile.name))
                    return
                }

                moveFile(inputFile, File(config.duplicateFilesMovePath), true)
                summary!!.outputFolder = config.duplicateFilesMovePath
                summary!!.action = "move"
            }
        }

        // notify listeners
        val eventData = FileErrorEventData()
        eventData.inputFolder = summary!!.inputFolder
        eventData.inputFilename = summary!!.inputFilename
        eventData.outputFolder = summary!!.outputFolder
        eventData.outputFilename = summary!!.outputFilename
        eventData.action = summary!!.action
        eventDispatcher.fireEvent(Event.FILE_ERROR, eventData)
    }

    /**
     * Process an errored file.
     *
     * @param inputFile The file.
     */
    private fun processErroredFile(inputFile: File) {

        actionPerformed = ""
        summary = Summary()

        summary!!.inputFilename = inputFile.name
        summary!!.inputFolder = inputFile.parent

        when (config.errorFilesAction) {
            Config.FilesAction.IGNORE -> {
                logger.logger.warning(
                        String.format("- File '%s' ignored (left in place).", inputFile.name))
                summary!!.action = "ignore"
            }

            Config.FilesAction.DELETE -> {
                deleteFile(inputFile)
                summary!!.action = "delete"
            }

            Config.FilesAction.MOVE   -> {
                if (config.errorFilesMovePath.isEmpty()) {
                    logger.logger.warning(String.format(
                            "- Error files move path is empty, cannot move file '%s'.",
                            inputFile.name))
                    return
                }

                moveFile(inputFile, File(config.errorFilesMovePath), true)
                summary!!.outputFolder = config.errorFilesMovePath
                summary!!.action = "move"
            }
        }

        // notify listeners
        val eventData = FileUnknownEventData()
        eventData.inputFolder = summary!!.inputFolder
        eventData.inputFilename = summary!!.inputFilename
        eventData.outputFolder = summary!!.outputFolder
        eventData.outputFilename = summary!!.outputFilename
        eventData.action = summary!!.action
        eventDispatcher.fireEvent(Event.FILE_ERROR, eventData)
    }

    /**
     * Delete a file.
     *
     * @param inputFile The file to delete.
     */
    private fun deleteFile(inputFile: File) {

        try {
            if ((!options.dryRun!!)) {
                Files.delete(inputFile.toPath())
            }
            logger.logger.fine(String.format("- File '%s' deleted.", inputFile.name))
        } catch (e: IOException) {
            logger.logger.warning(String.format("- Error deleting file: %s", e))
        }

    }

    /**
     * Move a file to a destination folder.
     *
     * @param inputFile         The file to move
     * @param destinationFolder The folder where to move it
     */
    private fun moveFile(inputFile: File,
                         destinationFolder: File,
                         autoRenameIfExisting: Boolean) {

        if (!destinationFolder.exists()) {
            if ((!options.dryRun!!)) {
                destinationFolder.mkdirs()
            }
            logger.logger.fine(
                    String.format("- Created folder '%s'.", destinationFolder.absolutePath))
        }

        try {
            var destination = destinationFolder.toPath().resolve(inputFile.name)
            if (Files.exists(destination)) {
                val time = Date().time.toString()
                destination = destinationFolder.toPath().resolve(inputFile.name + '-' + time)
            }

            if ((!options.dryRun!!)) {
                Files.move(inputFile.toPath(), destination)
            }
            logger.logger.fine(
                    String.format("- File '%s' moved to '%s'.", inputFile.name, destinationFolder))
        } catch (e: IOException) {
            logger.logger.severe(String.format("- Error moving file: %s", e))
        }

    }

    private fun replaceWordsSeparatorsInFileNameFragment(fileName: String): String {

        var existingSeparator = " "
        if (fileName.matches(" ".toRegex())) {
            existingSeparator = " "
        } else if (fileName.matches("_".toRegex())) {
            existingSeparator = "_"
        } else if (fileName.matches("\\.".toRegex())) {
            existingSeparator = "."
        }

        return fileName.replace(existingSeparator, config.tvShowsWordsSeparatorFile)
    }

    private fun replaceWordsSeparatorsInFileName(fileNameWithoutExtension: String): String {

        return fileNameWithoutExtension.replace(" ", config.tvShowsWordsSeparatorFile).replace("_",
                                                                                               config.tvShowsWordsSeparatorFile).replace(
                ".", config.tvShowsWordsSeparatorFile)
    }

    private fun replaceWordsSeparatorsInTvShowName(fileNameWithoutExtension: String): String {

        return fileNameWithoutExtension.replace(" ", config.tvShowsWordsSeparatorShow).replace("_",
                                                                                               config.tvShowsWordsSeparatorShow).replace(
                ".", config.tvShowsWordsSeparatorShow)
    }

    /**
     * Find all the possible destinations for a file.
     *
     * @param fileClassification The file classification
     * @return A list of destinations
     */
    private fun findSuitableDestinations(fileClassification: Classification): ArrayList<Map<*, *>> {

        val destinations = ArrayList<Map<*, *>>()

        for (outputFolder in config.outputFolders) {
            if (outputFolder["contents"] == fileClassification.name) {
                logger.logger.fine(String.format("- Suitable destination folder found: '%s'.",
                                                 outputFolder["path"]))

                destinations.add(outputFolder)
            }
        }

        return destinations
    }

    /**
     * Move a file to the first of the suitable destinations.
     *
     * @param inputFile
     * @param suitableDestinations
     */
    private fun moveRegularFileToDestination(inputFile: File,
                                             suitableDestinations: ArrayList<Map<*, *>>) {

        if (suitableDestinations.isEmpty()) {
            logger.logger.warning("- No suitable destination found, skipping.")
            return
        }

        // use the first suitable destination
        val destinationFolder = File(suitableDestinations[0]["path"].toString())

        moveTheFile(inputFile, destinationFolder)
    }

    /**
     * Moves a music file to one of the suitable destinations.
     *
     * @param inputFile            The input file
     * @param fileClassification   The file classification
     * @param suitableDestinations List of suitable destinations
     */
    private fun moveMusicToDestination(inputFile: File,
                                       fileClassification: Classification,
                                       suitableDestinations: ArrayList<Map<*, *>>) {

        if (fileClassification.albumName.isEmpty()) {
            // no album, it is just a regular move
            moveRegularFileToDestination(inputFile, suitableDestinations)
            return
        }

        // use the first suitable destination, adding the album as subfolder
        val albumFolder = Paths.get(suitableDestinations[0]["path"].toString(),
                                    fileClassification.albumName).toFile()

        moveTheFile(inputFile, albumFolder)
    }

    /**
     * Moves a TV show file to one of the suitable destinations.
     *
     * @param inputFile            The input file
     * @param fileClassification   The file classification
     * @param suitableDestinations List of suitable destinations
     */
    private fun moveTvShowToDestination(inputFile: File,
                                        fileClassification: Classification,
                                        suitableDestinations: ArrayList<Map<*, *>>) {

        // find the parent destination folder for that TV show
        var parentDestinationFolder = findParentDestinationFolder(inputFile, fileClassification,
                                                                  suitableDestinations)

        // if no suitable destination found, use the last one as autocreate
        if (parentDestinationFolder == null) {
            val path = suitableDestinations[suitableDestinations.size - 1]["path"].toString()
            parentDestinationFolder = File(path)

            logger.logger.fine(
                    String.format("- No suitable destination folder found, using '%s' as default.",
                                  path))
        }

        // apply season and numbering schemas
        val seasonName = applySeasonSchema(fileClassification)
        val tvShowFileName = applyTvShowNumberingSchema(fileClassification)

        // replace separators in TV show name
        fileClassification.tvShowName = replaceWordsSeparatorsInTvShowName(
                fileClassification.tvShowName)

        // ensure we have a valid folder name for the tv show, wheather preexisting or new
        if (fileClassification.tvShowFolderName.isEmpty()) {
            fileClassification.tvShowFolderName = fileClassification.tvShowName
        }

        // the real destination folder is a subfolder of the parent found
        val tvShowDestinationFolder = Paths.get(parentDestinationFolder.absolutePath,
                                                fileClassification.tvShowFolderName,
                                                seasonName).toFile()

        if (!tvShowDestinationFolder.exists()) {
            if ((!options.dryRun!!)) {
                tvShowDestinationFolder.mkdirs()
            }
            logger.logger.fine(String.format("- Created folder for TV show/season: '%s'.",
                                             tvShowDestinationFolder.absolutePath))
        } else {
            logger.logger.fine(String.format("- Using existing folder for TV show/season: '%s'.",
                                             tvShowDestinationFolder.absolutePath))
        }

        // move the file
        moveTheFile(inputFile, tvShowDestinationFolder, tvShowFileName)
    }

    /**
     * Find the parent folder of an existing TV show folder.
     *
     * @param inputFile            The input file
     * @param fileClassification   The file classification
     * @param suitableDestinations List of suitable destinations
     * @return The destination folder
     */
    private fun findParentDestinationFolder(inputFile: File,
                                            fileClassification: Classification,
                                            suitableDestinations: ArrayList<Map<*, *>>): File? {

        logger.logger.fine(
                String.format("- Find suitable parent destination folder for file \"%s\"",
                              inputFile.name))

        // NOTE: using a final array[1] because of lambda usage limitations
        val parentDestinationFolder = arrayOfNulls<File>(1)

        suitableDestinations.forEach { destination ->
            val destinationFolder = File(destination["path"].toString())

            logger.logger.fine(
                    String.format("- Checking candidate parent destination folder \"%s\"",
                                  destinationFolder))

            // try finding an existing subfolder for the TV show
            val tvShowSubfolders = destinationFolder.list { dir, name ->

                // for tvshows output folders, only accept it as a destination if
                // a folder for the TV show already exists
                if (FileMatcher.matchTVShowName(name, fileClassification.tvShowName)) {

                    // save real folder name to avoid creating extra folders on case or separators change
                    fileClassification.tvShowFolderName = name

                    logger.logger.finer(String.format("- Matched folder name \"%s\"", name))
                    return@list true
                }

                false
            }

            if (tvShowSubfolders != null && tvShowSubfolders.size > 0) {
                // get the first one as new destination
                parentDestinationFolder[0] = destinationFolder
            }
        }

        if (parentDestinationFolder[0] != null) {
            logger.logger.fine(String.format("- Using parent destination folder \"%s\"",
                                             parentDestinationFolder[0]))
            return parentDestinationFolder[0]
        }

        logger.logger.fine("- Parent destination folder not found")
        return null
    }

    private fun applySeasonSchema(classification: Classification): String {

        // default season name
        var seasonName = String.format(config.tvShowsSeasonSchema, classification.season)

        seasonName = replaceTag(seasonName, "season", classification.season)

        return seasonName
    }

    private fun applyTvShowNumberingSchema(classification: Classification): String {

        // default
        var seasonAndEpisode = config.tvShowsNumberingSchema

        seasonAndEpisode = replaceTag(seasonAndEpisode, "season", classification.season)
        seasonAndEpisode = replaceTag(seasonAndEpisode, "episode", classification.episode)

        val newName = ArrayList<String>()
        newName.add(replaceWordsSeparatorsInFileNameFragment(classification.tvShowName))
        newName.add(config.tvShowsWordsSeparatorFile)
        newName.add(seasonAndEpisode)

        if (!classification.tvShowRest.isEmpty()) {
            newName.add(config.tvShowsWordsSeparatorFile)
            newName.add(replaceWordsSeparatorsInFileNameFragment(classification.tvShowRest))
        }

        val baseName = newName.joinToString("")

        return replaceWordsSeparatorsInFileName(baseName) + "." + classification.extension
    }

    /**
     * Replaces a tag ("{tagname:length}") by a numeric value.
     *
     * @param inputString
     * @param tag
     * @param value
     * @return
     */
    private fun replaceTag(inputString: String,
                           tag: String,
                           value: Int?): String {

        var replacedString = inputString

        val regExp = Pattern.compile(
                String.format("(?<pre>)(?<tag>\\{%s(?::(?<number>[0-9]+))\\})(?<post>)", tag))
        val matcher = regExp.matcher(inputString)
        if (matcher.find()) {
            val number = Integer.parseInt(matcher.group("number"))

            val formatted = String.format("%0" + number + "d", value)
            replacedString = matcher.replaceAll("\${pre}$formatted\${post}")

        }

        return replacedString
    }

    /**
     * Do the actual file move.
     *
     * @param inputFile         The input file
     * @param destinationFolder The destination folder
     * @param newName           The name to give to the copied/moved file
     */
    private fun moveTheFile(inputFile: File,
                            destinationFolder: File,
                            newName: String = "") {
        var newName = newName

        try {
            if (!destinationFolder.exists()) {
                if ((!options.dryRun!!)) {
                    destinationFolder.mkdirs()
                    logger.logger.fine(String.format("- Created destination folder '%s'.",
                                                     destinationFolder.absolutePath))
                }
            }

            if (newName.isEmpty()) {
                newName = inputFile.name
            }

            // create summary
            summary!!.inputFolder = inputFile.parent
            summary!!.inputFilename = inputFile.name
            summary!!.outputFolder = destinationFolder.toString()
            summary!!.outputFilename = newName

            if (options.copyOnly!!) {
                if ((!options.dryRun!!)) {
                    Files.copy(inputFile.toPath(), destinationFolder.toPath().resolve(newName))
                }
                actionPerformed = String.format("copied [%s] to [%s] as [%s]",
                                                inputFile.absolutePath,
                                                destinationFolder.absolutePath, newName)
                logger.logger.info(
                        String.format("- File '%s' copied to '%s' as '%s'.", inputFile.name,
                                      destinationFolder.absolutePath, newName))
                summary!!.action = "copy"

            } else {
                if ((!options.dryRun!!)) {
                    Files.move(inputFile.toPath(), destinationFolder.toPath().resolve(newName))
                }
                actionPerformed = String.format("moved [%s] to [%s] as [%s]",
                                                inputFile.absolutePath,
                                                destinationFolder.absolutePath, newName)
                logger.logger.info(
                        String.format("- File '%s' moved to '%s' as '%s'.", inputFile.name,
                                      destinationFolder.absolutePath, newName))
                summary!!.action = "move"
            }

        } catch (e: FileAlreadyExistsException) {

            isDuplicated = true

            var msg = "- Cannot move already existing file '%s' to '%s': %s"
            if (options.copyOnly!!) {
                msg = "- Cannot copy already existing file '%s' to '%s': %s"
            }
            logger.logger.severe(String.format(msg, inputFile.name, destinationFolder.absolutePath,
                                               e.toString()))
        } catch (e: IOException) {

            var msg = "- Cannot move file '%s' to '%s': %s"
            if (options.copyOnly!!) {
                msg = "- Cannot copy file '%s' to '%s': %s"
            }
            logger.logger.severe(String.format(msg, inputFile.name, destinationFolder.absolutePath,
                                               e.toString()))
        }

    }

    internal inner class Summary {

        var inputFolder = ""
        var inputFilename = ""
        var outputFolder = ""
        var outputFilename = ""
        var action = ""
    }
}
/**
 * Do the actual file move.
 *
 * @param inputFile
 * @param destinationFolder
 */
