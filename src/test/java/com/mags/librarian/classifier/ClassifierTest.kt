/*
 * This file is part of the librarian application.
 *
 * Copyright (c) Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package com.mags.librarian.classifier

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.BeforeClass
import org.junit.Test
import java.io.File

class ClassifierTest {
    @Test
    @Throws(Exception::class)
    fun classifyVideos() {
        val expected = Classification()
        expected.name = "videos"
        expected.fileName = "test1.avi"
        expected.baseName = "test1"
        expected.extension = "avi"
        assertEquals(expected, classifier.classify(File("test1.avi"), File("/music")))

        expected.fileName = "test2.mkv"
        expected.baseName = "test2"
        expected.extension = "mkv"
        assertEquals(expected, classifier.classify(File("test2.mkv"), File("/music")))

        expected.fileName = "test3.mp3"
        expected.baseName = "test3"
        expected.extension = "mp3"
        assertNotEquals(expected, classifier.classify(File("test3.mp3"), File("/music")))
    }

    @Test
    @Throws(Exception::class)
    fun classifyTVShow_nXnn() {
        val expected = Classification()
        expected.fileName = "A_TV_show_2x10_something.avi"
        expected.baseName = "A_TV_show_2x10_something"
        expected.extension = "avi"
        expected.name = "tvshows"
        expected.season = 2
        expected.episode = 10
        expected.tvShowRest = "something"
        expected.tvShowName = "A TV show"

        assertEquals(expected, classifier.classify(File(expected.fileName), File("/input1")))
    }

    @Test
    @Throws(Exception::class)
    fun classifyTVShow_SnnEnn() {
        val expected = Classification()
        expected.fileName = "A_TV_show_S02E10_something.avi"
        expected.baseName = "A_TV_show_S02E10_something"
        expected.extension = "avi"
        expected.name = "tvshows"
        expected.season = 2
        expected.episode = 10
        expected.tvShowRest = "something"
        expected.tvShowName = "A TV show"

        assertEquals(expected, classifier.classify(File(expected.fileName), File("/input1")))
    }

    @Test
    @Throws(Exception::class)
    fun classifyTVShow_SnnEnn_Dots() {
        val expected = Classification()
        expected.fileName = "A.TV.show.S02E10.something.avi"
        expected.baseName = "A.TV.show.S02E10.something"
        expected.extension = "avi"
        expected.name = "tvshows"
        expected.season = 2
        expected.episode = 10
        expected.tvShowRest = "something"
        expected.tvShowName = "A TV show"

        assertEquals(expected, classifier.classify(File(expected.fileName), File("/input1")))
    }

    @Test
    @Throws(Exception::class)
    fun classifyTVShow_WithSpacesAndDashes() {
        val expected = Classification()
        expected.fileName = "A TV show - 2x10 something else.avi"
        expected.baseName = "A TV show - 2x10 something else"
        expected.extension = "avi"
        expected.name = "tvshows"
        expected.season = 2
        expected.episode = 10
        expected.tvShowRest = "something else"
        expected.tvShowName = "A TV show"

        assertEquals("TV show nXnn", expected,
                     classifier.classify(File(expected.fileName), File("/input1")))
    }

    @Test
    @Throws(Exception::class)
    fun classifyTVShow_SEE_withoutRest() {
        val expected = Classification()
        expected.fileName = "A TV show 123.avi"
        expected.baseName = "A TV show 123"
        expected.extension = "avi"
        expected.name = "tvshows"
        expected.season = 1
        expected.episode = 23
        expected.tvShowRest = ""
        expected.tvShowName = "A TV show"

        assertEquals("TV show SEE", expected,
                     classifier.classify(File(expected.fileName), File("/input1")))
    }

    @Test
    @Throws(Exception::class)
    fun classifyTVShow_SEE_withRest() {
        val expected = Classification()
        expected.fileName = "A TV show 123 something.avi"
        expected.baseName = "A TV show 123 something"
        expected.extension = "avi"
        expected.name = "tvshows"
        expected.season = 1
        expected.episode = 23
        expected.tvShowRest = "something"
        expected.tvShowName = "A TV show"

        assertEquals("TV show SEE with rest", expected,
                     classifier.classify(File(expected.fileName), File("/input1")))
    }

    @Test
    @Throws(Exception::class)
    fun classifyMovie_WithYear() {
        val expected = Classification()
        expected.fileName = "A movie (1945) something.avi"
        expected.baseName = "A movie (1945) something"
        expected.extension = "avi"
        expected.name = "videos"

        assertEquals("Movie with year", expected,
                     classifier.classify(File(expected.fileName), File("/input1")))
    }

    @Test
    @Throws(Exception::class)
    fun classifyMovie_WithYear_WithoutParenthesis() {
        val expected = Classification()
        expected.fileName = "A movie 1945 something.avi"
        expected.baseName = "A movie 1945 something"
        expected.extension = "avi"
        expected.name = "videos"

        assertEquals("Movie with year without parenthesis", expected,
                     classifier.classify(File(expected.fileName), File("/input1")))
    }

    @Test
    @Throws(Exception::class)
    fun classifyMovie_WithYear_WithoutParenthesis_WithoutRest() {
        val expected = Classification()
        expected.fileName = "A movie 1945.avi"
        expected.baseName = "A movie 1945"
        expected.extension = "avi"
        expected.name = "videos"

        assertEquals("Movie with year without parenthesis without rest", expected,
                     classifier.classify(File(expected.fileName), File("/input1")))
    }

    companion object {
        internal var classifier = Classifier()

        @BeforeClass
        @Throws(Exception::class)
        @JvmStatic
        fun setUpBeforeClass() {
            classifier = Classifier()
            classifier.addCriterium("videos", arrayOf("avi", "mkv"), arrayOf())
            classifier.addCriterium("music", arrayOf("mp3", "ogg"), arrayOf())
            // SssEee
            classifier.addCriterium("tvshows", arrayOf("avi", "mkv"),
                                    arrayOf("(?<name>.+)S(?<season>[0-9]{1,3})E(?<episode>[0-9]{1,3})(?<rest>.*)"))
            // ssxee
            classifier.addCriterium("tvshows", arrayOf("avi", "mkv"),
                                    arrayOf("(?<name>.+)(?<season>[0-9]{1,2})x(?<episode>[0-9]{1,3})(?<rest>.*)"))
            // see
            classifier.addCriterium("tvshows", arrayOf("avi", "mkv"),
                                    arrayOf("(?<name>.+(?:[^\\p{Alnum}\\(]))(?<season>[0-9]{1})(?<episode>[0-9]{2})" + "(?:(?<rest>[^0-9].*)|\\z)"))
        }
    }
}