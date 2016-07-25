package com.mags.librarian.test;

import com.mags.librarian.classifier.Classification;
import com.mags.librarian.classifier.Classifier;

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
        classifier.addCriterium("tvshows", "(?<name>.+)(?:.*[^0-9])(?<season>[0-9]{1,2})x(?<episode>[0-9]{1,3})(?<rest>.*)");
    }

    @Test
    public void classify() throws Exception {

        Classification expected = new Classification();
        expected.setName("videos");

        assertEquals(expected, classifier.classify("test1.avi"));
        assertEquals(expected, classifier.classify("test2.mkv"));

        assertNotEquals(expected, classifier.classify("test3.mp3"));
    }

    @Test
    public void classifyTVShow() throws Exception {

        Classification expected = new Classification();
        expected.setName("tvshows");
        expected.setSeason(2);
        expected.setEpisode(10);
        expected.setTvshowName("A.TV.show");

        assertEquals("TV show nXnn", expected, classifier.classify("A_TV_show_2x10_something.avi"));
        assertEquals("TV show SnnEnn", expected, classifier.classify("A_TV_show_S02E10_something_else.avi"));
        assertEquals("TV show SnnEnn", expected, classifier.classify("A.TV.show.S02E10.something.else.avi"));

        assertNotEquals(expected, classifier.classify("test1.avi"));
    }

}