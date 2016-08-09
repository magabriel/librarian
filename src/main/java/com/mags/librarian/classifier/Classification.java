/*
 * This file is part of the librarian application.
 *
 * Copyright (c) Miguel Angel Gabriel <magabriel@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package com.mags.librarian.classifier;


/**
 * Represents the classification results for a file.
 */
public class Classification {

    public String name = "";
    public Integer season = 0;
    public Integer episode = 0;
    public String tvShowName = "";
    public String tvShowNamePostSeparator = "";
    public String tvShowRest = "";
    public String tvShowFolderName = "";

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Classification that = (Classification) obj;

        return this.hashCode() == obj.hashCode();
    }

    public String toString() {
        if (name.equals("")) {
            return super.toString();
        }

        if (name.equals("tvshows")) {
            return String.format("%s: \"%s\" (%s/%s) %s %s (sep:%s)", name, tvShowName, season, episode,
                                 tvShowFolderName, tvShowRest, tvShowNamePostSeparator);
        }

        return String.format("%s", name);
    }

    @Override
    public int hashCode() {

        int result = name.hashCode();
        result = 31 * result + season.hashCode();
        result = 31 * result + episode.hashCode();
        result = 31 * result + tvShowName.hashCode();
        result = 31 * result + tvShowNamePostSeparator.hashCode();
        result = 31 * result + tvShowRest.hashCode();
        result = 31 * result + tvShowFolderName.hashCode();
        return result;
    }
}
