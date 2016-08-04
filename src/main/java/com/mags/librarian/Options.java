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

    Verbosity verbosity = Verbosity.NORMAL;
    Level logLevel = Level.INFO;
    Boolean dryRun = false;
    Boolean copyOnly = false;
    String logFileName = "";
    String rssFileName = "";

}
