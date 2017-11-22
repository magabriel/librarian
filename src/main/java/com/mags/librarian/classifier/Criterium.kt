/*
 * This file is part of the librarian application.
 *
 * Copyright (c) Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package com.mags.librarian.classifier

/**
 * Stores a single classification criterium.
 */
data class Criterium(var name: String = "",
                     var extensions: Array<String> = arrayOf<String>(),
                     var filters: Array<String> = arrayOf<String>()) {
}

