;;; Analysis tools for IMDb lists

(ns imdb-list-analyzer.analysis
  (:require
    [imdb-list-analyzer.math-tools :as mtools]
    [imdb-list-analyzer.imdb-data :as imdb]
    [imdb-list-analyzer.common :as com]
    [clojure.set]))

(defn corr-vs-imdb
  "Compute correlation between list ratings and average IMDb ratings in given titles"
  [titles-coll]
  (mtools/correlation (map :rate titles-coll) (map :imdb-rate titles-coll)))

(defn rating-num
  "Number of rated movie titles in the list of titles"
  [titles-coll]
  (count titles-coll))

(defn rating-mean
  "Compute the mean rating in given movie titles"
  [titles-coll]
  (mtools/mean (map :rate titles-coll)))

(defn imdb-mean
  "Compute the mean of average IMDb ratings for given movie titles"
  [titles-coll]
  (mtools/mean (map :imdb-rate titles-coll)))

(defn rating-stdev
  "Compute the standard deviation for the ratings in given titles"
  [titles-coll]
  (mtools/stdev (map :rate titles-coll)))

(defn imdb-stdev
  "Compute the standard deviation for the average IMDb ratings on given titles"
  [titles-coll]
  (mtools/stdev (map :imdb-rate titles-coll)))

(defn rating-entropy
  "Compute the Shannon information entropy on the empirical distribution of
  ratings on given titles"
  [titles-coll]
  (mtools/entropy (vals (frequencies (map :rate titles-coll)))))

(defn imdb-entropy
  "Compute the Shannon information entropy on the empirical distribution of
  rounded IMDb average ratings on given titles"
  [titles-coll]
  (mtools/entropy (vals (frequencies (map #(Math/round ^double (:imdb-rate %)) titles-coll)))))

(defn max-entropy
  "Compute the theoretical maximum Shannon entropy, given a number of discrete
  rating-classes (equivalent to a uniform distribution)"
  [n]
  (mtools/entropy (repeat n 1)))

(defn rating-frequencies
  "Return the (non-zero) frequency of each rating among given titles"
  [titles-coll]
  (merge
    (zipmap imdb/rates-range (repeat (count imdb/rates-range) 0))  ; defaults
    (frequencies (map :rate titles-coll))))  ; actual nonzero frequencies

(defn imdb-frequencies
  "Return the (non-zero) frequency of each rounded IMDb average rating, among given titles"
  [titles-coll]
  (merge
    (zipmap imdb/rates-range (repeat (count imdb/rates-range) 0))  ; defaults
    (frequencies (map #(Math/round ^double (:imdb-rate %)) titles-coll))))  ; actual nonzero frequencies

(defn- rating-directors
  "Mapping that connects each director to all ratings on
  his/her movies, restricting to given titles and list-ratings"
  [titles-coll]
  (com/invert-multimap
    (map #(vector (:rate %) (:directors %)) titles-coll)))

(defn- sample-null-ref-value
  "Compute sample mean from an empirical distribution, using in total num-values samples"
  [num-values emp-distr]
  (mtools/mean (take num-values (mtools/sample-distr emp-distr))))

 "Number of random samples in a statistical test"
(def ^:private num-samples 1000)  ; FIXME: should recycle samples instead of regenerating

(defn- compute-reference-value
  "Compute a statistical p-value for a director, given the following arguments:
    * rates: all list-ratings for a director's movies
    * emp-distr: empirical distribution of all movie ratings, regardless of director
  The quality statistic, p-value, measures the probability that a random sample of ratings
  from an empirical distribution has an average below director's average. The closer the
  p-value is to 1.0, the better the director is compared to other directors; p-values
  close to 0.0 indicate that the director performs poorly.
  "
  [rates emp-distr]
  (let [mu (mtools/mean rates)
        num (count rates)
        distr-mu (:empirical-mean emp-distr)  ; empirical mean represents an average director
        samples (take num-samples (repeatedly #(sample-null-ref-value num emp-distr)))]
    (/ (count (filter #(if (<= distr-mu mu) (< % mu) (<= % mu)) samples)) (count samples))))

(defn- director-empirical-rank
  "Compute a statistical p-value for each director (quality measure)"
  [director-rate-lists emp-distr]
  (map #(compute-reference-value % emp-distr) director-rate-lists))

(defn- director-rank
  "Compute a mapping from directors to their quality measure (statistical p-value)"
  [titles-coll]
  (let [dirs (rating-directors titles-coll)
        emp-distr (mtools/gen-emp-distr (map :rate titles-coll))]
    (map vector dirs (director-empirical-rank (map second dirs) emp-distr))))

(defn director-qualities
  "Compute a list of directors, sorted by their quality parameters (statistical p-values).
  Best directors first (high p-values)."
  [titles-coll]
  (reverse (sort-by second (director-rank titles-coll))))


;; Dual-list analysis functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- common-id-set
  "Set of 'id's that are fould in both title collections"
  [titles-coll another-titles-coll]
  (let [a-ids (set (map :id titles-coll))
        b-ids (set (map :id another-titles-coll))]
    (clojure.set/intersection a-ids b-ids)))

(defn- sorted-common-titles
  "Sorted (sub)sequence of titles whose id's are found in the 'id-set'.
  Sorting makes the ordering unique and comparable to other collections with same titles."
  [titles-coll id-set]
  (sort-by :id (filter #(id-set (:id %)) titles-coll)))

(defn corr-vs-another
  "Pearson correlation coefficient between two collections of title ratings,
  restricting to their shared titles. FIXME: if too few shared titles, then probably fails"
  [titles-coll-a titles-coll-b]
  (let [id-set (common-id-set titles-coll-a titles-coll-b)
        sorted-coll-a (sorted-common-titles titles-coll-a id-set)
        sorted-coll-b (sorted-common-titles titles-coll-b id-set)]
    (mtools/correlation (map :rate sorted-coll-a) (map :rate sorted-coll-b))))

(defn common-count
  "Number of shared titles in two title collections"
  [titles-coll another-titles-coll]
  (count (common-id-set titles-coll another-titles-coll)))

