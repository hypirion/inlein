(ns user
  (:require [clojure.repl :refer :all]
            [clojure.pprint :refer [pprint]]
            [reloader :refer :all]
            [inlein.daemon.system :refer [new-system-atom]]
            [inlein.daemon.utils :as utils]))

(def config {:port 0
             :inlein-home (utils/inlein-home)})

(set-init! #(new-system-atom config))


