;; IMDb List Analyzer main program
;;
;; Esa Junttila 2015-11-01 (originally 2013-06-29)
;;

(ns imdb-list-analyzer.core
  (:require [imdb-list-analyzer.imdb-data :as imdb]
            [imdb-list-analyzer.analysis :as ana]
			[imdb-list-analyzer.result-view :as resview]
			[imdb-list-analyzer.dual-result-view :as dualview])
  (:gen-class))

(defn missing-file-err [filename] (.println *err* (str "Cannot find input file:" filename)))

(defn file-exists [filename] (.exists (clojure.java.io/as-file filename)))

(defn one-file-analysis
  [filename]
  (if (file-exists filename)
    (do
      (println (str "Analyzing single-list IMDb ratings from " filename))
      (resview/view-results (resview/compute-results (rest (imdb/read-imdb-data filename)))))
    (missing-file-err filename)))

(defn dual-file-analysis
  [filename-a, filename-b]
  (cond
    (not (file-exists filename-a)) (missing-file-err filename-a)
    (not (file-exists filename-b)) (missing-file-err filename-b)
	:else
	  (dualview/view-dual-results (
	    dualview/compute-dual-results
	    (rest (imdb/read-imdb-data filename-a))
	    (rest (imdb/read-imdb-data filename-b))))))

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
	  (= (count args) 2) (dual-file-analysis (first args) (second args))
	  :else (print-usage))))
