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
 * Represents the classification results for a file.
 */
data class Classification(var name: String = "",
                          var fileName: String = "",
                          var baseName: String = "",
                          var extension: String = "",
                          var season: Int = 0,
                          var episode: Int = 0,
                          var tvShowName: String = "",
                          var tvShowRest: String = "",
                          var tvShowFolderName: String = "",
                          var albumName: String = "")
