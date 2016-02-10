(defproject inlein/client "0.1.0-SNAPSHOT"
  :description "Inlein your dependencies."
  :url "https://github.com/hyPiRion/inlein"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies []
  :source-paths []
  :java-source-paths ["src"]
  :javac-options ["-target" "1.7" "-source" "1.7" "-Xlint:-options"]
  :main inlein.client.Main
  :scm {:dir ".."}
  :aliases {"test" "junit"
            "javadoc" ["shell" "javadoc" "-d" "javadoc/inlein-${:version}"
                       "-sourcepath" "src" "inlein.client"]}
  :plugins [[lein-shell "0.5.0"]
            [lein-junit "1.1.8"]]
  :uberjar-name "inlein-%s.jar"
  ;; to avoid complaints from Leiningen
  :profiles {:uberjar {:aot :all}
             :dev {:java-source-paths ["test"]
                   :junit ["test"]
                   :dependencies [[junit/junit "4.11"]]}})
