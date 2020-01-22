/*
 * This file is part of the librarian application.
 *
 * Copyright (c) 2016 Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code
 */

package com.mags.librarian.event

data class FileUnknownEventData(var inputFolder: String = "",
                                var inputFilename: String = "",
                                var outputFolder: String = "",
                                var outputFilename: String = "",
                                var action: String = "") : EventData