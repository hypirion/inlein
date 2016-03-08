#!/usr/bin/env inlein

;; Solution to the New Year's Resolution problem in the Facebook Hacker Cup
;; 2015 Qualification:
;; https://www.facebook.com/hackercup/problem/1036037553088752/
;;
;; Usage: ./resolution.clj < input.txt > output.txt

'{:dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/math.combinatorics "0.1.1"]]
  :jvm-opts []}
;; Although we could use the default jvm opts, optimisations may be necessary to
;; get this to reasonable performance, as it's brute forcing the result.

(require '[clojure.string :as s]
         '[clojure.math.combinatorics :as combo])

(defn sum-foods [foods]
  (reduce #(mapv + %1 %2) [0 0 0] foods))

(defn solve [{:keys [goal foods]}]
  (let [subset-counts (map sum-foods (combo/subsets foods))]
    (boolean (some #(= goal %) subset-counts))))

(def read-food (juxt read read read))

(defn read-input []
  (let [goal (read-food)
        food-count (read)
        foods (vec (repeatedly food-count read-food))]
    {:goal goal
     :foods foods}))

(defn pprint-result [t result]
  (format "Case #%d: %s" (inc t) (if result "yes" "no")))

(defn main []
  (let [T (read)
        inputs (doall (repeatedly T read-input))
        results (pmap solve inputs)]
    (->> (map-indexed pprint-result results)
         (s/join "\n")
         (println))
    (flush)
    (System/exit 0)))

(main)
