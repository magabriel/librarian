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
import com.mags.librarian.config.ConfigAdaptor;
import com.mags.librarian.config.ConfigLoader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;

public class Main {

    static final String NAME = "librarian";
    static final String VERSION = "0.1";
    static final String COPYRIGHT = "(C) MAGS 2016";

    static final String CONFIG_FILE = "librarian.yml";
    static final String LOG_FILE = "librarian.log";
    private static String configFile = System.getProperty("user.dir") + '/' + CONFIG_FILE;
    private static String logFile = System.getProperty("user.dir") + '/' + LOG_FILE;
    private static Config config;
    private static Options options;

    public static void main(String[] args) {

        Log.setLogFileName(logFile);

        writeMessage(NAME + " version " + VERSION + " " + COPYRIGHT);

        readOptions(args);

        loadConfig();

        Processor processor = new Processor(options, config);
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
                    options.setCopyOnly(true);
                    break;

                case "--create-config":
                    createConfig();
                    // no point on continuing execution
                    System.exit(0);
                    break;

                case "--dry-run":
                    options.setDryRun(true);
                    break;

                case "-v":
                    options.setVerbosity(Options.Verbosity.NORMAL);
                    Log.setConsoleLogLevel(Level.INFO);
                    break;

                case "-vv":
                    options.setVerbosity(Options.Verbosity.HIGH);
                    Log.setConsoleLogLevel(Level.CONFIG);
                    break;

                case "--quiet":
                    options.setVerbosity(Options.Verbosity.NONE);
                    Log.setConsoleLogLevel(Level.OFF);

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
                        Log.setLogFileName(logFile);
                    }
                    break;

                case "--loglevel":
                    if (i < args.length - 1) {
                        i++;
                        String level = args[i];

                        try {
                            Level logLevel = Level.parse(level.toUpperCase());
                            options.setLogLevel(logLevel);
                            Log.getLogger().setLevel(logLevel);
                        } catch (IllegalArgumentException e) {
                            Log.getLogger().severe(String.format("Invalid log level \"%s\"", level));

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
        writeMessage("         --loglevel <level> : Loglevel (NONE, INFO, WARNING, SEVERE). Default INFO.");
        writeMessage("         -c --config <file> : Use that config file instead of the one in execution directory.");
        writeMessage("         -l --log <file>    : Write to that log file instead of creating one in the execution directory.");
        writeMessage("         -v, -vv            : Verbosity normal (default) or high.");
        writeMessage("         --quiet            : Do not write messages.");

        System.exit(1);
    }

    /**
     * Loads the configuration file.
     */
    private static void loadConfig() {

        ConfigLoader configLoader = new ConfigLoader();

        try {
            configLoader.load(configFile);
            ConfigAdaptor adaptor = new ConfigAdaptor(configLoader);
            config = adaptor.process();

        } catch (FileNotFoundException e) {
            Log.getLogger().severe("ERROR: Configuration file '" + configFile + "' not found.");
            Log.getLogger().severe("HINT: You can generate a default configuration file with the provided command line option.");

            System.exit(1);
        }
    }

    private static void createConfig() {

        ConfigLoader configLoader = new ConfigLoader();

        try {
            configLoader.createDefault("/librarian-default.yml", configFile);
            Log.getLogger().info("Default configuration file created as '" + configFile + "'");

        } catch (FileNotFoundException e) {
            Log.getLogger().severe("ERROR: Configuration file '" + configFile + "' could not be created. Check intermediate folders exist.");
            System.exit(1);

        } catch (IOException e) {
            Log.getLogger().severe("ERROR: Configuration file '" + configFile + "' could not be created: '" + e.getMessage() + "'");
            System.exit(1);
        }
    }

    public static void writeMessage(String msg) {

        if (options != null) {
            if (options.getVerbosity() == Options.Verbosity.NONE) {
                return;
            }
        }

        System.err.println(msg);
    }

    public static void writeMessage() {

        if (options != null) {
            if (options.getVerbosity() == Options.Verbosity.NONE) {
                return;
            }
        }

        System.err.println();
    }

}
