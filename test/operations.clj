(ns operations
  (:refer-clojure :exclude [+ - * /])
  (:use
     cssgen cssgen.types
     clojure.test
     [clojure.contrib.generic.arithmetic :only (+ - * /)]))

(def all-lengths [em ex px in cm mm pt pc % deg])

(deftest test-length-+
  (doseq [unit all-lengths]
    (is (= (unit 3)
           (+ (unit 1) (unit 2))))))

(deftest test-length-unary-
  (doseq [unit all-lengths]
    (is (= (unit -2)
           (- (unit 2))))))

(deftest test-length--
  (doseq [unit all-lengths]
    (is (= (unit 2)
           (- (unit 4) (unit 2))))))

(deftest test-length*number
  (doseq [unit all-lengths]
    (is (= (unit 6)
           (* (unit 3) 2)))
    (is (= (unit 6.0)
           (* (unit 3) 2.0)))))

(deftest test-number*length
  (doseq [unit all-lengths]
    (is (= (unit 6)
           (* 2 (unit 3))))
    (is (= (unit 6.0)
           (* 2.0 (unit 3))))))


(deftest test-length-divided-by-number
  (doseq [unit all-lengths]
    (is (= (unit 2)
           (/ (unit 4) 2)))
    (is (= (unit 2.0)
           (/ (unit 4) 2.0)))))


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
