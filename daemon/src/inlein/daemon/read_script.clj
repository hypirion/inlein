(ns inlein.daemon.read-script
  (:require [inlein.daemon.dependencies :as deps]
            [clojure.java.io :as io])
  (:import (java.io FileNotFoundException)))

(defn- slurp*
  [file]
  (try (slurp file)
       (catch FileNotFoundException fnfe
         (throw (ex-info "FileNotFoundException"
                         {:error (str "Could not open file "
                                      (.getMessage fnfe))})))))

(defn- read-string*
  [data]
  (try (read-string data)
       (catch RuntimeException e
           (throw (ex-info "RuntimeException"
                         {:error (str "Could not parse inlein options: "
                                      (.getMessage e))})))))

(defn- validate-params
  [raw-params]
  (when-not (= 'quote (first raw-params))
    (throw (ex-info "Parameters is not quoted"
                    {:error (str "Parameters\n" (prn-str raw-params) "are not quoted")}))))

(defn- extract-jvm-opts
  [params]
  (let [cp-string (deps/classpath-string (:dependencies params))]
    {:jvm-opts (concat (:jvm-opts params ["-XX:+TieredCompilation" "-XX:TieredStopAtLevel=1"])
                       ["-cp" cp-string])}))

(defn read-script-params
  [file]
  (let [contents (slurp* file)
        raw-params (read-string* contents)
        params (second raw-params)]
    (validate-params raw-params)
    (extract-jvm-opts params)))
