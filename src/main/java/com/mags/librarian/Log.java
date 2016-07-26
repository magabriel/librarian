/*
 * This file is part of the librarian application.
 *
 * Copyright (c) Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
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

    private static Log ourInstance = new Log();
    private Level logLevel = Level.FINE;
    private Level consoleLogLevel = Level.INFO;

    private Logger logger;
    private String logFileName;

    private Log() {

    }

    public static void setLogFileName(String logFileName) {

        ourInstance.logFileName = logFileName;
    }

    public static void setLogLevel(Level logLevel) {

        ourInstance.logLevel = logLevel;
    }

    public static void setConsoleLogLevel(Level logLevel) {

        ourInstance.consoleLogLevel = logLevel;
    }

    /**
     * Retrieve the logger instance.
     *
     * @return
     */
    public static Logger getLogger() {

        if (ourInstance.logger == null) {
            ourInstance.initLogger();
        }
        return ourInstance.logger;
    }

    /**
     * Initializations.
     */
    private void initLogger() {

        logger = Logger.getLogger(this.getClass().getName());
        logger.setUseParentHandlers(false);
        logger.setLevel(logLevel);

        /*
         * Create the console handler with reduced info and INFO level
         */
        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new java.util.logging.Formatter() {
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
            Handler fileHandler = new FileHandler(logFileName, true);
            fileHandler.setFormatter(new java.util.logging.Formatter() {
                @Override
                public String format(LogRecord record) {

                    SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH.mm:ss");

                    return String.format(
                            "%s [%s] %s\n",
                            dt.format(new Date()),
                            record.getLevel().toString().substring(0, 2),
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
