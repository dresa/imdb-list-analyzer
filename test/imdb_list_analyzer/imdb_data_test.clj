;; Testing IMDb data handling.
;; Tested on Clojure 1.7.0
;;
;; Esa Junttila
;; 2018-08-05 (originally 2013-06-30)

(ns imdb-list-analyzer.imdb-data-test
  (:require [clojure.test :refer :all]
            [imdb-list-analyzer.imdb-data :refer :all]))

;
; Old IMDb CSV format (pre-2017)
;

(deftest csv-read
  (testing "Read all 1648 rows (including header) from an example CSV file."
    (is (= (count (read-raw-data "resources/example_ratings_A.csv")) 1648))))

(deftest example-imdb-data-read
  (testing "Read all 1648 rows in parsed form."
    (is (= (count (read-imdb-file "resources/example_ratings_A.csv")) 1648))))

(deftest example-imdb-data-read-json
  (testing "Read all 1648 rows in parsed form with a JSON round-trip."
    (is (= (->
             "resources/example_ratings_A.csv"
             read-raw-data
             convert-csv-to-json-str
             parse-imdb-data-from-json-str
             count)
           1648))))

;
; New IMDb CSV format (post-2018)
;

(deftest csv-read
  (testing "Read all 2009 rows (including header) from another example CSV file."
    (is (= (count (read-raw-data "resources/example_ratings_2018_format_A.csv")) 2009))))

(deftest example-imdb-data-read
  (testing "Read all 2009 rows in parsed form."
    (is (= (count (read-imdb-file "resources/example_ratings_2018_format_A.csv")) 2009))))

(deftest example-imdb-data-read-json
  (testing "Read all 1648 rows in parsed form with a JSON round-trip."
    (is (= (->
             "resources/example_ratings_2018_format_A.csv"
             read-raw-data
             convert-csv-to-json-str
             parse-imdb-data-from-json-str
             count)
           2009))))

