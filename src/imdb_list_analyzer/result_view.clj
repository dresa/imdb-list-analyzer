;;; Viewer for results from IMDb list analysis (on a single list)

(ns imdb-list-analyzer.result-view
  (:require [imdb-list-analyzer.common :as common]
            [clojure.string :as string]
            [imdb-list-analyzer.analysis :as ana]
            [imdb-list-analyzer.imdb-data :as imdb]
            [cheshire.core :as json])
  (:import (java.util Locale)))

"Collection of all analysis results"
(defrecord AnalysisResult [num mean stdev corr freq-hash entropy]) ; TODO: should we list all keys?

(defn compute-results
  "Return a hash map of analysis results."
  [titles-coll]
  (map->AnalysisResult
    {:num (ana/rating-num titles-coll),
     :mean (ana/rating-mean titles-coll)
     :imdb-mean (ana/imdb-mean titles-coll)
     :stdev (ana/rating-stdev titles-coll)
     :imdb-stdev (ana/imdb-stdev titles-coll)
     :corr (ana/corr-vs-imdb titles-coll)
     :freq-hash (ana/rating-frequencies titles-coll)
     :imdb-freq-hash (ana/imdb-frequencies titles-coll)
     :entropy (ana/rating-entropy titles-coll)
     :imdb-entropy (ana/imdb-entropy titles-coll)
     :dir-ranks (ana/director-qualities titles-coll)
     :discrepancy (ana/rating-discrepancy titles-coll)
     :genres (ana/genre-averages titles-coll)
     :years (ana/yearly-averages titles-coll)}))

"Maximum Shannon information entropy, given the rating scale"
(def max-entr (ana/max-entropy (count imdb/rates-range)))

(defn limited-precision
  "Return a string that represents given number 'x' with given
   number of decimals."
  [x num-decimals]
  {:pre [(number? x) (integer? num-decimals) (not (neg? num-decimals))]
   :post (string? %)}
  (let [format-str (str "%." num-decimals "f")]
    (String/format (Locale/getDefault) format-str (to-array [(double x)]))))

(defn dir-rank-str
  "String representation of a single director result, along with p-value and ratings"
  [dir rank-val rates]
  (format "%-26s; %-6s; %s" (str dir) (double rank-val) (str rates)))

(defn directors-ranks-strs
  "String representation of a collection of director results,
  along with p-values and individual ratings"
  [dir-ranks title]
  (string/join
    [title "\n"
     "Director-name; Rank-p-value; Rates" "\n"
     "----------------------------------" "\n"
    (string/join "\n" (for [[[dir rates] rank-val] dir-ranks] (dir-rank-str dir rank-val rates)))]))

(defn view-count-str
  "String representation for title count"
  [ana-results]
  (string/join [
    "Number of movie ratings" "\n"
    (:num ana-results) "\n"]))

(defn view-mean-str
  "String representation for mean rating"
  [ana-results]
  (string/join [
    "Mean of movie ratings" "\n"
    (limited-precision (:mean ana-results) 3)
    " (IMDb: " (limited-precision (:imdb-mean ana-results) 3) ")\n"]))

(defn view-sd-str
  "String representation for standard deviation of ratings"
  [ana-results]
  (string/join [
     "Standard deviation of movie ratings" "\n"
     (limited-precision (:stdev ana-results) 3)
     " (IMDb: " (limited-precision (:imdb-stdev ana-results) 3) ")\n"]))

(defn view-corr-str
  "String representation for ratings correlation"
  [ana-results]
  (string/join [
     "Correlation between ratings and IMDb rating averages" "\n"
     (limited-precision (:corr ana-results) 3) "\n"]))

(defn view-freq-str
  "String representation for ratings frequencies (vs. IMDb rounded averages)"
  [ana-results]
  (string/join [
     "Frequencies of ratings" "\n"
     (string/join
       (map
         (fn [rate]
           (let [freq ((:freq-hash ana-results) rate)
                 imdb-freq ((:imdb-freq-hash ana-results) rate)
                 num (:num ana-results)]
             (string/join
               ["Rate " rate " occurs " freq " times ("
                (limited-precision (* 100 (/ freq num)) 3) " %) versus IMDb " imdb-freq "\n"])))
         imdb/rates-range))]))

(defn view-entropy-str
  "String representation for rating distribution entropy"
  [ana-results]
  (let [entr (limited-precision (:entropy ana-results) 3)
        imdb-entr (limited-precision (:imdb-entropy ana-results) 3)
		max-entr (limited-precision max-entr 3)]
    (string/join [
      "Entropy of ratings (in bits)" "\n"
      entr " (in IMDb " imdb-entr "; maximum is " max-entr ")\n"])))

(def num-directors 30)

(defn view-directors-str
  "String representation for the best and worst directors"
  [ana-results]
  (string/join [
    (directors-ranks-strs (take num-directors (:dir-ranks ana-results)) "The best directors:") "\n\n"
    (directors-ranks-strs (take-last num-directors (:dir-ranks ana-results)) "The worst directors:") "\n"]))

(defn disc-str
  "String representation of a single discrepancy result, along with ratings and p-value diff"
  [title rate imdb-rate discrepancy]
  (format
    "%-36s; %-3s; %-4s; %s"
    (if (<= (count title) 36) title (subs title 0 36))
    (str rate)
    (str imdb-rate)
    (limited-precision discrepancy 3)))

(defn discrepancy-strs
  "String representation of a set of discrepancy results."
  [discr-map]
  (string/join
    "\n"
    (for [dm discr-map] (disc-str (:title dm) (:rate dm) (:imdb-rate dm) (:discrepancy dm)))))

(def num-surprises 30)

(defn view-discrepancy-str
  "String representation for the largest discrepancies between ratings and IMDb averages."
  [ana-results]
  (string/join "\n" ["Surprising likes: Title; Rate; IMDb average; Diff in p-value"
                    (discrepancy-strs (take num-surprises (:discrepancy ana-results)))
                    ""
                    "Surprising dislikes: Title; Rate; IMDb average; Diff in p-value"
                    (discrepancy-strs (take-last num-surprises (:discrepancy ana-results)))
                    ""]))

(defn genre-str
  [genre-map]
  (let [[genre cnt avg imdb-avg avg-quan] (vals (select-keys genre-map [:genre :count :avg :imdb-avg :avg-q]))]
    (format
      "%-14s; %-4s; %-5s; %-5s; %s"
      (if (<= (count genre) 12) genre (subs genre 0 14))
      (str cnt)
      (limited-precision avg 3)
      (limited-precision imdb-avg 3)
      (limited-precision avg-quan 3))))

(defn view-genres-str
  "String representation of genres and their average ratings."
  [ana-results]
  (string/join "\n" ["Genre analysis:"
                     "Genre; Count; Average rate; IMDb average rate; Average quantile"
                     (string/join
                       "\n"
                       (for [g (:genres ana-results)] (genre-str g)))
                     ""]))

(defn year-str
  [year-map]
  (let [[year cnt avg imdb-avg avg-quan] (vals (select-keys year-map [:year :count :avg :imdb-avg :avg-q]))]
    (format
      "%-5s; %-3s; %-5s; %-5s; %s"
      (str year)
      (str cnt)
      (limited-precision avg 2)
      (limited-precision imdb-avg 2)
      (limited-precision avg-quan 2))))

(defn view-yearly-str
  "String representation of yearly movies and their average ratings."
  [ana-results]
  (string/join "\n" ["Yearly analysis:"
                     "Year; Count; Average rate; IMDb average rate; Average quantile"
                     (string/join
                       "\n"
                       (for [y (:years ana-results)] (year-str y)))
                     ""]))


(defn view-results-str
  "String representation of all analysis results"
  [ana-results]
  (string/join "\n"
    ["-------------------------------------"
     "- IMDb single-list analysis results -"
     "-------------------------------------"
     (view-count-str ana-results)
     (view-mean-str ana-results)
     (view-sd-str ana-results)
     (view-corr-str ana-results)
     (view-freq-str ana-results)
     (view-entropy-str ana-results)
     (view-directors-str ana-results)
     (view-discrepancy-str ana-results)
     (view-genres-str ana-results)
     (view-yearly-str ana-results)]))

(defn view-results
  "Print all single-list analysis results in human-readable form to standard output."
  [ana-result enc]
  (common/println-enc (view-results-str ana-result) enc))

(defn jsonify-single-result
  "JSON string of an AnalysisResult."
  [single-results]
  (json/generate-string {"singleresults" single-results}))
