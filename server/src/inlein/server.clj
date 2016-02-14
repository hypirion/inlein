(ns inlein.server
  (import (java.net ServerSocket)
          (java.io BufferedInputStream)
          (com.hypirion.bencode BencodeReader BencodeWriter))
  (:gen-class))

(defn- untransform
  [val]
  (condp instance? val
    java.util.List (mapv untransform val)
    java.util.Map (into {}
                        (for [[k v] val]
                          [(keyword k) (untransform v)]))
    val))

(defn bencode-read [^BencodeReader in]
  (untransform (.read in)))

(defn- transform
  [val]
  (condp instance? val
    java.util.List (mapv transform val)
    java.util.Map (into {}
                        (for [[k v] val]
                          (let [k' (if (keyword? k)
                                     (name k)
                                     (str k))]
                            [k' (transform v)])))
    val))

(defn- bencode-write [^BencodeWriter out val]
  (.write out (transform val)))

(defmulti handle-request (fn [operation in out] (:op operation)))

(defmethod handle-request "ping" [op in out]
  (bencode-write out {:ret "PONG"}))

(defmethod handle-request :default [operation in out]
  (println "unknown op!")
  (bencode-write out {:ret "unknown operation"}))

(defn- do-request [client-sock]
  (with-open [client-sock client-sock
              out (BencodeWriter. (.getOutputStream client-sock))
              in (-> (.getInputStream client-sock)
                     (BufferedInputStream.)
                     (BencodeReader.))]
    (when-not (.. client-sock getInetAddress isSiteLocalAddress)
      (let [first-op (bencode-read in)]
        (println "first op:" first-op)
        (handle-request first-op in out)))))

(defn -main
  [& args]
  (let [server-socket (ServerSocket. 0)]
    (println "listening on" (.getLocalPort server-socket))
    (loop []
      (let [client-sock (.accept server-socket)]
        (.start (Thread. #(do-request client-sock) "handle-request")))
      (recur))))
