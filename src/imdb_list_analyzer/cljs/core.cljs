(ns ^:figwheel-always imdb-list-analyzer.cljs.core
  (:require
    [reagent.core :as r]
    [ajax.core :refer [GET POST]]
    [goog.string :as gstring]
    [goog.string.format]))

(enable-console-print!)

(println "Hello world!")

(defonce app-state (r/atom {}))

(defonce test-conn (r/atom ""))

(defn round-num [num precision]
  (if (nil? num)
    num
    (gstring/format (str "%." precision "f") num)))

;TODO error handler
(defn result-handler [response]
  (do
    (println (-> response (js/JSON.parse) (js->clj :keywordize-keys true)))
    (swap! app-state assoc (-> response (js/JSON.parse) (js->clj :keywordize-keys true)))))

(defn test-handler [response]
  (do
    (println (str response))
    (reset! test-conn (str response))))

(defn inst-component []
  [:div
   [:h2 "IMDB list analyzer"]])

(defn form-component []
  [:div.container
   [:form {:encType "multipart/form-data"
           :method "post"
           :name "csv-form"
           :id "csv-form"}
    [:input {:class "btn btn-default btn-file"
             :type "file" :name "csv"
             :id "csv-input" :accept ".csv"
             :required true}]
    [:br]
    [:input  {:class "btn btn-primary"
              :type "button"
              :value "Analyze file"
              :onClick #(POST "/analyze"
                              {:body (js/FormData.
                                       (.getElementById js/document "csv-form"))

                               :handler result-handler})}]
    [:br]
    [:br]
    [:input {:class "btn btn-default"
             :type "button"
             :value "Hello (test connection)"
             :onClick #(POST "/hello"
                             {:body true
                              :handler test-handler})}]
    [:br]
    [:br]
    [:p (str @test-conn)]]])

(defn result-component []
  (let [results (first (first @app-state))
        single-results (:singleresults results)
        freqs (:freq-hash single-results)
        imdb-freqs (:imdb-freq-hash single-results)
        best-dirs (take 10 (:dir-ranks single-results))
        worst-dirs (take-last 10 (:dir-ranks single-results))]
    [:div.container
     [:h3 "IMDB single-list analysis results"]
      [:table.table
       [:thead
        [:tr
         [:th "Metric"]
         [:th "Result"]
         [:th "IMDB Average"]]
        [:tbody
         [:tr
          [:td "Number of movie ratings"]
          [:td (str (:num single-results))]
          [:td ""]]
         [:tr
          [:td "Mean of movie ratings"]
          [:td (str (round-num (:mean single-results) 2))]
          [:td (str (round-num (:imdb-mean single-results) 2))]]
         [:tr
          [:td "Standard deviation of movie ratings"]
          [:td (str (round-num (:stdev single-results) 2))]
          [:td (str (round-num (:imdb-stdev single-results) 2))]]
         [:tr
          [:td "Correlation between ratings and IMDb rating averages"]
          [:td (str (round-num (:corr single-results) 2))]
          [:td ""]]]]]

      [:h3 "Frequencies of ratings"]
      [:table.table
       [:thead
        [:tr
         [:th "Rate"]
         [:th "Frequency"]
         [:th "Freq %"]
         [:th "IMDb frequency"]]]
       [:tbody
        (for [freq freqs]
          ^{:key (key freq)}
          [:tr
           [:td (str (key freq))]
           [:td (val freq)]
           [:td (str (round-num (* 100 (/ (val freq) (:num single-results))) 2) " %")]
           [:td ((key freq) imdb-freqs)]])]]

      [:h3 "The best directors"]
      [:table.table
        [:thead
         [:tr
          [:th "Director-name"]
          [:th "Rank-p-value"]
          [:th "Rates"]]]
        [:tbody
         (for [dir-data best-dirs
               :let [dir (str (first (first dir-data)))
                     rates (str (last (first dir-data)))
                     p-value (str (last dir-data))]]
           ^{:key dir}
           [:tr
            [:td dir]
            [:td p-value]
            [:td rates]])]]

     [:h3 "The worst directors"]
     ;TODO remove repeating code by making a list-directors function
     [:table.table
      [:thead
       [:tr
        [:th "Director-name"]
        [:th "Rank-p-value"]
        [:th "Rates"]]]
      [:tbody
       (for [dir-data worst-dirs
             :let [dir (str (first (first dir-data)))
                   rates (str (last (first dir-data)))
                   p-value (str (last dir-data))]]
         ^{:key dir}
         [:tr
          [:td dir]
          [:td p-value]
          [:td rates]])]]

     #_[:p (str "RAW DEV")
     #_[:p (str results)]]]))

(defn root-component []
  [:div.container
    [inst-component]
    [form-component]
    [result-component]])

(defn ^:export run []
  (r/render [root-component]
            (.getElementById js/document "app")))
