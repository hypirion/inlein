(ns inlein.daemon.read-script
  (:require [inlein.daemon.dependencies :as deps]
            [clojure.java.io :as io])
  (:import (java.io File FileNotFoundException)))

(defn- slurp*
  [file]
  (try (slurp file)
       (catch FileNotFoundException fnfe
         (throw (ex-info "FileNotFoundException"
                         {:error (str "Could not open file "
                                      (.getMessage fnfe))})))))

(defn- read-string*
  [data fname]
  (try (read-string data)
       (catch RuntimeException e
           (throw (ex-info "RuntimeException"
                           {:error (str "Could not parse inlein options for file "
                                        fname ": " (.getMessage e))})))))

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

(defn- file-params [fname]
  (let [contents (slurp* fname)
        raw-params (read-string* contents fname)]
    (validate-params raw-params fname)
    (-> (second raw-params)
        deps/add-global-exclusions)))

(defn- rel-path [source fname]
  (prn (list 'rel-path source fname))
  (if (.isAbsolute (File. fname))
    fname
    (-> (File. source)
        (.getParentFile)
        (File. fname)
        (.getPath))))

(defn- all-file-params
  ([fname]
   (all-file-params {} fname))
  ([already-fetched fname]
   (if (already-fetched fname)
     already-fetched
     (let [params (file-params fname)]
       (reduce
        all-file-params
        (assoc already-fetched fname params)
        (map #(rel-path fname %) (:file-deps params)))))))

(defn read-script-params
  ([fname]
   (read-script-params fname {}))
  ([fname opts]
   (-> (all-file-params fname)
       (get fname)
       (extract-jvm-opts (select-keys opts [:transfer-listener])))))
