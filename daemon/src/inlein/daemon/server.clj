(ns inlein.daemon.server
  (:require [com.stuartsierra.component :as component]
            [clojure.java.io :as io]
            [inlein.daemon.read-script :as rs]
            [inlein.daemon.dependencies :as deps]
            [inlein.daemon.time :as t])
  (:import (java.net ServerSocket SocketTimeoutException InetAddress)
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
                      :timestamp (t/now-string)
                      :msg msg}))

(defn- info [^BencodeWriter out ^String msg]
  (bencode-write out {:type "log"
                      :level "info"
                      :timestamp (t/now-string)
                      :msg msg}))

(defmulti handle-request (fn [operation in out] (:op operation)))

(defmethod handle-request "ping" [op in out]
  (write-response out op {:msg "PONG"}))

(defmethod handle-request "shutdown" [op in out]
  (write-response out op {:msg "ok, shutting down"})
  ::shutdown)

(defmethod handle-request "jvm-opts" [op in out]
  (let [log-fn (deps/transfer-logger (fn [s]
                                       (info out s)
                                       (println s)))]
    (write-response out op (rs/read-script-params (:file op)
                                                  {:transfer-listener log-fn}))))

(defmethod handle-request :default [op in out]
  (write-response out op
                  {:error (str "Inlein server is not familiar with the operation "
                               (:op op))}))

(defn handle-request-errors [op in out]
  (try (handle-request op in out)
       (catch clojure.lang.ExceptionInfo ei
         (write-response out op
                         (ex-data ei)))
       (catch Exception e
         (write-response out op
                         {:error (str "Unhandled error -- " (.getMessage e))})
         (.printStackTrace e))))

(defn- do-request [system-atom client-sock]
  (with-open [client-sock client-sock
              out (BencodeWriter. (.getOutputStream client-sock))
              in (-> (.getInputStream client-sock)
                     (BufferedInputStream.)
                     (BencodeReader.))]
    (loop [op (bencode-read in)]
      (if-not op
        nil
        (do
          (println "op request:" op)
          (write-ack out op)
          (let [res (handle-request-errors op in out)]
            (println res)
            (case res
              ::shutdown (.start (Thread. #(swap! system-atom component/stop)))
              (recur (bencode-read in)))))))))

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
      (let [sock (doto (ServerSocket. (:port cfg) 0 (InetAddress/getLoopbackAddress))
                       (.setSoTimeout 100))
            thread (doto (Thread. #(run-server system-atom sock))
                     (.start))]
        (.mkdirs (io/file (:inlein-home cfg)))
        (spit (io/file (:inlein-home cfg) "port")
              (str (.getLocalPort sock)))
        (.deleteOnExit (io/file (:inlein-home cfg) "port"))
        (println "Inlein server listening on" (.getLocalPort sock))
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
