(in-ns 'cssgen)

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
        (let [n (Integer/parseInt s 16)]
          (-> n
            (bit-shift-left 4)
            (bit-or n)
            (bit-and 0xff))))
         (rest m))))

(defn col
  ([r g b]
   {:pre [(every? integer? [r g b])] :post [(= Color (class %))]}
   (letfn [(limit [x] (max 0 (min x 255)))]
     (Color. (limit r) (limit g) (limit b))))
  ([s]
   {:pre [(string? s)] :post [(= Color (class %))]}
   (apply col (color-string->rgb s))))

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

