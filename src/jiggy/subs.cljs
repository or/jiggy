(ns jiggy.subs
  (:require
   [re-frame.core :refer [reg-sub]]))

(reg-sub
 :eccentricity
 (fn [db _]
   (:eccentricity db)))

(reg-sub
 :jigsaw-width
 (fn [db _]
   (:jigsaw-width db)))

(reg-sub
 :jigsaw-height
 (fn [db _]
   (:jigsaw-height db)))

(reg-sub
 :jigsaw
 (fn [db _]
   (:jigsaw db)))
