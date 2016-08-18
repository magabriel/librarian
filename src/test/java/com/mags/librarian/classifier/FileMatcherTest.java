/*
 * This file is part of the librarian application.
 *
 * Copyright (c) 2016 Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code
 */

package com.mags.librarian.classifier;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class FileMatcherTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void matchTVShowName() throws Exception {

        // TV show name with spaces
        assertTrue("Spaces", FileMatcher.matchTVShowName("A TV show.s01e02.avi", "A TV Show"));
        assertTrue("Underscores", FileMatcher.matchTVShowName("A_TV_show.s01e02.avi", "A TV Show"));
        assertTrue("Dots", FileMatcher.matchTVShowName("A.TV.show.s01e02.avi", "A TV Show"));
        assertTrue("Mixed", FileMatcher.matchTVShowName("a_tv.show.s01e02.avi", "A TV Show"));
    }

    @Test
    public void matchTVShowNameWithDots() throws Exception {

        // TV show name with spaces
        assertTrue("Spaces", FileMatcher.matchTVShowName("A TV show.s01e02.avi", "A.TV.Show"));
        assertTrue("Underscores", FileMatcher.matchTVShowName("A_TV_show.s01e02.avi", "A.TV.Show"));
        assertTrue("Dots", FileMatcher.matchTVShowName("A.TV.show.s01e02.avi", "A.TV.Show"));
        assertTrue("Mixed", FileMatcher.matchTVShowName("a_tv.show.s01e02.avi", "A.TV.Show"));
    }

    @Test
    public void matchTVShow() throws Exception {

        Criterium criterium = new Criterium();
        criterium.name = "tvshows";
        criterium.regExp = "(?<name>.+)S(?<season>[0-9]{1,2})E(?<episode>[0-9]{1,3})(?<rest>.*)";

        Classification expected = new Classification();
        expected.fileName = "A.TV.Show.S01E02.mkv";
        expected.baseName = "A.TV.Show.S01E02";
        expected.extension = "mkv";
        expected.name = "tvshows";
        expected.season = 1;
        expected.episode = 2;
        expected.tvShowName = "A TV Show";

        assertEquals(expected, FileMatcher.matchTVShow(expected.fileName, criterium));
    }

    @Test
    public void matchTVShowSeasonXTwoFigures() throws Exception {

        Criterium criterium = new Criterium();
        criterium.name = "tvshows";
        criterium.regExp = "(?<name>.+(?:[^\\p{Alnum}]))(?<season>[0-9]{1,2})x(?<episode>[0-9]{1,3})(?<rest>.*)";

        Classification expected = new Classification();
        expected.fileName = "A.TV.Show.70x02.mkv";
        expected.baseName = "A.TV.Show.70x02";
        expected.extension = "mkv";
        expected.name = "tvshows";
        expected.season = 70;
        expected.episode = 2;
        expected.tvShowName = "A TV Show";

        assertEquals(expected, FileMatcher.matchTVShow(expected.fileName, criterium));
    }

    @Test
    public void matchTVShowSpacesAndDashes() throws Exception {

        Criterium criterium = new Criterium();
        criterium.name = "tvshows";
        criterium.regExp = "(?<name>.+)S(?<season>[0-9]{1,2})E(?<episode>[0-9]{1,3})(?<rest>.*)";

        Classification expected = new Classification();
        expected.fileName = "A TV Show - S01E02 - The title.mkv";
        expected.baseName = "A TV Show - S01E02 - The title";
        expected.extension = "mkv";
        expected.name = "tvshows";
        expected.season = 1;
        expected.episode = 2;
        expected.tvShowName = "A TV Show";
        expected.tvShowRest = "The title";

        assertEquals(expected, FileMatcher.matchTVShow(expected.fileName, criterium));
    }

    @Test
    public void matchRegExp() throws Exception {
        assertTrue(FileMatcher.matchRegExp("a.song.mp3", "\\.mp3|\\.ogg|music|album|disco|cdrip'"));
    }
}