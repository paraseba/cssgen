(ns cssgen
  (:require [clojure.string :as s]
            [clojure.java.io :as io])
  (:use [cssgen.types :only (repr)]))

(defprotocol Container
  (nest [child parent])
  (add-properties [this new-props])
  (add-rules [this new-rules]))

(defrecord Mixin [properties rules]
  Container
  (add-properties [_ new-props] (Mixin. (concat properties new-props) rules))
  (add-rules [_ new-rules] (Mixin. properties (concat rules new-rules)))
  (nest [this parent]
    (-> parent
      (add-properties properties)
      (add-rules rules))))

(defrecord Rule [selector properties rules]
  Container
  (add-properties [_ new-props] (Rule. selector (concat properties new-props) rules))
  (add-rules [_ new-rules] (Rule. selector properties (concat rules new-rules)))
  (nest [this parent]
    (add-rules parent [this])))

(defn- properties [x] (:properties x))
(defn- rules [x] (:rules x))
(defn- selector [x] (:selector x))
(def ^{:private true} empty-mixin (Mixin. [] []))
(defn- empty-rule [selector] (Rule. selector [] []))

(defn- container? [x]
  (or (instance? Mixin x) (instance? Rule x)))

(defn- parse-and-nest [base forms]
  (letfn [(process-group [base group]
             (if (container? (first group))
               (reduce #(nest %2 %1) base group)
               (add-properties base (partition 2 group))))]
    (let [grouped (partition-by container? (filter (complement nil?) forms))]
      (reduce process-group base grouped))))

(defn rule [selector & forms]
  (parse-and-nest (empty-rule selector) forms))

(defn mixin [& forms]
  (parse-and-nest empty-mixin forms))

(defn- rule-css [rule]
  (letfn [(format-prop [prop]
            (let [key (repr (first prop))
                  vals (s/join " " (map repr (next prop)))]
              (format "  %s: %s;" key vals)))

          (format-props [props]
            (let [lines (map format-prop props)]
              (s/join "\n" lines)))

          (nest-single-selector [parent child]
            (if (.contains child "&")
              (.replace child "&" parent)
              (str parent (if-not (s/blank? parent) " ") child)))

          (nest-selector [parent child]
            (let [parents (s/split (or parent "") #",")
                  children (s/split (or child "") #",")]
              (s/join ", " (for [p parents c children]
                             (nest-single-selector (s/trim p) (s/trim c))))))

          (child-rule-css [rule parent-selector]
            (let [selector (selector rule)
                  properties (properties rule)
                  children (rules rule)
                  nested-selector (nest-selector parent-selector selector)
                  parent-css (format "%s {\n%s\n}\n"
                                     nested-selector (format-props properties))
                  children-css (s/join "\n" (for [c children]
                                              (child-rule-css c nested-selector)))]
              (str parent-css children-css)))]

    (child-rule-css rule nil)))

(defn css [& rules]
  (apply str (map rule-css rules)))

(defn css-file [path & rules]
  (spit path (apply css rules)))

(defmacro rule-seq [& rules]
  (let [name 'rule-sequence]
    `(defonce ~name [~@rules])))

