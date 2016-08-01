/*
 * This file is part of the librarian application.
 *
 * Copyright (c) Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package com.mags.librarian.config;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedHashMap;

import static org.junit.Assert.*;

public class ConfigLoaderTest {

    private ConfigLoader loader;

    @Before
    public void setUp() throws Exception {

        loader = new ConfigLoader();
        loader.loadFromString(getYamlSource());

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void loadFromString() throws Exception {

        LinkedHashMap config = loader.getConfigRaw();

        // non-existent
        assertEquals(null, config.get("key0"));

        // existent
        assertEquals("value1", config.get("key1"));
        assertEquals(123, config.get("key2"));
    }

    @Test
    public void getValueString() throws Exception {

        assertEquals(null, loader.getValueString("key0"));
        assertEquals("notfound", loader.getValueString("key0", "notfound"));

        assertEquals("value1", loader.getValueString("key1"));
        assertEquals("abc", loader.getValueString("key3.key3_1.key3_1_1"));
    }

    @Test
    public void getValueInt() throws Exception {

        assertTrue(123 == loader.getValueInt("key2"));
        assertTrue(null == loader.getValueInt("key0"));
        assertTrue(9876 == loader.getValueInt("key0", 9876));
    }

    @Test
    public void getValueBoolean() throws Exception {

        assertEquals(true, loader.getValueBoolean("key3.key3_1.key3_1_2"));
        assertEquals(true, loader.getValueBoolean("key3.key3_1.key3_1_3"));
        assertEquals(false, loader.getValueBoolean("key3.key3_1.key3_1_3_non_existent"));
    }

    @Test
    public void getValueList() throws Exception {

        String[] expected = {"first", "second", "third"};

        assertArrayEquals(expected, loader.getValueListStrings("key4").toArray(new String[0]));

        // empty arrray if key not found
        assertArrayEquals(new String[0], loader.getValueListStrings("key0").toArray(new String[0]));
        assertArrayEquals(new String[0], loader.getValueListStrings("key3.key0").toArray(new String[0]));
    }

    private String getYamlSource() {

        return "key1: value1\n" +
                "key2: 123\n" +
                "key3:\n" +
                "  key3_1:\n" +
                "    key3_1_1: abc\n" +
                "    key3_1_2: true\n" +
                "    key3_1_3: yes\n" +
                "key4:\n" +
                "    - first\n" +
                "    - second\n" +
                "    - third";
    }

}