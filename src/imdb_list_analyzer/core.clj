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
            [imdb-list-analyzer.analysis :as ana]
            [imdb-list-analyzer.result-view :as resview]
            [imdb-list-analyzer.dual-result-view :as dualview])
  (:gen-class))

(defn missing-file-err
  "Report into stderr about a missing filename."
  [filename]
  (.println *err* (str "Cannot find input file: " filename)))

(defn file-exists?
  "Does a given filename exist in the filesystem?"
  [filename]
  (.exists (clojure.java.io/as-file filename)))

(defn one-file-analysis
  "Analyze a single IMDb ratings list, in CSV format, given as a filename.
  Write the results into stdout. If there is no file matching
  with given filename, report to stderr."
  [filename]
  (if (file-exists? filename)
    (do
      (println (str "Analyzing single-list IMDb ratings from " filename))
      (resview/view-results (resview/compute-results (rest (imdb/read-imdb-data filename)))))
    (missing-file-err filename)))

(defn dual-file-analysis
  "Analyze two IMDb ratings lists, in CSV format, given as filenames.
  Write the results into stdout. Both filenames must refer to
  an existing file; if not, report to stderr."
  [filename-a, filename-b]
  (cond
    (not (file-exists? filename-a)) (missing-file-err filename-a)
    (not (file-exists? filename-b)) (missing-file-err filename-b)
    :else
      (dualview/view-dual-results (
        dualview/compute-dual-results
        (rest (imdb/read-imdb-data filename-a))
        (rest (imdb/read-imdb-data filename-b))))))

(defn print-usage []
  (println
    "  Analyze an IMDb CSV file (movie ratings data), or two files.\n"
    "  Usage:\n"
    "   lein run <filenameA> [filenameB]\n"
    "  Examples:\n"
    "   lein run resources\\example_ratings_A.csv\n"
    "   lein run resources\\example_ratings_A.csv resources\\example_ratings_B.csv"))

(defn -main
  "Run IMDb analyzer on given IMDb ratings file or files. Read usage and documentation."
  [& args]
  ;; work around dangerous default behaviour in Clojure
  (alter-var-root #'*read-eval* (constantly false))
  (case (count args)
    0 (print-usage)
    1 (one-file-analysis (first args))
    2 (dual-file-analysis (first args) (second args))
    (print-usage)))
