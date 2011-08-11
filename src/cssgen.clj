(ns cssgen
  (:use [clojure.string :only [join]]
        [clojure.core.incubator :only [seqable?]]))
  ;(:require (clojure.contrib [string :as s]
                             ;[strint :as strint]
                             ;[io :as io]
                             ;[def :as ccdef]))
  ;(:use [cssgen.types :only (repr)]))

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

(defn indent [n s]
  (str (join (repeat (* 2 n) \ )) s))

(defn compile-properties [spaces props]
  (join "\n"
        (map (fn [[name value]]
               (indent spaces
                       (str (to-css name)
                            " "
                            (to-css value)
                            ";")))
             (partition 2 props))))

(defn selector [rule]
  {:pre [(seqable? rule)]}
  (to-css (first rule)))

(defn selector-string [parent-rules rule]
  (cond
    (and (seq parent-rules) (re-matches #".*&.*" (selector rule)))
    (join " " (concat (butlast (map selector parent-rules))
                      [(clojure.string/replace (selector rule)
                                               "&"
                                               (selector (last parent-rules)))]))
    (seq parent-rules) (join " " (concat (map selector parent-rules) [(selector rule)]))
    :else (selector rule)))

(defn compile-rule
  ([parent-rules rule]
   {:pre [(seqable? rule)]
    :post [(string? %)]}
   (let [subrules (filter vector? rule)
         properties (remove vector? (rest rule))
         tabs (count parent-rules)]
     (str (indent tabs (selector-string parent-rules rule)) " {\n"
          (compile-properties (inc tabs) properties)
          "\n"
          (indent tabs "}")
          (when (seq subrules)
            (str "\n"
                 (join "\n"
                       (map (partial compile-rule (conj parent-rules rule))
                            subrules)))))))
  ([rule] (compile-rule [] rule)))

(defn css [& rules]
  (join "\n" (map compile-rule rules)))

;(defprotocol Container
  ;(nest [child parent])
  ;(add-properties [this new-props])
  ;(add-rules [this new-rules]))

;(defrecord Mixin [properties rules]
  ;Container
  ;(add-properties [_ new-props] (Mixin. (concat properties new-props) rules))
  ;(add-rules [_ new-rules] (Mixin. properties (concat rules new-rules)))
  ;(nest [this parent]
    ;(-> parent
      ;(add-properties properties)
      ;(add-rules rules))))

;(defrecord Rule [selector properties rules]
  ;Container
  ;(add-properties [_ new-props] (Rule. selector (concat properties new-props) rules))
  ;(add-rules [_ new-rules] (Rule. selector properties (concat rules new-rules)))
  ;(nest [this parent]
    ;(add-rules parent [this])))

;(defn- properties [x] (:properties x))
;(defn- rules [x] (:rules x))
;(defn- selector [x] (:selector x))
;(ccdef/defvar- empty-mixin (Mixin. [] []))
;(defn- empty-rule [selector] (Rule. selector [] []))

;(defn- container? [x]
  ;(or (instance? Mixin x) (instance? Rule x)))

;(defn- parse-and-nest [base forms]
  ;(letfn [(process-group [base group]
             ;(if (container? (first group))
               ;(reduce #(nest %2 %1) base group)
               ;(add-properties base (partition 2 group))))]
    ;(let [grouped (partition-by container? (filter (complement nil?) forms))]
      ;(reduce process-group base grouped))))

;(defn rule [selector & forms]
  ;(parse-and-nest (empty-rule selector) forms))

;(defn mixin [& forms]
  ;(parse-and-nest empty-mixin forms))

;(defn- rule-css [rule]
  ;(letfn [(format-prop [prop]
            ;(let [vals (s/join " " (map repr (next prop)))]
              ;(strint/<< "  ~(repr (first prop)): ~{vals};")))

          ;(format-props [props]
            ;(let [lines (map format-prop props)]
              ;(s/join "\n" lines)))

          ;(nest-single-selector [parent child]
            ;(if (s/substring? "&" child)
              ;(s/replace-str "&" parent child)
              ;(str parent (if-not (s/blank? parent) " ") child)))

          ;(nest-selector [parent child]
            ;(let [parents (s/split #"," (or parent ""))
                  ;children (s/split #"," (or child ""))]
              ;(s/join ", " (for [p parents c children]
                             ;(nest-single-selector (s/trim p) (s/trim c))))))

          ;(child-rule-css [rule parent-selector]
            ;(let [selector (selector rule)
                  ;properties (properties rule)
                  ;children (rules rule)
                  ;nested-selector (nest-selector parent-selector selector)
                  ;parent-css (strint/<< "~{nested-selector} {\n~(format-props properties)\n}\n")
                  ;children-css (s/join "\n" (map #(child-rule-css % nested-selector) children))]
              ;(str parent-css children-css)))]

    ;(child-rule-css rule nil)))

;(defn css [& rules]
  ;(s/map-str rule-css rules))

;(defn css-file [path & rules]
  ;(io/spit path (apply css rules)))

