(ns cssgen
  (:require (clojure.contrib [string :as s]
                             [strint :as strint]
                             [io :as io]))
  (:require [clojure.contrib.generic.arithmetic :as generic]))

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
  (repr [_] (str (s/as-str mag) (s/as-str unit))))

(defrecord Color [r g b]
  Value
  (repr [_] (format "#%02X%02X%02X" (int r) (int g) (int b))))


(defn- make-color [r g b]
  (letfn [(limit [x] (max 0 (min x 255)))]
    (Color. (limit r) (limit g) (limit b))))

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


(defn str->color [s]
  (letfn [(remove-number-sign [s] (s/replace-first-re #"#" "" s))
          (duplicate [s] (if (= (.length s) 3) (apply str (interleave s s)) s))]
    (let [components (->> s remove-number-sign duplicate (re-seq #".."))
          [r g b] (map #(Integer/parseInt % 16) components)]

      (make-color r g b))))


(defn make-value [mag unit]
  (if (= (s/as-str unit) "$")
    (str->color (s/as-str mag))
    (Length. mag unit)))

(defmacro def-value-constr [name]
  `(defn ~name [x#] (make-value x# ~(keyword name))))

(def-value-constr em)
(def-value-constr ex)
(def-value-constr px)
(def-value-constr in)
(def-value-constr cm)
(def-value-constr mm)
(def-value-constr pt)
(def-value-constr pc)
(def-value-constr %)
(def-value-constr $)
(defn col [x] ($ x))

(defmethod generic/+ [Length Length]
  [{ua :unit ma :mag} {ub :unit mb :mag}]
  {:pre [(= ua ub)]}
  (make-value (+ ma mb) ua))

(defmethod generic/- [Length]
  [{ua :unit ma :mag}]
  (make-value (- ma) ua))

(defmethod generic/- [Length Length]
  [{ua :unit ma :mag} {ub :unit mb :mag}]
  {:pre [(= ua ub)]}
  (make-value (- ma mb) ua))

(defmethod generic/* [Length Number]
  [{ua :unit ma :mag} num]
  (make-value (* ma num) ua))

(defmethod generic/* [Number Length]
  [num {ua :unit ma :mag}]
  (make-value (* ma num) ua))

(generic/defmethod* generic / [Length Number]
  [{ua :unit ma :mag} num]
  (make-value ((generic/qsym generic /) ma num) ua))


(defmacro compwise-col-col-op [sym f]
  (let [f f]
    `(defmethod ~sym [Color Color]
       [{ra# :r ga# :g ba# :b} {rb# :r gb# :g bb# :b}]
       (make-color (~f ra# rb#) (~f ga# gb#) (~f ba# bb#)))))

(compwise-col-col-op generic/+ +)
(compwise-col-col-op generic/- -)
(compwise-col-col-op generic/* *)
(generic/defmethod* generic / [Color Color]
  [{ra :r ga :g ba :b} {rb :r gb :g bb :b}]
  (make-color ((generic/qsym generic /) ra rb)
              ((generic/qsym generic /) ga gb)
              ((generic/qsym generic /) ba bb)))


(defmacro compwise-col-num-op [sym f]
  (let [f f]
    `(do
      (defmethod ~sym [Color Number]
        [{r# :r g# :g b# :b} num#]
        (make-color (~f r# num#) (~f g# num#) (~f b# num#)))
      (defmethod ~sym [Number Color]
        [num# {r# :r g# :g b# :b}]
        (make-color (~f num# r#) (~f num# g#) (~f num# b#))))))

(compwise-col-num-op generic/+ +)
(compwise-col-num-op generic/- -)
(compwise-col-num-op generic/* *)
(generic/defmethod* generic / [Color Number]
  [{r :r g :g b :b} num]
  (make-color ((generic/qsym generic /) r num)
              ((generic/qsym generic /) g num)
              ((generic/qsym generic /) b num)))
(generic/defmethod* generic / [Number Color]
  [num {r :r g :g b :b}]
  (make-color ((generic/qsym generic /) num r)
              ((generic/qsym generic /) num g)
              ((generic/qsym generic /) num b)))



(defn is-obj? [x]
  ;I'm using this function in a partition-by, it's important that it
  ;always returs false or nil, but not both
  (boolean (and (map? x)
                (if-let [t (:tag x)]
                  (or (= t ::Prop)
                      (= t ::Rule)
                      (= t ::Mixin))))))

(defn prop [& forms]
  (letfn [(expand-item [item]
            (if (is-obj? item)
              (flatten (:prop item))
              [item]))]
    (let [expanded (mapcat expand-item forms)
          in-pairs (apply vector (partition 2 expanded))]
        {:tag ::Prop :prop in-pairs})))


(defn create-props [forms]
  (letfn [(ready? [x] (or (nil? x) (is-obj? x)))]
    (let [parts (partition-by ready? forms)]
      (mapcat #(if (ready? (first %))
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

