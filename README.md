# imdb-list-analyzer

Internet Movie Database, or IMDb for short, offers a massive amount of information about movies, series, and the like. The details about movies and personal ratings are easily viewable on www.imdb.com, but the analysis capability on the IMDb site is lacking.

`imdb-list-analyzer` provides tools for analyzing IMDb movie ratings, and personal ratings in particular.


## Installation
The software is implemented in Clojure. It currently requires:

* Clojure 1.7.0 or newer
* clojure.data.csv 0.1.4 or newer
* cheshire 5.5.0 or newer
* Leiningen 2.2.0 or newer (optional)

Installation guide:

1. Install Clojure from http://clojure.org/downloads
2. Install Leiningen from http://leiningen.org/
3. Download the source code from GitHub: http://github.com/dresa/imdb-list-analyzer
4. Run `lein deps` to download the required libraries from Clojars with Leiningen


## Usage on command-line

Usage with Leiningen:

    $ lein run <filenameA> [filenameB] [in-encoding=<encoding>] [out-encoding=<encoding>]

The program uses the command-line arguments:

* `<filenameA>`: CSV file of IMDb movie ratings
* `[filenameB]`: another CSV file of IMDb movie ratings
* `[in-encoding=<encoding>]`: encoding used in reading ratings file(s)
* `[out-encoding=<encoding>]`: encoding used in analysis results output

Common encoding choices are, for example, `UTF-8`, `windows-1252`, `cp850`, or their aliases. For Windows Command Prompt users, Scandinavian characters require `cp850` encoding for output.

Functionality:

* If no arguments are given, a usage message is shown.
* If one filename is given, return a full single-list analysis result.
* If two filenames are given, return a full dual-list cross-analysis result.

If Leiningen is not available, launch the main program `core.clj` with an ordinary combination of `java` and `clojure.jar`. Add the command-line parameters as usual.


## Input files

Each IMDb user is able to export their own movie ratings from IMDb webpage as a CSV file. It is exactly this format that the input file is expected to satisfy.

If you have rated movies on IMDb, you can your grab a personal ratings list as follows:

1. Login to http://www.imdb.com with your account.
2. Go to *"Your Ratings"* department (in you Account Menu), showing all your movie ratings.
3. Click the three dots on top of screen, and choose *"Export"*.
4. Save the CSV file in your filesystem.

There exist *old* and *new* formats for IMDb CSV files; we call them *pre-2017* and *post-2018* formats. IMDb has [published some information about the change](https://getsatisfaction.com/imdb/topics/updates-to-the-ratings-pages-and-functionality) in the format. The software accepts both old and new IMDb CSV formats. Its output continues to use fieldnames that match with the old format (for consistency).

The original IMDb CSV format, until end of 2017, contained the following column headers: *"position", "const", "created", "modified", "description", "Title", "Title type", "Directors", "You rated", "IMDb Rating", "Runtime (mins)", "Year", "Genres", "Num. Votes", "Release Date (month/day/year)",* and *"URL"*.

The new IMDb CSV format, valid from the end of 2017, contains the following column headers: *"Const", "Your Rating", "Date Rated", "Title", "URL", "Title Type", "IMDb Rating", "Runtime (mins)", "Year", "Genres", "Num Votes", "Release Date"*, and *"Directors"*.

Until 2017, the default encoding of ratings file was `UTF-8`, but since 2018, `windows-1252` is used instead (also known as `cp1252` and "*ANSI*").


## Output
For a one-file analysis, we will see the following results:

* **number** of movie ratings in the list
* **mean** movie ratings (compared to IMDb average rating)
* **standard deviation** of list ratings (compared to stdev of IMDb averages)
* Pearson **correlation** coefficient between list ratings and IMDb averages
* **frequencies** of each rating (compared to rounded IMDb averages)
* Shannon information **entropy** of empirical rating distribution
* **Best 20 directors** (statistical p-value)
* **Worst 20 directors** (statistical p-value)
* **Surprising likes** (difference in your p-value and IMDb's p-value)
* **Surprising dislikes** (difference in your p-value and IMDb's p-value)
* **Genre analysis** (average ratings per genre)
* **Yearly analysis** (average ratings per year)

For a two-file analysis, we have the following results:

* **number** of shared movie titles
* Pearson **correlation** between the ratings of shared titles in the two lists

### Statistical test
The best and worst directors are chosen by a custom statistical test. It is based on the empirical distribution of ratings found in the list. We use a statistical *p*-value as a quality measure: how likely a director's movie rating average exceeds that of a random set of ratings? If *p*-value is close to 1 (good) or close to 0 (bad), it means that the average rating of director's movies cannot be explained by randomness, but the director's skill (or lack thereof).


## Visualization of analysis results

This project of analyzing IMDb ratings has been extended by [tkasu](https://github.com/tkasu/imdb-list-analyzer), who has created visualizations and graphical representations for the analysis results of this project.


## Example with one IMDb list on Windows

    $ lein run resources\rates_A.csv in-encoding=windows-1252 out-encoding=cp850

Gives the following result:

    -------------------------------------
    - IMDb single-list analysis results -
    -------------------------------------
    Number of movie ratings
    2074

    Mean of movie ratings
    5.420 (IMDb: 7.196)

    Standard deviation of movie ratings
    2.800 (IMDb: 0.953)

    Correlation between ratings and IMDb rating averages
    0.562

    Frequencies of ratings
    Rate 1 occurs 190 times (9.161 %) versus IMDb 0
    Rate 2 occurs 210 times (10.125 %) versus IMDb 0
    Rate 3 occurs 231 times (11.138 %) versus IMDb 15
    Rate 4 occurs 222 times (10.704 %) versus IMDb 25
    Rate 5 occurs 215 times (10.366 %) versus IMDb 64
    Rate 6 occurs 202 times (9.740 %) versus IMDb 269
    Rate 7 occurs 215 times (10.366 %) versus IMDb 745
    Rate 8 occurs 220 times (10.608 %) versus IMDb 884
    Rate 9 occurs 191 times (9.209 %) versus IMDb 71
    Rate 10 occurs 178 times (8.582 %) versus IMDb 1

    Entropy of ratings (in bits)
    3.318 (in IMDb 1.892; maximum is 3.322)

	The best directors:
    Director-name; Rank-p-value; Rates
    ----------------------------------
    Chuck Jones               ; 1.0   ; [9 9 9 9 8 9 10]
    Christopher Nolan         ; 1.0   ; [8 10 10 7 8 10 10 9 10]
                              ; 1.0   ; [9 7 6 5 4 6 10 10 9 6 10 8 6 9 8 8 8 10 5 10 10 10 10 10]
    Wes Anderson              ; 0.999 ; [10 7 10 9 8]
    Darren Aronofsky          ; 0.999 ; [10 9 10 7 5 10 10]
    Charles Chaplin           ; 0.999 ; [10 8 7 10 9 8]
    Lee Unkrich               ; 0.999 ; [8 9 9 10 10]
    Buster Keaton             ; 0.998 ; [7 8 8 6 6 10 8 10 8]
    Pete Docter               ; 0.996 ; [10 9 10]
    Ang Lee                   ; 0.994 ; [9 8 8 9 8]
    Peter Jackson             ; 0.992 ; [9 7 10 10 10 6 4 5 8]
    Jani-Petteri Passi        ; 0.989 ; [10 10]
    Jukka Kärkkäinen          ; 0.989 ; [10 10]
    Richard Linklater         ; 0.988 ; [10 9 7 10 5]
    Lars von Trier            ; 0.988 ; [6 8 10 10 7]
    Sergio Leone              ; 0.988 ; [6 9 10 9 7]
    George Cukor              ; 0.987 ; [8 10 8 9]
    Alfonso Cuarón            ; 0.987 ; [10 10 7 8]
    Bryan Singer              ; 0.987 ; [8 7 9 9 6 9 7 5]
    Alejandro Amenábar        ; 0.986 ; [10 9 5 10]
    Andrew Stanton            ; 0.981 ; [8 10 9]
    Ron Howard                ; 0.98  ; [6 10 9 9 9 4]
    Gore Verbinski            ; 0.979 ; [9 5 10 8 8]
    John Lasseter             ; 0.976 ; [10 8 7 8]
    George Roy Hill           ; 0.976 ; [10 10 6 7]
    Damien Chazelle           ; 0.975 ; [10 9]
    James Gunn                ; 0.975 ; [10 9]
    Brad Bird                 ; 0.97  ; [8 9 10 8 4]
    Ben Affleck               ; 0.969 ; [9 8 9]
    Terry Jones               ; 0.969 ; [10 10 6]

    The worst directors:
    Director-name; Rank-p-value; Rates
    ----------------------------------
    Simon Wincer              ; 0.058 ; [3 1]
    Lana Wachowski            ; 0.058 ; [3 7 4 2 1]
    Jonathan Hensleigh        ; 0.058 ; [3 1]
    Kevin Greutert            ; 0.058 ; [2 2]
    Roland Emmerich           ; 0.058 ; [7 4 1 4 1]
    Lee Tamahori              ; 0.058 ; [1 3]
    Daniel Alfredson          ; 0.058 ; [3 1]
    Stephen Herek             ; 0.058 ; [2 2]
    Lilly Wachowski           ; 0.058 ; [3 7 4 2 1]
    Louis Leterrier           ; 0.056 ; [1 4 5 2]
    Sydney Pollack            ; 0.056 ; [4 2 2 4]
    Simon West                ; 0.04  ; [2 1 4]
    Brad Silberling           ; 0.026 ; [1 2]
    Ted Post                  ; 0.026 ; [1 2]
    Claes Olsson              ; 0.026 ; [2 1]
    Jerry Paris               ; 0.026 ; [2 1]
    Walter Hill               ; 0.026 ; [3 3 2 3 4]
    Peter Berg                ; 0.019 ; [6 1 4 2 2 3]
    Sylvester Stallone        ; 0.014 ; [2 4 2 2 4]
    Alexander Payne           ; 0.013 ; [3 2 2 2]
    Ere Kokkonen              ; 0.009 ; [1 1 1 1 4 2 9 5]
    John Moore                ; 0.008 ; [3 1 1]
    Spede Pasanen             ; 0.007 ; [1 1]
    Michael Bay               ; 0.007 ; [2 2 4 1 3]
    Paul W.S. Anderson        ; 0.007 ; [1 1]
    Jason Friedberg           ; 0.007 ; [1 1]
    Peter Hyams               ; 0.007 ; [1 1]
    Ivan Reitman              ; 0.007 ; [1 8 1 1 1]
    Aaron Seltzer             ; 0.007 ; [1 1]
    William Lustig            ; 0.007 ; [1 1]

    Surprising likes: Title; Rate; IMDb average; Diff in p-value
    Kesäkaverit                         ; 9  ; 5.6 ; 0.804
    Dumb and Dumber To                  ; 9  ; 5.6 ; 0.804
    Kuutamolla                          ; 9  ; 5.7 ; 0.796
    Iron Sky                            ; 9  ; 5.9 ; 0.779
    Immortals                           ; 9  ; 6.0 ; 0.764
    Tonight She Comes                   ; 8  ; 3.9 ; 0.756
    Clarence                            ; 10 ; 6.5 ; 0.754
    Mother!                             ; 10 ; 6.6 ; 0.730
    Knight and Day                      ; 9  ; 6.3 ; 0.713
    Musta jää                           ; 10 ; 6.7 ; 0.705
    Commando                            ; 10 ; 6.7 ; 0.705
    The Blair Witch Project             ; 9  ; 6.4 ; 0.688
    Sin City: A Dame to Kill For        ; 9  ; 6.5 ; 0.665
    Sky Captain and the World of Tomorro; 8  ; 6.1 ; 0.651
    Rentun ruusu                        ; 9  ; 6.6 ; 0.641
    Spider-Man 3                        ; 8  ; 6.2 ; 0.636
    Uuno Turhapuro armeijan leivissä    ; 9  ; 6.7 ; 0.616
    The Ninth Gate                      ; 9  ; 6.7 ; 0.616
    Rare Exports                        ; 9  ; 6.7 ; 0.616
    The Other Guys                      ; 9  ; 6.7 ; 0.616
    Prozac Nation                       ; 8  ; 6.3 ; 0.614
    The Cell                            ; 8  ; 6.3 ; 0.614
    Last Action Hero                    ; 8  ; 6.3 ; 0.614
    The Kids Are All Right              ; 10 ; 7.0 ; 0.609
    Game Night                          ; 10 ; 7.0 ; 0.609
    Leijonasydän                        ; 10 ; 7.0 ; 0.609
    Maleficent                          ; 10 ; 7.0 ; 0.609
    The Broken                          ; 7  ; 5.5 ; 0.608
    Veijarit                            ; 7  ; 5.5 ; 0.608
    Presidentintekijät                  ; 7  ; 5.7 ; 0.592
    
    Surprising dislikes: Title; Rate; IMDb average; Diff in p-value
    Les triplettes de Belleville        ; 1  ; 7.8 ; -0.706
    Dabba                               ; 1  ; 7.8 ; -0.706
    Harold and Maude                    ; 2  ; 8.0 ; -0.708
    Rosemary's Baby                     ; 2  ; 8.0 ; -0.708
    Komisario Palmun erehdys            ; 2  ; 8.0 ; -0.708
    The Searchers                       ; 2  ; 8.0 ; -0.708
    The Night of the Hunter             ; 2  ; 8.0 ; -0.708
    Stalag 17                           ; 2  ; 8.0 ; -0.708
    You Can't Take It with You          ; 2  ; 8.0 ; -0.708
    No Man's Land                       ; 2  ; 8.0 ; -0.708
    Witness for the Prosecution         ; 3  ; 8.4 ; -0.717
    The Cove                            ; 3  ; 8.4 ; -0.717
    Once Were Warriors                  ; 1  ; 7.9 ; -0.746
    Offret                              ; 2  ; 8.1 ; -0.761
    Paris, Texas                        ; 2  ; 8.1 ; -0.761
    Stalker                             ; 2  ; 8.1 ; -0.761
    The Man Who Shot Liberty Valance    ; 2  ; 8.1 ; -0.761
    Kakushi-toride no san-akunin        ; 2  ; 8.1 ; -0.761
    Amores perros                       ; 2  ; 8.1 ; -0.761
    Underground                         ; 2  ; 8.1 ; -0.761
    Harvey                              ; 1  ; 8.0 ; -0.805
    Hable con ella                      ; 1  ; 8.0 ; -0.805
    Krótki film o milosci               ; 2  ; 8.3 ; -0.810
    Rashômon                            ; 2  ; 8.3 ; -0.810
    Kimi no na wa.                      ; 2  ; 8.4 ; -0.823
    Sunset Blvd.                        ; 2  ; 8.4 ; -0.823
    3 Idiots                            ; 2  ; 8.4 ; -0.823
    It's a Wonderful Life               ; 2  ; 8.6 ; -0.841
    Barry Lyndon                        ; 1  ; 8.1 ; -0.857
    M - Eine Stadt sucht einen Mörder   ; 1  ; 8.3 ; -0.906

    Genre analysis:
    Genre; Count; Average rate; IMDb average rate; Average quantile
    Short         ; 26  ; 7.462; 7.912; 0.712
    Animation     ; 152 ; 6.414; 7.630; 0.604
    Biography     ; 142 ; 6.359; 7.570; 0.599
    Music         ; 52  ; 6.250; 7.571; 0.588
    Musical       ; 54  ; 6.167; 7.515; 0.580
    War           ; 111 ; 6.153; 7.665; 0.579
    Documentary   ; 37  ; 6.054; 7.697; 0.569
    History       ; 101 ; 5.901; 7.578; 0.554
    Western       ; 46  ; 5.761; 7.430; 0.540
    Drama         ; 1148; 5.723; 7.433; 0.537
    Family        ; 193 ; 5.720; 7.298; 0.536
    Adventure     ; 459 ; 5.590; 7.146; 0.524
    Romance       ; 328 ; 5.521; 7.298; 0.517
    Sci-Fi        ; 329 ; 5.456; 6.994; 0.510
    Mystery       ; 270 ; 5.411; 7.210; 0.506
    Sport         ; 56  ; 5.393; 7.198; 0.504
    Crime         ; 401 ; 5.272; 7.204; 0.491
    Fantasy       ; 272 ; 5.199; 7.182; 0.484
    Comedy        ; 635 ; 5.096; 7.017; 0.473
    Thriller      ; 632 ; 5.085; 7.072; 0.472
    Action        ; 507 ; 5.067; 6.906; 0.470
    Horror        ; 203 ; 4.394; 6.583; 0.400
    Film-Noir     ; 16  ; 4.188; 7.863; 0.378

    Yearly analysis:
    Year; Count; Average rate; IMDb average rate; Average quantile
    1926 ; 1  ; 10.00; 8.20 ; 0.96
    1924 ; 1  ; 10.00; 8.20 ; 0.96
    1935 ; 1  ; 9.00 ; 8.00 ; 0.87
    1921 ; 2  ; 9.00 ; 8.05 ; 0.87
    1972 ; 4  ; 8.50 ; 8.45 ; 0.82
    1928 ; 3  ; 8.33 ; 8.10 ; 0.80
    1942 ; 4  ; 8.00 ; 7.83 ; 0.77
    1930 ; 2  ; 8.00 ; 7.85 ; 0.77
    1925 ; 2  ; 8.00 ; 8.10 ; 0.77
    1902 ; 1  ; 8.00 ; 8.20 ; 0.77
    1938 ; 4  ; 7.50 ; 7.90 ; 0.72
    1923 ; 2  ; 7.50 ; 8.05 ; 0.72
    1920 ; 2  ; 7.50 ; 7.95 ; 0.72
    1927 ; 3  ; 7.33 ; 7.87 ; 0.70
    1957 ; 7  ; 7.29 ; 8.37 ; 0.69
    1952 ; 7  ; 7.29 ; 7.91 ; 0.69
    1964 ; 8  ; 7.13 ; 7.24 ; 0.68
    1951 ; 4  ; 7.00 ; 8.13 ; 0.66
    1936 ; 2  ; 7.00 ; 8.00 ; 0.66
    1933 ; 3  ; 7.00 ; 7.87 ; 0.66
    1939 ; 5  ; 6.80 ; 7.92 ; 0.64
    1956 ; 6  ; 6.67 ; 7.73 ; 0.63
    1944 ; 2  ; 6.50 ; 8.15 ; 0.61
    1934 ; 2  ; 6.50 ; 7.75 ; 0.61
    1929 ; 2  ; 6.50 ; 7.80 ; 0.61
    1980 ; 13 ; 6.38 ; 7.38 ; 0.60
    1948 ; 3  ; 6.33 ; 8.20 ; 0.60
    1940 ; 9  ; 6.33 ; 7.47 ; 0.60
    1984 ; 23 ; 6.26 ; 7.34 ; 0.59
    1976 ; 9  ; 6.22 ; 7.33 ; 0.59
    1968 ; 8  ; 6.13 ; 7.71 ; 0.58
    1966 ; 7  ; 6.00 ; 7.96 ; 0.56
    1932 ; 2  ; 6.00 ; 7.45 ; 0.56
    2014 ; 81 ; 5.99 ; 7.25 ; 0.56
    2010 ; 63 ; 5.94 ; 7.08 ; 0.56
    2018 ; 26 ; 5.92 ; 7.23 ; 0.56
    1975 ; 10 ; 5.90 ; 7.58 ; 0.55
    2012 ; 67 ; 5.90 ; 7.08 ; 0.55
    1982 ; 16 ; 5.88 ; 7.48 ; 0.55
    2002 ; 51 ; 5.86 ; 7.24 ; 0.55
    1953 ; 6  ; 5.83 ; 8.13 ; 0.55
    1981 ; 10 ; 5.80 ; 7.19 ; 0.54
    1959 ; 10 ; 5.80 ; 7.45 ; 0.54
    1979 ; 16 ; 5.75 ; 7.37 ; 0.54
    2008 ; 65 ; 5.74 ; 7.11 ; 0.54
    2017 ; 69 ; 5.72 ; 7.14 ; 0.54
    2004 ; 61 ; 5.69 ; 7.22 ; 0.53
    1995 ; 37 ; 5.65 ; 7.32 ; 0.53
    1961 ; 5  ; 5.60 ; 7.84 ; 0.52
    1922 ; 5  ; 5.60 ; 7.26 ; 0.52
    2015 ; 77 ; 5.57 ; 7.23 ; 0.52
    2011 ; 72 ; 5.57 ; 7.09 ; 0.52
    1994 ; 33 ; 5.55 ; 7.28 ; 0.52
    2013 ; 80 ; 5.54 ; 7.12 ; 0.52
    1997 ; 50 ; 5.52 ; 7.02 ; 0.52
    2006 ; 64 ; 5.50 ; 7.16 ; 0.51
    1969 ; 6  ; 5.50 ; 7.57 ; 0.51
    1954 ; 6  ; 5.50 ; 8.12 ; 0.51
    2009 ; 71 ; 5.49 ; 7.20 ; 0.51
    1973 ; 16 ; 5.44 ; 7.56 ; 0.51
    1941 ; 5  ; 5.40 ; 7.88 ; 0.50
    1985 ; 23 ; 5.39 ; 7.23 ; 0.50
    1993 ; 32 ; 5.38 ; 6.94 ; 0.50
    2003 ; 48 ; 5.27 ; 7.07 ; 0.49
    1988 ; 27 ; 5.26 ; 7.14 ; 0.49
    1937 ; 4  ; 5.25 ; 7.75 ; 0.49
    1931 ; 4  ; 5.25 ; 7.93 ; 0.49
    2007 ; 67 ; 5.22 ; 7.19 ; 0.49
    2001 ; 55 ; 5.18 ; 6.98 ; 0.48
    2016 ; 97 ; 5.14 ; 7.10 ; 0.48
    1987 ; 23 ; 5.13 ; 6.75 ; 0.48
    1974 ; 9  ; 5.00 ; 7.34 ; 0.46
    1963 ; 10 ; 5.00 ; 7.53 ; 0.46
    1946 ; 6  ; 5.00 ; 7.85 ; 0.46
    1983 ; 13 ; 4.92 ; 6.90 ; 0.46
    1999 ; 51 ; 4.84 ; 7.14 ; 0.45
    1996 ; 24 ; 4.83 ; 6.94 ; 0.45
    1967 ; 6  ; 4.83 ; 7.87 ; 0.45
    1949 ; 4  ; 4.75 ; 7.20 ; 0.44
    1977 ; 11 ; 4.73 ; 6.91 ; 0.43
    1998 ; 47 ; 4.62 ; 6.92 ; 0.42
    1978 ; 7  ; 4.57 ; 6.67 ; 0.42
    2005 ; 44 ; 4.55 ; 7.02 ; 0.42
    1991 ; 24 ; 4.54 ; 7.05 ; 0.42
    1971 ; 12 ; 4.50 ; 7.52 ; 0.41
    2000 ; 48 ; 4.42 ; 6.76 ; 0.40
    1986 ; 18 ; 4.39 ; 7.06 ; 0.40
    1958 ; 7  ; 4.29 ; 7.81 ; 0.39
    1990 ; 23 ; 4.26 ; 6.87 ; 0.39
    1970 ; 6  ; 4.17 ; 7.22 ; 0.38
    1960 ; 7  ; 4.14 ; 7.77 ; 0.37
    1992 ; 32 ; 4.13 ; 6.91 ; 0.37
    1965 ; 6  ; 4.00 ; 7.18 ; 0.36
    1947 ; 3  ; 4.00 ; 7.80 ; 0.36
    1962 ; 8  ; 3.75 ; 7.77 ; 0.33
    1955 ; 4  ; 3.75 ; 7.70 ; 0.33
    1950 ; 6  ; 3.67 ; 8.12 ; 0.32
    1989 ; 23 ; 3.61 ; 6.92 ; 0.32
    1943 ; 2  ; 3.00 ; 7.55 ; 0.25
    1945 ; 1  ; 2.00 ; 7.60 ; 0.14



## Example with two IMDb lists:

    $ lein run resources\rates_A.csv resources\rates_B.csv

Gives the following result:

    -----------------------------------
    - IMDb dual-list analysis results -
    -----------------------------------
    Number of shared titles
    1814

    Correlation between ratings
    0.792


## To do

* Extended GUI, more visualizations
* Extended analytics capabilities.
* Increase coverage of unit tests on core functions.
* Possibility to retrieve the ratings directly from IMDb website via authentication (if possible).


## License

Copyright © 2013–2018 Esa Junttila

Distributed under the Eclipse Public License, the same as Clojure.
