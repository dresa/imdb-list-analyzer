;;;; IMDb List Analyzer main program
;;;
;;; Analyze IMDb CSV files that contain movie ratings data.
;;; The analysis covers the following statistics:
;;; * Number: number of rated movie titles OR number of rated titles both lists contain
;;; * Mean: arithmetic mean of your ratings
;;; * Standard deviation: standard deviation of your movie ratings
;;; * Correlation: correlation coefficient between ratings and IMDb averages (or other ratings)
;;; * Entropy: information content of one rating, measured in bits, based on Shannon entropy
;;; * Best directors: directors whose movies you rate highly (custom statistical test)
;;; * Worst directors: directors whose movies you rate poorly (custom statistical test)
;;;
;;; Anyone with an IMDb account can retrieve their own ratings file as follows.
;;; 1. Login to www.imdb.com with you account.
;;; 2. Search for a personal "Your Ratings" view that contains all your rated movies.
;;; 3. Click "Export this list" at the bottom of the page.
;;; 4. Save file into the filesystem.
;;; 5. Launch this program with a command-line argument that is the filepath of downloaded CSV file.
;;;
;;; Esa Junttila 2015-11-01 (originally 2013-06-29)

(ns imdb-list-analyzer.core
  (:require [imdb-list-analyzer.imdb-data :as imdb]
            [imdb-list-analyzer.result-view :as resview]
            [imdb-list-analyzer.dual-result-view :as dualview])
  (:import (java.io File))
  (:gen-class))

(defn missing-file-err
  "Report into stderr about a missing filename."
  [filename]
  (binding [*out* *err*]  ; writing to stderr instead of stdout
    (println (str "Cannot find input file: " filename))))

(defn file-exists?
  "Does a given filename exist in the filesystem?"
  [filename]
  (.exists ^File (clojure.java.io/as-file filename)))

(defn one-input-analysis
  "Analyze a single IMDb ratings list, given as a CSV string
  with headers. Return an AnalysisResult record."
  [input-data]
  (resview/compute-results (rest (imdb/parse-imdb-data input-data))))

(defn one-file-analysis
  "Analyze a single IMDb ratings list, in CSV format, given as a filename
  or a file. Write the results into stdout. If there is no file
  matching with given file, report to stderr."
  [file]
  (if (file-exists? file)
    (one-input-analysis (imdb/read-raw-data file))
    (missing-file-err file)))

(defn dual-input-analysis
  "Analyze two IMDb rating lists. Each input arg refers to
  a collection of IMDb ratings list data: a sequence of string sequences (CSV).
  Return an DualAnalysisResult record."
  [input-data-a input-data-b]
  (dualview/compute-dual-results
    (rest (imdb/parse-imdb-data input-data-a))
    (rest (imdb/parse-imdb-data input-data-b))))

(defn dual-file-analysis
  "Analyze two IMDb rating lists. Each input refers to a file or filename
  of a CSV-formatted IMDb ratings list. Return an AnalysisResult record."
  [file-a, file-b]
  (cond
    (not (file-exists? file-a)) (missing-file-err file-a)
    (not (file-exists? file-b)) (missing-file-err file-b)
    :else (dual-input-analysis
            (imdb/read-raw-data file-a)
            (imdb/read-raw-data file-b))))

(defn print-usage []
  (println
    "  Analyze an IMDb CSV file (movie ratings data), or two files.\n"
    "  Usage:\n"
    "   lein run <filenameA> [filenameB]\n"
    "  Examples:\n"
    "   lein run resources\\example_ratings_A.csv\n"
    "   lein run resources\\example_ratings_A.csv resources\\example_ratings_B.csv"))

(defn -main
  "Run IMDb analyzer on given IMDb ratings file or files.
  Show analysis results on-screen. Read usage and documentation."
  [& args]
  ;; work around dangerous default behaviour in Clojure
  (alter-var-root #'*read-eval* (constantly false))
  (case (count args)
    0 (print-usage)
    1 (resview/view-results (one-file-analysis (File. (first args))))
    2 (dualview/view-dual-results (dual-file-analysis (first args) (second args)))
    (print-usage)))
