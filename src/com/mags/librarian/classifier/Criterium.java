package com.mags.librarian.classifier;

/**
 * Stores a single classification criterium.
 */
public class Criterium {

    private String name;
    private String regExp;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegExp() {
        return regExp;
    }

    public void setRegExp(String regExp) {
        this.regExp = regExp;
    }
}
