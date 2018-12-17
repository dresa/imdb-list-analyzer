;;; Common functions to be used anywhere in the
;;; imdb-list-analyzer program.

(ns imdb-list-analyzer.common
  (:import java.nio.charset.Charset
           java.io.File))
 
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
  "First value in coll for which the predicate returns true."
  [pred coll]
  (first (filter pred coll)))


(defn file-exists?
  "Does a given filename exist in the filesystem?"
  [filename]
  (.exists ^File (clojure.java.io/as-file filename)))


"Local encoding constant: to interpret special Western characters correctly,
 such as Scandinavian characters, make a best guess for the encoding.
 It could be, for example, 'UTF-8', 'windows-1252', or 'Cp850'."
(def local-encoding (.name (Charset/defaultCharset)))

"Windows-related encoding needed to convert a string into target encoding for
print-outs. Perhaps it's default codepage for 'print' method in Java on Windows."
(def trans-enc "cp1252")

(defn encoding-supported?
  "Does this system support the encoding/codepage with given name?"
  [name]
  (Charset/isSupported name))

(defn create-encoding
  "Return a Charset instance what matches with given encoding name, or 'nil' if not supported."
  [name]
  (if (encoding-supported? name) (Charset/forName name)))

(defn change-encoding
  "Change the representative encoding of a string."
  [s source-enc target-enc]
  (String. (.getBytes s source-enc) (create-encoding target-enc)))

(defn println-enc
  "Print a string with given encoding (via a Windows-related encoding conversion)"
  ([s] (println s))
  ([s enc]
   (if enc
     (println (change-encoding s enc trans-enc))
     (println s))))
