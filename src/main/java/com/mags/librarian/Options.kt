/*
 * This file is part of the librarian application.
 *
 * Copyright (c) Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package com.mags.librarian

import java.util.logging.Level

/**
 * Stores the user options.
 */
internal class Options(var verbosity: Verbosity = Verbosity.NORMAL,
                       var logLevel: Level = Level.INFO,
                       var dryRun: Boolean = false,
                       var copyOnly: Boolean = false,
                       var logFileName: String = "",
                       var rssFileName: String = "") {

    internal enum class Verbosity {
        NONE,
        NORMAL,
        HIGH
    }

}
