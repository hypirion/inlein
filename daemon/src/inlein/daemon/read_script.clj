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
  [data file]
  (try (read-string data)
       (catch RuntimeException e
           (throw (ex-info "RuntimeException"
                           {:error (str "Could not parse inlein options for file "
                                        file ": " (.getMessage e))})))))

(defn- validate-params
  [raw-params file]
  (when-not (= 'quote (first raw-params))
    (throw (ex-info (str "Parameters in " file " are not quoted")
                    {:error (str "Parameters in " file "\n" (prn-str raw-params) "are not quoted")})))
  (let [params (second raw-params)]
    (when (and (contains? params :file-deps)
               (not (set? (:file-deps params))))
      (let [msg (str ":file-deps in " file " must be a set ")]
        (throw (ex-info msg {:error msg}))))))

(defn- extract-jvm-opts
  [params opts]
  (let [cp-string (deps/classpath-string (:dependencies params)
                                         (select-keys opts [:transfer-listener]))]
    {:jvm-opts (concat (:jvm-opts params ["-XX:+TieredCompilation" "-XX:TieredStopAtLevel=1"])
                       ["-cp" cp-string])}))

(defn- file-params [file]
  (let [contents (slurp* file)
        raw-params (read-string* contents file)]
    (validate-params raw-params file)
    (-> (second raw-params)
        deps/add-global-exclusions)))

(defn read-script-params
  ([file]
   (read-script-params file {}))
  ([file opts]
   (-> (file-params file)
       (extract-jvm-opts (select-keys opts [:transfer-listener])))))
