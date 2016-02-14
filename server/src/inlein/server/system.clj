(ns inlein.server.system
  (:require [com.stuartsierra.component :as component]
            [inlein.server.server :refer [inlein-server]])
  (:gen-class))

(defn new-system [config]
  (component/system-map
   :server (inlein-server config)))

(defn -main [& args]
  (component/start (new-system {:port 0})))
