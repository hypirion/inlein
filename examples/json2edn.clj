#!/usr/bin/env inlein

'{:dependencies [[org.clojure/clojure "1.8.0"]
                 [cheshire "5.5.0"]]}

(require '[cheshire.core :refer [parsed-seq]])

(doseq [data (parsed-seq *in*)]
  (prn data))
