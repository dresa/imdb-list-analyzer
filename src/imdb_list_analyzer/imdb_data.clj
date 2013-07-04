;; Dealing with IMDb rating list data.
;;
;; Esa Junttila
;; 2013-06-30

(ns imdb-list-analyzer.imdb-data
  (:require [clojure-csv.core :as csv]
            [clojure.java.io :as io]))

; In order to interpret special Western characters correctly, such as
; Scandinavian characters, make a best guess for the encoding.
; It could be, for example, "UTF-8" or "windows-1253"
(def local-encoding (.name (java.nio.charset.Charset/defaultCharset)))

; Movie title that contains all movie-related information
(defrecord Title [list-index id created modified desc title type directors rate imdb-rate runtime year genres numvotes released URL])

(defn parse-number
  "Parse a number from a number string. Returns the number, integer or double,
   or nil if unsuccessful."
  [number-string]
  (try (read-string number-string)
    (catch Exception e nil)))

(def date-syntaxes ["EEE MMM d HH:mm:ss yyyy" "yyyy-MM-dd" "yyyy-MM"])

; TODO: use clj-date instead?
(defn parse-date
  ([date-string]
    (let [f #(try (parse-date date-string %) (catch java.text.ParseException pe nil))]
      (some #(if (complement (nil? %)) %) (map f date-syntaxes))))
  ([date-string syntax]
    (.parse (java.text.SimpleDateFormat. syntax java.util.Locale/US) date-string)))

(defn parse-vec
  [delim text]
  (clojure.string/split text (re-pattern delim)))

(def input-fns
  [parse-number   ; int
   identity
   parse-date  ; TODO: PROBLEM WITH TIMEZONES
   (fn [_] (constantly nil))  ; unknown date syntax
   identity
   identity
   identity
   (partial parse-vec ",")
   parse-number  ; int
   parse-number  ; double
   parse-number  ; int PROBLEM
   parse-number  ; int
   (partial parse-vec ",")
   parse-number  ; int
   parse-date
   identity])

(defn parse-line
  [title-tokens]
  (apply ->Title (map #(%1 %2) input-fns title-tokens)))

(defn read-data
  "Read IMDb ratings data from a CSV-formatted file.
   The result is a lazy sequence of vectors (of fields)."
  [filename]
  (with-open [file (io/reader filename)]
    (csv/parse-csv (slurp file :encoding local-encoding))))

(defn read-imdb-data
  [filename]
  (let [data (read-data filename)
        headers (apply ->Title (first data))
        lines (rest data)]
    (conj (map parse-line lines) headers)))
