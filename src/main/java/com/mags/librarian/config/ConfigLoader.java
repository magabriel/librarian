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
    private LinkedHashMap<String, Object> configObj;
    private Map<String, Object> configMap = new LinkedHashMap<String, Object>();

    public ConfigLoader() {

        configYaml = new Yaml();
    }

    /**
     * @param fileName
     * @throws FileNotFoundException
     */
    public void load(String fileName)
            throws FileNotFoundException {

        // load the config file
        InputStream input = new FileInputStream(new File(fileName));
        configObj = (LinkedHashMap<String, Object>) this.configYaml.load(input);

        flatten("", configObj, configMap);
    }

    /**
     * Load a configuration file contents from a string.
     *
     * @param document
     */
    public void loadFromString(String document) {

        configObj = (LinkedHashMap<String, Object>) this.configYaml.load(document);

        flatten("", configObj, configMap);
    }

    /**
     * Return the raw configuration object.
     *
     * @return The raw configuration object.
     */
    public Map<String, Object> getConfigRaw() {

        return configObj;
    }

    /**
     * Return the flattened configuration object.
     *
     * @return the flattened configuration object.
     */
    Map<String, Object> getConfigFlat() {

        return configMap;
    }

    /**
     * Flatten the config object (recursive hashmap) to a flat hasmap.
     *
     * @param baseKey      The base for the flattened keys.
     * @param recursiveMap The config object being flattened.
     * @param flatMap      The flattened config object.
     */
    private void flatten(String baseKey, Map<String, Object> recursiveMap, Map<String, Object> flatMap) {

        recursiveMap.forEach((key, value) -> {
                                 String flatKey = key;
                                 if (!baseKey.isEmpty()) {
                                     flatKey = baseKey + "." + key;
                                 }

                                 if (value.getClass().getName().equals("java.util.LinkedHashMap")) {
                                     flatten(flatKey, (Map<String, Object>) value, flatMap);
                                 } else {
                                     flatMap.put(flatKey, value);
                                 }
                             }
        );
    }

    /**
     * Create the default config file from a given template.
     *
     * @param templateFile
     * @param fileName
     * @throws IOException
     */
    public void createDefault(String templateFile, String fileName)
            throws IOException {

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

        return (String) this.configMap.getOrDefault(key, defaultValue);
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

        return (Integer) this.configMap.getOrDefault(key, defaultValue);
    }

    /**
     * * Retrieve a Boolean value with default.
     *
     * @param key
     * @return The value of the key or false if not found.
     */
    Boolean getValueBoolean(String key) {

        return getValueBoolean(key, false);
    }

    /**
     * * Retrieve a Boolean value with default.
     *
     * @param key
     * @return The value of the key or false if not found.
     */
    Boolean getValueBoolean(String key, Boolean defaultValue) {

        return (Boolean) this.configMap.getOrDefault(key, defaultValue);
    }

    /**
     * Retrieve a list value (a list of Strings).
     *
     * @param key
     * @return The value of the key as a List, or an empty List if not set.
     */
    List<String> getValueListStrings(String key) {

        return getValueListStrings(key, Collections.emptyList());
    }

    /**
     * Retrieve a list value (a list of Strings).
     *
     * @param key
     * @return The value of the key as a List, or an empty List if not set.
     */
    List<String> getValueListStrings(String key, List<String> defaultValue) {

        return (List<String>) this.configMap.getOrDefault(key, defaultValue);
    }

    /**
     * Retrieve a list value (a list of Maps)
     *
     * @param key
     * @return The value of the key as a List, or an empty List if not set.
     */
    List<Map> getValueListMap(String key) {

        return getValueListMap(key, Collections.emptyList());
    }

    /**
     * Retrieve a list value (a list of Maps)
     *
     * @param key
     * @return The value of the key as a List, or an empty List if not set.
     */
    List<Map> getValueListMap(String key, List<Map> defaultValue) {

        return (List<Map>) this.configMap.getOrDefault(key, defaultValue);
    }
}

