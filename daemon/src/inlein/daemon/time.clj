(ns inlein.daemon.time
  (:import (java.util TimeZone Date)
           (java.text SimpleDateFormat)))

(def ^:private iso-8601-formatter
  (doto (SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    (.setTimeZone (TimeZone/getTimeZone "UTC"))))

(defn now-string
  []
  (locking iso-8601-formatter
    (.. iso-8601-formatter (format (Date.)) toString)))
