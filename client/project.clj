(defproject inlein/client "0.3.0-SNAPSHOT"
  :description "Inlein your dependencies."
  :url "https://github.com/hyPiRion/inlein"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[com.hypirion/bencode "0.1.1"]]
  :source-paths []
  :java-source-paths ["src"]
  :javac-options ["-target" "1.7" "-source" "1.7" "-Xlint:-options" "-XDignore.symbol.file"]
  :main inlein.client.Main
  :scm {:dir ".."}
  :aliases {"javadoc" ["shell" "javadoc" "-d" "javadoc/inlein-${:version}"
                       "-sourcepath" "src" "inlein.client"]
            "uberjar" ["do"
                       ["uberjar"]
                       ["shell" "sh" "executable-jars.sh" "${:version}"]]}
  :plugins [[lein-shell "0.5.0"]]
  :jar-name "inlein-no-deps-%s.jar"
  :uberjar-name "inlein-%s.jar"
  ;; to avoid complaints from Leiningen
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[org.clojure/clojure "1.8.0"]]}})
