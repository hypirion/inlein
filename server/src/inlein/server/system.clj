(ns inlein.server.system
  (:require [com.stuartsierra.component :as component]
            [inlein.server.server :refer [inlein-server]]
            [inlein.server.utils :as utils]
            [clojure.java.io :as io])
  (:gen-class))

;; TODO: consider meta-merging.
(defn new-system [config]
  (component/system-map
   :server (inlein-server config)))

(defn -main [& args]
  (component/start
   (new-system {:port 0
                :inlein-home (utils/inlein-home)})))
