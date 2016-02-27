(ns inlein.daemon.server
  (:require [com.stuartsierra.component :as component]
            [clojure.java.io :as io])
  (:import (java.net ServerSocket SocketTimeoutException)
           (java.io BufferedInputStream)
           (com.hypirion.bencode BencodeReader BencodeWriter)))

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

(defn- write-ack [^BencodeWriter out msg]
  (bencode-write out {:type "ack" :op (:op msg)}))

(defn- write-response [^BencodeWriter out op resp]
  (bencode-write out
                 (merge {:type "response"
                         :returns (:op op)}
                        resp)))

(defn- warn [^BencodeWriter out ^String msg]
  (bencode-write out {:type "log"
                      :level "warn"
                      :timestamp "zero"
                      :msg msg}))

(defmulti handle-request (fn [operation in out] (:op operation)))

(defmethod handle-request "ping" [op in out]
  (write-response out op {:msg "PONG"}))

(defmethod handle-request "shutdown" [op in out]
  (write-response out op {:msg "ok, shutting down"})
  ::shutdown)

(defmethod handle-request :default [op in out]
  (write-response out op
                  {:error (str "Inlein server is not familiar with the operation "
                               (:op op))}))

(defn- do-request [system-atom client-sock]
  (with-open [client-sock client-sock
              out (BencodeWriter. (.getOutputStream client-sock))
              in (-> (.getInputStream client-sock)
                     (BufferedInputStream.)
                     (BencodeReader.))]
    (when-not (.. client-sock getInetAddress isSiteLocalAddress)
      (let [first-op (bencode-read in)]
        (println "first op:" first-op)
        (write-ack out first-op)
        (let [res (handle-request first-op in out)]
          (println res)
          (case res
            ::shutdown (.start (Thread. #(swap! system-atom component/stop)))
            nil))))))

(defn run-server [system-atom server-socket]
  (try
    (loop []
      (when (.isInterrupted (Thread/currentThread))
        (throw (InterruptedException.)))
      (try
        (let [client-sock (.accept server-socket)]
          (.start (Thread. #(do-request system-atom client-sock) "handle-request")))
        (catch SocketTimeoutException _))
      (recur))
    (catch InterruptedException _
      (println "Server interrupted"))))

(defrecord InleinServer [cfg system-atom socket socket-thread]
  component/Lifecycle
  (start [this]
    (if socket-thread
      this
      (let [sock (doto (ServerSocket. (:port cfg))
                   (.setSoTimeout 100))
            thread (doto (Thread. #(run-server system-atom sock))
                     (.start))]
        (.mkdirs (io/file (:inlein-home cfg)))
        (println "server listening on" (.getLocalPort sock))
        (spit (io/file (:inlein-home cfg) "port")
              (str (.getLocalPort sock)))
        (.deleteOnExit (io/file (:inlein-home cfg) "port"))
        (assoc this
               :socket sock
               :socket-thread thread))))
  (stop [this]
    (when socket-thread
      (.interrupt (:socket-thread this))
      (.join (:socket-thread this))
      (io/delete-file (io/file (:inlein-home cfg) "port") :silently))
    (assoc this :socket nil :socket-thread nil)))

(defn inlein-server [config system-atom]
  (->InleinServer config system-atom nil nil))
