(ns inlein.daemon.utils
  (:require [clojure.java.io :as io])
  (:import (java.nio.file Path Paths)))

(defn inlein-home
  "Returns the path to the inlein home directory."
  []
  (or
   (System/getenv "INLEIN_HOME")
   (.getAbsolutePath (io/file (System/getProperty "user.home") ".inlein"))))

(defn- to-path
  [fname]
  (if (instance? Path fname)
    fname
    (Paths/get fname (make-array String 0))))

(defn abs-path
  "Converts path to an absolute, normalised path string."
  [path]
  (-> (to-path path)
      (.toAbsolutePath)
      (.normalize)
      (.toString)))

(defn resolve-sibling
  "Converts sibling to a path which is relative to source's
  directory. Returns the absolute, normalised path string."
  [source sibling]
  (-> (to-path source)
      (.resolveSibling sibling)
      (abs-path)))
