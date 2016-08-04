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

    public Log(String logFileName) {

        this.logFileName = logFileName;
        initLogger();
    }

    void setLogLevel(Level logLevel) {

        fileHandler.setLevel(logLevel);
    }

    void setConsoleLogLevel(Level logLevel) {

        consoleHandler.setLevel(consoleLogLevel);
    }

    public void setLogFileName(String logFileName) throws IOException {

        this.fileHandler = new FileHandler(logFileName, true);
    }

    java.util.logging.Logger getLogger() {

        return logger;
    }

    /**
     * Initializations.
     */
    void initLogger() {

        logger = java.util.logging.Logger.getLogger(this.getClass().getName());
        logger.setUseParentHandlers(false);
        logger.setLevel(logLevel);

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
                            record.getLevel().toString().substring(0, 3),
                            record.getMessage()
                    );
                }
            });
            logger.addHandler(fileHandler);

        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
    }

}
