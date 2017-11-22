/*
 * This file is part of the librarian application.
 *
 * Copyright (c) 2016 Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code
 */

package com.mags.librarian.event

class EventDispatcher {

    private val listeners = mutableMapOf<Event, MutableList<Listener<EventData>>>()

    fun addListener(eventKey: Event,
                    listener: Listener<EventData>) {

        if (!listeners.containsKey(eventKey)) {
            listeners.put(eventKey, mutableListOf())
        }

        listeners[eventKey]!! += listener
    }

    fun removeListener(eventKey: Event,
                       listener: Listener<EventData>) {

        if (listeners.containsKey(eventKey)) {
            listeners[eventKey]!!.forEach {
                if (it == listener) {
                    listeners[eventKey]!!.remove(listener)
                }
            }
        }
    }

    fun fireEvent(eventKey: Event,
                  eventData: EventData) {

        if (listeners.containsKey(eventKey)) {
            listeners[eventKey]!!.forEach { it.onEvent(eventData) }
        }
    }
}
