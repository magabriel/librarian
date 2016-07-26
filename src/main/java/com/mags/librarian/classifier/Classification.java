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
 * Represents the classification results.
 */
public class Classification {

    private String name = "";
    private Integer season = 0;
    private Integer episode = 0;
    private String tvshowName = "";
    private String tvshowRest = "";

    @Override
    public boolean equals(Object obj) {
        return this.toString().equals(obj.toString());
    }

    public String toString() {
        if (name.equals("")) {
            return super.toString();
        }

        if (name.equals("tvshows")) {
            return String.format("%s: \"%s\" (%s/%s)", name, tvshowName, season, episode);
        }

        return String.format("%s", name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSeason() {
        return season;
    }

    public void setSeason(Integer season) {
        this.season = season;
    }

    public Integer getEpisode() {
        return episode;
    }

    public void setEpisode(Integer episode) {
        this.episode = episode;
    }

    public String getTvshowName() {
        return tvshowName;
    }

    public void setTvshowName(String tvshowName) {
        this.tvshowName = tvshowName;
    }

    public String getTvshowRest() {

        return tvshowRest;
    }

    public void setTvshowRest(String tvshowRest) {

        this.tvshowRest = tvshowRest;
    }
}
