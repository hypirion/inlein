(ns reloader
  (:require [clojure.repl :refer :all]
            [clojure.pprint :refer [pprint]]
            [clojure.tools.namespace.repl :refer [disable-reload! refresh]]
            [com.stuartsierra.component :as component]))

(disable-reload!)

(def system (atom nil))

(def ^:private initializer nil)

(defn set-init! [init]
  (alter-var-root #'initializer (constantly init)))

(defn- stop-system [s]
  (if s (component/stop s)))

(defn init []
  (alter-var-root #'system #(do (stop-system @%)
                                (initializer)))
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
  (refresh :after 'reloader/go))
