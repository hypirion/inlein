#!/bin/sh

#_[
   eval "exec $(inlein --sh-cmd "$0" "$@")"
#_ 0]

'{:dependencies [[org.clojure/clojure "1.9.0-alpha13"]
                 [reply "0.3.7" :exclusions [net.cgrand/parsley]]
                 [net.cgrand/parsley "0.9.3"] ;; to fix parsley warning
                 [com.cemerick/pomegranate "0.3.1"]]
  :jvm-opts ["-XX:+TieredCompilation" "-XX:TieredStopAtLevel=1" "-Dfile.encoding=UTF-8"]}

(require 'cemerick.pomegranate)
(cemerick.pomegranate/add-dependencies :coordinates
                                       (mapv (fn [s] [(read-string s) "RELEASE"])
                                             *command-line-args*)
                                       :repositories [["central" {:url "https://repo1.maven.org/maven2/" :snapshots false}]
                                                      ["clojars" {:url "https://clojars.org/repo/"}]])

(reply.ReplyMain/main (into-array String ["--standalone"]))
