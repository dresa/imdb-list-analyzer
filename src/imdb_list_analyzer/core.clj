;; Dummy main program: IMDb List Analyzer
;;
;; Esa Junttila 2013-06-29
;;

(ns imdb-list-analyzer.core
  (:require [imdb-list-analyzer.imdb-data :as imdb]
            [imdb-list-analyzer.analysis :as ana]
			[imdb-list-analyzer.result-view :as resview])
  (:gen-class))

(defn one-file-analysis
  [filename]
  (if (.exists (clojure.java.io/as-file filename))
    (do
      (println (str "Analyzing single-list IMDb ratings from " filename))
      (resview/view-result (resview/compute-results (rest (imdb/read-imdb-data filename)))))
    (.println *err* (str "Cannot find input file:" filename))))

(defn two-file-analysis [filename-a, filename-b] "Two-file analysis has not been implemented yet.")

(defn print-usage []
  (println
    "  Usage:\n"
    "   lein run <filenameA> [filenameB]\n"
    "   for example: lein run resources\\example_ratings_A.csv"))

(defn -main
  "Run IMDb analysis on the example dataset."
  [& args]
  (let [filename "resources/example_ratings_A.csv"]
    ;; work around dangerous default behaviour in Clojure
    (alter-var-root #'*read-eval* (constantly false))
    (cond
	  (= (count args) 0) (print-usage)
	  (= (count args) 1) (one-file-analysis (first args))
	  (= (count args) 2) (two-file-analysis (first args) (second args))
	  :else (print-usage))))
