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
    @Throws(Exception::class)
    fun setUp() {
        myEventDispatcher = getEventDispatcher()
        myMover = getMover()
    }

    @AfterEach
    @Throws(Exception::class)
    fun tearDown() {
        logger!!.close()
    }

    @Test
    @Throws(Exception::class)
    fun moveVideo() {
        val classification = Classification()
        classification.name = "videos"
        myMover!!.moveToDestination(File("/input/my_movie.avi"), classification)

        assertEquals("moved [/input/my_movie.avi] to [/output/videos] as [my_movie.avi]",
                     myMover!!.actionPerformed)
    }

    @Test
    @Throws(Exception::class)
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

    companion object {
        private var myMover: Mover? = null
        private var logger: Log? = null
        private var myEventDispatcher: EventDispatcher? = null

        @AfterAll
        @Throws(Exception::class)
        fun tearDownAfterClass() {

        }

        private fun getMover(): Mover {
            val options = Options()
            options.dryRun = true
            val config = setUpConfig()

            logger = Log(System.getProperty("java.io.tmpdir") + "/librarian.log")
            return Mover(options, config, logger!!, myEventDispatcher!!)
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