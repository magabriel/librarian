/*
 * This file is part of the librarian application.
 *
 * Copyright (c) 2016 Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package com.mags.librarian

import com.rometools.rome.feed.synd.SyndContentImpl
import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.feed.synd.SyndEntryImpl
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.FeedException
import com.rometools.rome.io.SyndFeedOutput
import java.io.FileWriter
import java.io.IOException
import java.util.*
import javax.inject.Inject

/**
 * Writes an RSS feed.
 */
class FeedWriter
@Inject constructor(rssFilename: String,
                    private val logWriter: LogWriter,
                    private val feed: SyndFeed) {

    private val entries: MutableList<SyndEntry>
    private var rssFilename = ""

    init {
        this.rssFilename = rssFilename

        feed.feedType = "rss_2.0"
        feed.title = "Librarian - Processed files"
        feed.description = ""
        feed.link = ""

        entries = mutableListOf()
    }

    fun addEntry(title: String,
                 description: String) {

        val entry = SyndEntryImpl()
        entry.link = ""
        entry.title = title

        entry.publishedDate = Date()
        val content = SyndContentImpl()
        content.type = "text/plain"
        content.value = description

        entry.description = content

        entries.add(entry)
    }

    fun hasEntries(): Boolean {
        return !entries.isEmpty()
    }

    fun writeFeed() {

        if (rssFilename.isEmpty()) {
            return
        }

        feed.entries = entries

        val output = SyndFeedOutput()

        try {
            val writer = FileWriter(rssFilename)
            output.output(feed, writer)
        } catch (e: IOException) {
            logWriter.severe(e.message)
        } catch (e: FeedException) {
            logWriter.severe(e.toString())
            e.printStackTrace()
        }

    }

}
