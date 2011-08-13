(ns cssgen
  (:use [clojure.string :only [join split]]
        [clojure.core.incubator :only [seqable?]]))

(defprotocol CssValue
  (to-css [this]))

(extend-protocol CssValue
  Object
  (to-css [this] (str this))
  clojure.lang.Keyword
  (to-css [this] (name this))
  clojure.lang.Ratio
  (to-css [this] (to-css (double this)))
  clojure.lang.PersistentVector
  (to-css [this] (join " " (map to-css this)))
  clojure.lang.PersistentList
  (to-css [this] (join " " (map to-css this))))

(load "types")

(defn indent [n s]
  (str (join (repeat (* 2 n) \ )) s))

(defn compile-properties [spaces props]
  (join "\n"
        (map (fn [[name value]]
               (indent spaces
                       (str (to-css name)
                            ": "
                            (to-css value)
                            ";")))
             (partition 2 props))))

(defn selector [rule]
  {:pre [(seqable? rule)]}
  (to-css (first rule)))

(defn selector-string [parent-selectors rule]
  (cond
    (and (seq parent-selectors) (re-matches #".*&.*" (selector rule)))
    (join " " (concat (butlast parent-selectors)
                      [(clojure.string/replace (selector rule)
                                               "&"
                                               (last parent-selectors))]))
    (seq parent-selectors) (join " " (concat parent-selectors [(selector rule)]))
    :else (selector rule)))

(defn flatten-rule [rule]
  (filter (complement seq?)
          (rest (tree-seq seq? identity (seq rule)))))

(defn split-rule [rule]
  {:post [(map? %) (= 2 (count %)) (contains? % :rules) (contains? % :props)]}
  (reduce
    (fn [{:keys [props] :as res} item]
      (cond
        (and (vector? item) (even? (count props))) (update-in res [:rules] conj item)
        (seq? item) (merge-with conj res (split-rule item))
        :else (update-in res [:props] conj item)))
    {:rules [] :props []}
    (rest (flatten-rule rule))))

(declare compile-subrules)
(defn compile-rule
  ([parent-selectors rule]
   {:pre [(seqable? rule)] :post [(string? %)]}
   (let [{subrules :rules properties :props} (split-rule rule)
         tabs (count parent-selectors)
         subrules (compile-subrules parent-selectors rule subrules)]
     (str (indent tabs (selector-string parent-selectors rule)) " {\n"
          (compile-properties (inc tabs) properties)
          "\n"
          (indent tabs "}")
          (when (seq subrules) (str "\n" (join "\n" subrules))))))
  ([rule] (compile-rule [] rule)))

(defn compile-subrules [parent-selectors rule subrules]
  {:pre [(seqable? rule)] :post [(seqable? %) (every? string? %)]}
  (let [selectors (split (selector rule) #"\s*,\s*")]
    (for [sel selectors r subrules]
      (compile-rule (conj parent-selectors sel) r))))

(defn css [& rules]
  (join "\n" (map compile-rule rules)))

