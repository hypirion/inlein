(ns inlein.server.system
  (:require [com.stuartsierra.component :as component]
            [inlein.server.server :refer [inlein-server]]
            [clojure.java.io :as io])
  (:gen-class))

;; TODO: consider meta-merging.
(defn new-system [config]
  (component/system-map
   :server (inlein-server config)))

(defn -main [& args]
  (let [inlein-home (or
                     (get (System/getenv) "INLEIN_HOME")
                     (.getAbsolutePath (io/file (System/getProperty "user.home") ".inlein")))]
    (component/start (new-system {:port 0
                                  :inlein-home inlein-home}))))
