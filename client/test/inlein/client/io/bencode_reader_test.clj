(ns inlein.client.io.bencode-reader-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io])
  (:import (inlein.client.io BencodeReader BencodeReadException)
           (java.io ByteArrayInputStream EOFException)))

(defn- bais [str]
  (ByteArrayInputStream. (.getBytes str "UTF-8")))

(defn- bencode-reader [str]
  (BencodeReader. (bais str)))

(defn- bencode-vals
  "Returns a lazy sequence of all the values in the bencoded string"
  [str]
  (let [rdr (bencode-reader str)]
    (take-while #(not (nil? %))
                (repeatedly #(.read rdr)))))

(defn- bencode-val
  "Returns the first bencode value in the string, or nil if none were found"
  [str]
  (first (bencode-vals str)))

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

(deftest read-strings
  (let [read-str (fn [s] (.readString (bencode-reader s)))]
    (testing "correctly encoded bencode strings"
      (are [x y] (= x (read-str y))
        ""       "0:"
        ":"      "1::"
        "ie"     "2:ie"
        "foo"    "3:foo"
        "3:foo"  "5:3:foo"
        "plaît"  "6:plaît"
        "banana" "6:banana"))
    (testing "incorrectly encoded bencode strings"
      (are [val re] (thrown-with-msg? BencodeReadException re
                                      (read-str val))
        ":fofoo" #"must contain at least one digit"
        "-501AS" #"when reading bencode-length"
        "50-"    #"when reading bencode-length"
        "hootoo" #"when reading bencode-length")
      (are [val] (thrown? EOFException (read-str val))
        "1:"
        "500:foo"
        "20:200"
        "5:four"))))

(deftest read-list
  (let [read-lst (fn [s] (.readList (bencode-reader s)))]
    (testing "correctly encoded bencode lists"
      (are [x y] (= x (read-lst y))
        [] "le"
        [1] "li1ee"
        [1 2 3] "li1ei2ei3ee"
        [2 "foo"] "li2e3:fooe"
        [[]] "llee"
        [[] [1 2]] "lleli1ei2eee"
        [["foo"] "bar" ["baz"]] "ll3:fooe3:barl3:bazee"))))

(deftest read-dict
  (let [read-dict (fn [s] (.readDict (bencode-reader s)))]
    (testing "correctly bencoded dicts"
      (are [x y]  (= x (read-dict y))
          {} "de"
          {"foo" "bar"} "d3:foo3:bare"
          {"foo" [{}]} "d3:fooldeee"
          {"x" {"y" {"z" 1}}} "d1:xd1:yd1:zi1eeee"))))

(deftest read-arbitrary
  (testing "that we read correct bencode type with .read"
    (are [x y] (= x (bencode-val y))
      ""         "0:"
      "plaît"    "6:plaît"
      10         "i10e"
      0          "i0e"
      -100       "i-100e"
      [1]        "li1ee"
      [[] [1 2]] "lleli1ei2eee"
      {"foo" 1}  "d3:fooi1ee"
      {"foo" [{}]} "d3:fooldeee"
      {"x" {"y" {"z" 1}}} "d1:xd1:yd1:zi1eeee")))

(deftest read-multiple
  (testing "that a stream of bencoded values can be read"
    (are [x y] (= x (bencode-vals y))
      [] ""
      [[]] "le"
      [{} [] 0 "hello world!"] "delei0e12:hello world!")))
