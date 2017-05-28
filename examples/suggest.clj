#!/usr/bin/env inlein

'{:dependencies [[org.clojure/clojure "1.8.0"]]
  :file-deps #{"utils/string.clj"}}

(def commands #{"help" "build" "deploy" "test" "retest"})
(def input-command (first *command-line-args*))
(defn closest-commands []
  (->> commands
       (map #(vector % (damerau-levenshtein input-command %)))
       (sort-by second)
       (filter #(< (second %) 4))
       (map first)))

(when-not input-command
  (println "Usage:" (System/getProperty "$0") "[command]")
  (println "Where [command] is one of the following commands:")
  (doseq [cmd (sort commands)]
    (println " " cmd))
  (System/exit 1))

(when-not (commands input-command)
  (println "Could not find command" input-command)
  (when-let [cmds (seq (closest-commands))]
    (println "Perhaps you meant one of these?")
    (doseq [cmd cmds]
      (println " " cmd)))
  (System/exit 1))

(println "Running command" input-command
         "with arguments" (rest *command-line-args*))
