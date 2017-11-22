/*
 * This file is part of the librarian application.
 *
 * Copyright (c) Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package com.mags.librarian.config

import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.*

class ConfigLoaderTest {
    private var loader: ConfigLoader? = null
    private val yamlSource: String
        get() = "key1: value1\n" + "key2: 123\n" + "key3:\n" + "  key3_1:\n" + "    key3_1_1: abc\n" + "    key3_1_2: true\n" + "    key3_1_3: yes\n" + "key4:\n" + "    - first\n" + "    - second\n" + "    - third"

    @Before
    @Throws(Exception::class)
    fun setUp() {
        loader = ConfigLoader()
        loader!!.loadFromString(yamlSource)

    }

    @After
    @Throws(Exception::class)
    fun tearDown() {

    }

    @Test
    @Throws(Exception::class)
    fun loadFromString() {
        val config = loader!!.configRaw
        // non-existent
        assertEquals(null, config!!["key0"])
        // existent
        assertEquals("value1", config["key1"])
        assertEquals(123, config["key2"])
    }

    @Test
    @Throws(Exception::class)
    fun getValueString() {
        assertEquals("", loader!!.getValueString("key0"))
        assertEquals("notfound", loader!!.getValueString("key0", "notfound"))

        assertEquals("value1", loader!!.getValueString("key1"))
        assertEquals("abc", loader!!.getValueString("key3.key3_1.key3_1_1"))
    }

    @Test
    @Throws(Exception::class)
    fun getValueInt() {
        assertTrue(123 == loader!!.getValueInt("key2"))
        assertTrue(null == loader!!.getValueInt("key0"))
        assertTrue(9876 == loader!!.getValueInt("key0", 9876))
    }

    @Test
    @Throws(Exception::class)
    fun getValueBoolean() {
        assertEquals(true, loader!!.getValueBoolean("key3.key3_1.key3_1_2"))
        assertEquals(true, loader!!.getValueBoolean("key3.key3_1.key3_1_3"))
        assertEquals(false, loader!!.getValueBoolean("key3.key3_1.key3_1_3_non_existent"))
    }

    @Test
    @Throws(Exception::class)
    fun getValueList() {
        val expected = arrayOf("first", "second", "third")

        assertArrayEquals(expected, loader!!.getValueListStrings("key4").toTypedArray())
        // empty arrray if key not found
        assertArrayEquals(arrayOfNulls<String>(0),
                          loader!!.getValueListStrings("key0").toTypedArray())
        assertArrayEquals(arrayOfNulls<String>(0),
                          loader!!.getValueListStrings("key3.key0").toTypedArray())
    }

    @Test
    @Throws(Exception::class)
    fun getConfigFlat() {
        val expected = LinkedHashMap<String, Any>()
        expected.put("key1", "value1")
        expected.put("key2", 123)
        expected.put("key3.key3_1.key3_1_1", "abc")
        expected.put("key3.key3_1.key3_1_2", true)
        expected.put("key3.key3_1.key3_1_3", true)
        expected.put("key4", Arrays.asList("first", "second", "third"))
        val actual = loader!!.configFlat

        assertEquals(expected, actual)
    }

}