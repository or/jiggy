(ns jiggy.client
  (:require
   [jiggy.events]
   [jiggy.subs]
   [jiggy.views]
   [re-frame.core :as rf :refer [dispatch-sync]]
   [reagent.core :as r]))

(dispatch-sync [:initialize-db])

(defn stop []
  (println "Stopping..."))

(defn start
  []
  (println "Starting...")
  (r/render [jiggy.views/app]
            (.getElementById js/document "app")))

(defn ^:export init []
  (start))
