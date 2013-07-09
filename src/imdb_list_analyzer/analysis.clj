; Analysis tools for IMDb lists

(ns imdb-list-analyzer.analysis
  (:require
    [imdb-list-analyzer.math-tools :as mtools]
    [imdb-list-analyzer.imdb-data :as imdb]))

(defn corr-vs-imdb
  [titles-coll]
  (mtools/correlation (map :rate titles-coll) (map :imdb-rate titles-coll)))

(defn rating-num
  [titles-coll]
  (count titles-coll))

(defn rating-frequencies
  [titles-coll]
  (merge
    (zipmap imdb/rates-range (repeat (count imdb/rates-range) 0))  ; defaults
    (frequencies (map :rate titles-coll))))  ; actual nonzero frequencies

