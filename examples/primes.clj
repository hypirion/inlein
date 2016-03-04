#!/usr/bin/env inlein

'{:dependencies [[org.clojure/clojure "1.8.0"]
                 [com.hypirion/primes "0.2.1"]]
  :jvm-opts ["-XX:+TieredCompilation" "-XX:TieredStopAtLevel=1"]}

(require '[com.hypirion.primes :as p])

(when-not (first *command-line-args*)
  (println "Usage:" (System/getProperty "$0") "prime-number")
  (System/exit 1))

(-> (first *command-line-args*)
    (Long/parseLong)
    (p/get)
    println)
