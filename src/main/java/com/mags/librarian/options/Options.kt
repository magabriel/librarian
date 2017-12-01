/*
 * This file is part of the librarian application.
 *
 * Copyright (c) 2017 Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code
 */

package com.mags.librarian.options

import java.util.logging.Level

/**
 * Stores the user options.
 */
data class Options(var verbosity: Verbosity = Verbosity.NORMAL,
                   var logLevel: Level = Level.INFO,
                   var dryRun: Boolean = false,
                   var copyOnly: Boolean = false,
                   var configFileName: String = "",
                   var logFileName: String = "",
                   var rssFileName: String = "",
                   var help: Boolean = false,
                   var createConfig: Boolean = false) {

    enum class Verbosity {
        NONE, NORMAL, HIGH
    }

}
