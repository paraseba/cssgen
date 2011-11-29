(in-ns 'cssgen)
(require '[clojure.algo.generic.arithmetic :as generic])

(defrecord Length [mag unit]
  CssValue
  (to-css [_] (str (to-css mag) (to-css unit))))

(defrecord Color [r g b]
  CssValue
  (to-css [_] (format "#%02X%02X%02X" (int r) (int g) (int b))))

(defn- color-string->rgb [s]
  (let [m (re-matches #"^\s*#?(\p{XDigit}{1,2})(\p{XDigit}{1,2})(\p{XDigit}{1,2})\s*$" s)]
    (map
      (fn [s]
        (let [n (Integer/parseInt (str s s) 16)]
          (bit-and n 0xff)))
      (rest m))))

(defn rgb
  ([r g b]
   {:pre [(every? integer? [r g b])] :post [(= Color (class %))]}
   (letfn [(limit [x] (max 0 (min x 255)))]
     (Color. (limit r) (limit g) (limit b))))
  ([s]
   {:pre [(or (string? s) (keyword? s) (symbol? s))]
    :post [(= Color (class %))]}
   (apply rgb (color-string->rgb (name s)))))

(defn- make-length [mag unit]
  {:pre [(number? mag) (or (symbol? unit) (keyword? unit) (string? unit))]}
  (Length. mag unit))

(defmacro ^{:private true} def-length-constructor [name]
  `(defn ~name [x#] (make-length x# ~(keyword name))))

(def-length-constructor em)
(def-length-constructor ex)
(def-length-constructor px)
(def-length-constructor in)
(def-length-constructor cm)
(def-length-constructor mm)
(def-length-constructor pt)
(def-length-constructor pc)
(def-length-constructor %)
(def-length-constructor deg)

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


(defmacro ^{:private true} compwise-col-col-op [sym f]
  (let [f f]
    `(defmethod ~sym [Color Color]
       [{ra# :r ga# :g ba# :b} {rb# :r gb# :g bb# :b}]
       (rgb (~f ra# rb#) (~f ga# gb#) (~f ba# bb#)))))

(compwise-col-col-op generic/+ +)
(compwise-col-col-op generic/- -)
(compwise-col-col-op generic/* *)
(generic/defmethod* generic / [Color Color]
  [{ra :r ga :g ba :b} {rb :r gb :g bb :b}]
  (rgb ((generic/qsym generic /) ra rb)
       ((generic/qsym generic /) ga gb)
       ((generic/qsym generic /) ba bb)))

(defmacro ^{:private true} compwise-col-num-op [sym f]
  (let [f f]
    `(do
      (defmethod ~sym [Color Number]
        [{r# :r g# :g b# :b} num#]
        (rgb (~f r# num#) (~f g# num#) (~f b# num#)))
      (defmethod ~sym [Number Color]
        [num# {r# :r g# :g b# :b}]
        (rgb (~f num# r#) (~f num# g#) (~f num# b#))))))

(compwise-col-num-op generic/+ +)
(compwise-col-num-op generic/- -)
(compwise-col-num-op generic/* *)
(generic/defmethod* generic / [Color Number]
  [{r :r g :g b :b} num]
  (rgb ((generic/qsym generic /) r num)
       ((generic/qsym generic /) g num)
       ((generic/qsym generic /) b num)))
(generic/defmethod* generic / [Number Color]
  [num {r :r g :g b :b}]
  (rgb ((generic/qsym generic /) num r)
       ((generic/qsym generic /) num g)
       ((generic/qsym generic /) num b)))

