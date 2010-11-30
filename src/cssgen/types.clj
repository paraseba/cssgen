(ns cssgen.types
  (:require
     (clojure.contrib [string :as s]
                      [def :as ccdef])
     [clojure.contrib.generic.arithmetic :as generic]))

(defprotocol Value
  (repr [x]))

(defrecord Length [mag unit]
  Value
  (repr [_] (str (s/as-str mag) (s/as-str unit))))

(defrecord Color [r g b]
  Value
  (repr [_] (format "#%02X%02X%02X" (int r) (int g) (int b))))


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


(defn- make-color
  ([r g b] (letfn [(limit [x] (max 0 (min x 255)))]
             (Color. (limit r) (limit g) (limit b))))

  ([string] (letfn [(remove-number-sign [s] (s/replace-first-re #"#" "" s))
                    (duplicate [s] (if (= (.length s) 3) (apply str (interleave s s)) s))]
              (let [components (->> string s/as-str remove-number-sign duplicate (re-seq #".."))
                    [r g b] (map #(Integer/parseInt % 16) components)]

                (make-color r g b)))))


(defn- make-length [mag unit]
  {:pre [(number? mag)]}
  (Length. mag (s/as-str unit)))

(ccdef/defmacro- def-length-constr [name]
  `(defn ~name [x#] (make-length x# ~(keyword name))))

(def-length-constr em)
(def-length-constr ex)
(def-length-constr px)
(def-length-constr in)
(def-length-constr cm)
(def-length-constr mm)
(def-length-constr pt)
(def-length-constr pc)
(def-length-constr %)
(def-length-constr deg)

(defn col
  ([x] (make-color x))
  ([r g b] (make-color r g b)))
(def $ col)

(defmethod generic/+ [Length Length]
  [{ua :unit ma :mag} {ub :unit mb :mag}]
  {:pre [(= ua ub)]}
  (make-length (+ ma mb) ua))

(defmethod generic/- Length
  [{ua :unit ma :mag}]
  (make-length (- ma) ua))

(defmethod generic/- [Length Length]
  [{ua :unit ma :mag} {ub :unit mb :mag}]
  {:pre [(= ua ub)]}
  (make-length (- ma mb) ua))

(defmethod generic/* [Length Number]
  [{ua :unit ma :mag} num]
  (make-length (* ma num) ua))

(defmethod generic/* [Number Length]
  [num {ua :unit ma :mag}]
  (make-length (* ma num) ua))

(generic/defmethod* generic / [Length Number]
  [{ua :unit ma :mag} num]
  (make-length ((generic/qsym generic /) ma num) ua))


(ccdef/defmacro- compwise-col-col-op [sym f]
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


(ccdef/defmacro- compwise-col-num-op [sym f]
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

