(ns cssgen
  (:use [clojure.string :only [join split split-lines blank? trim]]))

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

(defn- is-rule? [r]
  (and (map? r)
       (every? r [:selector :subrules :props])))

(defn- parse-rule
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
  {:pre  [(vector? rule) (seq rule)]
   :post [(is-rule? %)]}
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
            (cond
              (nil? item) rule
              (is-rule? props item)
                (update-in rule [:subrules] conj (parse-rule item))
              :else (update-in rule [:props] conj item)))]

    (-> (reduce parse-item
                {:subrules [] :props [] :selector (first rule)}
                (rest (expand-mixins rule)))
      (update-in [:props] (partial partition-all 2)))))

(def ^{:doc "Joins a sequence inserting newlines between elements" :private true}
  in-lines (partial join "\n"))

(defn- indent
  "Indent the string s by 2n spaces"
  [n s]
  {:pre [(number? n) (string? s)]}
  (let [spacer (apply str (repeat (* 2 n) \ ))]
    (->> s
      split-lines
      (map #(str spacer %))
      in-lines)))

(defn- compile-properties
  "Given a seq of seq pairs, generate CSS code.
  Example:
    (compile-properties '((:color :blue) (:width \"900px\")))"
  [props]
  {:pre [(sequential? props) (every? sequential? props)]
   :post [(string? %)]}
  (->> props
    (map (fn [[name value]]
           (format "%s: %s;" (to-css name) (to-css value))))
    in-lines))

(defn- selector-string
  "Generates the selector resulting of joining the parent and child
  selectors. Make & substitution and coma expansion. parent-selector
  and selector must be rule items (to-css will be called on them)."
  [parent-selector selector]
  {:pre [(not (nil? selector))]}
  (let [nest-selector (fn [parent child]
                        (if (.contains child "&")
                          (.replace child "&" parent)
                          (trim (join " " [parent child]))))

        parents (split (to-css parent-selector) #",")
        children (split (to-css selector)  #",")]

    (join ", "
          (for [p parents c children]
            (nest-selector (trim p) (trim c))))))

(defn- format-rule
  "Generate rule css given all the parts as strings"
  [selector props subrules tabs]
  {:pre [(every? string? [selector props subrules]) (number? tabs)]}
  (indent tabs
          (str selector " {\n" props "\n}" subrules)))

(defn- compile-rule
  "Generate CSS string for a rule and its sub-rules"
  ([rule parent-selector tabs]
   {:pre  [(is-rule? rule) (>= tabs 0)]
    :post [(string? %)]}
   (let [{:keys [subrules props selector]} rule
         full-selector (selector-string parent-selector selector)
         subrules (map #(compile-rule % full-selector 1) subrules)
         props-css (indent 1 (compile-properties props))
         subrules-css (if (seq subrules) (str "\n" (in-lines subrules)) "")]
     (format-rule full-selector props-css subrules-css tabs)))
  ([rule] (compile-rule rule nil 0)))

; -- let the hacking begin --

; this approach doesn't work well
;
;(def arith-functions [+ - * /])
;(def meta+ (meta #'+))
;(def meta- (meta #'-))
;(def meta* (meta #'*))
;(def meta-div (meta #'/))
;(def inline-keys [:inline :inline-arities])

;(defn with-css-arithmetic* [f]
  ;(with-redefs [+ generic/+ - generic/- * generic/* / (generic/qsym generic /)]
    ;(f)))

;(defmacro removing-inline [& body]
  ;`(do
     ;(apply alter-meta! #'+ dissoc inline-keys)
     ;(apply alter-meta! #'- dissoc inline-keys)
     ;(apply alter-meta! #'* dissoc inline-keys)
     ;(apply alter-meta! #'/ dissoc inline-keys)
     ;(try
       ;(with-css-arithmetic* (fn [] ~@body))
       ;(finally
         ;(alter-meta! #'+ merge meta+)
         ;(alter-meta! #'- merge meta-)
         ;(alter-meta! #'* merge meta*)
         ;(alter-meta! #'/ merge meta-div)))))

;(defmacro with-css-arithmetic [& body]
  ;`(removing-inline ~@body))

; -- let the hacking end -

(defn css
  "Generate CSS string for the rules and their sub-rules"
  [& rules]
  (->> rules
    (map (comp compile-rule parse-rule))
    in-lines))

