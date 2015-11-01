; Viewer for IMDb dual list analysis results

(ns imdb-list-analyzer.dual-result-view
  (:require [imdb-list-analyzer.analysis :as ana]
            [imdb-list-analyzer.imdb-data :as imdb]))

(defrecord DualAnalysisResult [common corr])

(defn compute-dual-results
  "Return a hash map of analysis results of dual collections."
  [titles-coll-a titles-coll-b]
  (map->DualAnalysisResult
    {:common(ana/common-count titles-coll-a titles-coll-b),
     :corr (ana/corr-vs-another titles-coll-a titles-coll-b)}))

(defn view-dual-results-str
  "Convert dual-list analysis results into a string."
  [dual-ana-results]
  (clojure.string/join "\n"
    ["-----------------------------------"
	 "- IMDb dual-list analysis results -"
	 "-----------------------------------"
    "Count of shared titles" (str (:common dual-ana-results) "\n")
    "Correlation between ratings" (str (:corr dual-ana-results) "\n")]))

(defn view-dual-results
  [dual-ana-results]
  (println (view-dual-results-str dual-ana-results)))
