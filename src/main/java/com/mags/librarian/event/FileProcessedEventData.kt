/*
 * This file is part of the librarian application.
 *
 * Copyright (c) 2016 Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code
 */

package com.mags.librarian.event

import com.mags.librarian.classifier.Classification

class FileProcessedEventData : EventData {

    var inputFolder: String? = null
    var inputFilename: String? = null
    var outputFolder: String? = null
    var outputFilename: String? = null
    var fileClassification: Classification? = null
    var action: String? = null
    var actionPerformed: String? = null

}
