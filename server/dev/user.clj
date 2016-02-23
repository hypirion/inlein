(ns user
  (:require [clojure.repl :refer :all]
            [clojure.pprint :refer [pprint]]
            [clojure.java.io :as io]
            [clojure.tools.namespace.repl :refer [disable-reload! refresh]]
            [com.stuartsierra.component :as component]
            [inlein.server.system :refer [new-system-atom]]
            [inlein.server.utils :as utils]))

(def config {:port 0
             :inlein-home (utils/inlein-home)})

;; Want to do disable-reload! here to avoid system redefinition, but it should
;; be fine I _think_. Since this doesn't fit with reloaded.repl we have to do it
;; manually. However, it means that changes in config (^) will not be updated.
;; Boo.
(disable-reload!)

(def system (atom nil))

(defn- stop-system [s]
  (if s (component/stop s)))

(defn init []
  (alter-var-root #'system #(do (stop-system @%)
                                (new-system-atom config)))
  :ok)

(defn start []
  (swap! system component/start)
  :started)

(defn stop []
  (swap! system stop-system)
  :stopped)

(defn go []
  (init)
  (start))

(defn clear []
  (swap! system #(do (stop-system %) nil))
  :ok)

(defn reset []
  (stop)
  (refresh :after 'user/start))
