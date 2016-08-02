# Librarian

[![Build Status](https://travis-ci.org/magabriel/librarian.svg?branch=master)](https://travis-ci.org/magabriel/librarian)

**librarian** is a rule-based file classifier.

## Description

**librarian** reads files from certain filesystem folders and moves/copies them to some other configured destination folders.
 
The configuration is written in an easy to customize YAML file.

### Motivation

I wanted a way to automatically move my downloaded files to certain folders. This project replaces a bash script
I hacked together quite some time ago in a more elegant and efficient way (I hope). Also, it is a good excuse
to teach myself Java.

## Features

- YAML configuration file.
- Input files can be any type (regex driven on filename).
- TV shows episodes are recognized via a special regex and classified by TV show name and season.
- Optional logging.
- Optional RSS feed.

## Changelog

### 0.1

- First functional version.
