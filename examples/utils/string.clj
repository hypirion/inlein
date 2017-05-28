#!/usr/bin/env inlein

'{}

;; from https://github.com/technomancy/leiningen/blob/463b6dc9b9e7713d462136b29417d8b9a0f5dade/leiningen-core/src/leiningen/core/main.clj#L174
(let [next-dist-row (fn [s t x pprev prev]
                      (let [t-len (count t)
                            eq-chars (fn [x y] (= (nth s x) (nth t (dec y))))]
                        (reduce (fn [row y]
                                  (let [min-step
                                        (cond->
                                            (min (inc (peek row))     ;; addition cost
                                                 (inc (get prev y))   ;; deletion cost
                                                 (cond-> (get prev (dec y)) ;; substitution cost
                                                   (not (eq-chars x y)) inc))
                                          (and (pos? x) (pos? (dec y)) ;; check for transposition
                                               (eq-chars x (dec y))
                                               (eq-chars (dec x) y)
                                               (not (eq-chars x y)))
                                          (min (inc (get pprev (- y 2)))))] ;; transposition cost
                                    (conj row min-step)))
                                [(inc x)]
                                (range 1 (inc t-len)))))]
  (defn damerau-levenshtein
    "Returns the Damerau–Levenshtein distance between two strings."
    [s t]
    (let [s-len (count s)
          t-len (count t)
          first-row (vec (range (inc t-len)))
          matrix (reduce (fn [matrix x]
                           (conj matrix
                                 (next-dist-row s t x
                                                (peek (pop matrix))
                                                (peek matrix))))
                         [[] first-row]
                         (range s-len))]
      (peek (peek matrix)))))
