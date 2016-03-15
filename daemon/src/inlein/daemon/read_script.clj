(ns inlein.daemon.read-script
  (:require [clojure.string :as c.str]
            [inlein.daemon.dependencies :as deps]
            [inlein.daemon.utils :as utils]
            [flatland.ordered.set :as ordered.set])
  (:import (java.io FileNotFoundException)
           (flatland.ordered.set OrderedSet)))

;; Used to prettyprint errors: Filenames becomes relative to the root file,
;; which is the best we can do right now (without passing in yet another
;; paramter when calling jvm-opts).
(def ^:dynamic ^:private *root* nil)

(defn- readable-fname [fname]
  (utils/relativize-sibling *root* fname))

(defn- slurp*
  [fname]
  (try (slurp fname)
       (catch FileNotFoundException fnfe
         (let [fname (readable-fname fname)]
           (throw (ex-info "FileNotFoundException"
                           {:error (str "Could not open file " fname ": "
                                        (.getMessage fnfe))}))))))

(defn- read-string*
  [data fname]
  (try (read-string data)
       (catch RuntimeException e
         (let [fname (readable-fname fname)]
           (throw (ex-info "RuntimeException"
                           {:error (str "Could not parse inlein options for file "
                                        fname ": " (.getMessage e))}))))))

(defn- validate-params
  [raw-params fname]
  (when-not (= 'quote (first raw-params))
    (let [fname (readable-fname fname)]
      (throw (ex-info (str "Parameters in " fname " are not quoted")
                      {:error (str "Parameters in " fname "\n" (prn-str raw-params)
                                   "are not quoted")}))))
  (let [params (second raw-params)]
    (when (and (contains? params :file-deps)
               (not (set? (:file-deps params))))
      (let [msg (str ":file-deps in " (readable-fname fname) " must be a set ")]
        (throw (ex-info msg {:error msg}))))))

(defn- extract-jvm-opts
  [params extra-deps opts]
  (let [params (deps/add-global-exclusions params)
        cp-string (deps/classpath-string (concat (:dependencies params) extra-deps)
                                         (select-keys opts [:transfer-listener]))]
    {:jvm-opts (concat (:jvm-opts params ["-XX:+TieredCompilation" "-XX:TieredStopAtLevel=1"])
                       ["-cp" cp-string])}))

(defn- absolutize-file-deps [params fname]
  (if-not (:file-deps params)
    params
    (assoc params
           :file-deps (into #{} (map #(utils/resolve-abs-sibling fname %)
                                     (:file-deps params))))))

(defn- single-file-params [fname]
  (let [contents (slurp* fname)
        raw-params (read-string* contents fname)]
    (validate-params raw-params fname)
    (-> (second raw-params)
        (absolutize-file-deps fname)
        deps/add-global-exclusions)))


(defn- all-file-params
  ([fname]
   (all-file-params {} fname))
  ([already-fetched fname]
   (if (already-fetched fname)
     already-fetched
     (let [params (single-file-params fname)]
       (reduce
        all-file-params
        (assoc already-fetched fname params)
        (:file-deps params))))))

(defn- os-peek [^OrderedSet stack]
  (peek (.i->k stack)))

(defn- os-member
  [^OrderedSet stack elem]
  (if-let [loc (get (.k->i stack) elem)]
    (remove #{:flatland.ordered.set/empty}
            (subvec (.i->k stack) loc))))

(defn- detect-cycles
  [fmap ^OrderedSet stack]
  (let [fname (os-peek stack)
        params (get fmap fname)]
    (doseq [dep-fname (:file-deps params)]
      (when (contains? stack dep-fname)
        (let [cycle (map readable-fname (os-member stack dep-fname))]
          (throw (ex-info "Cyclic :file-dep detected"
                          {:error (str "Cyclic :file-dep detected: "
                                       (c.str/join " -> " cycle)
                                       " -> " (readable-fname dep-fname))}))))
      (detect-cycles fmap (conj stack dep-fname)))))

(defn- toposort
  [fmap root ^OrderedSet order]
  (if (contains? order root)
    order
    (let [deps (:file-deps (get fmap root))]
      (conj (reduce #(toposort fmap %2 %1) order deps)
            root))))

(defn- assoc-nonempty [m k v]
  (if (seq v)
    (assoc m k v)
    m))

(defn- cat-merge [k params]
  (apply concat (keep k params)))

(defn- merge-params [params]
  (-> {}
      (assoc-nonempty :dependencies (cat-merge :dependencies (reverse params)))
      (assoc-nonempty :jvm-opts (cat-merge :jvm-opts params))
      (assoc-nonempty :exclusions (cat-merge :exclusions (reverse params)))))

(defn- convert-maven-dep
  ([artifact version]
   (convert-maven-dep nil artifact nil nil version))
  ([group artifact version]
   (convert-maven-dep group artifact nil nil version))
  ([group artifact packaging version]
   (convert-maven-dep group artifact packaging nil version))
  ([group artifact packaging classifier version]
   (cond-> [(symbol (if group
                      (str group "/" artifact)
                      artifact))
            version]
     packaging  (conj :extension packaging)
     classifier (conj :classifier classifier))))

(defn- parse-maven-deps
  [deps-str]
  (->> (c.str/split (or deps-str "") #";")
       (remove empty?)
       (mapv #(apply convert-maven-dep (c.str/split % #":")))))

(defn read-script-params
  ([fname]
   (read-script-params fname nil {}))
  ([fname deps opts]
   (if fname
     (let [fname (utils/abs-path fname)]
       (binding [*root* fname]
         (let [all-params (all-file-params fname)
               _ (detect-cycles all-params (ordered.set/ordered-set fname))
               order (toposort all-params fname (ordered.set/ordered-set))
               params (merge-params (map all-params order))]
           (-> params
               (extract-jvm-opts (parse-maven-deps deps) (select-keys opts [:transfer-listener]))
               (assoc :files (seq order))))))
     (extract-jvm-opts {} (parse-maven-deps deps) (select-keys opts [:transfer-listener])))))
