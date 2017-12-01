(ns xmlisp.instaparse
  (:require [instaparse.core :as insta]
            [clojure.string  :as str]))

(insta/defparser whitespace "whitespace = #'\\s+'")

(insta/defparser xml
  "
doc = node+
identifier = #'[a-zA-Z][a-zA-Z0-9]*'
text = #'[^<>\"]*'
attr = identifier <'='> <'\"'> text <'\"'>
attr-list = attr*
opening-tag = <'<'> identifier attr-list <'>'>
closing-tag = <'</'> identifier <'>'>
self-closing-tag = <'<'> identifier attr-list <'/>'>
node = self-closing-tag | (opening-tag (node | text)* closing-tag)
" :auto-whitespace whitespace)

(defn to-map [& args]
  (when-not (empty? args)
    (into {} args)))

(defn format-tag [tag & [attrs]]
  (cond-> [(keyword tag)]
    attrs (conj attrs)))

(defn die [msg]
  (throw (IllegalArgumentException. msg)))

(def transforms
  {:identifier identity
   :text str/trim
   :attr (fn [k v] [(keyword k) v])
   :attr-list to-map
   :self-closing-tag format-tag
   :opening-tag format-tag
   :closing-tag format-tag
   :node (fn [[tag1 :as opening] & rst]
           (let [children (butlast rst)
                 [tag2 :as closing] (last rst)]
             (if (= tag1 tag2)
               (into opening children)
               (die (format "tag mismatch: expected %s, got %s" tag1 tag2)))))})

(def parse (comp (partial insta/transform transforms) xml))
