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
public class Options {

    public enum Verbosity {
        NONE,
        NORMAL,
        HIGH
    }

    private Verbosity verbosity = Verbosity.NORMAL;
    private Level logLevel = Level.INFO;
    private Boolean dryRun = false;
    private Boolean copyOnly = false;

    public Level getLogLevel() {

        return logLevel;
    }

    public void setLogLevel(Level logLevel) {

        this.logLevel = logLevel;
    }

    public Verbosity getVerbosity() {

        return verbosity;
    }

    public void setVerbosity(Verbosity verbosity) {

        this.verbosity = verbosity;
    }

    public Boolean getDryRun() {

        return dryRun;
    }

    public void setDryRun(Boolean dryRun) {

        this.dryRun = dryRun;
    }


    public Boolean getCopyOnly() {

        return copyOnly;
    }

    public void setCopyOnly(Boolean copyOnly) {

        this.copyOnly = copyOnly;
    }
}
