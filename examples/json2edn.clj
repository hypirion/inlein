#!/usr/bin/env inlein

'{:dependencies [[org.clojure/clojure "1.8.0"]
                 [cheshire "5.5.0"]]
  :jvm-opts ["-XX:+TieredCompilation" "-XX:TieredStopAtLevel=1"]}

(require '[cheshire.core :refer [parsed-seq]])

(doseq [data (parsed-seq *in*)]
  (prn data))
