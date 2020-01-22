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
import com.mags.librarian.options.Options
import java.io.File
import java.io.IOException
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject

class Mover
@Inject constructor(private val options: Options,
                    private val config: Config,
                    private val logger: LogWriter,
                    private val eventDispatcher: EventDispatcher) {
    private var isDuplicated = false

    // for testing purposes
    var actionPerformed = ""
        private set
    var summary: Summary? = null
        private set

    /**
     * Move a file to its destination folder depending on its classification.
     */
    fun moveToDestination(inputFile: File,
                          fileClassification: Classification) {

        actionPerformed = ""
        summary = Summary()

        // find all suitable destinations for this file
        val suitableDestinations = findSuitableDestinations(fileClassification)

        when {
            fileClassification.name == "tvshows" ->
                // if it is a tvshow, move it (special treatment)
                moveTvShowToDestination(inputFile, fileClassification, suitableDestinations)
            fileClassification.name == "music"   ->
                // if it is a music file, move it (special treatment)
                moveMusicToDestination(inputFile, fileClassification, suitableDestinations)
            else                                 ->
                // move other file
                moveRegularFileToDestination(inputFile, suitableDestinations)
        }

        if (!actionPerformed.isEmpty()) {
            // notify listener
            val eventData = FileProcessedEventData(summary!!.inputFolder,
                                                   summary!!.inputFilename,
                                                   summary!!.outputFolder,
                                                   summary!!.outputFilename,
                                                   fileClassification,
                                                   summary!!.action,
                                                   actionPerformed)
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
     */
    fun processUnknownFile(inputFile: File) {

        actionPerformed = ""
        summary = Summary()

        summary!!.inputFilename = inputFile.name
        summary!!.inputFolder = inputFile.parent

        when (config.unknownFilesAction) {
            Config.FilesAction.IGNORE -> {
                logger.warning("- File '${inputFile.name}' ignored (left in place).")
                summary!!.action = "ignore"
            }

            Config.FilesAction.DELETE -> {
                deleteFile(inputFile)
                summary!!.action = "delete"
            }

            Config.FilesAction.MOVE   -> {
                if (config.unknownFilesMovePath.isEmpty()) {
                    logger.warning("- Unknown files move path is empty, cannot move file '${inputFile.name}'.")
                    return
                }

                moveErroredFile(inputFile, File(config.unknownFilesMovePath))
                summary!!.outputFolder = config.unknownFilesMovePath
                summary!!.action = "move"
            }
        }

        // notify listeners
        eventDispatcher.fireEvent(Event.FILE_UNKNOWN,
                                  FileUnknownEventData(summary!!.inputFolder,
                                                       summary!!.inputFilename,
                                                       summary!!.outputFolder,
                                                       summary!!.outputFilename,
                                                       summary!!.action))
    }

    /**
     * Process a duplicate file.
     */
    private fun processDuplicateFile(inputFile: File) {

        actionPerformed = ""
        summary = Summary()

        summary!!.inputFilename = inputFile.name
        summary!!.inputFolder = inputFile.parent

        when (config.duplicateFilesAction) {
            Config.FilesAction.IGNORE -> {
                logger.warning("- File '${inputFile.name}' ignored (left in place).")
                summary!!.action = "ignore"
            }

            Config.FilesAction.DELETE -> {
                deleteFile(inputFile)
                summary!!.action = "delete"
            }

            Config.FilesAction.MOVE   -> {
                if (config.duplicateFilesMovePath.isEmpty()) {
                    logger.warning("- Duplicate files move path is empty, cannot move file '${inputFile.name}'.")
                    return
                }

                moveErroredFile(inputFile, File(config.duplicateFilesMovePath))
                summary!!.outputFolder = config.duplicateFilesMovePath
                summary!!.action = "move"
            }
        }

        // notify listeners
        eventDispatcher.fireEvent(Event.FILE_ERROR,
                                  FileErrorEventData(summary!!.inputFolder,
                                                     summary!!.inputFilename,
                                                     summary!!.outputFolder,
                                                     summary!!.outputFilename,
                                                     summary!!.action))
    }

    /**
     * Process an errored file.
     */
    private fun processErroredFile(inputFile: File) {

        actionPerformed = ""
        summary = Summary()

        summary!!.inputFilename = inputFile.name
        summary!!.inputFolder = inputFile.parent

        when (config.errorFilesAction) {
            Config.FilesAction.IGNORE -> {
                logger.warning("- File '${inputFile.name}' ignored (left in place).")
                summary!!.action = "ignore"
            }

            Config.FilesAction.DELETE -> {
                deleteFile(inputFile)
                summary!!.action = "delete"
            }

            Config.FilesAction.MOVE   -> {
                if (config.errorFilesMovePath.isEmpty()) {
                    logger.warning("- Error files move path is empty, cannot move file '${inputFile.name}'.")
                    return
                }

                moveErroredFile(inputFile, File(config.errorFilesMovePath))
                summary!!.outputFolder = config.errorFilesMovePath
                summary!!.action = "move"
            }
        }

        // notify listeners
        eventDispatcher.fireEvent(Event.FILE_ERROR,
                                  FileErrorEventData(summary!!.inputFolder,
                                                     summary!!.inputFilename,
                                                     summary!!.outputFolder,
                                                     summary!!.outputFilename,
                                                     summary!!.action))
    }

    /**
     * Delete a file.
     */
    private fun deleteFile(inputFile: File) {

        try {
            if ((!options.dryRun)) {
                inputFile.delete()
            }
            logger.fine("- File '${inputFile.name}' deleted.")
        } catch (e: IOException) {
            logger.warning("- Error deleting file: $e")
        }
    }

    /**
     * Move an errored file to a destination folder.
     */
    private fun moveErroredFile(inputFile: File,
                                destinationFolder: File) {

        if (!destinationFolder.exists()) {
            if ((!options.dryRun)) {
                destinationFolder.mkdirs()
            }
            logger.fine("- Created folder '${destinationFolder.absolutePath}'.")
        }

        try {
            var destination = destinationFolder.absoluteFile.resolve(inputFile.name)
            if (destination.exists()) {
                val time = Date().time.toString()
                destination = destinationFolder.resolve(inputFile.name + '-' + time)
            }

            if ((!options.dryRun)) {
                inputFile.copyTo(destination)
                inputFile.delete()
            }
            logger.fine("- File '${inputFile.name}' moved to '${destinationFolder}'.")
        } catch (e: IOException) {
            logger.severe("- Error moving file: $e")
        }

    }

    private fun replaceWordsSeparatorsInFileNameFragment(fileName: String): String {

        var existingSeparator = " "
        when {
            fileName.matches(" ".toRegex())   -> existingSeparator = " "
            fileName.matches("_".toRegex())   -> existingSeparator = "_"
            fileName.matches("\\.".toRegex()) -> existingSeparator = "."
        }

        return fileName.replace(existingSeparator, config.tvShowsWordsSeparatorFile)
    }

    private fun replaceWordsSeparatorsInFileName(fileNameWithoutExtension: String): String {

        var r = fileNameWithoutExtension
        r = r.replace(" ", config.tvShowsWordsSeparatorFile)
        r = r.replace("_", config.tvShowsWordsSeparatorFile)
        r = r.replace(".", config.tvShowsWordsSeparatorFile)
        return r
    }

    private fun replaceWordsSeparatorsInTvShowName(fileNameWithoutExtension: String): String {

        var r = fileNameWithoutExtension
        r = r.replace(" ", config.tvShowsWordsSeparatorShow)
        r = r.replace("_", config.tvShowsWordsSeparatorShow)
        r = r.replace(".", config.tvShowsWordsSeparatorShow)
        return r
    }

    /**
     * Find all the possible destinations for a file.
     */
    private fun findSuitableDestinations(fileClassification: Classification): MutableList<Map<String, String>> {

        val destinations = mutableListOf<Map<String, String>>()

        config.outputFolders.forEach { outputFolder ->
            if (outputFolder["contents"] == fileClassification.name) {
                logger.fine("- Suitable destination folder found: '${outputFolder["path"]}'.")

                // make folder path absolute
                val absoluteOutputFolder = outputFolder.toMutableMap()
                absoluteOutputFolder["path"] = File(absoluteOutputFolder["path"]).absolutePath.toString()
                destinations.add(absoluteOutputFolder.toMap())
            }
        }

        return destinations
    }

    /**
     * Move a file to the first of the suitable destinations.
     */
    private fun moveRegularFileToDestination(inputFile: File,
                                             suitableDestinations: MutableList<Map<String, String>>) {

        if (suitableDestinations.isEmpty()) {
            logger.warning("- No suitable destination found, skipping.")
            return
        }

        // use the first suitable destination
        val destinationFolder = File(suitableDestinations[0]["path"].toString())

        moveTheFile(inputFile, destinationFolder)
    }

    /**
     * Moves a music file to one of the suitable destinations.
     */
    private fun moveMusicToDestination(inputFile: File,
                                       fileClassification: Classification,
                                       suitableDestinations: MutableList<Map<String, String>>) {

        if (fileClassification.albumName.isEmpty()) {
            // no album, it is just a regular move
            moveRegularFileToDestination(inputFile, suitableDestinations)
            return
        }

        // use the first suitable destination, adding the album as subfolder
        val albumFolder = File(suitableDestinations[0]["path"]).resolve(fileClassification.albumName)

        moveTheFile(inputFile, albumFolder)
    }

    /**
     * Moves a TV show file to one of the suitable destinations.
     */
    private fun moveTvShowToDestination(inputFile: File,
                                        fileClassification: Classification,
                                        suitableDestinations: List<Map<String, String>>) {

        // find the parent destination folder for that TV show
        var parentDestinationFolder = findParentDestinationFolder(inputFile, fileClassification,
                                                                  suitableDestinations)

        // if no suitable destination found, use the last one as autocreate
        if (parentDestinationFolder == null) {
            val path = suitableDestinations[suitableDestinations.size - 1]["path"].toString()
            parentDestinationFolder = File(path)

            logger.fine("- No suitable destination folder found, using '$path' as default.")
        }

        // apply season and numbering schemas
        val seasonName = applySeasonSchema(fileClassification)
        val tvShowFileName = applyTvShowNumberingSchema(fileClassification)

        // replace separators in TV show name
        fileClassification.tvShowName = replaceWordsSeparatorsInTvShowName(fileClassification.tvShowName)

        // ensure we have a valid folder name for the tv show, wheather preexisting or new
        if (fileClassification.tvShowFolderName.isEmpty()) {
            fileClassification.tvShowFolderName = fileClassification.tvShowName
        }

        // the real destination folder is a subfolder of the parent found
        val tvShowDestinationFolder = parentDestinationFolder.resolve(fileClassification.tvShowFolderName).resolve(
                seasonName)

        if (!tvShowDestinationFolder.exists()) {
            if ((!options.dryRun)) {
                tvShowDestinationFolder.mkdirs()
            }
            logger.fine("- Created folder for TV show/season: '${tvShowDestinationFolder.absolutePath}'.")
        } else {
            logger.fine("- Using existing folder for TV show/season: '${tvShowDestinationFolder.absolutePath}'.")
        }

        // move the file
        moveTheFile(inputFile, tvShowDestinationFolder, tvShowFileName)
    }

    /**
     * Find the parent folder of an existing TV show folder.
     */
    private fun findParentDestinationFolder(inputFile: File,
                                            fileClassification: Classification,
                                            suitableDestinations: List<Map<String, String>>): File? {

        logger.fine("- Find suitable parent destination folder for file \"${inputFile.name}\"")

        // NOTE: using a final array[1] because of lambda usage limitations
        val parentDestinationFolder = arrayOfNulls<File>(1)

        suitableDestinations.forEach { destination ->
            val destinationFolder = File(destination["path"].toString())

            logger.fine("- Checking candidate parent destination folder \"$destinationFolder\"")

            // try finding an existing subfolder for the TV show
            val tvShowSubfolders = destinationFolder.list { _, name ->

                // for tvshows output folders, only accept it as a destination if
                // a folder for the TV show already exists
                if (FileMatcher.matchTVShowName(name, fileClassification.tvShowName)) {

                    // save real folder name to avoid creating extra folders on case or separators change
                    fileClassification.tvShowFolderName = name

                    logger.finer("- Matched folder name \"$name\"")
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
            logger.fine("- Using parent destination folder \"$parentDestinationFolder[0]\"")
            return parentDestinationFolder[0]
        }

        logger.fine("- Parent destination folder not found")
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

        val newName = mutableListOf<String>().apply {
            add(replaceWordsSeparatorsInFileNameFragment(classification.tvShowName))
            add(config.tvShowsWordsSeparatorFile)
            add(seasonAndEpisode)
            if (!classification.tvShowRest.isEmpty()) {
                add(config.tvShowsWordsSeparatorFile)
                add(replaceWordsSeparatorsInFileNameFragment(classification.tvShowRest))
            }
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

        val regExp = Pattern.compile(String.format("(?<pre>)(?<tag>\\{%s(?::(?<number>[0-9]+))\\})(?<post>)",
                                                   tag))
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
        var theNewName = newName

        try {
            if (!destinationFolder.exists()) {
                if ((!options.dryRun)) {
                    destinationFolder.mkdirs()
                    logger.fine("- Created destination folder '${destinationFolder.absolutePath}'.")
                }
            }

            if (theNewName.isEmpty()) {
                theNewName = inputFile.name
            }

            // create summary
            summary!!.inputFolder = inputFile.parent
            summary!!.inputFilename = inputFile.name
            summary!!.outputFolder = destinationFolder.toString()
            summary!!.outputFilename = theNewName

            if (options.copyOnly) {
                if ((!options.dryRun)) {
                    inputFile.copyTo(destinationFolder.resolve(theNewName))
                }
                actionPerformed = "copied [${inputFile.absolutePath}] to [${destinationFolder.absolutePath}] as [$theNewName]"
                logger.info("- File '${destinationFolder.absolutePath}' copied to '${inputFile.name}' as '$theNewName'.")
                summary!!.action = "copy"

            } else {
                if ((!options.dryRun)) {
                    Files.move(inputFile.toPath(), destinationFolder.resolve(theNewName).toPath())
                }
                actionPerformed = "moved [${inputFile.absolutePath}] to [${destinationFolder.absolutePath}] as [$theNewName]"
                logger.info("- File '${inputFile.name}' moved to '${destinationFolder.absolutePath}' as '$theNewName'.")
                summary!!.action = "move"
            }

        } catch (e: FileAlreadyExistsException) {

            isDuplicated = true

            var action = if (options.copyOnly) "copy" else "move"
            var msg = "- Cannot $action already existing file '${inputFile.name}' to '${destinationFolder.absolutePath}'"
            logger.severe(msg)
        } catch (e: IOException) {

            var action = if (options.copyOnly) "copy" else "move"
            var msg = "- Cannot $action file '${inputFile.name}' to '${destinationFolder.absolutePath}': $e"
            logger.severe(msg)
        }

    }

    data class Summary(var inputFolder: String = "",
                       var inputFilename: String = "",
                       var outputFolder: String = "",
                       var outputFilename: String = "",
                       var action: String = "")
}

