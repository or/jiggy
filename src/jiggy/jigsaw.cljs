(ns jiggy.jigsaw
  (:require [jiggy.catmullrom :as cmr]))

(defn generate [{:keys [jigsaw-width jigsaw-height eccentricity]}]
  (println jigsaw-width jigsaw-height eccentricity)
  {:points (vec (map (fn [y]
                       (vec (map (fn [x]
                                   {:x (condp = x
                                         0            0
                                         jigsaw-width 1.0
                                         (+ (/ (double x) jigsaw-width) (* (- (rand) 0.5) (/ 1.0 jigsaw-width) (/ eccentricity 100.0))))
                                    :y (condp = y
                                         0             0
                                         jigsaw-height 1.0
                                         (+ (/ (double y) jigsaw-height) (* (- (rand) 0.5) (/ 1.0 jigsaw-height) (/ eccentricity 100.0))))}
                                   ) (range (inc jigsaw-width))))
                       ) (range (inc jigsaw-height))))})
