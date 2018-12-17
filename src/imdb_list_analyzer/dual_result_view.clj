;;; Viewer for results from IMDb dual-list analysis (from two IMDb lists)

(ns imdb-list-analyzer.dual-result-view
  (:require [imdb-list-analyzer.common :as common]
            [imdb-list-analyzer.analysis :as ana]
            [imdb-list-analyzer.result-view :as resview]
            [cheshire.core :as json]))

"Storage for dual-list analysis results"
(defrecord DualAnalysisResult [common corr])

(defn compute-dual-results
  "Hash map of analysis results of dual collections."
  [titles-coll-a titles-coll-b]
  (map->DualAnalysisResult
    {:common(ana/common-count titles-coll-a titles-coll-b),
     :corr (ana/corr-vs-another titles-coll-a titles-coll-b)}))

(defn view-dual-results-str
  "String representation of all dual-list analysis results"
  [dual-ana-results]
  (clojure.string/join "\n"
    ["-----------------------------------"
     "- IMDb dual-list analysis results -"
     "-----------------------------------"
    "Number of shared titles" (str (:common dual-ana-results) "\n")
    "Correlation between ratings" (str (resview/limited-precision (:corr dual-ana-results) 3) "\n")]))

(defn view-dual-results
  "Print all dual-list analysis results in human-readable form to standard output."
  [dual-ana-results enc]
  (common/println-enc (view-dual-results-str dual-ana-results) enc))

(defn jsonify-dual-result
  "JSON string of a DualAnalysisResult."
  [dual-results]
  (json/generate-string {"dualresults" dual-results}))
