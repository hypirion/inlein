#!/usr/bin/env inlein

'{:file-deps #{"deps/a.clj" "deps/ab.clj"}
  :dependencies [[org.clojure/clojure "1.8.0"]]}

(println a ab)
