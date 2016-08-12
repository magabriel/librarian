/*
 * This file is part of the librarian application.
 *
 * Copyright (c) 2016 Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code
 */

package com.mags.librarian;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

/**
 * Configures the logger.
 */
public class Log {

    private Level logLevel = Level.FINE;
    private Level consoleLogLevel = Level.INFO;

    private java.util.logging.Logger logger;
    private String logFileName;
    private Handler consoleHandler;
    private Handler fileHandler;

    Log(String logFileName) {

        this.logFileName = logFileName;

        logger = java.util.logging.Logger.getLogger(this.getClass().getName());
        logger.setUseParentHandlers(false);
    }

    void start() {

        initLogger();
    }

    void close() {

        for (Handler handler : logger.getHandlers()) {
            logger.removeHandler(handler);
        }
    }

    void setLogLevel(Level logLevel) {

        this.logLevel = logLevel;
    }

    void setConsoleLogLevel(Level logLevel) {

        consoleLogLevel = logLevel;
    }

    public void setLogFileName(String logFileName) throws IOException {

        this.logFileName = logFileName;
    }

    java.util.logging.Logger getLogger() {

        return logger;
    }

    /**
     * Initializations.
     */
    void initLogger() {

        logger.setLevel(Level.ALL);

        /*
         * Create the console handler with reduced info and INFO level
         */
        consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {

                return String.format(
                        "%s\n",
                        record.getMessage()
                );
            }
        });
        consoleHandler.setLevel(consoleLogLevel);
        logger.addHandler(consoleHandler);

        /*
         * Create the custom file handler
         */
        try {
            if (logFileName == null) {
                // no log file name, no logging
                return;
            }
            //FileHandler file name with max size and number of log files limit
            fileHandler = new FileHandler(logFileName, true);
            fileHandler.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord record) {

                    SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH.mm:ss");

                    return String.format(
                            "%s [%s] %s\n",
                            dt.format(new Date()),
                            record.getLevel().toString().concat("      ").substring(0, 6),
                            record.getMessage()
                    );
                }
            });
            fileHandler.setLevel(logLevel);
            logger.addHandler(fileHandler);

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
