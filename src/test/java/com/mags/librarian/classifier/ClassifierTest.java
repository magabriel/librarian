/*
 * This file is part of the librarian application.
 *
 * Copyright (c) Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package com.mags.librarian.classifier;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ClassifierTest {

    static Classifier classifier;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        classifier = new Classifier();
        classifier.addCriterium("videos", new String[]{"avi", "mkv"}, new String[]{});
        classifier.addCriterium("music", new String[]{"mp3", "ogg"}, new String[]{});
        // SssEee
        classifier.addCriterium("tvshows",
                                new String[]{"avi", "mkv"},
                                new String[]{"(?<name>.+)S(?<season>[0-9]{1,3})E(?<episode>[0-9]{1,3})(?<rest>.*)"});
        // ssxee
        classifier.addCriterium("tvshows",
                                new String[]{"avi", "mkv"},
                                new String[]{"(?<name>.+)(?<season>[0-9]{1,2})x(?<episode>[0-9]{1,3})(?<rest>.*)"});
        // see
        classifier.addCriterium("tvshows",
                                new String[]{"avi", "mkv"},
                                new String[]{"(?<name>.+(?:[^\\p{Alnum}\\(]))(?<season>[0-9]{1})(?<episode>[0-9]{2})"
                                        + "(?:(?<rest>[^0-9].*)|\\z)"});
    }

    @Test
    public void classifyVideos() throws Exception {

        Classification expected = new Classification();
        expected.name = "videos";
        expected.fileName = "test1.avi";
        expected.baseName = "test1";
        expected.extension = "avi";
        assertEquals(expected, classifier.classify(new File("test1.avi"), new File("/music")));

        expected.fileName = "test2.mkv";
        expected.baseName = "test2";
        expected.extension = "mkv";
        assertEquals(expected, classifier.classify(new File("test2.mkv"), new File("/music")));

        expected.fileName = "test3.mp3";
        expected.baseName = "test3";
        expected.extension = "mp3";
        assertNotEquals(expected, classifier.classify(new File("test3.mp3"), new File("/music")));
    }

    @Test
    public void classifyTVShow_nXnn() throws Exception {

        Classification expected = new Classification();
        expected.fileName = "A_TV_show_2x10_something.avi";
        expected.baseName = "A_TV_show_2x10_something";
        expected.extension = "avi";
        expected.name = "tvshows";
        expected.season = 2;
        expected.episode = 10;
        expected.tvShowRest = "something";
        expected.tvShowName = "A TV show";

        assertEquals(expected, classifier.classify(new File(expected.fileName), new File("/input1")));
    }

    @Test
    public void classifyTVShow_SnnEnn() throws Exception {

        Classification expected = new Classification();
        expected.fileName = "A_TV_show_S02E10_something.avi";
        expected.baseName = "A_TV_show_S02E10_something";
        expected.extension = "avi";
        expected.name = "tvshows";
        expected.season = 2;
        expected.episode = 10;
        expected.tvShowRest = "something";
        expected.tvShowName = "A TV show";

        assertEquals(expected, classifier.classify(new File(expected.fileName), new File("/input1")));
    }

    @Test
    public void classifyTVShow_SnnEnn_Dots() throws Exception {

        Classification expected = new Classification();
        expected.fileName = "A.TV.show.S02E10.something.avi";
        expected.baseName = "A.TV.show.S02E10.something";
        expected.extension = "avi";
        expected.name = "tvshows";
        expected.season = 2;
        expected.episode = 10;
        expected.tvShowRest = "something";
        expected.tvShowName = "A TV show";

        assertEquals(expected, classifier.classify(new File(expected.fileName), new File("/input1")));
    }

    @Test
    public void classifyTVShow_WithSpacesAndDashes() throws Exception {

        Classification expected = new Classification();
        expected.fileName = "A TV show - 2x10 something else.avi";
        expected.baseName = "A TV show - 2x10 something else";
        expected.extension = "avi";
        expected.name = "tvshows";
        expected.season = 2;
        expected.episode = 10;
        expected.tvShowRest = "something else";
        expected.tvShowName = "A TV show";

        assertEquals("TV show nXnn", expected, classifier.classify(new File(expected.fileName), new File("/input1")));
    }

    @Test
    public void classifyTVShow_SEE_withoutRest() throws Exception {

        Classification expected = new Classification();
        expected.fileName = "A TV show 123.avi";
        expected.baseName = "A TV show 123";
        expected.extension = "avi";
        expected.name = "tvshows";
        expected.season = 1;
        expected.episode = 23;
        expected.tvShowRest = "";
        expected.tvShowName = "A TV show";

        assertEquals("TV show SEE", expected, classifier.classify(new File(expected.fileName), new File("/input1")));
    }

    @Test
    public void classifyTVShow_SEE_withRest() throws Exception {

        Classification expected = new Classification();
        expected.fileName = "A TV show 123 something.avi";
        expected.baseName = "A TV show 123 something";
        expected.extension = "avi";
        expected.name = "tvshows";
        expected.season = 1;
        expected.episode = 23;
        expected.tvShowRest = "something";
        expected.tvShowName = "A TV show";

        assertEquals("TV show SEE with rest",
                     expected,
                     classifier.classify(new File(expected.fileName), new File("/input1")));
    }

    @Test
    public void classifyMovie_WithYear() throws Exception {

        Classification expected = new Classification();
        expected.fileName = "A movie (1945) something.avi";
        expected.baseName = "A movie (1945) something";
        expected.extension = "avi";
        expected.name = "videos";

        assertEquals("Movie with year",
                     expected,
                     classifier.classify(new File(expected.fileName), new File("/input1")));
    }

    @Test
    public void classifyMovie_WithYear_WithoutParenthesis() throws Exception {

        Classification expected = new Classification();
        expected.fileName = "A movie 1945 something.avi";
        expected.baseName = "A movie 1945 something";
        expected.extension = "avi";
        expected.name = "videos";

        assertEquals("Movie with year without parenthesis",
                     expected,
                     classifier.classify(new File(expected.fileName), new File("/input1")));
    }

    @Test
    public void classifyMovie_WithYear_WithoutParenthesis_WithoutRest() throws Exception {

        Classification expected = new Classification();
        expected.fileName = "A movie 1945.avi";
        expected.baseName = "A movie 1945";
        expected.extension = "avi";
        expected.name = "videos";

        assertEquals("Movie with year without parenthesis without rest",
                     expected,
                     classifier.classify(new File(expected.fileName), new File("/input1")));
    }
}