/*
 * This file is part of the librarian application.
 *
 * Copyright (c) 2017 Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code
 */

package com.mags.librarian.di

import com.mags.librarian.FeedWriter
import com.mags.librarian.LogWriter
import com.mags.librarian.options.Options
import com.rometools.rome.feed.synd.SyndFeedImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class FeedWriterModule {
    @Provides
    @Singleton
    fun provideFeedWriter(options: Options,
                          logWriter: LogWriter): FeedWriter {
        return FeedWriter(options.rssFileName, logWriter, SyndFeedImpl())
    }
}
