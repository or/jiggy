(ns jiggy.jigsaw
  (:require [jiggy.catmullrom :as cmr]))

(defn generate-points [jigsaw-width jigsaw-height eccentricity]
  (into {}
        (for [x (range (inc jigsaw-width)) y (range (inc jigsaw-height))]
          [[x y]
           {:x (condp = x
                 0            0
                 jigsaw-width 1.0
                 (+ (/ (double x) jigsaw-width) (* (- (rand) 0.5) (/ 1.0 jigsaw-width) (/ eccentricity 100.0))))
            :y (condp = y
                 0             0
                 jigsaw-height 1.0
                 (+ (/ (double y) jigsaw-height) (* (- (rand) 0.5) (/ 1.0 jigsaw-height) (/ eccentricity 100.0))))}])))

(defn vertical-points [points x jigsaw-height]
  (map (fn [y] (points [x y])) (range (inc jigsaw-height))))

(defn horizontal-points [points y jigsaw-width]
  (map (fn [x] (points [x y])) (range (inc jigsaw-width))))

(defn add-horizontal-connectors [eccentricity length path]
  (cons (first path)
        (->> path
             (partition 2 1)
             (map (fn [[p0 p1]]
                    (let [{x0 :x y0 :y} p0
                          {x1 :x y1 :y} p1
                          dir           (- (* (rand-int 2) 2) 1)
                          dir2          (- (* (rand-int 2) 2) 1)
                          mx            (+ x0 (* (- x1 x0) 0.5))
                          my            (+ y0 (* (- y1 y0) 0.5))
                          nx            (+ mx (* length dir))
                          ny            (+ my (* (- y1 y0) (/ eccentricity 100.0) (rand) 0.3 dir2))
                          b1x           (+ x0 (* (- x1 x0) 0.40))
                          b1y           (+ y0 (* (- y1 y0) 0.40))
                          b2x           (+ x0 (* (- x1 x0) 0.60))
                          b2y           (+ y0 (* (- y1 y0) 0.60))
                          c1x           (+ mx (* length dir 0.75))
                          c1y           (- ny (* (- y1 y0) 0.12))
                          c2x           (+ mx (* length dir 0.75))
                          c2y           (+ ny (* (- y1 y0) 0.12))
                          ]
                      [{:x b1x :y b1y}
                       {:x c1x :y c1y}
                       {:x nx :y ny}
                       {:x c2x :y c2y}
                       {:x b2x :y b2y}
                       p1])))
             (flatten))))

(defn swap-components [{x :x y :y}]
  {:x y :y x})

(defn add-vertical-connectors [eccentricity length path]
  (->> path
       (map swap-components)
       (add-horizontal-connectors eccentricity length)
       (map swap-components)))

(defn generate-curves [jigsaw-width jigsaw-height eccentricity points]
  (let [connector-length (min (/ 0.4 jigsaw-width) (/ 0.4 jigsaw-height))]
    (-> {}
        (into
         (for [x (range (inc jigsaw-width))]
           [[:vertical x]
            (let [path (vertical-points points x jigsaw-height)]
              (cmr/catmullrom
               (condp = x
                 0            (flatten (map #(vec (repeat 6 %)) path))
                 jigsaw-width (flatten (map #(vec (repeat 6 %)) path))
                 (add-horizontal-connectors eccentricity (* connector-length) path))))]))
        (into
         (for [y (range (inc jigsaw-height))]
           [[:horizontal y]
            (let [path (horizontal-points points y jigsaw-width)]
              (cmr/catmullrom
               (condp = y
                 0             (flatten (map #(vec (repeat 6 %)) path))
                 jigsaw-height (flatten (map #(vec (repeat 6 %)) path))
                 (add-vertical-connectors eccentricity (* connector-length (/ jigsaw-width jigsaw-height)) path)))
              )])))))

(defn shift [shift-x shift-y [x y]]
  [(+ x shift-x) (+ y shift-y)])

(defn reverse-curve [curve]
  (vec (reverse (map #(vec (reverse %)) curve))))

(defn generate-pieces [jigsaw-width jigsaw-height curves]
  (into []
        (for [x (range jigsaw-width) y (range jigsaw-height)]
          (do
            (println "x" x (count (curves [:vertical x])))
            (println "vert:" (* y 6) (* (inc y) 6))
            (println "y" y (count (curves [:horizontal y])))
            (println "hori:" (* x 6) (* (inc x) 6))
            (let [curve-length 6
                  top          (subvec (curves [:horizontal y]) (* x curve-length) (* (inc x) curve-length))
                  bottom       (reverse-curve (subvec (curves [:horizontal (inc y)]) (* x curve-length) (* (inc x) curve-length)))
                  left         (reverse-curve (subvec (curves [:vertical x]) (* y curve-length) (* (inc y) curve-length)))
                  right        (subvec (curves [:vertical (inc x)]) (* y curve-length) (* (inc y) curve-length))
                  mid-x        (/ (reduce + (map #(first (last (last %))) [top right bottom left])) 4)
                  mid-y        (/ (reduce + (map #(second (last (last %))) [top right bottom left])) 4)
                  ]
              {:id    [x y]
               :pos-x mid-x
               :pos-y mid-y
               :curve (concat top right bottom left)
               #_     (vec (map (fn [curve]
                             (vec (map (partial shift (- mid-x) (- mid-y)) curve)))
                           (concat top right bottom left)))})))))

(defn generate [{:keys [jigsaw-width jigsaw-height eccentricity]}]
  (let [points (generate-points jigsaw-width jigsaw-height eccentricity)
        curves (generate-curves jigsaw-width jigsaw-height eccentricity points)
        pieces (generate-pieces jigsaw-width jigsaw-height curves)]
    {:points points
     :curves curves
     :pieces pieces}))
