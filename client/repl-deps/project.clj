(defproject com.hypirion/inlein-repl "0.2.0-SNAPSHOT"
  :description "Base repl dependencies for inlein."
  :url "http://inlein.org"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths []
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [net.cgrand/parsley "0.9.3" :exclusions [org.clojure/clojure]]
                 [reply "0.3.7" :exclusions [ring/ring-core
                                             com.cemerick/drawbridge]]])
