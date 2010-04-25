(ns cssgen
  (:require (clojure.contrib [string :as s]
                             [strint :as strint]
                             [seq :as sq]
                             [io :as io])))

(defn prop [& forms]
  (letfn [(expand-item [item]
            (if (map? item)
              (sq/flatten (:prop item))
              [item]))]
    (let [expanded (mapcat expand-item forms)]
      {:prop (apply vector (partition 2 expanded))})))

(defmulti add-rule-item #(:selector %2))

(defmethod add-rule-item nil
  [parent new-prop]
  (merge-with concat parent new-prop))

(defmethod add-rule-item :default
  [parent rule]
  (merge-with conj parent {:children rule}))

(defn rule [selector & forms]
  (reduce add-rule-item {:selector selector, :children nil} forms))


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
                  children (s/split #"," child)]
              (s/join ", " (for [p parents c children]
                             (nest-single-selector (s/trim p) (s/trim c))))))

          (child-rule-css [rule parent-selector]
            (let [selector (:selector rule)
                  children (:children rule)
                  nested-selector (nest-selector parent-selector selector)
                  parent-css (strint/<<
"~{nested-selector} {
~(format-prop (:prop rule))
}
")
                  children-css (s/join "\n" (map #(child-rule-css % nested-selector) children))]
              (str parent-css children-css)))]
                 

    (child-rule-css rule nil)))

(defn css-file [path & rules]
  (io/spit path (s/map-str rule-css rules)))

