/*
 * This file is part of the librarian application.
 *
 * Copyright (c) 2016 Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code
 */

package com.mags.librarian.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class EventDispatcher {

    private Map<Event, List<Listener<EventData>>> listeners = new HashMap<>();

    public void addListener(Event eventKey, Listener<EventData> listener) {

        if (!listeners.containsKey(eventKey)) {
            listeners.put(eventKey, new ArrayList<>());
        }

        List<Listener<EventData>> existing = listeners.get(eventKey);
        existing.add(listener);
    }

    public void removeListener(Event eventKey, Listener<EventData> listener) {

        if (!listeners.containsKey(eventKey)) {
            return;
        }

        List<Listener<EventData>> existing = listeners.get(eventKey);

        listeners.get(eventKey).forEach(kListener -> {
            if (kListener == listener) {
                listeners.remove(listener);
            }
        });
    }

    public void fireEvent(Event eventKey, EventData eventData) {

        if (!listeners.containsKey(eventKey)) {
            return;
        }

        listeners.get(eventKey).forEach(listener -> listener.onEvent(eventData));
    }
}
