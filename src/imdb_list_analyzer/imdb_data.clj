;;; Parse IMDb rating list data in CSV files.
;;
;; Esa Junttila
;; 2015-11-01 (originally 2013-06-30)

(ns imdb-list-analyzer.imdb-data
  (:require [imdb-list-analyzer.simple-csv :as csv]
            [cheshire.core :as json])
  (:import java.nio.charset.Charset
           [java.text ParseException
                      SimpleDateFormat]
           [java.util Locale
                      TimeZone
                      Calendar]))

;; In order to interpret special Western characters correctly, such as
;; Scandinavian characters, make a best guess for the encoding.
;; It could be, for example, "UTF-8" or "windows-1252"
(def local-encoding (.name (Charset/defaultCharset)))

"Movie title that contains all movie-related information"
(defrecord Title [list-index id created modified desc title type directors rate imdb-rate runtime year genres numvotes released URL])

"Range of values for 'rate' information: 1,2,...,9,10."
(def rates-range (map inc (range 10)))


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

(defn parse-str-data
  "Read IMDb ratings data from a CSV strings.
  The result is a lazy sequence of sequences (of fields)."
  [s]
  (csv/parse-csv s))

(defn read-raw-data
  "Read IMDb ratings data from a CSV-formatted file.
   The 'file' may be a filename, a file, or a reader.
   The result is a lazy sequence of string vectors (of fields)."
  [file]
  (parse-str-data (slurp file :encoding local-encoding)))

(defn parse-imdb-data
  "Parse a sequence of Titles from a sequence of IMDb data with a header.
  The 'data' arg is a sequence where each item is a sequence of strings;
  in other words it contains rows in an IMDb list with column values."
  [data]
  (let [headers (apply ->Title (first data))  ; preserve column names as strings
        lines (rest data)]
    (conj (map parse-line lines) headers)))  ; parse Titles and attach 'headers' as the first item

(defn read-imdb-file
  "Parse a sequence of Titles from a CSV file that contains IMDb ratings data.
  The 'file' may be a filename, a file, or a reader."
  [file]
  (parse-imdb-data (read-raw-data file)))


;; JSON-related functions
;; We use the following JSON format (naming as in exported IMDb files):
;; {"imdbratings": [{title}, {title}, {title}, ...]}
;; where each {title} is a map from imdb-column-names to str values.

(def imdb-column-names ["position" "const" "created" "modified" "description"
                        "Title" "Title type" "Directors" "You rated" "IMDb Rating"
                        "Runtime (mins)" "Year" "Genres" "Num. Votes"
                        "Release Date (month/day/year)" "URL"])
"Column names in CSV files of IMDb ratings; also used as keys in JSON strings."

(defn parse-json-item
  "Parse a Title from a single map of IMDb data fields for a single movie item."
  [item-map]
  (apply ->Title (map #(%1 %2) input-fns (map item-map imdb-column-names))))

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
  "Convert raw CSV data (sequence of string sequences) into
  a 'JSON' structure of maps and vectors."
  [csv-data]
  {"imdbratings" (->> csv-data rest (map #(zipmap imdb-column-names %)))})

(defn convert-csv-to-json-str
  "Convert raw CSV data (sequence of string sequences) into a JSON string."
  [csv-data]
  (json/generate-string (convert-csv-to-json-map csv-data)))
