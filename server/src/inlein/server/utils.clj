(ns inlein.server.utils
  (:require [clojure.java.io :as io]))

(defn inlein-home
  "Returns the path to the inlein home directory."
  []
  (or
   (System/getenv "INLEIN_HOME")
   (.getAbsolutePath (io/file (System/getProperty "user.home") ".inlein"))))
