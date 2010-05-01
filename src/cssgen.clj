(ns cssgen
  (:require (clojure.contrib [string :as s]
                             [strint :as strint]
                             [io :as io])))

(defmulti add-rule-item #(:tag %2))

(defmethod add-rule-item ::Prop [parent {prop :prop}]
  (merge-with concat parent {:prop prop}))

(defmethod add-rule-item ::Rule [parent rule]
  (merge-with conj parent {:children rule}))

(defmethod add-rule-item ::Mixin [parent {components :components}]
  (reduce add-rule-item parent components))

(defmethod add-rule-item nil [parent _]
  parent)


(defprotocol Value
  (repr [x]))

(defrecord Length [mag unit]
  Value
  (repr [_] (str mag (s/as-str unit))))
           
(defrecord Color [r g b]
  Value
  (repr [_] (str "#" (s/map-str #(Integer/toHexString %) [r g b]))))

(extend-protocol Value
  nil
    (repr [_] "")

  clojure.lang.IPersistentVector
    (repr [v] (s/join " " (map repr v)))

  clojure.lang.Keyword
    (repr [k] (name k))

  java.lang.String
    (repr [s] s)

  Object
    (repr [i] (.toString i)))


(defn- str->color [s]
  (letfn [(extend [s] (.substring (str s s) 0 2))]
    (let [s (if (= (.length s) 3)
              (apply str (interleave s s))
              s)
          components (re-seq #".." s)
          [r g b] (map #(-> % s/as-str extend (Integer/parseInt 16)) components)]

      (Color. r g b))))


(defn- make-value [mag unit]
  (if (= unit "$")
    (str->color mag)
    (Length. mag unit)))


;(defn- symbol->value [sym]
;  (let [[_ unit mag] (s/partition #"em|ex|px|in|cm|mm|pt|pc|%|\$" (name sym))]
;      (make-value mag unit)))


(defn prop [& forms]
  (letfn [(expand-item [item]
            (if (map? item)
              (flatten (:prop item))
              [item]))]
    (let [expanded (mapcat expand-item forms)
          in-pairs (apply vector (partition 2 expanded))]
        {:tag ::Prop :prop in-pairs})))

(defn- create-props [forms]
  (letfn [(is-obj? [x] (or (map? x) (nil? x)))]
    (let [parts (partition-by is-obj? forms)]
      (mapcat #(if (is-obj? (first %))
                 %
                 [(apply prop %)])
              parts))))

(defn rule [selector & forms]
  (reduce add-rule-item
          {:tag ::Rule :selector selector :children nil}
          (create-props forms)))


(defn mixin [& forms]
  {:tag ::Mixin :components (vec (create-props forms))})

(defn rule-css [rule]
  (letfn [(format-prop [prop]
            (let [vals (s/join " " (map repr (next prop)))]
              (strint/<< "  ~(repr (first prop)): ~{vals};")))

          (format-props [props]
            (let [lines (map format-prop props)]
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
                  parent-css (strint/<< "~{nested-selector} {\n~(format-props prop)\n}\n")
                  children (or children components)
                  children-css (s/join "\n" (map #(child-rule-css % nested-selector) children))]
              (case tag
                ::Rule (str parent-css children-css)
                ::Mixin children-css
                (throw (Exception. "Can only render rules or mixins")))))]

    (child-rule-css rule nil)))

(defn css-file [path & rules]
  (io/spit path (s/map-str rule-css rules)))

