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
import com.mags.librarian.config.Config
import com.mags.librarian.event.EventDispatcher
import com.mags.librarian.options.Options
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.util.*

class MoverTest {
    @BeforeEach
    fun setUp() {
        myEventDispatcher = getEventDispatcher()
        myMover = getMover()
    }

    @AfterEach
    fun tearDown() {
        logWriter!!.close()
    }

    @Test
    fun moveVideo() {
        val classification = Classification()
        classification.name = "videos"
        myMover!!.moveToDestination(File("/input/my_movie.avi"), classification)

        assertEquals("moved [/input/my_movie.avi] to [/output/videos] as [my_movie.avi]",
                     myMover!!.actionPerformed)
    }

    @Test
    fun moveTvShow() {
        val classification = Classification()
        classification.fileName = "My_Tvshow_S02E10_some_data.avi"
        classification.baseName = "My_Tvshow_S02E10_some_data"
        classification.extension = "avi"
        classification.name = "tvshows"
        classification.season = 2
        classification.episode = 10
        classification.tvShowName = "My_Tvshow"
        classification.tvShowRest = "some_data"

        myMover!!.moveToDestination(File("/input/" + classification.fileName), classification)

        assertEquals("moved [/input/My_Tvshow_S02E10_some_data.avi] to [/output/tvshows/My_Tvshow/The.season.002] as [My_Tvshow_S=2E=010_some_data.avi]",
                     myMover!!.actionPerformed)
    }

    @Test
    fun doesNotMoveTvShowIntoDifferentShowFolderWithSamePrefix() {
        val outputFolder = File.createTempFile("librarian-mover-test", "")
        outputFolder.delete()
        outputFolder.mkdirs()

        try {
            // an existing folder for a different TV show with a matching prefix
            File(outputFolder, "House of cards").mkdirs()

            val options = Options()
            options.dryRun = true

            val config = Config()
            config.outputFolders = arrayOf(mapOf("path" to outputFolder.absolutePath,
                                                 "contents" to "tvshows"))

            val logWriter = LogWriter(java.util.logging.Logger.getLogger("librarian-mover-test"))
            logWriter.logFileName = File(outputFolder, "librarian.log").absolutePath

            val mover = Mover(options, config, logWriter, EventDispatcher())

            val classification = Classification()
            classification.fileName = "House.S01E02.avi"
            classification.baseName = "House.S01E02"
            classification.extension = "avi"
            classification.name = "tvshows"
            classification.season = 1
            classification.episode = 2
            classification.tvShowName = "House"

            mover.moveToDestination(File("/input/House.S01E02.avi"), classification)

            assertEquals(File(outputFolder, "House/Season_01").toString(),
                         mover.summary!!.outputFolder)

            logWriter.close()
        } finally {
            outputFolder.deleteRecursively()
        }
    }

    companion object {
        private var myMover: Mover? = null
        private var logWriter: LogWriter? = null
        private var myEventDispatcher: EventDispatcher? = null

        @AfterAll

        fun tearDownAfterClass() {

        }

        private fun getMover(): Mover {
            val options = Options()
            options.dryRun = true
            val config = setUpConfig()

            logWriter = LogWriter(java.util.logging.Logger.getLogger(this::class.java.name))
            logWriter!!.logFileName = System.getProperty("java.io.tmpdir") + "/librarian.log"
            return Mover(options, config, logWriter!!, myEventDispatcher!!)
        }

        private fun getEventDispatcher(): EventDispatcher {
            return EventDispatcher()
        }

        private fun setUpConfig(): Config {
            val config = Config()
            /*
         * Output folders
         */
            val outputFolders = ArrayList<Map<String, String>>()

            val outputFoldersItems0 = LinkedHashMap<String, String>()
            outputFoldersItems0.put("path", "/output/tvshows")
            outputFoldersItems0.put("contents", "tvshows")
            outputFolders.add(outputFoldersItems0)

            val outputFoldersItems1 = LinkedHashMap<String, String>()
            outputFoldersItems1.put("path", "/output/music")
            outputFoldersItems1.put("contents", "music")
            outputFolders.add(outputFoldersItems1)

            val outputFoldersItems2 = LinkedHashMap<String, String>()
            outputFoldersItems2.put("path", "/output/videos")
            outputFoldersItems2.put("contents", "videos")
            outputFolders.add(outputFoldersItems2)
            config.outputFolders = outputFolders.toTypedArray<Map<String, String>>()
            /*
         * Schemas
         */
            config.tvShowsSeasonSchema = "The.season.{season:3}"
            config.tvShowsNumberingSchema = "S={season:1}E={episode:3}"

            return config
        }
    }

}