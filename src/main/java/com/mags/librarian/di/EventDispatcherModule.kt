/*
 * This file is part of the librarian application.
 *
 * Copyright (c) 2017 Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code
 */

package com.mags.librarian.di

import com.mags.librarian.event.EventDispatcher
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class EventDispatcherModule {
    @Provides
    @Singleton
    fun provideEventDispatcher(): EventDispatcher = EventDispatcher()
}
