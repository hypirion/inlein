(ns user
  (:require [clojure.repl :refer :all]
            [clojure.pprint :refer [pprint]]
            [clojure.java.io :as io]
            [reloaded.repl :refer [system init start stop go reset]]
            [inlein.server.system :refer [new-system]]))

(def config {:port 0
             :inlein-home (or
                           (get (System/getenv) "INLEIN_HOME")
                           (.getAbsolutePath (io/file (System/getProperty "user.home") ".inlein")))})

(reloaded.repl/set-init! #(new-system config))
