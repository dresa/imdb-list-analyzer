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
;;; * Surprising ratings: both positive and negative surprises w.r.t average IMDb rating quantiles.
;;;
;;; Anyone with an IMDb account can retrieve their own ratings file as follows.
;;; 1. Login to www.imdb.com with you account.
;;; 2. Search for a personal "Your Ratings" view that contains all your rated movies.
;;; 3. Click "..." button and choose "Export" to download your ratings in a CSV file (in ANSI cp1252 encoding).
;;; 4. Save file into the filesystem.
;;; 5. Launch this program with a command-line argument that is the filepath of downloaded CSV file.
;;;
;;; Esa Junttila 2018-12-17 (originally 2013-06-29)

(ns imdb-list-analyzer.core
  (:require [imdb-list-analyzer.common :as common]
            [imdb-list-analyzer.imdb-data :as imdb]
            [imdb-list-analyzer.result-view :as resview]
            [imdb-list-analyzer.dual-result-view :as dualview])
  (:gen-class))

(defn missing-file-err
  "Report into stderr about a missing filename."
  [filename]
  (binding [*out* *err*]  ; writing to stderr instead of stdout
    (println (str "Cannot find input file: " filename))))

;;
;; Analysis functions
;;

(defn one-input-analysis
  "Analyze a single IMDb ratings list, given as a CSV sequence
  of string sequences, with a header. Return an AnalysisResult record."
  [input-data]
  (resview/compute-results (rest (imdb/parse-imdb-data input-data))))

(defn one-json-input-analysis
  "Analyze a single IMDb ratings list, given as a JSON string.
  Return a JSON string of an AnalysisResult record."
  [json-str]
  (let [titles-coll (imdb/parse-imdb-data-from-json-str json-str)]
    (resview/jsonify-single-result (resview/compute-results (rest titles-coll)))))

(defn one-file-analysis
  "Analyze a single IMDb ratings list, in CSV format, given as a filename
  or a file. Write the results into stdout. If there is no file
  matching with given file, report to stderr."
  ([file]
   (one-file-analysis file common/local-encoding))
  ([file encoding]
   (if (common/file-exists? file)
      (let [enc (or encoding common/local-encoding)]  ; non-nil encoding or local
        (one-input-analysis (imdb/read-raw-data file enc)))
     (missing-file-err file))))

(defn dual-input-analysis
  "Analyze two IMDb rating lists. Each input arg refers to
  a collection of IMDb ratings list data: a sequence of string sequences (CSV).
  Return a DualAnalysisResult record."
  [input-data-a input-data-b]
  (dualview/compute-dual-results
    (rest (imdb/parse-imdb-data input-data-a))
    (rest (imdb/parse-imdb-data input-data-b))))

(defn dual-json-input-analysis
  "Analyze two IMDb ratings lists, given as JSON strings.
  Return a JSON string of a DualAnalysisResult record."
  [json-str-a json-str-b]
  (dualview/jsonify-dual-result
    (dualview/compute-dual-results
      (rest (imdb/parse-imdb-data-from-json-str json-str-a))
      (rest (imdb/parse-imdb-data-from-json-str json-str-b)))))

(defn dual-file-analysis
  "Analyze two IMDb rating lists. Each input refers to a file or filename
  of a CSV-formatted IMDb ratings list. Optional input file encoding.
  Return an AnalysisResult record."
  ([file-a file-b]
   (dual-file-analysis file-a file-b common/local-encoding))
  ([file-a file-b encoding]
   (let [enc (or encoding common/local-encoding)]  ; non-nil encoding or local
     (cond
       (not (common/file-exists? file-a)) (missing-file-err file-a)
       (not (common/file-exists? file-b)) (missing-file-err file-b)
       :else (dual-input-analysis
               (imdb/read-raw-data file-a enc)
               (imdb/read-raw-data file-b enc))))))

;;
;; Usage and command-line argument parsers
;;

(defn print-usage []
  (println
    "  Analyze an IMDb movie ratings CSV file, or two such files.\n"
    "  Usage:\n"
    "    lein run <filenameA> [filenameB] [in-encoding=enc] [out-encoding=enc]\n"
    "  Examples:\n"
    "    lein run resources\\rates_A.csv\n"
    "    lein run resources\\rates_A.csv resources\\rates_B.csv\n"
    "    lein run resources\\rates_2017_A.csv in-encoding=UTF-8\n"
    "    lein run resources\\rates_A.csv in-encoding=cp1252 out-encoding=UTF-8\n"))


; Check if an argument specifies a character encoding/codepage.
"Prefix of the argument that specifies the encoding used in reading ratings file."
(def in-enc-prefix "in-encoding=")

"Prefix of the argument that specifies the encoding used in printing out the results."
(def out-enc-prefix "out-encoding=")

"Functions for recognizing whether arguments specify filenames or encodings."
(defn in-enc-arg? [s] (.startsWith s in-enc-prefix))
(defn out-enc-arg? [s] (.startsWith s out-enc-prefix))
(defn enc-arg? [s] (or (in-enc-arg? s) (out-enc-arg? s)))

(defn parse-enc
  "Parse encoding name from argument string and convert into a Charset."
  [s]
  (let [name (last (clojure.string/split s, #"=")) ]
    (if (common/encoding-supported? name)
      name
      (do
        (binding [*out* *err*]  ; writing to stderr instead of stdout
          (println (str "Error: encoding '" name "' not supported, aborting.")))
        (System/exit 4)))))

;;
;; Access to full analysis results in JSON format
;;

(defn full-one-analysis-results
  "Full JSON results from one-set rating analysis, from file 'filename' (in 'enc' encoding)."
  ([filename] (full-one-analysis-results filename nil))
  ([filename enc]
   (one-json-input-analysis (imdb/convert-csv-to-json-str (imdb/read-raw-data filename enc)))))

(defn full-two-analysis-results
  "Full JSON results from dual-set rating analysis, from given filenames (in 'enc' encoding)."
  ([file-a file-b] (full-two-analysis-results file-a file-b nil))
  ([file-a file-b enc]
   (dual-json-input-analysis
     (imdb/convert-csv-to-json-str (imdb/read-raw-data file-a enc))
     (imdb/convert-csv-to-json-str (imdb/read-raw-data file-b enc)))))

;;
;; Main program
;;

(defn -main
  "Run IMDb analyzer on given IMDb ratings file or files.
  Show analysis results on-screen. Read usage and documentation."
  [& args]
  ;; work around dangerous default behaviour in Clojure
  (alter-var-root #'*read-eval* (constantly false))
  (let [filenames (filter (complement enc-arg?) args)
        in-enc (if-let [enc-arg (common/find-first in-enc-arg? args)] (parse-enc enc-arg))  ; nil --> using default
        out-enc (if-let [enc-arg (common/find-first out-enc-arg? args)] (parse-enc enc-arg))]  ; nil --> using default
    (case (count filenames)
      0 (print-usage)
      1 (when-let [res (one-file-analysis (first filenames) in-enc)]
          (do
            (resview/view-results res out-enc)
            ;(common/println-enc (full-one-analysis-results (first filenames) in-enc) out-enc)
            ))
      2 (when-let [res (dual-file-analysis (first filenames) (second filenames) in-enc)]
          (do
            (dualview/view-dual-results res out-enc)
            ;(common/println-enc (full-two-analysis-results (first filenames) (second filenames) in-enc) out-enc)
            ))
      (print-usage))))
