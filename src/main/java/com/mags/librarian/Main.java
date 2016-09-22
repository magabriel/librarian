/*
 * This file is part of the librarian application.
 *
 * Copyright (c) Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package com.mags.librarian;

import com.mags.librarian.config.Config;
import com.mags.librarian.config.ConfigLoader;
import com.mags.librarian.config.ConfigReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;

public class Main {

    private static final String NAME = "librarian";
    private static final String VERSION = "0.3";
    private static final String COPYRIGHT = "(C) magabriel@gmail.com";

    private static final String CONFIG_FILE = "librarian.yml";
    private static final String LOG_FILE = "librarian.log";
    private static final String RSS_FILE = "librarian.rss";

    private static String configFile;
    private static String logFile;
    private static String rssFile;

    private static Config config;
    private static Options options;
    private static Log logger;

    public static void main(String[] args) {

        configFile = (new File(System.getProperty("user.dir"))).toPath().resolve(CONFIG_FILE).toString();
        logFile = (new File(System.getProperty("user.dir"))).toPath().resolve(LOG_FILE).toString();
        rssFile = (new File(System.getProperty("user.dir"))).toPath().resolve(RSS_FILE).toString();

        // create logger but no logging allowed yet
        logger = new Log(logFile);

        writeMessage(NAME + " version " + VERSION + " " + COPYRIGHT);

        readOptions(args);

        // start logging
        logger.start();

        loadConfig();

        Processor processor = new Processor(options, config, logger);
        processor.run();
    }

    /**
     * Read and configure the command line options.
     *
     * @param args
     */
    private static void readOptions(String[] args) {

        options = new Options();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            switch (arg) {
                case "--help":
                case "-h":
                    showUsage();
                    break;

                case "--copy":
                    options.copyOnly = true;
                    break;

                case "--create-config":
                    createConfig();
                    // no point on continuing execution
                    System.exit(0);
                    break;

                case "--dry-run":
                    options.dryRun = true;
                    break;

                case "-v":
                    options.verbosity = Options.Verbosity.NORMAL;
                    logger.setConsoleLogLevel(Level.INFO);
                    break;

                case "-vv":
                    options.verbosity = Options.Verbosity.HIGH;
                    logger.setConsoleLogLevel(Level.CONFIG);
                    break;

                case "--quiet":
                    options.verbosity = Options.Verbosity.NONE;
                    logger.setConsoleLogLevel(Level.OFF);

                    break;

                case "--config":
                case "-c":
                    if (i < args.length - 1) {
                        i++;
                        configFile = args[i];
                    }
                    break;

                case "--log":
                case "-l":
                    if (i < args.length - 1) {
                        i++;
                        logFile = args[i];
                        try {
                            logger.setLogFileName(logFile);
                            options.logFileName = logFile;
                        } catch (IOException e) {
                            logger.getLogger().severe(e.getMessage());

                            System.exit(1);
                        }
                    }
                    break;

                case "--rss":
                case "-r":
                    if (i < args.length - 1) {
                        i++;
                        rssFile = args[i];
                        options.rssFileName = rssFile;
                    }
                    break;

                case "--loglevel":
                    if (i < args.length - 1) {
                        i++;
                        String level = args[i];

                        try {
                            Level logLevel = Level.parse(level.toUpperCase());
                            options.logLevel = logLevel;
                            logger.setLogLevel(logLevel);
                        } catch (IllegalArgumentException e) {
                            logger.getLogger().severe(String.format("Invalid log level \"%s\"", level));

                            System.exit(1);
                        }
                    }

                    break;

                default:
                    showUsage();
            }
        }
    }

    private static void showUsage() {

        writeMessage();
        writeMessage("Usage: librarian <options>");
        writeMessage();
        writeMessage("Options: -h | --help        : Show this help.");
        writeMessage("         --copy             : Copy instead of move the files.");
        writeMessage("         --create-config    : Create a default configuration file in current directory.");
        writeMessage("         --dry-run          : Do not change anything, just tell what would have been done.");
        writeMessage("         --loglevel <level> : Loglevel (ALL, FINEST, FINER, FINE, CONFIG, INFO, WARNING, " +
                             "SEVERE, OFF). Default INFO.");
        writeMessage("         -c --config <file> : Use that config file instead of the one in execution directory.");
        writeMessage(
                "         -l --log <file>    : Write to that log file instead of creating one in the execution directory.");
        writeMessage("         -r --rss<file>     : Write to that rss file instead of the one in execution directory.");
        writeMessage("         -v, -vv            : Verbosity normal (default) or high.");
        writeMessage("         --quiet            : Do not write messages.");

        System.exit(1);
    }

    /**
     * Loads the configuration file.
     */
    private static void loadConfig() {

        try {
            ConfigReader reader = new ConfigReader();
            config = reader.read(configFile);

        } catch (FileNotFoundException e) {
            logger.getLogger().severe(String.format("ERROR: Configuration file '%s' not found.", configFile));
            logger.getLogger().severe(
                    "HINT: You can generate a default configuration file with the provided command line option.");
            logger.getLogger().finer(e.toString());

            System.exit(1);
        }
    }

    private static void createConfig() {

        ConfigLoader configLoader = new ConfigLoader();

        try {
            configLoader.createDefault("/librarian-default.yml", configFile);
            logger.getLogger().info(String.format("Default configuration file created as '%s'", configFile));

        } catch (FileNotFoundException e) {
            logger.getLogger().severe(String.format(
                    "ERROR: Configuration file '%s' could not be created. Check intermediate folders exist.",
                    configFile));
            System.exit(1);

        } catch (IOException e) {
            logger.getLogger().severe(
                    String.format("ERROR: Configuration file '%s' could not be created: '%s'", configFile,
                                  e.getMessage()));
            System.exit(1);
        }
    }

    private static void writeMessage(String msg) {

        if (options != null) {
            if (options.verbosity == Options.Verbosity.NONE) {
                return;
            }
        }

        System.err.println(msg);
    }

    private static void writeMessage() {

        if (options != null) {
            if (options.verbosity == Options.Verbosity.NONE) {
                return;
            }
        }

        System.err.println();
    }

}
