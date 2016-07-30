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
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class MoverTest {

    @Test
    public void moveVideo() throws Exception {

        Mover mover = getMover();

        Classification classification = new Classification();
        classification.setName("videos");
        mover.moveToDestination(new File("/input/my_movie.avi"), classification);

        assertEquals("moved [/input/my_movie.avi] to [/output/videos] as [my_movie.avi]", mover.getActionPerformed());
    }

    @Test
    public void moveTvShow() throws Exception {

        Mover mover = getMover();

        Classification classification = new Classification();
        classification.setName("tvshows");
        classification.setSeason(2);
        classification.setEpisode(10);
        classification.setTvshowName("My_Tvshow");
        classification.setTvshowRest("_some_data.avi");

        mover.moveToDestination(new File("/input/My_Tvshow_S02E10_some_data.avi"), classification);

        assertEquals("moved [/input/My_Tvshow_S02E10_some_data.avi] to [/output/tvshows/My_Tvshow/The.season.002] as [My_Tvshow_S=2E=010_some_data.avi]", mover.getActionPerformed());
    }

    private Mover getMover() {

        Options options = new Options();
        options.setDryRun(true);

        Config config = setUpConfig();

        return new Mover(options, config);
    }

    private Config setUpConfig() {

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

        config.setOutputFolders(outputFolders.toArray(new Map[0]));

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
        config.setContentTypes(contentTypes.toArray(new Map[0]));

        /*
         * Schemas
         */
        config.setTvShowsSeasonSchema("The.season.{season:3}");
        config.setTvShowsNumberingSchema("S={season:1}E={episode:3}");

        return config;
    }

}