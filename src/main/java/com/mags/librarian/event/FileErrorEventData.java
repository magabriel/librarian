/*
 * This file is part of the librarian application.
 *
 * Copyright (c) 2016 Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code
 */

package com.mags.librarian.event;

public class FileErrorEventData implements EventData {

    public String inputFolder;
    public String inputFilename;
    public String outputFolder;
    public String outputFilename;
    public String action;
}
