;;; Common functions to be used anywhere in the
;;; imdb-list-analyzer program.

(ns imdb-list-analyzer.common)
 
(defn invert-multimap
  [rate-lists]
  "Invert a mapping from (duplicate) keys to lists:
   take as input keys and lists of associated values;
   return a hash-map where distinct values are mapped to a list of original keys.;

  Example:
    Original mappings:
      1 : [a b c]
      2 : [a]
      2 : [a b]
      3 : [c d]
      3 : []
    As input:
      (def input [ [1 [:a :b :c]] [2 [:a]] [2 [:a :b]] [3 [:c :d]] [3 []]])
    Result from (invert-multimap input):
      {:d [3], :c [1 3], :b [1 2], :a [1 2 2]}
  "
  (let [uniq (distinct (flatten (map second rate-lists)))]
    (reduce
      (fn [coll, val] (merge-with conj coll val))
      (zipmap uniq (repeat (count uniq) []))
      (for [[key val-list] rate-lists, val val-list] {val key}))))


(defn find-first
  "Return the first value in coll for which the predicate returns true."
  [pred coll]
  (first (filter pred coll)))

