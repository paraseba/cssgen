(ns operations
  (:refer-clojure :exclude [+ - * /])
  (:use
     cssgen cssgen.types
     clojure.test
     [clojure.contrib.generic.arithmetic :only (+ - * /)]))

(deftest test-length-+
  (are [a b res] (= res (+ a b))
    (em 1) (em 2) (em 3)
    (ex 1) (ex 2) (ex 3)
    (px 1) (px 2) (px 3)
    (in 1) (in 2) (in 3)
    (cm 1) (cm 2) (cm 3)
    (mm 1) (mm 2) (mm 3)
    (pt 1) (pt 2) (pt 3)
    (pc 1) (pc 2) (pc 3)
    (% 1) (% 2) (% 3)))
        

(deftest test-length--
  (are [a b res] (= res (- a b))
    (em 4) (em 2) (em 2)
    (ex 4) (ex 2) (ex 2)
    (px 4) (px 2) (px 2)
    (in 4) (in 2) (in 2)
    (cm 4) (cm 2) (cm 2)
    (mm 4) (mm 2) (mm 2)
    (pt 4) (pt 2) (pt 2)
    (pc 4) (pc 2) (pc 2)
    (% 4) (% 2) (% 2)))

(deftest test-length*number
  (are [a b res] (= res (* a b))
    (em 3) 2 (em 6)
    (em 3) 2.0 (em 6.0)
    (ex 3) 2 (ex 6)
    (ex 3) 2.0 (ex 6.0)
    (px 3) 2 (px 6)
    (px 3) 2.0 (px 6.0)
    (in 3) 2 (in 6)
    (in 3) 2.0 (in 6.0)
    (cm 3) 2 (cm 6)
    (cm 3) 2.0 (cm 6.0)
    (mm 3) 2 (mm 6)
    (mm 3) 2.0 (mm 6.0)
    (pt 3) 2 (pt 6)
    (pt 3) 2.0 (pt 6.0)
    (pc 3) 2 (pc 6)
    (pc 3) 2.0 (pc 6.0)
    (% 3) 2 (% 6)
    (% 3) 2.0 (% 6.0)))

(deftest test-number*length
  (are [a b res] (= res (* a b))
    2 (em 3) (em 6)
    2.0 (em 3) (em 6.0)
    2 (ex 3) (ex 6)
    2.0 (ex 3) (ex 6.0)
    2 (px 3) (px 6)
    2.0 (px 3) (px 6.0)
    2 (in 3) (in 6)
    2.0 (in 3) (in 6.0)
    2 (cm 3) (cm 6)
    2.0 (cm 3) (cm 6.0)
    2 (mm 3) (mm 6)
    2.0 (mm 3) (mm 6.0)
    2 (pt 3) (pt 6)
    2.0 (pt 3) (pt 6.0)
    2 (pc 3) (pc 6)
    2.0 (pc 3) (pc 6.0)
    2 (% 3) (% 6)
    2.0 (% 3) (% 6.0)))

(deftest test-length-divided-by-number
  (are [a b res] (= res (/ a b))
    (em 4) 2 (em 2)
    (em 4) 2.0 (em 2.0)
    (ex 4) 2 (ex 2)
    (ex 4) 2.0 (ex 2.0)
    (px 4) 2 (px 2)
    (px 4) 2.0 (px 2.0)
    (in 4) 2 (in 2)
    (in 4) 2.0 (in 2.0)
    (cm 4) 2 (cm 2)
    (cm 4) 2.0 (cm 2.0)
    (mm 4) 2 (mm 2)
    (mm 4) 2.0 (mm 2.0)
    (pt 4) 2 (pt 2)
    (pt 4) 2.0 (pt 2.0)
    (pc 4) 2 (pc 2)
    (pc 4) 2.0 (pc 2.0)
    (% 4) 2 (% 2)
    (% 4) 2.0 (% 2.0)))

(deftest color+color
  (are [a b res] (= res (+ a b))
    (col :000) (col :abc) (col :abc)
    (col :111) (col :abc) (col :bcd)
    (col :abc) (col :111) (col :bcd)
    (col :aaa) (col :aaa) (col :fff)))

(deftest color-color
  (are [a b res] (= res (- a b))
    (col :abc) (col :000) (col :abc)
    (col :bcd) (col :abc) (col :111)
    (col :abc) (col :abc) (col :000)
    (col :aaa) (col :fff) (col :000)))

(deftest color*color
  (are [a b res] (= res (* a b))
    (col :abc) (col :000) (col :000)
    (col :bcd) (col :010101) (col :bcd)
    (col :567) (col :020202) (col :ace)
    (col :020304) (col :513) (col :a3c)
    (col :aaa) (col :fff) (col :fff)))

(deftest color-divided-by-color
  (are [a b res] (= res (/ a b))
    (col :abc) (col :010101) (col :abc)
    (col :804) (col :444) (col :020001)))

(deftest color+number
  (are [a b res] (= res (+ a b))
    (col :abc) 0 (col :abc)
    (col :abc) 2 (col :acbdce)
    (col :abc) 200 (col :fff)))

(deftest number+color
  (are [a b res] (= res (+ a b))
    0 (col :abc) (col :abc)
    2 (col :abc) (col :acbdce)
    200 (col :abc) (col :fff)))

(deftest color-number
  (are [a b res] (= res (- a b))
    (col :abc) 0 (col :abc)
    (col :987) 2 (col :978675)
    (col :abc) 250 (col :000)))

(deftest number-color
  (are [a b res] (= res (- a b))
    0 (col :abc) (col :000)
    255 (col :010203) (col :fefdfc)
    10 (col :000) (col :0a0a0a)))

(deftest color*number
  (are [a b res] (= res (* a b))
    (col :abc) 0 (col :000)
    (col :123) 2 (col :224466)
    (col :abc) 250 (col :fff)))

(deftest number*color
  (are [a b res] (= res (* a b))
    0 (col :abc) (col :000)
    2 (col :123) (col :224466)
    250 (col :abc) (col :fff)))

(deftest color-divided-by-number
  (are [a b res] (= res (/ a b))
    (col :000) 10 (col :000)
    (col :abc) 1 (col :abc)
    (col :864) 2 (col :443322)))

(deftest number-divided-by-color
  (are [a b res] (= res (/ a b))
    12 (col :010203) (col :0c0604)
    0 (col :abc) (col :000)))
