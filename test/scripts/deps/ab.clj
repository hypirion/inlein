'{:file-deps #{"a.clj" "b.clj"}
  :dependencies [[com.gfredericks/seventy-one "0.1.1"]]}

(require '[com.gfredericks.seventy-one :refer [seventy-one]])

(def ab (str a b seventy-one))
