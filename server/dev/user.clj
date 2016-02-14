(ns user
  (:require [clojure.repl :refer :all]
            [clojure.pprint :refer [pprint]]
            [reloaded.repl :refer [system init start stop go reset]]
            [inlein.server.system :refer [new-system]]))

(reloaded.repl/set-init! #(new-system {:port 0}))
