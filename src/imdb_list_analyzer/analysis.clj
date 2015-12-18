;;; Analysis tools for IMDb lists

(ns imdb-list-analyzer.analysis
  (:require
    [imdb-list-analyzer.math-tools :as mtools]
    [imdb-list-analyzer.imdb-data :as imdb]
    [imdb-list-analyzer.common :as com]
    [clojure.set])
  (:import (java.util Collections)))

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

;; Director rankings

"Number of random samples in a statistical test"
(def num-samples 1000)

"Random seed for generating random numbers that can be replicated"
(def random-seed 1)

(defn- rating-directors
  "Mapping that connects each director to all ratings on
  his/her movies, restricting to given titles and list-ratings"
  [titles-coll]
  (com/invert-multimap
    (map #(vector (:rate %) (:directors %)) titles-coll)))

(defn- sample-null-ref-value
  "Compute sample mean from an empirical distribution, using in total num-values samples"
  [emp-distr randoms]
  (mtools/mean (mtools/sample-distr emp-distr randoms)))

(defn- compute-reference-value
  "SLOW, deprecated.
  Compute a statistical p-value for a director, given the following arguments:
    * rates: all list-ratings for a director's movies
    * emp-distr: empirical distribution of all movie ratings, regardless of director
  The quality statistic, p-value, measures the probability that a random sample of ratings
  from an empirical distribution has an average below director's average. The closer the
  p-value is to 1.0, the better the director is compared to other directors; p-values
  close to 0.0 indicate that the director performs poorly.
  "
  [rates emp-distr samples-by-size]
  (let [mu (mtools/mean rates)
        num (count rates)
        distr-mu (:mean emp-distr)  ; empirical mean represents an average director
        samples (get samples-by-size num)]
    (/ (count (filter #(if (<= distr-mu mu) (< % mu) (<= % mu)) samples)) (count samples))))

(defn- compute-fast-reference-value
  "Compute reference value in a faster way.
  Assume that samples are in sorted order, and then use a binary search."
  [rates emp-distr samples-by-size]
  (let [mu (double (mtools/mean rates))  ; average rate for a specific director
        num (count rates)  ; number of ratings for the director
        distr-mu (:mean emp-distr)  ; empirical mean represents an average director
        samples (get samples-by-size num)  ; pre-computed, randomly generated avg rating samples
        eps 1e-10
        ref-mu (if (<= distr-mu mu) (- mu eps) (+ mu eps))  ; include or exclude equal samples
        idx (Collections/binarySearch samples ref-mu)  ; find the switch point in ordered samples
        k (if (mtools/non-neg-num? idx) idx (dec (Math/abs idx)))] ; how many samples below ref-val?
    (/ k (count samples))))

(defn- director-empirical-rank
  "Compute a statistical p-value for each director (quality measure)"
  [director-rate-lists emp-distr samples-by-size]
  (map #(compute-fast-reference-value % emp-distr samples-by-size) director-rate-lists))

(defn- director-rank
  "Compute a mapping from directors to their quality measure (statistical p-value).
  This implementation pre-computes samples for each size of a director's ratings
  basket, perhaps ranging from 1 to 15. These samples are then compared to
  rating averages."
  [titles-coll]
  (let [dirs (rating-directors titles-coll)
        emp-distr (mtools/generate-emp-distr (frequencies (map :rate titles-coll)))
        max-dir-rates (apply max (map count (vals dirs)))
        rnd-count (* num-samples max-dir-rates)
        randoms (seq (take rnd-count (mtools/rnd-gen random-seed)))
        size-range (range 1 (inc max-dir-rates))
        ; rate basket size --> random sample average on ratings:
        samples-by-size (zipmap
                          size-range
                          (for [s size-range]
                            (sort  ; sorted to enable binary search
                              (map
                                #(double (sample-null-ref-value emp-distr %))
                                (take num-samples (partition-all s randoms))))))]
    (map vector
         dirs
         (director-empirical-rank
           (map second dirs)
           emp-distr
           samples-by-size))))

(defn director-qualities
  "Compute a list of directors, sorted by their quality parameters (statistical p-values).
  Best directors first (high p-values)."
  [titles-coll]
  (reverse (sort-by second (director-rank titles-coll))))


;; Rating discrepancies

(defn rating-discrepancy
  "Compute quantile-based differences between my ratings and IMDb averages."
  [titles-coll]
  (let [[my-rates imdb-rates] (map #(map % titles-coll) [:rate :imdb-rate])
        to-distr #(mtools/generate-emp-distr (frequencies %))
        [my-distr imdb-distr] (map to-distr [my-rates imdb-rates])
        to-cumu-map (fn [distr] (zipmap (:points distr) (rest (:cumu-probs distr))))
        my-quantiles (map #(mtools/smooth-ecdf % my-distr) my-rates)  ; approx. half-integers
        imdb-cumu (to-cumu-map imdb-distr)
        imdb-quantiles (map #(get imdb-cumu %) imdb-rates)
        discrepancy (map - my-quantiles imdb-quantiles)]
    (reverse (sort-by
               :discrepancy
               (for [[t d] (map vector titles-coll discrepancy)]
                 (zipmap
                   [:title :rate :imdb-rate :discrepancy]
                   [(:title t) (:rate t) (:imdb-rate t) d]))))))

;; Genre analysis

(defn- rating-genres
  "Mapping that connects each genre to all ratings on
  labeled movies, restricting to given titles and list-ratings"
  [titles-coll col]
  (com/invert-multimap
    (map #(vector (col %) (:genres %)) titles-coll)))

(defn genre-averages
  "Compute the average ratings for each genre"
  [titles-coll]
  (let [my-genre-rates (rating-genres titles-coll :rate)
        imdb-genre-rates (rating-genres titles-coll :imdb-rate)
        [my-rates imdb-rates] (map #(map % titles-coll) [:rate :imdb-rate])
        to-distr #(mtools/generate-emp-distr (frequencies %))
        [my-distr imdb-distr] (map to-distr [my-rates imdb-rates])]
    (reverse (sort-by
               :avg
               (for [g (sort (keys my-genre-rates))]
                 (let [avg (mtools/mean (get my-genre-rates g))]
                   {:genre g
                    :count (count (get my-genre-rates g))
                    :avg avg
                    :imdb-avg (mtools/mean (get imdb-genre-rates g))
                    :avg-q (mtools/smooth-ecdf avg my-distr)}))))))

;; Analysis by year
(defn- rating-years
  "Mapping that connects each year to all ratings on
  movies published that year, restricting to given titles and list-ratings"
  [titles-coll col]
  (com/invert-multimap
    (map #(vector (col %) [(:year %)]) titles-coll)))

(defn yearly-averages
  "Compute for each year the average rating of movies published"
  [titles-coll]
  (let [my-year-rates (rating-years titles-coll :rate)
        imdb-year-rates (rating-years titles-coll :imdb-rate)
        [my-rates imdb-rates] (map #(map % titles-coll) [:rate :imdb-rate])
        to-distr #(mtools/generate-emp-distr (frequencies %))
        [my-distr imdb-distr] (map to-distr [my-rates imdb-rates])]
    (reverse (sort-by
               :avg
               (for [y (sort (keys my-year-rates))]
                 (let [avg (mtools/mean (get my-year-rates y))
                       imdb-avg (mtools/mean (get imdb-year-rates y))]
                   {:year y
                    :count (count (get my-year-rates y))
                    :avg avg
                    :imdb-avg imdb-avg
                    :avg-q (mtools/smooth-ecdf avg my-distr)}))))))


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

