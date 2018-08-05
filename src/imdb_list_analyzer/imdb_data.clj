;;; Parse IMDb rating list data in CSV files.
;;
;; Esa Junttila
;; 2018-08-05 (originally 2013-06-30 and 2015-11-01)

(ns imdb-list-analyzer.imdb-data
  (:require [clojure.string :as string] ; shorthand
            [clojure.data.csv :as csv]  ; parse CSV strings
            [cheshire.core :as json])   ; handle JSON string format
  (:import java.nio.charset.Charset
           [java.text ParseException
                      SimpleDateFormat]
           [java.util Locale
                      TimeZone
                      Calendar]))


"Internal Movie Title that contains all movie-related information. Imitates old pre-2017 IMDb export format."
(defrecord Title [list-index id created modified desc title type directors
                  rate imdb-rate runtime year genres numvotes released URL])


;
; IMDb data export definitions
;

"Column names in old-format CSV files (and internal labels) of IMDb ratings;
also used as keys in JSON strings."
(def imdb-column-names
  ["position" "const" "created" "modified" "description" "Title" "Title type"
   "Directors" "You rated" "IMDb Rating" "Runtime (mins)" "Year" "Genres"
   "Num. Votes" "Release Date (month/day/year)" "URL"])

"Column names in new-format CSV files of IMDb ratings;
also used as keys in JSON strings."
(def imdb-column-names-new-format
  ["Const" "Your Rating" "Date Rated" "Title" "URL" "Title Type" "IMDb Rating"
   "Runtime (mins)" "Year" "Genres" "Num Votes" "Release Date" "Directors"])

"Mapping from new-format fields (extended by nils at 13 14 15) to old-format indices."
(def field-format-map [13 0 2 14 15 3 5 12 1 6 7 8 9 10 11 4])

"Range of values for 'rate' information: 1,2,...,9,10."
(def rates-range (map inc (range 10)))

"Identifier for old IMDb 2017 CSV data format"
(def old-format-size (count imdb-column-names))

"Identifier for new IMDb 2018 CSV data format"
(def new-format-size (count imdb-column-names-new-format))

"Local encoding constant: to interpret special Western characters correctly,
 such as Scandinavian characters, make a best guess for the encoding.
 It could be, for example, 'UTF-8' or 'windows-1252'."
(def local-encoding (.name (Charset/defaultCharset)))


;
; IMDb data parsing functions
;

(defn parse-number
  "Parse a number from a number string. Return the number, integer or double,
   or nil if unsuccessful."
  [number-string]
  (try (read-string number-string)
    (catch Exception _ nil)))

(def date-locale (Locale/getDefault))
(def date-time-zone (TimeZone/getDefault))
(def date-syntaxes ["EEE MMM d HH:mm:ss yyyy" "yyyy-MM-dd" "yyyy-MM"])

(defn parse-date
  "Parse a date from a date-string based on the most common IMDb date formats.
  Either (parse-date date-str) or (parse-date date-str format-str).
  For example:
    (parse-date '2015-12-31')
    (parse-date '2015-12-31', 'yyyy-MM-dd')
    (parse-date '2015-04')
    (parse-date 'Thu Oct 29 00:00:00 2015')"
  ([date-string]
    (let [f #(try (parse-date date-string %) (catch ParseException _ nil))]
      (some #(if (complement (nil? %)) %) (map f date-syntaxes))))
  ([date-string syntax]
    (let [formatter (SimpleDateFormat. ^String syntax ^Locale date-locale)
          cal (Calendar/getInstance date-time-zone date-locale)]
      (do
        (.setTimeZone formatter date-time-zone)
        (.setTime cal (.parse formatter date-string))
        cal))))

(defn- parse-vec
  "Split text into a sequence of trimmed parts by delim(iter)."
  [delim text]
  (map clojure.string/trim (clojure.string/split text (re-pattern delim))))

;;
;; Functions for old pre-2017 data format.
;;

(def input-fns
  "Assign a parse function for each column in CSV files."
  [parse-number               ; "position", int
   identity                   ; "const", str
   parse-date                 ; "created", date
   (fn [_] (constantly nil))  ; "modified", unknown date syntax
   identity                   ; "description", str
   identity                   ; "Title", str
   identity                   ; "Title type", str
   (partial parse-vec ",")    ; "Directors"
   parse-number               ; "You rated", int     
   parse-number               ; "IMDb Rating", double
   parse-number               ; "Runtime (mins)", int PROBLEM
   parse-number               ; "Year", int
   (partial parse-vec ",")    ; "Genres", seq of str
   parse-number               ; "Num. Votes", int
   parse-date                 ; "Release Date (month/day/year)", date
   identity])                 ; "URL", str

(defn parse-line
  "Parse one row from an IMDb CSV file: parse a Title record from column values."
  [title-tokens]
  (apply ->Title (map #(%1 %2) input-fns title-tokens)))

(defn parse-imdb-data-old-format
  "Parse a sequence of Titles from a sequence of IMDb data (new post-2017 format)
  with a header. The 'data' arg is a sequence where each item is a sequence of
  strings; in other words it contains rows in an IMDb list with column values."
  [data]
  (let [headers (apply ->Title (first data))  ; preserve column names as strings
        lines (rest data)]
    (conj (map parse-line lines) headers)))  ; parse Titles and attach 'headers' as the first item

;;
;; Functions to convert new post-2018 data format into internal pre-2017 data format.
;;

(defn sample-indices
  "Return sampled elements from coll, indicated with (zero-based) indices as idx-order.
  For example (= (sample-indices [11 22 33 44] [1 3 2 0 1 2]) [22 44 33 11 22 33])."
  [coll idx-order]
  (vec (map (partial nth coll) idx-order)))

(defn convert-format
  "Convert a ratings data line from new format into old format (internal representation)."
  [coll]
  (sample-indices (concat coll ["" "" ""]) field-format-map))

(defn parse-imdb-data-new-format
  "Parse a sequence of Titles from a sequence of IMDb data (new post-2017 format)
  with a header. The 'data' arg is a sequence where each item is a sequence of
  strings; in other words it contains rows in an IMDb list with column values."
  [data]
  (let [headers (apply ->Title (convert-format (first data)))  ; preserve column names as strings
        titles (map #(parse-line (convert-format %)) (rest data))] ; parse Titles from lines
    (conj titles headers)))  ; attach 'headers' as the first item before Titles


; Base functions

(defn parse-imdb-data
  "Parse an IMDb ratings file. IMDb changed its file format late in 2017, as marked in
  https://getsatisfaction.com/imdb/topics/updates-to-the-ratings-pages-and-functionality notice.
  We try to guess whether the input uses new or old format, and use what seems to work."
  [data]
  (let [num-fields (count (first data))]
    (cond
      (= num-fields new-format-size) (parse-imdb-data-new-format data)     ; use new IMDb CSV format
      (= num-fields old-format-size) (parse-imdb-data-old-format data))))  ; use old IMDb CSV format

(defn parse-str-data
  "Read IMDb ratings data from a CSV strings.
  The result is a lazy sequence of sequences (of fields)."
  [s]
  (csv/read-csv s))  ; default delimiter \, and quotation char \"

(defn read-raw-data
  "Read IMDb ratings data from a CSV-formatted file.
   The 'file' may be a filename, a file, or a reader.
   The result is a lazy sequence of string vectors (of fields)."
  [file]
  (parse-str-data (slurp file :encoding local-encoding)))

(defn read-imdb-file
  "Parse a sequence of Titles from a CSV file that contains IMDb ratings data.
  The 'file' may be a filename, a file, or a reader."
  [file]
  (parse-imdb-data (read-raw-data file)))


;; JSON-related functions
;; We use the following JSON format (naming as in exported IMDb files):
;; {"imdbratings": [{title}, {title}, {title}, ...]}
;; where each {title} is a map from imdb-column-names to str values.


(defn parse-json-item
  "Parse a Title from a single map of IMDb data fields for a single movie item."
  [item-map]
  (apply ->Title (map #(%1 %2) input-fns (map #(get item-map % "") imdb-column-names))))

(defn parse-imdb-data-from-json-map
  "Parse Titles from a JSON structure of maps and vectors that contains IMDb ratings."
  [json-map]
  (let [headers (apply ->Title imdb-column-names)  ; preserve column names as strings
        item-coll (get json-map "imdbratings")]
    (conj (map parse-json-item item-coll) headers)))  ; parse Titles and attach 'headers' as the first item

(defn parse-imdb-data-from-json-str
  "Parse Titles from a JSON string that contains IMDb ratings."
  [json-str]
  (parse-imdb-data-from-json-map (json/parse-string json-str)))

(defn convert-csv-to-json-map
  "Convert raw CSV data (sequence of string sequences) into a 'JSON' structure
   of maps and vectors (use old format as internal representation)."
  [csv-data]
  (let [n (count (first csv-data))
        new-fmt (= n new-format-size)
        old-fmt (= n old-format-size)
        rearrange (cond new-fmt convert-format, old-fmt identity)]
    {"imdbratings" (->> csv-data rest (map #(zipmap imdb-column-names (rearrange %))))}))

(defn convert-csv-to-json-str
  "Convert raw CSV data (sequence of string sequences) into a JSON string."
  [csv-data]
  (json/generate-string (convert-csv-to-json-map csv-data)))
