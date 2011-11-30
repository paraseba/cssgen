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
  "Parse a rule returning a map of :subrules, :props and :selector.
  Subrules are parsed recursively. Does mixin expansion. A mixin is
  a seq, subrules are vectors, other elements are interpreted as
  properties pairs. Example

  (parse-rule [:body
                 :color :white
                 '(font-size 13px)
                 [:wrapper
                   :background-color :gray]])"
  [rule]
  (let [is-mixin? seq?
        mixin-content identity
        expand-mixins
          (fn ;"Given a rule, flattens all included mixins. A mixin is a seq"
            [rule]
            (->> (seq rule) (tree-seq is-mixin? mixin-content)
              rest (filter (complement seq?))))

        is-rule?
          (fn ;"Determines if a rule item is a sub-rule
            [previous-props item]
            (and (vector? item) ;rules are vectors
                 (even? (count previous-props)))) ; in even positions

        parse-item
          (fn ;"Parses a rule item, deciding if it's part of a property or a subrule
            [{:keys [props] :as rule} item]
            (if (is-rule? props item)
              (update-in rule [:subrules] conj (parse-rule item))
              (update-in rule [:props] conj item)))]

    (-> (reduce parse-item
                {:subrules [] :props [] :selector (first rule)}
                (rest (expand-mixins rule)))
      (update-in [:props] (partial partition-all 2)))))

(def ^{:doc "Joins a sequence inserting newlines between elements"}
  in-lines (partial join "\n"))

(defn indent
  "Indent the string s by 2n spaces"
  [n s]
  (let [spacer (apply str (repeat (* 2 n) \ ))]
    (->> s
      split-lines
      (map #(str spacer %))
      in-lines)))

(defn compile-properties
  "Given a seq of seq pairs, generate CSS code.
  Example:
    (compile-properties '((:color :blue) (:width \"900px\")))"
  [props]
  (->> props
    (map (fn [[name value]]
           (format "%s: %s;" (to-css name) (to-css value))))
    in-lines))

(defn selector-string
  "Generates the selector resulting of joining the parent and child
  selectors. Make & substitution and coma expansion. parent-selector
  and selector must be rule items (to-css will be called on them)."
  [parent-selector selector]
  (let [nest-selector (fn [parent child]
                        (if (.contains child "&")
                          (.replace child "&" parent)
                          (trim (join " " [parent child]))))

        parents (split (to-css parent-selector) #",")
        children (split (to-css selector)  #",")]

    (join ", "
          (for [p parents c children]
            (nest-selector (trim p) (trim c))))))

(defn compile-rule
  "Generate CSS string for a rule and its sub-rules"
  ([rule parent-selector tabs]
   (let [{:keys [subrules props selector]} rule
         full-selector (selector-string parent-selector selector)
         subrules (map #(compile-rule % full-selector 1) subrules)
         props-css (indent 1 (compile-properties props))
         subrules-css (when (seq subrules) (str "\n" (in-lines subrules)))
         css (str full-selector " {\n"
                  props-css
                  "\n}"
                  subrules-css)]
     (indent tabs css)))
  ([rule] (compile-rule rule nil 0)))

(defn css
  "Generate CSS string for the rules and their sub-rules"
  [& rules]
  (->> rules
    (map (comp compile-rule parse-rule))
    in-lines))

