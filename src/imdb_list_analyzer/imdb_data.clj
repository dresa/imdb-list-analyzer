;;; Parse IMDb rating list data in CSV files.
;;
;; Esa Junttila
;; 2015-11-01 (originally 2013-06-30)

(ns imdb-list-analyzer.imdb-data
  (:require [clojure-csv.core :as csv]
            [clojure.java.io :as io])
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

(defn read-raw-data
  "Read IMDb ratings data from a CSV-formatted file.
   The result is a lazy sequence of vectors (of fields)."
  [filename]
  (with-open [file (io/reader filename)]
    (csv/parse-csv (slurp file :encoding local-encoding))))

(defn read-imdb-data
  "Parse a sequence of Titles from a CSV filename that contains IMDb ratings data."
  [filename]
  (let [data (read-raw-data filename)
        headers (apply ->Title (first data))  ; preserve column names as strings
        lines (rest data)]
    (conj (map parse-line lines) headers)))  ; attach 'headers' as the first item

