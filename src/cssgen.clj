(ns cssgen
  (:use [clojure.string :only [join split split-lines blank? trim]]
        [clojure.core.incubator :only [seqable?]]))

(defprotocol CssValue
  (to-css [this]))

(extend-protocol CssValue
  nil
  (to-css [_] "")
  Object
  (to-css [this] (str this))
  clojure.lang.Keyword
  (to-css [this] (name this))
  clojure.lang.Ratio
  (to-css [this] (to-css (double this)))
  clojure.lang.IPersistentVector
  (to-css [this] (join " " (map to-css this)))
  clojure.lang.IPersistentList
  (to-css [this] (join " " (map to-css this))))

(load "types")

(defn parse-rule
  [rule]
  (letfn [(expand-mixins [rule]
            (filter (complement seq?)
                    (rest (tree-seq seq? identity (seq rule)))))

          (parse-item [{:keys [props] :as rule} item]
            (if (and (vector? item) (even? (count props)))
              (update-in rule [:subrules] conj (parse-rule item))
              (update-in rule [:props] conj item)))]

    (-> (reduce parse-item
                {:subrules [] :props []}
                (rest (expand-mixins rule)))
      (update-in [:props] (partial partition-all 2))
      (assoc :selector (first rule)))))

(defn indent [n s]
  (let [spacer (apply str (repeat (* 2 n) \ ))]
    (->> s
      split-lines
      (map #(str spacer %))
      (join "\n"))))

(defn compile-properties [props]
  (->> props
    (map (fn [[name value]]
           (format "%s: %s;" (to-css name) (to-css value))))
    (join "\n")))

(defn selector-string [parent-selector selector]
  (let [nest-selector (fn [parent child]
                        (if (.contains child "&")
                          (.replace child "&" parent)
                          (str parent
                               (if-not (blank? parent) " ")
                               child)))

        parents (split (to-css parent-selector) #",")
        children (split (to-css selector)  #",")]

    (join ", "
          (for [p parents c children]
            (nest-selector (trim p) (trim c))))))

(defn compile-rule
  "Generate CSS string for a rule"
  ([rule parent-selector tabs]
   (let [{:keys [subrules props selector]} rule
         full-selector (selector-string parent-selector selector)
         subrules (map #(compile-rule % full-selector 1) subrules)]
     (indent tabs
       (str full-selector " {\n"
            (indent 1 (compile-properties props))
            "\n}"
            (when (seq subrules)
              (str "\n" (join "\n" subrules)))))))
  ([rule] (compile-rule rule nil 0)))

(defn css [& rules]
  (join "\n" (map (comp compile-rule parse-rule) rules)))

