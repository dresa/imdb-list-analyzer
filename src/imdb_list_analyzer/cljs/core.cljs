(ns ^:figwheel-always imdb-list-analyzer.cljs.core
  (:require
    [reagent.core :as r]
    [ajax.core :refer [GET POST]]))

(enable-console-print!)

(println "Hello world!")

(defonce app-state (r/atom {}))

(defonce test-conn (r/atom ""))

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
        single-results (:singleresults results)]
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
          [:td (str (:mean single-results))]
          [:td (str (:imdb-mean single-results))]]
         [:tr
          [:td "Standard deviation of movie ratings"]
          [:td (str (:stdev single-results))]
          [:td (str (:imdb-stdev single-results))]]
         [:tr
          [:td "Correlation between ratings and IMDb rating averages"]
          [:td (str (:corr single-results))]
          [:td ""]
          ]]]]
     [:p (str "RAW DEV")
     [:p (str results)]]]))

(defn root-component []
  [:div.container
    [inst-component]
    [form-component]
    [result-component]])

(defn ^:export run []
  (r/render [root-component]
            (.getElementById js/document "app")))
