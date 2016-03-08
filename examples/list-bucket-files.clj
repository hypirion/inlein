#!/usr/bin/env inlein

'{:dependencies [[org.clojure/clojure "1.8.0"]
                 [amazonica "0.3.51" :exclusions [com.amazonaws/aws-java-sdk]]
                 [com.amazonaws/aws-java-sdk-core "1.10.49"]
                 [com.amazonaws/aws-java-sdk-s3 "1.10.49"]]}

(require '[amazonica.aws.s3 :as s3])

(def bucket-name (first *command-line-args*))

(when-not bucket-name
  (println "Usage:" (System/getProperty "$0") "s3-bucket")
  (System/exit 1))

(doseq [{:keys [key etag size]} (:object-summaries
                                 (s3/list-objects :bucket-name bucket-name))
        :when (pos? size)]
  (println key "--" etag))
