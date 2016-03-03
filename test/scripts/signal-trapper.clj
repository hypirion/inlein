#!/usr/bin/env inlein

'{:dependencies [[org.clojure/clojure "1.8.0"]
                 [beckon "0.1.1"]]
  :jvm-opts ["-XX:+TieredCompilation" "-XX:TieredStopAtLevel=1"]}

(require 'beckon)

(def sig (first *command-line-args*))

(reset! (beckon/signal-atom sig)
        [(fn [] (println "got" sig))])

(Thread/sleep 4000)

(println "exiting")
