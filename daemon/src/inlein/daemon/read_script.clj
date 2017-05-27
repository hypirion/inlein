(ns inlein.daemon.read-script
  (:require [inlein.daemon.dependencies :as deps]
            [clojure.string :as c.str]
            [flatland.ordered.set :as ordered.set])
  (:import (java.io FileNotFoundException)
           (java.nio.file Paths)
           (flatland.ordered.set OrderedSet)))

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

(defn into-path [fname]
  (Paths/get fname (make-array String 0)))

(defn- rel-path [source fname]
  (let [fpath (into-path fname)]
    (if (.isAbsolute fpath)
      fname
      (-> (into-path source)
          (.resolveSibling fpath)
          (.toAbsolutePath)
          (.normalize)
          (.toString)))))

(defn- abs-path [path]
  (-> (into-path path)
      (.toAbsolutePath)
      (.normalize)
      (.toString)))

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

(defn- os-peek [^OrderedSet stack]
  (peek (.i->k stack)))

(defn- os-member
  [^OrderedSet stack elem]
  (if-let [loc (get (.k->i stack) elem)]
    (remove #{:flatland.ordered.set/empty}
            (subvec (.i->k stack) loc))))

(defn- detect-cycles
  ([fmap ^OrderedSet stack]
   (let [fname (os-peek stack)
         params (get fmap fname)]
     (doseq [dep (:file-deps params)]
       (let [dep-fname (rel-path fname dep)]
         (when (contains? stack dep-fname)
           (throw (ex-info "Cyclic :file-dep detected"
                           {:error (str "Cyclic :file-dep detected: "
                                        (c.str/join " -> " (os-member stack dep-fname))
                                        " -> " dep-fname)})))
         (detect-cycles fmap (conj stack dep-fname)))))))

(defn read-script-params
  ([fname]
   (read-script-params fname {}))
  ([fname opts]
   (let [fname (abs-path fname)
         all-params (all-file-params fname)]
     (detect-cycles all-params (ordered.set/ordered-set fname))
     (-> (get all-params fname)
         (extract-jvm-opts (select-keys opts [:transfer-listener]))))))
