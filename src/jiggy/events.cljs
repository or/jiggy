(ns jiggy.events
  (:require
   [re-frame.core :as rf]
   [jiggy.jigsaw :as jigsaw]))

(def  default-db
  {:eccentricity  5
   :jigsaw-width  10
   :jigsaw-height 15})

(rf/reg-event-db
 :initialize-db
 (fn [db _]
   (merge default-db db)))

(rf/reg-event-db
 :set
 (fn [db [_ key value]]
   (assoc db key value)))

(rf/reg-event-db
 :generate-jigsaw
 (fn [db [_]]
   (println db)
   (assoc db :jigsaw (jigsaw/generate db))))
