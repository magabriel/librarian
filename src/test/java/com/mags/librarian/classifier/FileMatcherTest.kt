/*
 * This file is part of the librarian application.
 *
 * Copyright (c) 2016 Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code
 */

package com.mags.librarian.classifier

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FileMatcherTest {

    @BeforeEach
    fun setUp() {

    }

    @AfterEach
    fun tearDown() {

    }

    @Test
    fun matchTVShowName() {
        // TV show name with spaces
        // "Spaces"
        assertTrue(FileMatcher.matchTVShowName("A TV show", "A TV Show"))
        // "Underscores"
        assertTrue(FileMatcher.matchTVShowName("A_TV_show", "A TV Show"))
        // "Dots"
        assertTrue(FileMatcher.matchTVShowName("A.TV.show", "A TV Show"))
        // "Mixed"
        assertTrue(FileMatcher.matchTVShowName("a_tv.show", "A TV Show"))
    }

    @Test
    fun matchTVShowNameWithDots() {
        // TV show name with spaces
        // "Spaces"
        assertTrue(FileMatcher.matchTVShowName("A TV show", "A.TV.Show"))
        // "Underscores"
        assertTrue(FileMatcher.matchTVShowName("A_TV_show", "A.TV.Show"))
        // "Dots"
        assertTrue(FileMatcher.matchTVShowName("A.TV.show", "A.TV.Show"))
        // "Mixed"
        assertTrue(FileMatcher.matchTVShowName("a_tv.show", "A.TV.Show"))
    }

    @Test
    fun doesNotMatchPartialTVShowName() {
        // "House" must not match the folder of a different TV show
        assertFalse(FileMatcher.matchTVShowName("House of cards", "House"))
        assertFalse(FileMatcher.matchTVShowName("House", "House of cards"))
        // nor a folder that merely contains the name
        assertFalse(FileMatcher.matchTVShowName("Another House", "House"))
        assertFalse(FileMatcher.matchTVShowName("House_2015", "House"))
    }

    @Test
    fun matchTVShowNameWithSpecialCharacters() {
        // regex metacharacters must be treated as literal characters
        assertTrue(FileMatcher.matchTVShowName("C++", "C++"))
        assertFalse(FileMatcher.matchTVShowName("C++", "C+"))
        assertTrue(FileMatcher.matchTVShowName("Marvels_Agents_of_S.H.I.E.L.D", "Marvels Agents of S.H.I.E.L.D"))
    }

    @Test
    fun matchTVShow() {

        val criterium = Criterium()
        criterium.name = "tvshows"

        criterium.extensions = listOf("avi", "mkv")
        criterium.filters = listOf("(?<name>.+)S(?<season>[0-9]{1,2})E(?<episode>[0-9]{1,3})(?<rest>.*)")

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
    fun doesNotMatchTVShowBecauseOfExtension() {

        val criterium = Criterium()
        criterium.name = "tvshows"

        criterium.extensions = listOf("avi", "mkv")
        criterium.filters = listOf("(?<name>.+)S(?<season>[0-9]{1,2})E(?<episode>[0-9]{1,3})(?<rest>.*)")

        val expected = Classification()

        assertEquals(expected, FileMatcher.matchTVShow(expected.fileName, criterium))
    }

    @Test
    fun matchTVShowSeasonXTwoFigures() {

        val criterium = Criterium()
        criterium.name = "tvshows"

        criterium.extensions = listOf("avi", "mkv")
        criterium.filters = listOf("(?<name>.+(?:[^\\p{Alnum}]))(?<season>[0-9]{1,2})x(?<episode>[0-9]{1,3})(?<rest>.*)")

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
    fun matchTVShowSpacesAndDashes() {

        val criterium = Criterium()
        criterium.name = "tvshows"

        criterium.extensions = listOf("avi", "mkv")
        criterium.filters = listOf("(?<name>.+)S(?<season>[0-9]{1,2})E(?<episode>[0-9]{1,3})(?<rest>.*)")

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
    fun matchRegExp() {

        assertTrue(FileMatcher.matchRegExp("a.song.mp3", "\\.mp3|\\.ogg|music|album|disco|cdrip'"))
    }
}