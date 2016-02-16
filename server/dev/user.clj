(ns user
  (:require [clojure.repl :refer :all]
            [clojure.pprint :refer [pprint]]
            [clojure.java.io :as io]
            [reloaded.repl :refer [system init start stop go reset]]
            [inlein.server.system :refer [new-system]]
            [inlein.server.utils :as utils]))

(def config {:port 0
             :inlein-home (utils/inlein-home)})

(reloaded.repl/set-init! #(new-system config))
