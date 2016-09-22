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
import java.util.Arrays;

import static org.junit.Assert.*;

public class ClassifierTest {

    static Classifier classifier;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        classifier = new Classifier();
        classifier.addCriterium("videos", new String[]{"avi", "mkv"}, new String[]{});
        classifier.addCriterium("music", new String[]{"mp3", "ogg"}, new String[]{});
        classifier.addCriterium("tvshows",
                                new String[]{"avi", "mkv"},
                                new String[]{"(?<name>.+)S(?<season>[0-9]{1,3})E(?<episode>[0-9]{1,3})(?<rest>.*)"});
        classifier.addCriterium("tvshows",
                                new String[]{"avi", "mkv"},
                                new String[]{"(?<name>.+)(?<season>[0-9]{1,2})x(?<episode>[0-9]{1,3})(?<rest>.*)"});
    }

    @Test
    public void classify() throws Exception {

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
    public void TVShowWithSpacesAndDashes() throws Exception {

        Classification expected = new Classification();
        expected.fileName = "A TV show - 2x10 something else.avi";
        expected.baseName = "A TV show - 2x10 something else";
        expected.extension = "avi";
        expected.name = "tvshows";
        expected.season = 2;
        expected.episode = 10;
        expected.tvShowRest = "something else";
        expected.tvShowName = "A TV show";

        assertEquals("TV show nXnn", expected, classifier.classify(new File(expected.fileName), new
                File("/input1")));
    }

}