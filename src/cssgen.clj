(ns cssgen
  (:require (clojure.contrib [string :as s]
                             [strint :as strint]
                             [seq :as sq]
                             [io :as io])))

(defmulti add-rule-item #(:tag %2))

(defmethod add-rule-item ::Prop [parent {prop :prop}]
  (merge-with concat parent {:prop prop}))

(defmethod add-rule-item ::Rule [parent rule]
  (merge-with conj parent {:children rule}))

(defmethod add-rule-item ::Mixin [parent {components :components}]
  (reduce add-rule-item parent components))

(defn prop [& forms]
  (letfn [(expand-item [item]
            (if (map? item)
              (sq/flatten (:prop item))
              [item]))]
    (let [expanded (mapcat expand-item forms)]
      {:tag ::Prop :prop (apply vector (partition 2 expanded))})))

(defn rule [selector & forms]
  (reduce add-rule-item {:tag ::Rule :selector selector :children nil} forms))

(defn mixin [& forms]
  (let [filtered (filter (complement nil?) forms)]
    {:tag ::Mixin :components (vec filtered)}))

(defn rule-css [rule]
  (letfn [(format-prop [props]
            (let [lines (map (fn [[k,v]] (strint/<< "  ~(s/as-str k): ~(s/as-str v);"))
                             props)]
              (s/join "\n" lines)))

          (nest-single-selector [parent child]
            (if (s/substring? "&" child)
              (s/replace-str "&" parent child)
              (str parent (if-not (s/blank? parent) " ") child)))

          (nest-selector [parent child]
            (let [parents (s/split #"," (or parent ""))
                  children (s/split #"," (or child ""))]
              (s/join ", " (for [p parents c children]
                             (nest-single-selector (s/trim p) (s/trim c))))))

          (child-rule-css [{:keys (tag selector children components prop)} parent-selector]
            (let [nested-selector (nest-selector parent-selector selector)
                  parent-css (strint/<< "~{nested-selector} {\n~(format-prop prop)\n}\n")
                  children (or children components)
                  children-css (s/join "\n" (map #(child-rule-css % nested-selector) children))]
              (case tag
                ::Rule (str parent-css children-css)
                ::Mixin children-css
                (throw (Exception. "Can only render rules or mixins")))))]

    (child-rule-css rule nil)))

(defn css-file [path & rules]
  (io/spit path (s/map-str rule-css rules)))

