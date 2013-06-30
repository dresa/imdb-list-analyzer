;; Dealing with IMDb rating list data.
;;
;; Esa Junttila
;; 2013-06-30

(ns imdb-list-analyzer.imdb-data
  (:require [clojure-csv.core :as csv]
            [clojure.java.io :as io]))

; In order to interpret special Western characters correctly, such as
; Scandinavian characters, make a best guess for the encoding.
; It could be, for example, "UTF-8" or "windows-1253"
(def local-encoding (.name (java.nio.charset.Charset/defaultCharset)))

(defn read-data
  "Read IMDb ratings data from a CSV-formatted file.
   The result is a lazy sequence of vectors (of fields)."
  [filename]
  (with-open [file (io/reader filename)]
    (csv/parse-csv (slurp file :encoding local-encoding))))
