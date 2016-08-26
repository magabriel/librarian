# Librarian

[![Build Status](https://travis-ci.org/magabriel/librarian.svg?branch=master)](https://travis-ci.org/magabriel/librarian)

**librarian** is a rule-based file classifier.

## Description

**librarian** reads files from certain filesystem folders and moves/copies them to some other configured destination folders.
 
The configuration is written in an easy-to-customize YAML file.

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

### 0.2

- Many refactorings and fixes.

## Documentation

### Requirements

- Java 8.

### Installation

- From source:

    Clone this repository and do:
    
    ~~~YAML
    ./gradlew java
    ~~~

- Or download the last distribution `jar`.

### Usage

`java -jar /path/to/librarian.jar --help` will print all command line arguments.

### The `librarian.yml` file

Running `java -jar /path/to/librarian.jar` in an empty folder will produce an error message complaining that no 
`librarian.yml` can be found. 
 
#### Create a default `librarian.yml`

To create a default `librarian.yaml`, run  `java -jar /path/to/librarian.jar --create-config` and the default 
configuration file will be created in the current folder.

~~~YAML
# Default librarian configuration file

config:
  content_types:
    - tvshows: "(?<name>.+)S(?<season>[0-9]{1,2})E(?<episode>[0-9]{1,3})(?<rest>.*)"
    - tvshows: "(?<name>.+(?:[^\\p{Alnum}]))(?<season>[0-9]{1,2})x(?<episode>[0-9]{1,3})(?<rest>.*)"
    - tvshows: "(?<name>.+(?:[^\\p{Alnum}]))(?<season>[0-9])(?<episode>[0-9]{2})(?<rest>.*)"
    - music: '\.mp3|\.ogg|music|album|disco|cdrip'
    - videos: '\.avi|\.mpeg|\.mpg|\.mov|\.wmv|\.mp4|\.m4v|\.mkv'
    - books: 'ebook|\.pdf|\.epub|\.fb2'

  tvshows:
    numbering_schema: "S{season:2}E{episode:2}"
    season_schema: "Season_{season:2}"

    words_separator:
      show: "_"
      file: "_"

input:
  folders:
    - /my/input/folder1
    - /my/input/folder2

output:
  folders:
    -
      path: /my/output/folder/tvshows
      contents: tvshows
      auto_create: false

    -
      path: /my/output/folder/tvshows2
      contents: tvshows
      auto_create: true

    -
      path: /my/output/folder/movies
      contents: videos

    -
      path: /my/output/folder/music
      contents: music

    -
      path: /my/output/folder/books
      contents: books

~~~

#### Customize `librarian.yaml`

- `config.content_types`: A list of content types definitions, in the form `name: regular expression`. Feel free to use 
   your own names except for the special types `tvshows` and `music` entries, which may also requires an special format 
   (see below).
    
- `config.tvshows.numbering_schema`: The numbering schema to use for output TV shows episode files. The file will be 
   renamed using this pattern. You can use `{season:N}` and `{episode:N}` placeholders for season and episode numbers, 
   where `N` stands for the length, zero padded.

- `config.tvshows.season_schema`: The nameing schema to use TV shows season folders. The folders will be created using 
   this pattern. `{season:N}` is available as explained above.

- `config.tvshows.words_separator`: Contains the characters that will be used to replace word separators in show folders 
   and the episode file itself.

- `input.folders`: A list of paths to one or more input folders (i.e. where the input file will be found).

- `output.folders`: A list of output folders definitions (where the files will be copied to). See below for format.

##### Content types

A content type definition has just a name and a regular expression that will be tested against each input file. 

~~~YAML
- music: '\.mp3|\.ogg|music|album|disco|cdrip' 
~~~

will match files with `.mp3` or `.ogg` extensions and also files with "music", "album", "disco" or "cdrip" in its name.

###### TV Shows
   
**Tv shows** are special, because we need to capture the name of the show and the season and episode numbers. The following 
two default definitions cover pretty much all the cases:

~~~YAML
- tvshows: "(?<name>.+)S(?<season>[0-9]{1,2})E(?<episode>[0-9]{1,3})(?<rest>.*)"
- tvshows: "(?<name>.+(?:[^\\p{Alnum}]))(?<season>[0-9]{1,2})x(?<episode>[0-9]{1,3})(?<rest>.*)"
- tvshows: "(?<name>.+(?:[^\\p{Alnum}]))(?<season>[0-9])(?<episode>[0-9]{2})(?<rest>.*)"
~~~

These defintions will match files of the form `My tv show name S01E02 whatever.*`, `My tv show name 01x02 whatever.*` and
`My tv show name 102 whatever.*`

Things to remember:

- The definition name **must** be `tvshows`.
- There can be several defintions with the same name.
- Each of the regexes **must** have the following capture groups:
    - `name`: the name of the TV show.
    - `season`: the season number.
    - `episode`: the episode number.
    - `rest`: any other information left in the filename. 

###### Music albums

Files matched by `music` content type are assumed to be individual tracks in an album if they are inside a subfolder. 
The subfolder containing the files will be copied as is.

### Output folders definitions
 
`output.folders` is a list of output folders definition, each one of the form:

- `path`: The absolute or relative path of that folder.
- `contents`: The name of one of the content types defined in `config.content_types`.

The last one will be used as default for new TV shows.
 
### Execute the process

`java -jar /path/to/librarian.jar` will read the `librarian.yml` file in the current directory and act accordingly.

Both a log and a RSS files are writen in the current directory explaining what has been done. 

Several aspects can be customized using the provided command liner arguments: `java -jar /path/to/librarian.jar --help`
