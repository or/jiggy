(ns jiggy.views
  (:require
   [clojure.string :as s]
   [re-frame.core :as rf]
   [reagent.core :as r]))

(defn generic-field [key]
  [:input {:type      "input"
           :value     @(rf/subscribe [key])
           :on-change (fn [e] (rf/dispatch [:set key (-> e .-target .-value)]))}])

(defn integer-field [key]
  [:input {:type      "input"
           :value     @(rf/subscribe [key])
           :on-change (fn [e] (rf/dispatch [:set key (int (-> e .-target .-value))]))}])


(def image-pattern
  (let [image-url "/img/8bit.gif"]
    [:pattern {:id      "image"
               :x       "0"
               :y       "0"
               :width   "100%"
               :height  "100%"
               :viewBox "0 0 540 810"
               }
     [:image {:href                image-url
              :x                   0
              :y                   0
              :width               540
              :height              810
              :preserveAspectRatio "none"}]]))

(def filter-shadow
  [:filter {:id     "shadow"
            :x      "-20%"
            :y      "-20%"
            :width  "200%"
            :height "200%"}
   [:feOffset {:result "offsetOut"
               :in     "SourceAlpha"
               :dx     "10"
               :dy     "10"}]
   [:feGaussianBlur {:result       "blurOut"
                     :in           "offsetOut"
                     :stdDeviation "5"}]
   [:feBlend {:in   "SourceGraphic"
              :in2  "blurOut"
              :mode "normal"}]])

(def filter-shadow-high
  [:filter {:id     "shadow-high"
            :x      "-40%"
            :y      "-40%"
            :width  "300%"
            :height "300%"}
   [:feOffset {:result "offsetOut"
               :in     "SourceAlpha"
               :dx     "20"
               :dy     "20"}]
   [:feGaussianBlur {:result       "blurOut"
                     :in           "offsetOut"
                     :stdDeviation "10"}]
   [:feBlend {:in   "SourceGraphic"
              :in2  "blurOut"
              :mode "normal"}]])

(def filter-glow
  [:filter {:id     "glow"
            :x      "-100%"
            :y      "-100%"
            :width  "300%"
            :height "300%"}
   [:feColorMatrix {:in     "SourceAlpha"
                    :type   "matrix"
                    :values "0 0 0 1 0 0 0 0 1 0 0 0 0 0 0 0 0 0 1 0"
                    :result "colorOut"}]
   [:feGaussianBlur {:in           "colorOut"
                     :result       "blurOut"
                     :stdDeviation "10"}]
   [:feBlend {:in   "SourceGraphic"
              :in2  "glowOut"
              :mode "normal"}]])

(defn defs []
  (-> [:defs
       filter-shadow
       filter-shadow-high
       filter-glow
       image-pattern]))

(defn app
  []
  [:div
   {:style {:width "100%"}}
   [:div {:style {:display "inline"}}
    "jigsaw-width:" [integer-field :jigsaw-width]
    "jigsaw-height:" [integer-field :jigsaw-height]
    "eccentricity:" [integer-field :eccentricity]
    [:button {:on-click #(rf/dispatch [:generate-jigsaw])} "generate jigsaw"]]
   [:svg {:style               {:width      "100%"
                                :height     "90vh"
                                :border     "#888 1px solid"
                                :background "url('img/felt-table.jpg')"}
          :viewBox             "0 0 1000 600"
          :preserveAspectRatio "xMidYMin meet"}
    [defs]
    (let [width  540
          height 810
          jigsaw @(rf/subscribe [:jigsaw])]
      (into
       [:g
        [:rect {:width  width
                :height height
                :style  {:fill "url('#image')"}}]]
       (map-indexed (fn [r row]
                      (map-indexed (fn [c point]
                                     ^{:key [c r]} [:circle {:cx    (* (:x point) width)
                                                             :cy    (* (:y point) height)
                                                             :r     4
                                                             :style {:fill "#ddd"}
                                                             }]) row)) (:points jigsaw))
       ))]])
