(defproject inlein/client "0.1.0-SNAPSHOT"
  :description "Inlein your dependencies."
  :url "https://github.com/hyPiRion/inlein"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths []
  :java-source-paths ["src"]
  :javac-options ["-target" "1.7" "-source" "1.7" "-Xlint:-options"]
  :dependencies []
  :main inlein.client.Main
  :scm {:dir ".."}
  :uberjar-name "inlein-%s.jar"
  ;; to avoid complaints from Leiningen
  :profiles {:uberjar {:aot :all}})
