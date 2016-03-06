#!/usr/bin/env inlein

'{:dependencies [[org.clojure/clojure "1.8.0"]]
  :jvm-opts ["-XX:+TieredCompilation" "-XX:TieredStopAtLevel=1"]}

(prn *command-line-args*)
