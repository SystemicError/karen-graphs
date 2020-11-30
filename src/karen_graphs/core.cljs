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


(set! (.-onclick (.getElementById js/document "decade")) get-emblematics)
