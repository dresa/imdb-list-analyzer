(ns script.figwheel)

(require '[figwheel-sidecar.repl :as r]
         '[figwheel-sidecar.repl-api :as ra])

(ra/start-figwheel!
  {:figwheel-options {:ring-handler 'imdb-list-analyzer.server/app}
   :build-ids ["dev"]
   :all-builds
                     [{:id "dev"
                       :figwheel true
                       :source-paths ["src"]
                       :compiler {:main 'imdb-list-analyzer.cljs.core
                                  :http-server-root "public"
                                  :asset-path "js"
                                  :output-to "resources/public/js/main.js"
                                  :output-dir "resources/public/js"
                                  :on-jsload "imdb-list-analyzer.cljs.core/run"
                                  :verbose true}}]})

(ra/cljs-repl)
