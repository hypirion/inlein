(ns inlein.daemon.system
  (:require [com.stuartsierra.component :as component]
            [inlein.daemon.server :refer [inlein-server]]
            [inlein.daemon.utils :as utils]
            [clojure.java.io :as io])
  (:gen-class))

;; TODO: consider meta-merging.
(defn new-system-atom [config]
  (let [system-atom (atom nil)
        system (component/system-map
                :server (inlein-server config system-atom))]
    (reset! system-atom system)
    system-atom))

(defn -main [& args]
  (let [system-atom (new-system-atom
                     {:port 0
                      :inlein-home (utils/inlein-home)})]
    (swap! system-atom component/start)))

