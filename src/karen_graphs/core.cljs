(ns karen-graphs.core
  (:require [clojure.browser.repl :as repl]
            [karen-graphs.karens :as karens]
            [karen-graphs.data :as data]))

;; (defonce conn
;;   (repl/connect "http://localhost:9000/repl"))

(enable-console-print!)

;(println "Hello world!")

(def name-list (data/read-name-lists))

(def birth-totals (data/read-birth-data))

(def histogram (data/read-age-histogram))


(println (str "\nname-list: " (take 1 name-list)
;              "\nbirth-totals: " (take 2 birth-totals)
;              "\nhistogram: " histogram
;              "\nhistogram count: " (count histogram)))
              "\nprob-birth-name(nl,bt,ah,John,male)= " (karens/prob-birth-name name-list birth-totals histogram "John" "male")))

(defn get-emblematics
  "List the most emblematic names of a requested decade."
  []
  (let [emblems-p (.getElementById js/document "emblems")
        decade-str (.-value (.getElementById js/document "decade"))
        decade (js/parseInt decade-str)
        male-names (set (map :name (filter #(= (:sex %) "male") name-list)))
        female-names (set (map :name (filter #(= (:sex %) "female") name-list)))
        ;(prob-birth-decade-given-name-and-sex name-list birth-totals birth-name decade sex)
        male-names-probs (map #(hash-map :name %
                                         :prob (karens/prob-birth-decade-given-name-and-sex
                                                 name-list
                                                 birth-totals
                                                 histogram
                                                 decade
                                                 %
                                                 "male"))
                              male-names)
        filtered-male-names-probs (filter #(not= "Insufficient data." (:prob %)) male-names-probs)
        male-tops (take 10 (reverse (sort-by :prob filtered-male-names-probs)))
        female-names-probs (map #(hash-map :name %
                                           :prob (karens/prob-birth-decade-given-name-and-sex
                                                   name-list
                                                   birth-totals
                                                   histogram
                                                   decade
                                                   %
                                                   "female"))
                                female-names)
        filtered-female-names-probs (filter #(not= "Insufficient data." (:prob %)) female-names-probs)
        female-tops (take 10 (reverse (sort-by :prob filtered-female-names-probs)))
        m-preamble "<table>"
        m-amble (apply str (map #(str "<tr><td>" (:name %) "</td><td>" (:prob %) "</td></tr>") male-tops))
        m-postamble "</table>"
        f-preamble "<table>"
        f-amble (apply str (map #(str "<tr><td>" (:name %) "</td><td>" (:prob %) "</td></tr>") female-tops))
        f-postamble "</table>"
        ]
    (set! (.-innerHTML emblems-p) (str m-preamble m-amble m-postamble "<br><br>" f-preamble f-amble f-postamble))))

(defn draw-axes
  "Draws the axes for the graph."
  [canvas]
  (let [ctx (.getContext canvas "2d")
        decades (range 1920 2020 10)
        h (.-height canvas)
        w (.-width canvas)
        margin 50
        ]
    (.clearRect ctx 0 0 w h)
    (set! (.-fillStyle ctx) "#000000")
    (doseq [i (range 0 10)]
      (.fillText ctx
                 (str (nth decades i) "s")
                 (+ (* i (/ w 10.0)) 20)
                 (- h 25)))
    ; draw the vertical scale
    (set! (.-strokeStyle ctx) "#CCCCCC")
    (.beginPath ctx)
    (doseq [i (range 0 5)]
      (.moveTo ctx 0 (- h margin (* i 0.2 h)))
      (.lineTo ctx w (- h margin (* i 0.2 h)))
      )
    (.stroke ctx)
    ))

(defn draw-distribution
  "Draws the probabiliby distribution of the given name."
  [canvas birth-name sex]
  (let [ctx (.getContext canvas "2d")
        decades (range 1920 2020 10)
        probs (map #(karens/prob-birth-decade-given-name-and-sex
                       name-list
                       birth-totals
                       histogram
                       %
                       birth-name
                       sex)
                   decades)
        h (.-height canvas)
        w (.-width canvas)
        heights (map #(* h % 2.0) probs)
        margin 50
        ]
    (set! (.-fillStyle ctx) "#0000FF")
    (doseq [i (range 0 10)]
           (.fillRect ctx
                      (+ (* i (/ w 10.0)) 40)
                      (- h (nth heights i) margin)
                      30
                      (nth heights i)
                      ))
;    (print (str "Got request to draw distribution with name " birth-name " and sex " sex
;                "\nprobs: " probs
;                "\nheights: " heights))
     ))


(defn draw-popularity
  "Draws the probabiliby popularity of the given name."
  [canvas birth-name sex]
  (let [ctx (.getContext canvas "2d")
        decades (range 1920 2020 10)
        probs (map #(karens/prob-name-given-birth-decade-and-sex name-list birth-totals birth-name % sex) decades)
        h (.-height canvas)
        w (.-width canvas)
        heights (map #(* h % 2.0) probs)
        margin 50
        ]
    (set! (.-fillStyle ctx) "#FF0000")
    (doseq [i (range 0 10)]
           (.fillRect ctx
                      (+ (* i (/ w 10.0)) 10)
                      (- h (nth heights i) margin)
                      30
                      (nth heights i)
                      ))
    ;(print (str "Got request to draw popularity with name " birth-name " and sex " sex
    ;            "\nprobs: " probs
    ;            "\nheights: " heights))
    ))

(defn draw-graphs
  "Draw the prob vs. decade and popularity vs. decade graphs."
  []
  (let [canvas (.getElementById js/document "distribution")
        birth-name (.-value (.getElementById js/document "birth-name"))
        sex (.-value (.getElementById js/document "sex"))
        ]
    (draw-axes canvas)
    (draw-distribution canvas birth-name sex)
    (draw-popularity canvas birth-name sex)
    ))

(set! (.-onclick (.getElementById js/document "decade")) get-emblematics)

(set! (.-onclick (.getElementById js/document "graph")) draw-graphs)
