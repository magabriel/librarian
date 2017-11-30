/*
 * This file is part of the librarian application.
 *
 * Copyright (c) 2017 Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code
 */

package com.mags.librarian.Options

import java.util.logging.Level

/**
 * Stores the user options.
 */
class Options(var verbosity: Verbosity = Verbosity.NORMAL,
              var logLevel: Level = Level.INFO,
              var dryRun: Boolean = false,
              var copyOnly: Boolean = false,
              var logFileName: String = "",
              var rssFileName: String = "") {

    enum class Verbosity {
        NONE,
        NORMAL,
        HIGH
    }

}
