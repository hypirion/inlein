(ns inlein.daemon.dependencies
  (:require [clojure.string :as string]
            [cemerick.pomegranate.aether :as aether]))

(def ^:private default-repositories
  {"central" {:url "https://repo1.maven.org/maven2/" :snapshots false}
   "clojars" {:url "https://clojars.org/repo/"}})


(defn resolve-dependencies
  [dependencies]
  (aether/resolve-dependencies
   :coordinates dependencies
   :transfer-listener :stdout
   :repositories default-repositories))

(defn classpath-string
  [dependencies]
  (->> (resolve-dependencies dependencies)
      aether/dependency-files
      (filter #(re-find #"\.(jar|zip)$" (.getName %)))
      (map #(.getAbsolutePath %))
      (string/join java.io.File/pathSeparatorChar)))
