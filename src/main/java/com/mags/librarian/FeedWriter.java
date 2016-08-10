/*
 * This file is part of the librarian application.
 *
 * Copyright (c) 2016 Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package com.mags.librarian;

import com.rometools.rome.feed.synd.SyndContentImpl;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedOutput;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Writes an RSS feed.
 */
class FeedWriter {

    private final SyndFeedImpl feed;
    private final List<SyndEntry> entries;
    private String rssFilename = "";
    private Log logger;

    FeedWriter(String rssFilename, Log logger) {

        this.rssFilename = rssFilename;
        this.logger = logger;

        feed = new SyndFeedImpl();
        feed.setFeedType("rss_2.0");
        feed.setTitle("Librarian - Processed files");
        feed.setDescription("");
        feed.setLink("");

        entries = new ArrayList<>();
    }

    void addEntry(String title, String description) {

        SyndEntry entry = new SyndEntryImpl();
        entry.setLink("");
        entry.setTitle(title);

        entry.setPublishedDate(new Date());
        SyndContentImpl content = new SyndContentImpl();
        content.setType("text/plain");
        content.setValue(description);

        entry.setDescription(content);

        entries.add(entry);
    }

    void writeFeed() {

        if (rssFilename.isEmpty()) {
            return;
        }

        feed.setEntries(entries);

        SyndFeedOutput output = new SyndFeedOutput();

        try {
            Writer writer = new FileWriter(rssFilename);
            output.output(feed, writer);
        } catch (IOException e) {
            logger.getLogger().severe(e.getMessage());
        } catch (FeedException e) {
            logger.getLogger().severe(e.toString());
            e.printStackTrace();
        }
    }

}
