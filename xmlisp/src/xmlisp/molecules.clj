(ns xmlisp.molecules
  (:require [clojure.core.match :refer [match]]))

(defn matches [s item]
  (if (or (keyword? s) (char? s))
    (= s item)
    (s item)))

(defn to-molecule [c]
  (condp #(= %1 %2) c
    \< :start
    \/ :slash
    \> :end
    \" :quote
    \= :eq
    \space :space
    [:t* c]))

(def moleculize (partial mapv to-molecule))

(def atomic? (comp not :multi meta))

(defn multi [& args]
  (with-meta (vec args)
    {:multi true}))

(def reaction [a b]
  (match [a b]
         ;; Words
         [[:t* s1] [:t* s2]] [:t* (str s1 s2)]
         [[:t* s] a] (multi [:word s] a)
         [a [:t* s]] (multi a [:word s])
         [[:t* s1] [:word s2]] [:word (str s1 s2)]
         [[:word s1] [:t* s2]] [:word (str s1 s2)]

         ;; Overall structure
         [:slash :end] :self-closing-end
         [:start :slash] :closing-start

         ;; Tags
         [:start [:word s]] [:opening-tag* s]
         [[:opening-tag* & stuff] :end] (into [:opening-tag] stuff)
         [[:opening-tag* & stuff] :self-closing-end] (into [:opening-tag] stuff)

         ;; Attributes
         [:eq :quote] (multi :eq [:text*])
         [[:text* & s1] [:t* s2]] [:text* (str (first s1) s2)]
         [[:text* & s] :space] [:text* (str (first s) " ")]
         [[:text* & s] :quote] [:text (str (first s))]

         ;; Space removal
         [a :space] a
         [:space a] a

         :else nil))

(defn react [a b]
  (when-some [result (reaction a b)]
    (cond-> result
      (atomic? result) [result])))

(defn react-step [l]
  (let [[before [a b & after]] (split-at (-> l count dec rand-int) l)
        result (react a b)]
    (concat before
            (if result
              result
              [a b])
            after)))
