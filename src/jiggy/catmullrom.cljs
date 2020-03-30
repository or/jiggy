(ns jiggy.catmullrom
  (:require [clojure.string :as string]))

;; catmullrom

(defn smooth-component [dir v0 v1 v2 tension]
  (dir v1 (* tension (/ (- v2 v0) 6))))

(defn smooth-point [dir p0 p1 p2 tension]
  (->> (range 2)
       (map (fn [i] (smooth-component dir (p0 i) (p1 i) (p2 i) tension)))
       (vec)))

(defn calculate-cubic-bezier-curve
  [tension [p0 p1 p2 p3]]
  (let [cp1 (smooth-point + p0 p1 p2 tension)
        cp2 (smooth-point - p1 p2 p3 tension)]
    [p1 cp1 cp2 p2]))

(defn catmullrom
  [points & {:keys [tension] :or {tension 1}}]
  (->> (concat [(first points)] points [(last points)])
       (map (juxt :x :y))
       (partition 4 1)
       (map (partial calculate-cubic-bezier-curve tension))
       (vec)))

;; svg

(defn svg-move-to [[x y]]
  (str "M" x "," y))

(defn svg-curve-to [[_ cp1 cp2 p2]]
  (str "C" (string/join "," (flatten [cp1 cp2 p2]))))

(defn curve->svg-path [curve]
  (let [start (first (first curve))]
    (string/join "" (concat [(svg-move-to start)]
                            (map svg-curve-to curve)))))

;; position on path

(defn square [x]
  (* x x))

(defn distance [p0 p1]
  (->> [:x :y]
       (map (fn [comp] (- (comp p0) (comp p1))))
       (map square)
       (reduce +)
       (Math/sqrt)))

(defn path->leg-lengths [path]
  (->> path
       (partition 2 1)
       (map (fn [[p0 p1]] (* (distance p0 p1) (:d p1))))))

(defn find-leg-progress [leg-lengths progress]
  (let [total-length      (apply + leg-lengths)
        adjusted-progress (* progress total-length)]
    (->> leg-lengths
         (map-indexed vector)
         (reduce
          (fn [[_ _ traversed] [index leg-length]]
            (let [next-traversed (+ traversed leg-length)]
              (if (> adjusted-progress next-traversed)
                [index 1 next-traversed]
                (reduced [index
                          (/ (- adjusted-progress traversed) leg-length)
                          adjusted-progress]))))
          [0 0 0]))))

(defn interpolate-point-cubic [[p1 cp1 cp2 p2] t]
  (let [t2  (* t t)
        t3  (* t2 t)
        tr  (- 1 t)
        tr2 (* tr tr)
        tr3 (* tr2 tr)]
    (->> (range 2)
         (map (fn [i] (+ (* tr3 (p1 i))
                         (* 3 tr2 t (cp1 i))
                         (* 3 tr t2 (cp2 i))
                         (* t3 (p2 i)))))
         (map vector [:x :y])
         (into {}))))

(defn point-on-curve [path-data leg-lengths progress]
  (let [[leg-index sub-progress _] (find-leg-progress leg-lengths progress)]
    (interpolate-point-cubic (nth path-data leg-index) sub-progress)))
