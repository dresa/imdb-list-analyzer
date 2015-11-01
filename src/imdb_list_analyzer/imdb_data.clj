;; Dealing with IMDb rating list data.
;;
;; Esa Junttila
;; 2013-06-30

(ns imdb-list-analyzer.imdb-data
  (:require [clojure-csv.core :as csv]
            [clojure.java.io :as io]))

; In order to interpret special Western characters correctly, such as
; Scandinavian characters, make a best guess for the encoding.
; It could be, for example, "UTF-8" or "windows-1252"
(def local-encoding (.name (java.nio.charset.Charset/defaultCharset)))

; Movie title that contains all movie-related information
(defrecord Title [list-index id created modified desc title type directors rate imdb-rate runtime year genres numvotes released URL])

; Range of values for 'rate' information: 1,2,...,9,10.
(def rates-range (map inc (range 10)))


(defn parse-number
  "Parse a number from a number string. Returns the number, integer or double,
   or nil if unsuccessful."
  [number-string]
  (try (read-string number-string)
    (catch Exception e nil)))

(def date-locale (java.util.Locale/getDefault))
(def date-time-zone (java.util.TimeZone/getDefault))
(def date-syntaxes ["EEE MMM d HH:mm:ss yyyy" "yyyy-MM-dd" "yyyy-MM"])

(defn parse-date
  ([date-string]
    (let [f #(try (parse-date date-string %) (catch java.text.ParseException pe nil))]
      (some #(if (complement (nil? %)) %) (map f date-syntaxes))))
  ([date-string syntax]
    (let [formatter (java.text.SimpleDateFormat. syntax date-locale)
          cal (java.util.Calendar/getInstance date-time-zone date-locale)]
      (do
        (.setTimeZone formatter date-time-zone)
        (.setTime cal (.parse formatter date-string))
        cal))))

(defn parse-vec
  [delim text]
  (map clojure.string/trim (clojure.string/split text (re-pattern delim))))

(def input-fns
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
  [title-tokens]
  (apply ->Title (map #(%1 %2) input-fns title-tokens)))

(defn read-raw-data
  "Read IMDb ratings data from a CSV-formatted file.
   The result is a lazy sequence of vectors (of fields)."
  [filename]
  (with-open [file (io/reader filename)]
    (csv/parse-csv (slurp file :encoding local-encoding))))

(defn read-imdb-data
  [filename]
  (let [data (read-raw-data filename)
        headers (apply ->Title (first data))
        lines (rest data)]
    (conj (map parse-line lines) headers)))

