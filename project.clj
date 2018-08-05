(defproject imdb-list-analyzer "0.1.0-SNAPSHOT"
  :description "Tool for analyzing IMDb rating lists"
  :url "http://github.com/dresa/imdb-list-analyzer"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/data.csv "0.1.4"]  ; parsing CSV strings
                 [cheshire "5.5.0"]]  ; JSON functions
  :main imdb-list-analyzer.core
  :aot [imdb-list-analyzer.core])
