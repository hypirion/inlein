#!/bin/sh

#_[
   eval "exec $(inlein --sh-cmd "$0" "$@")"
#_ 0]

;; Your own, self-customised repl with all the tools you'd like to use!
;;
;; Note: Some signals may be broken with the JVM client, as it cannot pass
;; signals to the child process easily. As a result, the incredibly nasty hack
;; above is used to make shell call inlein, which in turn creates a shell
;; command which in turn is exec'd by shell. It's effectively a bit like
;; trampoline inlined. (This is also possible to fix with a non-jvm inlein
;; client, but we're not there yet)

'{:dependencies [[org.clojure/clojure "1.8.0"]
                 [reply "0.3.7" :exclusions [net.cgrand/parsley]]
                 [net.cgrand/parsley "0.9.3"] ;; to fix parsley warning
                 [org.clojure/math.numeric-tower "0.0.4"]
                 [org.clojure/math.combinatorics "0.1.1"]
                 [spyscope "0.1.5"]
                 [org.clojure/tools.trace "0.7.9"]]
  :jvm-opts ["-XX:+TieredCompilation" "-XX:TieredStopAtLevel=1" "-Dfile.encoding=UTF-8"]}

(require '[clojure.math.numeric-tower :refer :all]
         '[clojure.math.combinatorics :refer :all]
         '[clojure.pprint :refer [pprint]]
         '[clojure.tools.trace :as trace]
         'spyscope.core)

;; For pythonistas, we alias ** to expt
(def ** expt)

;; Common lispers prefer trace and untrace on functions
(defmacro trace [& body]
  `(trace/trace-vars ~@body))
(defmacro untrace [& body]
  `(trace/untrace-vars ~@body))

(reply.ReplyMain/main (into-array String ["--standalone"]))
