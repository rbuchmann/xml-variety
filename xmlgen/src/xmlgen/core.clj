(ns xmlgen.core
  (:require [clojure.spec.alpha     :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.data.xml       :as xml]
            [clojure.string         :as str])
  (:import com.github.javafaker.Faker))

(def f (Faker.))

(s/def ::simple-kw (s/with-gen keyword?
                     #(s/gen #{:font :margin :style :href :padding :position})))

(s/def ::attr-map (s/and (s/map-of ::simple-kw string?)
                         #(< (count %) 3)))

(s/def ::tag (s/with-gen keyword?
               #(s/gen #{:div :p :h1 :h2 :li :ul :ol :span})))

(s/def ::txt-node (s/with-gen string?
                    #(gen/fmap (fn [_] (-> f .lorem .word)) (gen/char-alpha))))

(s/def ::node (s/and (s/cat :tag ::tag
                            :attrs (s/? ::attr-map)
                            :children (s/* (s/or :node ::node
                                                 :txt-node ::txt-node)))
                     #(< (count %) 5)))

(defn vectorize [node]
  (if (sequential? node)
    (mapv vectorize node)
    node))

(def seq-size 1000)

(defn gen-pair []
  (binding [s/*recursion-limit* 2]
    (let [sexp  (-> ::node
                    s/gen
                    gen/generate
                    vectorize)
          xml (-> sexp xml/sexp-as-element xml/emit-str)
          hiccups (binding [*print-length* nil] (pr-str sexp))]
      (when (and (< (count xml) seq-size)
                 (< (count hiccups) seq-size))
        [xml hiccups]))))

(def format-pair (partial str/join \tab))

(defn lazy-spit [col]
  (doseq [item col]
    (spit "foo.pairs" (str item \newline) :append true)))

(defn gen-pairs [n]
  (->> (repeatedly gen-pair)
       (remove nil?)
       (take n)
       (map format-pair)
       lazy-spit))
