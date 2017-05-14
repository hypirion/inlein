(ns inlein.daemon.dependencies
  (:require [clojure.string :as string]
            [cemerick.pomegranate.aether :as aether]))

(def ^:private default-repositories
  {"central" {:url "https://repo1.maven.org/maven2/" :snapshots false}
   "clojars" {:url "https://clojars.org/repo/"}})

(defn dep-key-idx [dep key]
  (loop [i 2]
    (cond (<= (count dep) i) nil
          (= (dep i) key) (inc i)
          :else (recur (+ i 2)))))

(defn- merge-exclusions
  "Global exclusions override original ones"
  [originals globals]
  (concat globals (remove (set globals) originals)))

(defn- add-exclusions
  "Appends exclusions to dep, or inserts :exclusions if there are none."
  [exclusions dep]
  (if-let [exclusion-idx (dep-key-idx dep :exclusions)]
    (update dep exclusion-idx merge-exclusions exclusions)
    (conj dep :exclusions exclusions)))

(defn add-global-exclusions
  "Prepends :exclusions to all dependencies, if :exclusions is set."
  [{:keys [dependencies exclusions] :as params}]
  (if-not (and (seq dependencies) (seq exclusions))
    params
    (assoc params
           :dependencies (mapv #(add-exclusions exclusions %) dependencies))))

(defn resolve-dependencies
  ([dependencies]
   (resolve-dependencies dependencies {}))
  ([dependencies {:keys [transfer-listener]}]
   (aether/resolve-dependencies
    :coordinates dependencies
    :transfer-listener (or transfer-listener :stdout)
    :repositories default-repositories)))

(defn classpath-string
  ([dependencies]
   (classpath-string dependencies {}))
  ([dependencies opts]
   (->> (resolve-dependencies dependencies opts)
        aether/dependency-files
        (filter #(re-find #"\.(jar|zip)$" (.getName %)))
        (map #(.getAbsolutePath %))
        (string/join java.io.File/pathSeparatorChar))))

(defn transfer-logger
  [log-fn]
  (fn [{:keys [type method transferred resource error] :as evt}]
    (let [{:keys [name size repository transfer-start-time]} resource]
      (case type
        :started (let [ksize (if-not (neg? size)
                               (Math/round (double (max 1 (/ size 1024)))))

                       msg (string/join " "
                                        [(case method
                                           :get "Retrieving"
                                           :put "Sending")
                                         name
                                         (if ksize (format "(%sk)" ksize))
                                         (case method :get "from" :put "to")
                                         repository])]
                   (log-fn msg))
        (:corrupted :failed) (when error (log-fn (.getMessage error)))
        nil))))
