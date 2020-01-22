# Librarian

[![Build Status](https://travis-ci.org/magabriel/librarian.svg?branch=master)](https://travis-ci.org/magabriel/librarian)

**librarian** is a rule-based file classifier.

## Description

**librarian** reads files from certain filesystem folders and moves/copies them to some other configured destination folders.
 
The configuration is written in an easy-to-customize YAML file.

### Motivation

I wanted a way to automatically move my downloaded files to certain folders. This project replaces a bash script 
I hacked together quite some time ago in a more elegant and efficient way (I hope). Also, it is a good excuse 
to teach myself Java (and Kotlin).

## Features

- YAML configuration file.
- Input files can be any type (regex driven on filename).
- TV shows episodes are recognized via a special regex and classified by TV show name and season.
- Optional logging.
- Optional RSS feed.

## Changelog

### 0.8

- Replaced the copy+delete operation by move.

### 0.7

- Added Dagger2.
- Ported to Kotlin.

### 0.5 and before

- Work in progress.

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
config:
  extensions:
    - video: [avi, mpeg, mpg, mov, wmv, mp4, m4v, mkv, srt, sub]
    - audio: [mp3, ogg]
    - book:  [pdf, epub, fb2, mobi, azw]

  filters:
    - tvshow:
        # "name.S01E02.title.avi"
        - "(?<name>.+)S(?<season>[0-9]{1,2})E(?<episode>[0-9]{1,3})(?<rest>.*)"
        # "name.1x02.title.avi"
        - "(?<name>.+(?:[^\\p{Alnum}]))(?<season>[0-9]{1,2})x(?<episode>[0-9]{1,3})(?<rest>.*)"
        # "name.102.title.avi" (avoid matching movies with year)
        - "(?<name>.+(?:[^\\p{Alnum}\\(]))(?<season>[0-9]{1})(?<episode>[0-9]{2})(?:(?<rest>[^0-9].*)|\\z)"

  content_classes:
    - tvshows:
        extension: video
        filter: tvshow
    - videos:
        extension: video
    - music:
        extension: audio
    - books:
        extension: book

  tvshows:
    numbering_schema: "S{season:2}E{episode:2}"
    season_schema: "Season_{season:2}"

    words_separator:
      show: "_"
      file: "_"

  errors:
    unknown_files:
      action: move # ignore, move, delete
      move_path: /my/errors/folder/unknown

    duplicate_files:
      action: move # ignore, move, delete
      move_path: /my/errors/folder/duplicates

    error_files:
      action: move # ignore, move, delete
      move_path: /my/errors/folder/errors

  execute:
    success: "success_script.sh"
    error: "error_script.sh"
      
input:
  folders:
    - /my/input/folder1
    - /my/input/folder2

output:
  folders:
    -
      path: /my/output/folder/tvshows
      contents: tvshows

    -
      path: /my/output/folder/tvshows2
      contents: tvshows

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

- `config.extensions`: A list of extensions, in the form `extension_type: list_of_extensions`. Each filetype can be
   use later to define a content class.
   
- `filters`: A list of filter definitions in the form `filter_name: list_of_regular_expressions`. Feel free to use 
   your own names except for the special types `tvshows` and `music` entries, which may also requires an special format 
   (see below). Filters can be used to define content classes.

- `config.content_classes`: A list of content classes, each one having one extension type and one filter name. The name
   of the content class can be later used when defining input folders.
    
- `config.tvshows.numbering_schema`: The numbering schema to use for output TV shows episode files. The file will be 
   renamed using this pattern. You can use `{season:N}` and `{episode:N}` placeholders for season and episode numbers, 
   where `N` stands for the length, zero padded.

- `config.tvshows.season_schema`: The nameing schema to use TV shows season folders. The folders will be created using 
   this pattern. `{season:N}` is available as explained above.

- `config.tvshows.words_separator`: Contains the characters that will be used to replace word separators in show folders 
   and the episode file itself.

- `config.errors.unknown_files`: Define what to do with unrecognized files. The default action is `ignore`, so they 
   will be left in the input folder. Action `move` will move them to the `move_path` while action `delete` will delete 
   them.

- `config.duplicate.error_files`: Same as `unknown_files` for files with duplicate files errors.

- `config.errors.error_files`: Same as `unknown_files` for files with processing errors.

- `config.execute.success`: A command or script to execute for each successfully processed file. Example:

    ~~~.bash
    #!/bin/bash
    
    INPUTFOLDER=$1
    INPUTFILENAME=$2
    OUTPUTFOLDER=$3
    OUTPUTFILENAME=$4
    CLASS=$5
    ACTION=$6
    
    echo "SUCCESS: $INPUTFOLDER; $INPUTFILENAME; $OUTPUTFOLDER; $OUTPUTFILENAME; $ACTION; $CLASS"
    ~~~   

- `config.execute.error`: A command or script to execute for each errored file. Example:

    ~~~.bash
    #!/bin/bash
    
    INPUTFOLDER=$1
    INPUTFILENAME=$2
    OUTPUTFOLDER=$3
    OUTPUTFILENAME=$4
    ACTION=$5
    
    echo "ERROR: $INPUTFOLDER; $INPUTFILENAME; $OUTPUTFOLDER; $OUTPUTFILENAME; $ACTION"
    ~~~

- `input.folders`: A list of paths to one or more input folders (i.e. where the input file will be found).

- `output.folders`: A list of output folders definitions (where the files will be copied to). See below for format.

###### TV Shows
   
**Tv shows** are special, because we need to capture the name of the show and the season and episode numbers. 

~~~YAML
  filters:
    - tvshow:
        # "name.S01E02.title.avi"
        - "(?<name>.+)S(?<season>[0-9]{1,2})E(?<episode>[0-9]{1,3})(?<rest>.*)"
        # "name.1x02.title.avi"
        - "(?<name>.+(?:[^\\p{Alnum}]))(?<season>[0-9]{1,2})x(?<episode>[0-9]{1,3})(?<rest>.*)"
        # "name.102.title.avi" (avoid matching movies with year)
        - "(?<name>.+(?:[^\\p{Alnum}\\(]))(?<season>[0-9]{1,2})(?<episode>[0-9]{2})(?<rest>[^0-9].*)?"
~~~

These defintions will match files of the form `My tv show name S01E02 whatever.*`, `My tv show name 01x02 whatever.*` and
`My tv show name 102 whatever.*`

Things to remember:

- There can be several defintions with the same name.
- Each of the regexes **must** have the following capture groups:
    - `name`: the name of the TV show.
    - `season`: the season number.
    - `episode`: the episode number.
    - `rest`: any other information left in the filename. 

Finally, the content class name for tvshows **must** be `tvshows`.

###### Music albums

Files matched by `music` content class are assumed to be individual tracks in an album if they are inside a subfolder. 
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
