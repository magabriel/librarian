/*
 * This file is part of the librarian application.
 *
 * Copyright (c) 2016 Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code
 */

package com.mags.librarian.classifier

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.*

class FileMatcherTest {

    @Before
    @Throws(Exception::class)
    fun setUp() {

    }

    @After
    @Throws(Exception::class)
    fun tearDown() {

    }

    @Test
    @Throws(Exception::class)
    fun matchTVShowName() {
        // TV show name with spaces
        assertTrue("Spaces", FileMatcher.matchTVShowName("A TV show.s01e02.avi", "A TV Show"))
        assertTrue("Underscores", FileMatcher.matchTVShowName("A_TV_show.s01e02.avi", "A TV Show"))
        assertTrue("Dots", FileMatcher.matchTVShowName("A.TV.show.s01e02.avi", "A TV Show"))
        assertTrue("Mixed", FileMatcher.matchTVShowName("a_tv.show.s01e02.avi", "A TV Show"))
    }

    @Test
    @Throws(Exception::class)
    fun matchTVShowNameWithDots() {

        // TV show name with spaces
        assertTrue("Spaces", FileMatcher.matchTVShowName("A TV show.s01e02.avi", "A.TV.Show"))
        assertTrue("Underscores", FileMatcher.matchTVShowName("A_TV_show.s01e02.avi", "A.TV.Show"))
        assertTrue("Dots", FileMatcher.matchTVShowName("A.TV.show.s01e02.avi", "A.TV.Show"))
        assertTrue("Mixed", FileMatcher.matchTVShowName("a_tv.show.s01e02.avi", "A.TV.Show"))
    }

    @Test
    @Throws(Exception::class)
    fun matchTVShow() {

        val criterium = Criterium()
        criterium.name = "tvshows"

        criterium.extensions = Arrays.asList("avi", "mkv").toTypedArray()
        criterium.filters = Arrays.asList(
                "(?<name>.+)S(?<season>[0-9]{1,2})E(?<episode>[0-9]{1,3})(?<rest>.*)").toTypedArray()

        val expected = Classification()
        expected.fileName = "A.TV.Show.S01E02.mkv"
        expected.baseName = "A.TV.Show.S01E02"
        expected.extension = "mkv"
        expected.name = "tvshows"
        expected.season = 1
        expected.episode = 2
        expected.tvShowName = "A TV Show"

        assertEquals(expected, FileMatcher.matchTVShow(expected.fileName, criterium))
    }

    @Test
    @Throws(Exception::class)
    fun doesNotMatchTVShowBecauseOfExtension() {

        val criterium = Criterium()
        criterium.name = "tvshows"

        criterium.extensions = Arrays.asList("avi", "mkv").toTypedArray()
        criterium.filters = Arrays.asList(
                "(?<name>.+)S(?<season>[0-9]{1,2})E(?<episode>[0-9]{1,3})(?<rest>.*)").toTypedArray()

        val expected = Classification()

        assertEquals(expected, FileMatcher.matchTVShow(expected.fileName, criterium))
    }

    @Test
    @Throws(Exception::class)
    fun matchTVShowSeasonXTwoFigures() {

        val criterium = Criterium()
        criterium.name = "tvshows"

        criterium.extensions = Arrays.asList("avi", "mkv").toTypedArray()
        criterium.filters = Arrays.asList(
                "(?<name>.+(?:[^\\p{Alnum}]))(?<season>[0-9]{1,2})x(?<episode>[0-9]{1,3})(?<rest>.*)").toTypedArray()

        val expected = Classification()
        expected.fileName = "A.TV.Show.70x02.mkv"
        expected.baseName = "A.TV.Show.70x02"
        expected.extension = "mkv"
        expected.name = "tvshows"
        expected.season = 70
        expected.episode = 2
        expected.tvShowName = "A TV Show"

        assertEquals(expected, FileMatcher.matchTVShow(expected.fileName, criterium))
    }

    @Test
    @Throws(Exception::class)
    fun matchTVShowSpacesAndDashes() {

        val criterium = Criterium()
        criterium.name = "tvshows"

        criterium.extensions = Arrays.asList("avi", "mkv").toTypedArray()
        criterium.filters = Arrays.asList(
                "(?<name>.+)S(?<season>[0-9]{1,2})E(?<episode>[0-9]{1,3})(?<rest>.*)").toTypedArray()

        val expected = Classification()
        expected.fileName = "A TV Show - S01E02 - The title.mkv"
        expected.baseName = "A TV Show - S01E02 - The title"
        expected.extension = "mkv"
        expected.name = "tvshows"
        expected.season = 1
        expected.episode = 2
        expected.tvShowName = "A TV Show"
        expected.tvShowRest = "The title"

        assertEquals(expected, FileMatcher.matchTVShow(expected.fileName, criterium))
    }

    @Test
    @Throws(Exception::class)
    fun matchRegExp() {

        assertTrue(FileMatcher.matchRegExp("a.song.mp3", "\\.mp3|\\.ogg|music|album|disco|cdrip'"))
    }
}