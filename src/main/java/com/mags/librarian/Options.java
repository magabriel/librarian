/*
 * This file is part of the librarian application.
 *
 * Copyright (c) Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package com.mags.librarian;

import java.util.logging.Level;

/**
 * Stores the user options.
 */
class Options {

    enum Verbosity {
        NONE,
        NORMAL,
        HIGH
    }

    private Verbosity verbosity = Verbosity.NORMAL;
    private Level logLevel = Level.INFO;
    private Boolean dryRun = false;
    private Boolean copyOnly = false;
    private String logFileName = "";
    private String rssFileName = "";

    public String getLogFileName() {

        return logFileName;
    }

    void setLogFileName(String logFileName) {

        this.logFileName = logFileName;
    }

    String getRssFileName() {

        return rssFileName;
    }

    void setRssFileName(String rssFileName) {

        this.rssFileName = rssFileName;
    }

    Level getLogLevel() {

        return logLevel;
    }

    void setLogLevel(Level logLevel) {

        this.logLevel = logLevel;
    }

    Verbosity getVerbosity() {

        return verbosity;
    }

    void setVerbosity(Verbosity verbosity) {

        this.verbosity = verbosity;
    }

    Boolean getDryRun() {

        return dryRun;
    }

    void setDryRun(Boolean dryRun) {

        this.dryRun = dryRun;
    }

    Boolean getCopyOnly() {

        return copyOnly;
    }

    void setCopyOnly(Boolean copyOnly) {

        this.copyOnly = copyOnly;
    }
}
