(ns inlein.client.io.bencode-reader-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io])
  (:import (inlein.client.io BencodeReader BencodeReadException)
           (java.io ByteArrayInputStream EOFException)))

(defn- bais [^String str]
  (ByteArrayInputStream. (.getBytes str "UTF-8")))

(defn- bencode-reader [^String str]
  (BencodeReader. (bais str)))


(deftest read-integers
  (let [read-long (fn [s] (.readLong (bencode-reader s)))]
    (testing "correctly encoded bencode integers"
      (are [x y] (== x (read-long y))
        3      "i3e"
        3      "i3ei3e"
        0      "i0e"
        0      "i-0e"
        -10    "i-10e"
        1234   "i1234e"
        129348 "i129348e"))
    (testing "incorrectly encoded bencode integers"
      (are [val re] (thrown-with-msg? BencodeReadException re
                                      (read-long val))
        "foo"     #"must start with 'i'"
        "ie"      #"must contain at least one digit"
        "i-e"     #"must contain at least one digit"
        "i--010e" #"Unexpected character '-'"
        "i1000fe" #"Unexpected character 'f'"))
    (testing "eof of bencoded longs"
      (is (thrown? EOFException (read-long "")))
      (is (thrown? EOFException (read-long "i100")))
      (is (thrown? EOFException (read-long "i-12131233231"))))))
