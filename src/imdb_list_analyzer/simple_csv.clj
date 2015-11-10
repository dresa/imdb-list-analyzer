;;; Simple CSV reader functions.
;;; (to remove dependence on external libraries).
;;; Striving for minimal functionality :)
;;; Inconvenience: IMDb data uses comma as a delimiter AND list of directors contains commas

(ns imdb-list-analyzer.simple-csv
  (:require [clojure.string :as string]))

(defn strip [s chars]
  "Remove characters from a string s"
  (apply str (remove #((set chars) %) s)))

(defn parse-imdb-csv-line
  "Parse one line that is in CSV format.
  Args:
    line -- string, CSV line to be parsed
    delim-re -- re, delimiter regexp, such as ';'
    quote-ch -- string, remove quotation characters, such as '|\"'
    do-trim -- bool, remove leading and trailing whitespace
  Simple call with defaults: (parse-csv-line line)
  "
  ([line]
   (parse-imdb-csv-line line #"(\",\"|\";\")" "\"" true))  ; default delim: , or ; surrounded by "
  ([line delim-re quote-ch do-trim]
   (let [split #(string/split % delim-re)
         remquotes (if quote-ch #(strip % quote-ch) identity)
         trim (if do-trim string/trim identity)]
     (->> line split (map (comp remquotes trim)) ))))

(defn parse-imdb-csv
  "Parse CSV string s (IMDb format assumed)"
  [s]
  (map parse-imdb-csv-line (string/split-lines s)))
