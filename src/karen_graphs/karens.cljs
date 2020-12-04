(ns karen-graphs.karens
  (:require [clojure.string :as str]
            [karen-graphs.data :as data]))

(defn prob-birth-decade
  "Give the probability that someone was born in a given decade, using only their sex as evidence."
  [age-histogram decade sex]
  (let [; add up number of people with their decade and sex in the histogram 
        index (* 2 (- 201 (/ decade 10)))
        pentades (take 2 (drop index age-histogram))
        decaders (if (= sex "male")
                   (+ (:male-count (first pentades))
                      (if (> decade 1910)
                        (:male-count (nth pentades 1))
                        0))
                   (+ (:female-count (first pentades))
                      (if (> decade 1910)
                        (:female-count (nth pentades 1))
                        0)))
        ; divide by the total number of people in the histogram (US population)
        total (reduce + (map #(if (= sex "male")
                                (:male-count %)
                                (:female-count %))
                             age-histogram))
        ]
    (float (/ decaders total))))

(defn prob-name-given-birth-decade-and-sex
  "Give the probability that someone has a given name knowing only the decade of their birth and sex of as evidence."
  [name-list birth-totals birth-name decade sex]
  (let [name-count (:count (first (filter #(and (= sex (:sex %))
                                                (= birth-name (:name %))
                                                (= decade (:decade %))) name-list)))
        entry (first (filter #(= decade (:decade %)) birth-totals))
        total ((if (= "male" sex) :male-count :female-count) entry)
        ]
    (if (= nil name-count)
      0
      (float (/ name-count total)))))

(defn prob-birth-name
  "Give the probability of someone having a given birth name knowing only their sex."
  [name-list birth-totals age-histogram birth-name sex]
  (let [decades (range 1900 2020 10)
        ; find the total number of folks with the name and sex, divide by total number of folks with that sex
        sex-total (reduce + (map (if (= sex "male") :male-count :female-count) age-histogram))
        ; total number of folks with given name and sex
          ; survivors with name = survivors in age group * (folks born with name in that decade/folks born in that decade)
        name-counts (map :count (filter #(and (= (:sex %) sex)
                                              (= (:name %) birth-name)) name-list))
        survivor-counts (for [i (range (count name-counts))]
                          (* (reduce + (map #((if (= sex "male") :male-count :female-count) %) (take 2 (drop i age-histogram))))
                             (/ (nth name-counts i)
                                ((if (= sex "male") :male-count :female-count) (nth birth-totals i)))))
;        dummy (println (str "From prob-birth-name:"
;                            "\nsex: " sex
;                            "\nbirth-name: " birth-name
;                            "\nsex-total" sex-total
;                            "\nname-counts" name-counts
;                            "\nsurvivor-counts" survivor-counts))
        ]
    (float (/ (reduce + survivor-counts) sex-total))
    ))

(defn prob-birth-decade-given-name-and-sex
  "Compute the probability someone has a given birth decade given only their name and sex.  Account for whether they are alive or dead (i.e., age histograms)."
  [name-list birth-totals age-histogram decade birth-name sex]
  (let [decades (range 1920 2020 10)
        ; the probability that someone in this decade has this name
        probs-name (map #(prob-name-given-birth-decade-and-sex name-list birth-totals birth-name % sex) decades)
        ; the probability that someone born in this decade, given only sex
        probs-decade (map #(prob-birth-decade age-histogram % sex) decades)
        ; assume the two prior are independent, multiply to get prob has name and decade
        probs (map * probs-name probs-decade)
        ; index of the above, based on decade
        index (/ (- decade 1920) 10)
        ]
    (/ (nth probs index) (reduce + probs))))

