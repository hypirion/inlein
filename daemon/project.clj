(defproject inlein/daemon "0.1.0"
  :description "The Inlein daemon"
  :url "https://github.com/hyPiRion/inlein"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [com.stuartsierra/component "0.3.0"]
                 [com.cemerick/pomegranate "0.3.0"]
                 [com.hypirion/bencode "0.1.1"]]
  :main inlein.daemon.system
  :target-path "target/%s"
  :uberjar-name "inlein-daemon-%s-standalone.jar"
  :scm {:dir ".."}
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[org.clojure/tools.namespace "0.2.11"]]
                   :source-paths ["dev"]
                   :repl-options {:init-ns user}}})
