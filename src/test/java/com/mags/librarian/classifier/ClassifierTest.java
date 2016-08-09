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

import static org.junit.Assert.*;

public class ClassifierTest {

    static Classifier classifier;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        classifier = new Classifier();
        classifier.addCriterium("videos", "\\.avi$|\\.mkv$");
        classifier.addCriterium("music", "\\.mp3$|\\.ogg");
        classifier.addCriterium("tvshows", "(?<name>.+)S(?<season>[0-9]{1,2})E(?<episode>[0-9]{1,3})(?<rest>.*)");
        classifier.addCriterium("tvshows", "(?<name>.+)(?<season>[0-9]{1,2})x(?<episode>[0-9]{1,3})(?<rest>.*)");
    }

    @Test
    public void classify() throws Exception {

        Classification expected = new Classification();
        expected.name = "videos";

        assertEquals(expected, classifier.classify("test1.avi"));
        assertEquals(expected, classifier.classify("test2.mkv"));

        assertNotEquals(expected, classifier.classify("test3.mp3"));
    }

    @Test
    public void classifyTVShow() throws Exception {

        Classification expected = new Classification();
        expected.name = "tvshows";
        expected.season = 2;
        expected.episode = 10;
        expected.tvShowRest = "_something.avi";
        expected.tvShowNamePostSeparator = "_";
        expected.tvShowName = "A TV show";

        assertEquals("TV show nXnn", expected, classifier.classify("A_TV_show_2x10_something.avi"));
        assertEquals("TV show SnnEnn", expected, classifier.classify("A_TV_show_S02E10_something.avi"));

        expected.tvShowRest = ".something.avi";
        expected.tvShowNamePostSeparator = ".";
        assertEquals("TV show SnnEnn", expected, classifier.classify("A.TV.show.S02E10.something.avi"));

        assertNotEquals(expected, classifier.classify("test1.avi"));
    }

}