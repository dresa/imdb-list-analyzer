(ns imdb-list-analyzer.cljs.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [ajax.core :refer [GET POST]]))

(enable-console-print!)

(println "Hello world!")

(defn handler [response]
  (println (str response)))

(defui analysis-form
       Object
       (render [this]
               (dom/div nil
                 (dom/form #js
                         {:encType "multipart/form-data"
                          :method "post"
                          :name "csv-form"
                          :id "csv-form"}
                   (dom/h3 nil "Dev functionality, see results in browser's console")
                   (dom/input #js {:type "file" :name "csv"
                                   :id "csv-input" :accept ".csv"
                                   :required true})
                   (dom/input #js {:type "button"
                                   :value "Analyze file"
                                   :onClick #(POST "/analyze"
                                                   {:body (js/FormData.
                                                              (.getElementById js/document "csv-form"))

                                                    :handler handler})})
                   ;"TODO"
                   #_(dom/input #js {:type "button"
                             :value "Show sample results!"
                             :onClick #(POST "/sample"
                                             {:body "needed?"
                                              :handler handler})})
                   (dom/input #js {:type "button"
                             :value "Hello (test connection)"
                             :onClick #(POST "/hello"
                                             {:body true
                                              :handler handler})}))
                  (dom/div nil
                    (dom/h4 nil "Anyone with an IMDb account can retrieve their own ratings file as follows:")
                    (dom/ol nil
                          (dom/li nil "Login to www.imdb.com with you account.")
                          (dom/li nil "Search for a personal \"Your Ratings\" view that contains all your rated movies.")
                          (dom/li nil "Click \"Export this list\" at the bottom of the page.")
                          (dom/li nil "Save file into the filesystem.")
                          (dom/li nil "Use 'Choose file' & 'Analyze file' buttons to analyze your ratings"))))))


(def input (om/factory analysis-form))

(js/ReactDOM.render
  (input)
  (gdom/getElement "app"))