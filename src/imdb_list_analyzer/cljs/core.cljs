(ns ^:figwheel-always imdb-list-analyzer.cljs.core
  (:require
    [reagent.core :as r]
    [ajax.core :refer [GET POST]]
    [goog.string :as gstring]
    [goog.string.format]))

(enable-console-print!)

(defonce app-state (r/atom nil))

(defonce dom-state (r/atom {:loading false
                            :error nil
                            :file nil}))

(defn round-num [num precision]
  (if (nil? num)
    num
    (gstring/format (str "%." precision "f") num)))

(defn map-keywords-to-int [m]
  (reduce #(assoc %1 (-> %2 key name int) (val %2)) {} m))

(defn map-to-pair-data [m]
  (reduce #(conj %1 {:x (key %2) :y (val %2)}) [] m))

(defn make-histogram []
  (when @app-state
    (.addGraph js/nv (fn []
                       (let [chart (.. js/nv -models multiBarChart)]
                         (.. chart -xAxis
                             (tickFormat (.format js/d3 ",f"))
                             (axisLabel "Rating"))
                         (.. chart -yAxis
                             (tickFormat (.format js/d3 ",f"))
                             (axisLabel "Frequency"))
                         (let [results @app-state
                               single-results (:singleresults results)
                               freqs (map-keywords-to-int (:freq-hash single-results))
                               imdb-freqs (map-keywords-to-int (:imdb-freq-hash single-results))
                               pair-freqs (map-to-pair-data freqs)
                               imdb-pair-freqs (map-to-pair-data imdb-freqs)]
                           (.. js/d3 (select "#barchart svg")
                               (datum (clj->js [{:values pair-freqs
                                                 :key "your frequency"}
                                                {:values imdb-pair-freqs
                                                 :key "imdb frequency"}]))
                               (call chart))))))))

(defn result-handler [response]
    (do
    (println (-> response (js/JSON.parse) (js->clj :keywordize-keys true)))
    (reset! app-state (-> response (js/JSON.parse) (js->clj :keywordize-keys true)))
    (swap! dom-state assoc :loading false)
    (make-histogram)))

(defn error-handler [{:keys [status status-text]}]
  (do
    (swap! dom-state assoc :error (str status " " status-text ". Is the file a valid imdb csv-file?"))
    (swap! dom-state assoc :loading false)))

(defn histogram-component []
  [:div {:id "barchart" :style {:width 700 :height 400}}
   [:svg]])

(defn inst-component []
  [:div.container
   [:h2 "IMDB list analyzer"]
   [:br]
   [:div
      [:h4 "Anyone with an IMDb account can retrieve their own ratings file as follows:"]
        [:li "Login to www.imdb.com with you account."]
        [:li "Search for a personal \"Your Ratings\" view that contains all your rated movies."]
        [:li "Click \"Export this list\" at the bottom of the page."]
        [:li "Save file into the filesystem."]
        [:li "Use 'Choose file' & 'Analyze file' buttons to analyze your ratings"]]])

(defn form-component []
  [:div.container
   [:h3 "Load your ratings:"]
   [:form {:encType "multipart/form-data"
           :method "post"
           :name "csv-form"
           :id "csv-form"}
    [:input {:class "btn btn-default btn-file"
             :type "file" :name "csv"
             :id "csv-input" :accept ".csv"
             ;:value @file
             :on-change
              #(swap! dom-state assoc :file (.-name (aget (.-files (.getElementById js/document "csv-input")) 0)))}]
    [:br]
    [:div {:class "inline"}
     [:input  {:class "btn btn-primary"
              :type "button"
              :value "Analyze file"
              :disabled (or
                          (:loading @dom-state)
                          (nil? (:file @dom-state)))
              :onClick #(do
                         (swap! dom-state assoc :loading true)
                         (swap! dom-state assoc :error nil)
                         (POST "/analyze"
                              {:body (js/FormData.
                                       (.getElementById js/document "csv-form"))
                               :handler result-handler
                               :error-handler error-handler}))}]
     [:br]
     [:br]
     [:input  {:class "btn btn-default"
               :type "button"
               :value "Show example results"
               :disabled (:loading @dom-state)
               :onClick #(do
                          (swap! dom-state assoc :loading true)
                          (swap! dom-state assoc :error nil)
                          (POST "/analyze-example"
                                {:handler result-handler
                                 :error-handler error-handler}))}]
       [:div.loading {:hidden (not (:loading @dom-state))}
        [:i {:class "fa fa-cog fa-spin fa-4x"}]]]]
   [:div {:class "container"}
    [:h4 (:error @dom-state)]]])

(defn result-component []
  (let [results @app-state
        single-results (:singleresults results)
        freqs (map-keywords-to-int (:freq-hash single-results))
        imdb-freqs (map-keywords-to-int (:imdb-freq-hash single-results))
        best-dirs (take 10 (:dir-ranks single-results))
        worst-dirs (take-last 10 (:dir-ranks single-results))]
    [:div.container {:id "results-elem"
                     :hidden (nil? results)}
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
      [histogram-component]

     ;Button to re-render the histogram, as the graph is sometimes bugged on first load
      [:button {:class  "btn btn-default"
                :onClick #(make-histogram)}
       "Re-render graph"]
     
     ;Replaced by the graph
     #_[:table.table
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
           [:td (get imdb-freqs (key freq))]])]]

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
   ;[histogram-component]
    [result-component]])

(defn ^:export run []
  (r/render [root-component]
            (.getElementById js/document "app")))