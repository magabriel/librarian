/*
 * This file is part of the librarian application.
 *
 * Copyright (c) 2016 Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code
 */

package com.mags.librarian.config;

import java.io.File;
import java.io.FileNotFoundException;

public class ConfigReader {

    public Config read(String configFile) throws FileNotFoundException {

        Config config = new Config();

        ConfigLoader configLoader = new ConfigLoader();
        configLoader.load(configFile);
        ConfigAdaptor adaptor = new ConfigAdaptor(configLoader);
        config = adaptor.process();

        if (!config.include.isEmpty()) {

            File includeFile = new File(configFile).getParentFile().toPath().resolve(config.include).toFile();
            if (!includeFile.exists()) {
                throw new FileNotFoundException(
                        String.format("Included file \"%s\" does not exist.", includeFile.toString()));
            }

            ConfigLoader includedConfigLoader = new ConfigLoader();
            configLoader.load(includeFile.toString());
            Config includedConfig = adaptor.process();

            // merge both
            config = includedConfig.merge(config);
        }


        return config;
    }
}
