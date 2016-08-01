/*
 * This file is part of the librarian application.
 *
 * Copyright (c) Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package com.mags.librarian.config;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Loads a configuration YAML file.
 */
public class ConfigLoader {

    private final Yaml configYaml;
    private LinkedHashMap configObj;

    public ConfigLoader() {
        configYaml = new Yaml();
    }

    /**
     * Return the raw configuration object.
     *
     * @return The raw configuration object.
     */
    LinkedHashMap getConfigRaw() {
        return configObj;
    }

    /**
     * @param fileName
     * @throws FileNotFoundException
     */
    public void load(String fileName)
            throws FileNotFoundException {

        InputStream input = new FileInputStream(new File(fileName));

        this.configObj = (LinkedHashMap) this.configYaml.load(input);
    }

    /**
     * Load a configuration file contents from a string.
     *
     * @param document
     */
    public void loadFromString(String document) {
        this.configObj = (LinkedHashMap) this.configYaml.load(document);
    }

    /**
     * Create the default config file from a given template.
     *
     * @param templateFile
     * @param fileName
     * @throws IOException
     */
    public void createDefault(String templateFile, String fileName) throws IOException {

        File file = new File(fileName);
        if (file.exists()) {
            throw new FileAlreadyExistsException("File already exists. Delete or rename it an try again.");
        }

        // read default file
        InputStream res = getClass().getResourceAsStream(templateFile);
        BufferedReader br = new BufferedReader(new InputStreamReader(res));
        List<String> lines = br.lines().collect(Collectors.toList());

        // write it
        String content = String.join(System.lineSeparator(), lines);
        BufferedWriter bw = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
        bw.write(content);
        bw.close();
    }

    /**
     * Retrieve a String value.
     *
     * @param key
     * @return The value of the key or null if not found.
     */
    String getValueString(String key) {
        return getValueString(key, null);
    }

    /**
     * Retrieve a String value with default.
     *
     * @param key
     * @param defaultValue
     * @return The value of the key or defaultValue if not found.
     */
    String getValueString(String key, String defaultValue) {

        String[] keyParts = key.split("\\.");

        LinkedHashMap map = this.configObj;
        String value = defaultValue;
        for (String keyPart : keyParts) {
            if (!map.containsKey(keyPart)) {
                return defaultValue;
            }

            value = map.get(keyPart).toString();
            if (map.get(keyPart).getClass().getName().equals("java.util.LinkedHashMap")) {
                map = (LinkedHashMap) map.get(keyPart);
            }
        }

        return value;
    }

    /**
     * Retrieve an Integer value.
     *
     * @param key
     * @return The value of the key or null if not found.
     */
    Integer getValueInt(String key) {
        return getValueInt(key, null);
    }

    /**
     * Retrieve an Integer value with default.
     *
     * @param key
     * @param defaultValue
     * @return The value of the key or defaultValue if not found.
     */
    Integer getValueInt(String key, Integer defaultValue) {

        String[] keyParts = key.split("\\.");

        LinkedHashMap map = this.configObj;
        Integer value = defaultValue;
        for (String keyPart : keyParts) {
            if (!map.containsKey(keyPart)) {
                return defaultValue;
            }

            value = Integer.parseInt(map.get(keyPart).toString());
            if (map.get(keyPart).getClass().getName().equals("java.util.LinkedHashMap")) {
                map = (LinkedHashMap) map.get(keyPart);
            }
        }

        return value;
    }

    /**
     * * Retrieve a Boolean value with default.
     *
     * @param key
     * @return The value of the key or false if not found.
     */
    Boolean getValueBoolean(String key) {

        String[] keyParts = key.split("\\.");

        LinkedHashMap map = this.configObj;
        boolean value = false;
        for (String keyPart : keyParts) {
            if (!map.containsKey(keyPart)) {
                return false;
            }

            value = Boolean.parseBoolean(map.get(keyPart).toString());
            if (map.get(keyPart).getClass().getName().equals("java.util.LinkedHashMap")) {
                map = (LinkedHashMap) map.get(keyPart);
            }
        }

        return value;
    }

    /**
     * Retrieve a list value (a list of Strings).
     *
     * @param key
     * @return The value of the key as a List, or an empty List if not set.
     */
    List<String> getValueListStrings(String key) {

        String[] keyParts = key.split("\\.");
        LinkedHashMap map = this.configObj;
        List<String> value = Collections.emptyList();
        for (String keyPart : keyParts) {
            if (!map.containsKey(keyPart)) {
                return Collections.emptyList();
            }

            if (map.get(keyPart) != null) {
                if (map.get(keyPart).getClass().getName().equals("java.util.ArrayList")) {
                    value = (List<String>) map.get(keyPart);
                }

                if (map.get(keyPart).getClass().getName().equals("java.util.LinkedHashMap")) {
                    map = (LinkedHashMap) map.get(keyPart);
                }
            }
        }

        return value;
    }

    /**
     * Retrieve a list value (a list of Maps)
     *
     * @param key
     * @return The value of the key as a List, or an empty List if not set.
     */
    List<Map> getValueListMap(String key) {

        String[] keyParts = key.split("\\.");
        LinkedHashMap map = this.configObj;
        List<Map> value = Collections.emptyList();
        for (String keyPart : keyParts) {
            if (!map.containsKey(keyPart)) {
                return Collections.emptyList();
            }

            if (map.get(keyPart) != null) {
                if (map.get(keyPart).getClass().getName().equals("java.util.ArrayList")) {
                    value = (List<Map>) map.get(keyPart);
                }

                if (map.get(keyPart).getClass().getName().equals("java.util.LinkedHashMap")) {
                    map = (LinkedHashMap) map.get(keyPart);
                }
            }
        }

        return value;
    }

}

