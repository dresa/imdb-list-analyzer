# imdb-list-analyzer

Internet Movie Database, or IMDb for short, offers a massive amount of information about movies, series, and the like. The details about movies and personal ratings are easily viewable on www.imdb.com, but the analysis capability on the IMDb site is lacking.

`imdb-list-analyzer` provides tools analyzing IMDb movie ratings, and personal ratings in particular.


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


## Usage

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

If Leiningen is not available, launch the main program `core.clj` instead
with an ordinary combination of `java` and `clojure.jar`. Add the command-line parameters as usual.


## Input files

Each IMDb user is able to export their own movie ratings from IMDb webpage as a CSV file.
It is exactly this format that the input file is expected to satisfy.

If you have rated movies on IMDb, you can your grab a personal ratings list as follows:

1. Login to http://www.imdb.com with your account.
2. Go to *"Your Ratings"* department (in you Account Menu), showing all your movie ratings.
3. Click the three dots on top of screen, and choose *"Export"*.
4. Save the CSV file in your filesystem.

There exist *old* and *new* formats for IMDb CSV files. Details about IMDb's change of
format can be found in:
https://getsatisfaction.com/imdb/topics/updates-to-the-ratings-pages-and-functionality

The software accepts both old and new IMDb CSV formats. Its output continues to
use fieldnames that match with the old format (for consistency).

The original IMDb CSV format, until end of 2017, contained the following column headers:
*"position", "const", "created", "modified", "description", "Title", "Title type",
"Directors", "You rated", "IMDb Rating", "Runtime (mins)", "Year", "Genres",
"Num. Votes", "Release Date (month/day/year)",* and *"URL"*.

The new IMDb CSV format, valid from the end of 2017, contains the following column headers:
*"Const", "Your Rating", "Date Rated", "Title", "URL", "Title Type", "IMDb Rating",
"Runtime (mins)", "Year", "Genres", "Num Votes", "Release Date"*, and *"Directors"*.


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
* **Yearly analyisis** (average ratings per year)

For a two-file analysis, we have the following results:

* **number** of shared movie titles
* Pearson **correlation** between the ratings of shared titles in the two lists

### Statistical test
The best and worst directors are chosen by a custom statistical test. It is based
on the empirical distribution of ratings found in the list. We use a statistical
*p*-value as a quality measure: how likely a director's movie rating average
exceeds a random set of ratings? If *p*-value is close to 1 (good) or close
to 0 (bad), it means that the average rating of director's movies cannot be
explained by randomness, but the director's skill (or lack thereof).

## Example with one IMDb list

    $ lein run resources\example_ratings_2018_format_A.csv

Gives the following result:

-------------------------------------
- IMDb single-list analysis results -
-------------------------------------
Number of movie ratings
2008

Mean of movie ratings
5.434 (IMDb: 7.214)

Standard deviation of movie ratings
2.798 (IMDb: 0.928)

Correlation between ratings and IMDb rating averages
0.559

Frequencies of ratings
Rate 1 occurs 181 times (9.014 %) versus IMDb 0
Rate 2 occurs 205 times (10.209 %) versus IMDb 1
Rate 3 occurs 220 times (10.956 %) versus IMDb 9
Rate 4 occurs 214 times (10.657 %) versus IMDb 21
Rate 5 occurs 210 times (10.458 %) versus IMDb 62
Rate 6 occurs 196 times (9.761 %) versus IMDb 261
Rate 7 occurs 210 times (10.458 %) versus IMDb 720
Rate 8 occurs 212 times (10.558 %) versus IMDb 861
Rate 9 occurs 187 times (9.313 %) versus IMDb 72
Rate 10 occurs 173 times (8.616 %) versus IMDb 1

Entropy of ratings (in bits)
3.318 (in IMDb 1.879; maximum is 3.322)

The best directors:
Director-name; Rank-p-value; Rates
----------------------------------
Chuck Jones               ; 1.0   ; [9 9 9 9 8 9 10]
Christopher Nolan         ; 1.0   ; [8 10 10 7 8 10 10 9 10]
                          ; 1.0   ; [9 7 6 5 4 6 10 10 9 6 10 8 6 9 8 8 5 10 10 10 10 10]
Wes Anderson              ; 0.999 ; [10 7 10 9 8]
Darren Aronofsky          ; 0.999 ; [10 9 10 7 5 10 10]
Charles Chaplin           ; 0.999 ; [10 8 7 10 9 8]
Lee Unkrich               ; 0.999 ; [8 9 9 10 10]
Buster Keaton             ; 0.998 ; [7 8 8 6 6 10 8 10 8]
Pete Docter               ; 0.996 ; [10 9 10]
Ang Lee                   ; 0.995 ; [9 8 8 9 8]
Peter Jackson             ; 0.993 ; [9 7 10 10 10 6 4 5 8]
Richard Linklater         ; 0.989 ; [10 9 7 10 5]
Lars von Trier            ; 0.989 ; [6 8 10 10 7]
Sergio Leone              ; 0.989 ; [6 9 10 9 7]
George Cukor              ; 0.986 ; [8 10 8 9]
Alfonso Cuarón            ; 0.986 ; [10 10 7 8]
Alejandro Amenábar        ; 0.985 ; [10 9 5 10]
Andrew Stanton            ; 0.982 ; [8 10 9]
Gore Verbinski            ; 0.98  ; [9 5 10 8 8]
Ron Howard                ; 0.98  ; [6 10 9 9 9 4]

The worst directors:
Director-name; Rank-p-value; Rates
----------------------------------
Stephen Herek             ; 0.057 ; [2 2]
Sydney Pollack            ; 0.055 ; [4 2 2 4]
Louis Leterrier           ; 0.04  ; [1 4 2]
Simon West                ; 0.04  ; [2 1 4]
Walter Hill               ; 0.026 ; [3 3 2 3 4]
Brad Silberling           ; 0.025 ; [1 2]
Ted Post                  ; 0.025 ; [1 2]
Claes Olsson              ; 0.025 ; [2 1]
Jerry Paris               ; 0.025 ; [2 1]
Peter Berg                ; 0.019 ; [6 1 4 2 2 3]
Sylvester Stallone        ; 0.014 ; [2 4 2 2 4]
Alexander Payne           ; 0.013 ; [3 2 2 2]
Ere Kokkonen              ; 0.009 ; [1 1 1 1 4 2 9 5]
John Moore                ; 0.008 ; [3 1 1]
Spede Pasanen             ; 0.007 ; [1 1]
Michael Bay               ; 0.007 ; [2 2 4 1 3]
Paul W.S. Anderson        ; 0.007 ; [1 1]
Peter Hyams               ; 0.007 ; [1 1]
Ivan Reitman              ; 0.007 ; [1 8 1 1 1]
William Lustig            ; 0.007 ; [1 1]

Surprising likes: Title; Rate; IMDb average; Diff in p-value
Kesäkaverit                         ; 9  ; 5.6 ; 0.808
Dumb and Dumber To                  ; 9  ; 5.6 ; 0.808
Kuutamolla                          ; 9  ; 5.7 ; 0.799
Iron Sky                            ; 9  ; 5.9 ; 0.783
Immortals                           ; 9  ; 6.0 ; 0.767
Tonight She Comes                   ; 8  ; 3.9 ; 0.758
Clarence                            ; 10 ; 6.5 ; 0.756
Knight and Day                      ; 9  ; 6.3 ; 0.715
Musta jää                           ; 10 ; 6.7 ; 0.706
Commando                            ; 10 ; 6.7 ; 0.706
Mother!                             ; 10 ; 6.7 ; 0.706
The Blair Witch Project             ; 9  ; 6.4 ; 0.691
Sin City: A Dame to Kill For        ; 9  ; 6.5 ; 0.666
Sky Captain and the World of Tomorro; 8  ; 6.1 ; 0.654
Rentun ruusu                        ; 9  ; 6.6 ; 0.642
Spider-Man 3                        ; 8  ; 6.2 ; 0.638
Uuno Turhapuro armeijan leivissä    ; 9  ; 6.7 ; 0.617
The Ninth Gate                      ; 9  ; 6.7 ; 0.617
Rare Exports                        ; 9  ; 6.7 ; 0.617
The Other Guys                      ; 9  ; 6.7 ; 0.617

Surprising dislikes: Title; Rate; IMDb average; Diff in p-value
Witness for the Prosecution         ; 3  ; 8.4 ; -0.717
The Cove                            ; 3  ; 8.5 ; -0.730
Once Were Warriors                  ; 1  ; 7.9 ; -0.742
Offret                              ; 2  ; 8.1 ; -0.760
Paris, Texas                        ; 2  ; 8.1 ; -0.760
Stalker                             ; 2  ; 8.1 ; -0.760
The Man Who Shot Liberty Valance    ; 2  ; 8.1 ; -0.760
Kakushi-toride no san-akunin        ; 2  ; 8.1 ; -0.760
Amores perros                       ; 2  ; 8.1 ; -0.760
Underground                         ; 2  ; 8.1 ; -0.760
Harvey                              ; 1  ; 8.0 ; -0.801
Hable con ella                      ; 1  ; 8.0 ; -0.801
Krótki film o milosci               ; 2  ; 8.3 ; -0.810
Rashômon                            ; 2  ; 8.3 ; -0.810
Kimi no na wa.                      ; 2  ; 8.4 ; -0.822
3 Idiots                            ; 2  ; 8.4 ; -0.822
Sunset Blvd.                        ; 2  ; 8.5 ; -0.835
It's a Wonderful Life               ; 2  ; 8.6 ; -0.841
Barry Lyndon                        ; 1  ; 8.1 ; -0.856
M - Eine Stadt sucht einen Mörder   ; 1  ; 8.4 ; -0.919

Genre analysis:
Genre; Count; Average rate; IMDb average rate; Average quantile
Short         ; 25  ; 7.600; 7.932; 0.726
Biography     ; 132 ; 6.455; 7.583; 0.606
Animation     ; 148 ; 6.419; 7.634; 0.603
Musical       ; 54  ; 6.222; 7.530; 0.583
Music         ; 49  ; 6.204; 7.565; 0.582
War           ; 108 ; 6.167; 7.677; 0.578
History       ; 100 ; 6.020; 7.609; 0.564
Documentary   ; 36  ; 5.944; 7.717; 0.556
Family        ; 188 ; 5.793; 7.335; 0.542
Western       ; 46  ; 5.761; 7.433; 0.538
Drama         ; 1107; 5.725; 7.439; 0.535
Adventure     ; 452 ; 5.597; 7.178; 0.522
Sport         ; 52  ; 5.538; 7.258; 0.517
Romance       ; 319 ; 5.489; 7.293; 0.512
Sci-Fi        ; 314 ; 5.455; 7.011; 0.508
Mystery       ; 259 ; 5.378; 7.216; 0.500
Crime         ; 391 ; 5.269; 7.231; 0.489
Fantasy       ; 261 ; 5.230; 7.215; 0.485
Comedy        ; 603 ; 5.133; 7.067; 0.475
Thriller      ; 624 ; 5.050; 7.068; 0.466
Action        ; 491 ; 5.031; 6.905; 0.464
Horror        ; 195 ; 4.323; 6.571; 0.390
Film-Noir     ; 16  ; 4.188; 7.881; 0.375

Yearly analysis:
Year; Count; Average rate; IMDb average rate; Average quantile
1926 ; 1  ; 10.00; 8.20 ; 0.96
1924 ; 1  ; 10.00; 8.20 ; 0.96
1935 ; 1  ; 9.00 ; 8.00 ; 0.87
1921 ; 2  ; 9.00 ; 8.05 ; 0.87
1972 ; 4  ; 8.50 ; 8.50 ; 0.82
1928 ; 3  ; 8.33 ; 8.10 ; 0.80
1942 ; 4  ; 8.00 ; 7.83 ; 0.77
1930 ; 2  ; 8.00 ; 7.90 ; 0.77
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
1939 ; 5  ; 6.80 ; 7.94 ; 0.64
1956 ; 6  ; 6.67 ; 7.73 ; 0.63
1944 ; 2  ; 6.50 ; 8.15 ; 0.61
1934 ; 2  ; 6.50 ; 7.75 ; 0.61
1929 ; 2  ; 6.50 ; 7.80 ; 0.61
1980 ; 13 ; 6.38 ; 7.38 ; 0.60
1948 ; 3  ; 6.33 ; 8.20 ; 0.59
1940 ; 9  ; 6.33 ; 7.46 ; 0.59
1984 ; 23 ; 6.26 ; 7.34 ; 0.59
2018 ; 9  ; 6.22 ; 7.32 ; 0.58
1976 ; 9  ; 6.22 ; 7.33 ; 0.58
1968 ; 8  ; 6.13 ; 7.71 ; 0.57
2010 ; 61 ; 6.02 ; 7.13 ; 0.56
1966 ; 7  ; 6.00 ; 7.96 ; 0.56
1932 ; 2  ; 6.00 ; 7.45 ; 0.56
2014 ; 81 ; 5.99 ; 7.26 ; 0.56
2002 ; 50 ; 5.96 ; 7.32 ; 0.56
1982 ; 16 ; 5.88 ; 7.48 ; 0.55
2017 ; 46 ; 5.87 ; 7.07 ; 0.55
2012 ; 65 ; 5.85 ; 7.08 ; 0.55
1953 ; 6  ; 5.83 ; 8.13 ; 0.55
2008 ; 63 ; 5.83 ; 7.17 ; 0.54
1981 ; 10 ; 5.80 ; 7.19 ; 0.54
1959 ; 10 ; 5.80 ; 7.46 ; 0.54
1979 ; 16 ; 5.75 ; 7.37 ; 0.54
2004 ; 61 ; 5.69 ; 7.22 ; 0.53
1995 ; 37 ; 5.65 ; 7.32 ; 0.53
1997 ; 49 ; 5.61 ; 7.09 ; 0.52
2011 ; 71 ; 5.61 ; 7.09 ; 0.52
2015 ; 76 ; 5.61 ; 7.23 ; 0.52
1961 ; 5  ; 5.60 ; 7.84 ; 0.52
1922 ; 5  ; 5.60 ; 7.28 ; 0.52
2013 ; 79 ; 5.59 ; 7.17 ; 0.52
1994 ; 31 ; 5.58 ; 7.38 ; 0.52
1969 ; 6  ; 5.50 ; 7.57 ; 0.51
1954 ; 6  ; 5.50 ; 8.12 ; 0.51
2009 ; 71 ; 5.49 ; 7.20 ; 0.51
2006 ; 63 ; 5.46 ; 7.15 ; 0.51
1975 ; 9  ; 5.44 ; 7.51 ; 0.51
1973 ; 16 ; 5.44 ; 7.57 ; 0.51
1988 ; 26 ; 5.42 ; 7.25 ; 0.50
1941 ; 5  ; 5.40 ; 7.88 ; 0.50
1985 ; 23 ; 5.39 ; 7.23 ; 0.50
2003 ; 47 ; 5.36 ; 7.14 ; 0.50
1993 ; 32 ; 5.34 ; 6.93 ; 0.50
1937 ; 4  ; 5.25 ; 7.75 ; 0.49
1931 ; 4  ; 5.25 ; 7.98 ; 0.49
2007 ; 67 ; 5.22 ; 7.20 ; 0.48
1974 ; 9  ; 5.22 ; 7.34 ; 0.48
2001 ; 55 ; 5.18 ; 6.98 ; 0.48
2016 ; 91 ; 5.15 ; 7.12 ; 0.48
1987 ; 23 ; 5.13 ; 6.74 ; 0.47
1963 ; 10 ; 5.00 ; 7.53 ; 0.46
1946 ; 6  ; 5.00 ; 7.85 ; 0.46
1983 ; 13 ; 4.92 ; 6.90 ; 0.45
1999 ; 51 ; 4.84 ; 7.15 ; 0.44
1996 ; 24 ; 4.83 ; 6.94 ; 0.44
1967 ; 6  ; 4.83 ; 7.87 ; 0.44
1949 ; 4  ; 4.75 ; 7.20 ; 0.43
1977 ; 11 ; 4.73 ; 6.91 ; 0.43
1998 ; 47 ; 4.62 ; 6.92 ; 0.42
1978 ; 7  ; 4.57 ; 6.66 ; 0.42
2005 ; 44 ; 4.55 ; 7.02 ; 0.41
1991 ; 24 ; 4.54 ; 7.05 ; 0.41
1971 ; 12 ; 4.50 ; 7.52 ; 0.41
1990 ; 22 ; 4.41 ; 7.06 ; 0.40
1986 ; 18 ; 4.39 ; 7.06 ; 0.40
2000 ; 47 ; 4.36 ; 6.74 ; 0.39
1958 ; 7  ; 4.29 ; 7.84 ; 0.39
1970 ; 6  ; 4.17 ; 7.22 ; 0.37
1960 ; 7  ; 4.14 ; 7.76 ; 0.37
1992 ; 32 ; 4.13 ; 6.91 ; 0.37
1965 ; 6  ; 4.00 ; 7.18 ; 0.36
1947 ; 3  ; 4.00 ; 7.80 ; 0.36
1962 ; 8  ; 3.75 ; 7.77 ; 0.33
1955 ; 4  ; 3.75 ; 7.73 ; 0.33
1950 ; 6  ; 3.67 ; 8.13 ; 0.32
1989 ; 22 ; 3.64 ; 7.04 ; 0.32
1943 ; 2  ; 3.00 ; 7.55 ; 0.25
1945 ; 1  ; 2.00 ; 7.60 ; 0.14


## Example with two IMDb lists:

    $ lein run resources\example_ratings_2018_format_A.csv resources\example_ratings_2018_format_B.csv

Gives the following result:

    -----------------------------------
    - IMDb dual-list analysis results -
    -----------------------------------
    Number of shared titles
    1753

    Correlation between ratings
    0.792

## To do

* GUI, visualizations
* Extended analytics capabilities.
* Possibility to retrieve the ratings directly from IMDb website via authentication (if possible).


## License

Copyright © 2013–2018 Esa Junttila

Distributed under the Eclipse Public License, the same as Clojure.
