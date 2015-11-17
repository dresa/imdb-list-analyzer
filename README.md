# imdb-list-analyzer

Internet Movie Database, or IMDb for short, offers a massive amount of information about movies, series, and the like. The details about movies and personal ratings are easily viewable on www.imdb.com, but the analysis capability on the IMDb site is lacking.

`imdb-list-analyzer` provides tools analyzing IMDb movie ratings, and personal ratings in particular.

## Demo application

TODO

## Installation
The software is implemented in Clojure. It currently requires:

* Clojure 1.5.1 or newer
* Leiningen 2.2.0 or newer (optional)
* csv-clojure 2.0.1

Installation guide:

1. Install Clojure from http://clojure.org/downloads
2. Install Leiningen from http://leiningen.org/
3. Download the source code from GitHub: http://github.com/dresa/imdb-list-analyzer
4. Run `lein deps` to download the required libraries from Clojars with Leiningen


## Usage

### From command line

For the time being, only a command-line program is available.

Usage with Leiningen:

    $ lein run <filenameA> [filenameB]

The program uses the command-line arguments:

* `<filenameA>` is a CSV file of IMDb movie ratings
* `[filenameB]` is another CSV file of IMDb movie ratings

Functionality:

* If no arguments are given, a usage message is shown.
* If one filename is given, return a full single-list analysis result.
* If two filenames are given, return a full dual-list cross-analysis result.

If Leiningen is not available, launch the main program `core.clj` instead with an ordinary combination of `java` and `clojure.jar`. Add the command-line parameters as usual.

### Web GUI

    $ lein with-profile production ring server

Open http://localhost:3000/ in browser and follow instructions.

If you don't have an IMDB account you can use the example files from 'resources/'.

## Input files

The input file are expected to be CSV files in the IMDb movie list format. Typically, this means a text file that has the following headers: *"position", "const", "created", "modified", "description", "Title", "Title type", "Directors", "You rated", "IMDb Rating", "Runtime (mins)", "Year", "Genres", "Num. Votes", "Release Date (month/day/year)",* and *"URL"*.


If you have rated movies on IMDb, you can your grab a personal ratings list as follows:

1. Login to http://www.imdb.com with your account
2. Go to *"Your Ratings"* department, showing all your movie ratings
3. Click the *"Export this list"* link at the bottom of the screen.
4. Save the CSV file in your filesystem.


## Output
For a one-file analysis, we will see the following results:

* **number** of movie ratings in the list
* **mean** movie ratings (compared to IMDb average rating)
* **standard deviation** of list ratings (compared to stdev of IMDb averages)
* Pearson **correlation** coefficient between list ratings and IMDb averages
* **frequencies** of each rating (compared to rounded IMDb averages)
* Shannon information **entropy** of empirical rating distribution
* **Best 10 directors** (statistical p-value)
* **Worst 10 directors** (statistical p-value)

For a two-file analysis, we have the following results:

* **number** of shared movie titles
* Pearson **correlation** between the ratings of shared titles in the two lists

### Statistical test
The best and worst directors are chosen by a custom statistical test. It is based on the empirical distribution of ratings found in the list. We use a statistical *p*-value as a quality measure: how likely a director's movie rating average exceeds a random set of ratings? If *p*-value is close to 1 (good) or close to 0 (bad), it means that the average rating of director's movies cannot be explained by randomness, but the director's skill (or lack thereof).

## Example with one IMDb list

    $ lein run resources\example_ratings_A.csv

Gives the following result:

    -------------------------------------
    - IMDb single-list analysis results -
    -------------------------------------
    Number of movie ratings
    1647

    Mean of movie ratings
    5.422 (IMDb: 7.237)

    Standard deviation of movie ratings
    2.827 (IMDb: 0.957)

    Correlation between ratings and IMDb rating averages
    0.570

    Frequencies of ratings
    Rate 1 occurs 152 times (9.229 %) versus IMDb 0
    Rate 2 occurs 178 times (10.808 %) versus IMDb 1
    Rate 3 occurs 180 times (10.929 %) versus IMDb 8
    Rate 4 occurs 167 times (10.140 %) versus IMDb 16
    Rate 5 occurs 166 times (10.079 %) versus IMDb 53
    Rate 6 occurs 164 times (9.957 %) versus IMDb 220
    Rate 7 occurs 173 times (10.504 %) versus IMDb 556
    Rate 8 occurs 163 times (9.897 %) versus IMDb 724
    Rate 9 occurs 155 times (9.411 %) versus IMDb 67
    Rate 10 occurs 149 times (9.047 %) versus IMDb 2

    Entropy of ratings (in bits)
    3.319 (in IMDb 1.906; maximum is 3.322)

    The best directors:
    Director-name; Rank-p-value; Rates
    ----------------------------------
    Christopher Nolan         ; 1.0   ; [10 8 10 10 10 10 8 7]
                              ; 1.0   ; [10 4 6 6 10 7 10 6 10 10 8 10 6]
    Charles M. Jones          ; 0.998 ; [9 9 9 9 8]
    Darren Aronofsky          ; 0.997 ; [7 10 5 9 10 10]
    Lee Unkrich               ; 0.994 ; [10 10]
    Wes Anderson              ; 0.994 ; [7 10 10 9]
    Lars Von Trier            ; 0.993 ; [10 10]
    Peter Jackson             ; 0.992 ; [6 7 8 4 10 5 10 9 10]
    Alejandro Amenábar        ; 0.99  ; [9 10 10 5]
    Ron Howard                ; 0.989 ; [10 9 9 6 9]

    The worst directors:
    Director-name; Rank-p-value; Rates
    ----------------------------------
    Louis Leterrier           ; 0.028 ; [1 2 4]
    Jerry Paris               ; 0.026 ; [1 2]
    The Wachowski Brothers    ; 0.015 ; [7 7 2 2 1 1 3 3]
    Peter Hyams               ; 0.013 ; [1 1]
    Ivan Reitman              ; 0.012 ; [1 1 1 1 8]
    Alexander Payne           ; 0.012 ; [2 3 2 2]
    Ere Kokkonen              ; 0.01  ; [9 5 1 1 1 4 2 1]
    Paul Anderson             ; 0.009 ; [1 1]
    Michael Bay               ; 0.009 ; [2 4 3 1 2]
    Sylvester Stallone        ; 0.008 ; [4 2 2 2 2]


## Example with two IMDb lists:

    $ lein run resources\example_ratings_A.csv resources\example_ratings_B.csv

Gives the following result:

    -----------------------------------
    - IMDb dual-list analysis results -
    -----------------------------------
    Number of shared titles
    1411

    Correlation between ratings
    0.794


## To do

* GUI, visualizations
* Extended analytics capabilities.
* Possibility to retrieve the ratings directly from IMDb website via authentication (if possible).


## License

Copyright © 2013–2015 Esa Junttila

Distributed under the Eclipse Public License, the same as Clojure.
