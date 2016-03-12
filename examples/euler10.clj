#!/usr/bin/env inlein

;; Solution to problem 10 in Project Euler. If you're unfamiliar with primes and
;; the theory around it, I heavily recommend learning about how it works. See
;; for example https://en.wikipedia.org/wiki/Sieve_of_Eratosthenes for a good
;; start.

'{:dependencies [[org.clojure/clojure "1.8.0"]
                 [com.hypirion/primes "0.2.1"]]}

(require '[com.hypirion.primes :as p])

(when-not (first *command-line-args*)
  (println "Usage:" (System/getProperty "$0") "n")
  (println "    computes the sum of the primes below n")
  (System/exit 1))

(let [n (Long/parseLong (first *command-line-args*))]
  (println (reduce + (p/take-below n))))
