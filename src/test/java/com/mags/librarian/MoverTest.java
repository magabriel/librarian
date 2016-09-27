/*
 * This file is part of the librarian application.
 *
 * Copyright (c) Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package com.mags.librarian;

import com.mags.librarian.classifier.Classification;
import com.mags.librarian.config.Config;
import com.mags.librarian.event.EventDispatcher;
import org.junit.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class MoverTest {

    private static Mover mover;
    private static Log logger;
    private static EventDispatcher eventDispatcher;

    @Before
    public void setUp() throws Exception {
        eventDispatcher = getEventDispatcher();
        mover = getMover();
    }

    @After
    public void tearDown() throws Exception {
        logger.close();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {

    }

    @Test
    public void moveVideo() throws Exception {

        Classification classification = new Classification();
        classification.name = "videos";
        mover.moveToDestination(new File("/input/my_movie.avi"), classification);

        assertEquals("moved [/input/my_movie.avi] to [/output/videos] as [my_movie.avi]", mover.getActionPerformed());
    }

    @Test
    public void moveTvShow() throws Exception {

        Classification classification = new Classification();
        classification.fileName = "My_Tvshow_S02E10_some_data.avi";
        classification.baseName = "My_Tvshow_S02E10_some_data";
        classification.extension = "avi";
        classification.name = "tvshows";
        classification.season = 2;
        classification.episode = 10;
        classification.tvShowName = "My_Tvshow";
        classification.tvShowRest = "some_data";

        mover.moveToDestination(new File("/input/"+classification.fileName), classification);

        assertEquals(
                "moved [/input/My_Tvshow_S02E10_some_data.avi] to [/output/tvshows/My_Tvshow/The.season.002] as [My_Tvshow_S=2E=010_some_data.avi]",
                mover.getActionPerformed());
    }

    private static Mover getMover() {

        Options options = new Options();
        options.dryRun = true;

        Config config = setUpConfig();

        logger = new Log(System.getProperty("java.io.tmpdir") + "/librarian.log");
        return new Mover(options, config, logger, eventDispatcher);
    }

    private static EventDispatcher getEventDispatcher() {

        return new EventDispatcher();
    }

    private static Config setUpConfig() {

        Config config = new Config();

        /*
         * Output folders
         */
        ArrayList<Map> outputFolders = new ArrayList<>();
        outputFolders.add(
                new HashMap() {
                    {
                        put("path", "/output/tvshows");
                        put("contents", "tvshows");
                    }
                }
        );
        outputFolders.add(
                new HashMap() {
                    {
                        put("path", "/output/music");
                        put("contents", "music");
                    }
                }
        );
        outputFolders.add(
                new HashMap() {
                    {
                        put("path", "/output/videos");
                        put("contents", "videos");
                    }
                }
        );

        config.outputFolders = outputFolders.toArray(new Map[0]);

        /*
         * Content types
         */
        ArrayList<Map> contentTypes = new ArrayList<>();
        contentTypes.add(
                new HashMap() {
                    {
                        put("tvshows", "(?<name>.+)S(?<season>[0-9]{1,2})E(?<episode>[0-9]{1,3})(?<rest>.*)");
                        put("videos", "\\.avi|\\.mpeg|\\.mpg|\\.mov|\\.wmv|\\.mp4|\\.m4v|\\.mkv");
                        put("music", "\\.mp3|\\.ogg|music|album|disco|cdrip");
                    }
                }
        );
//        config.contentTypes = contentTypes.toArray(new Map[0]);

        /*
         * Schemas
         */
        config.tvShowsSeasonSchema = "The.season.{season:3}";
        config.tvShowsNumberingSchema = "S={season:1}E={episode:3}";

        return config;
    }

}