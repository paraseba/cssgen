(ns test.operations
  (:refer-clojure :exclude [+ - * /])
  (:use cssgen
        clojure.test
        [clojure.algo.generic.arithmetic :only (+ - * /)]))

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
    (rgb :000) (rgb :abc) (rgb :abc)
    (rgb :111) (rgb :abc) (rgb :bcd)
    (rgb :abc) (rgb :111) (rgb :bcd)
    (rgb :aaa) (rgb :aaa) (rgb :fff)))

(deftest color-color
  (are [a b res] (= res (- a b))
    (rgb :abc) (rgb :000) (rgb :abc)
    (rgb :bcd) (rgb :abc) (rgb :111)
    (rgb :abc) (rgb :abc) (rgb :000)
    (rgb :aaa) (rgb :fff) (rgb :000)))

(deftest color*color
  (are [a b res] (= res (* a b))
    (rgb :abc) (rgb :000) (rgb :000)
    (rgb :bcd) (rgb :010101) (rgb :bcd)
    (rgb :567) (rgb :020202) (rgb :ace)
    (rgb :020304) (rgb :513) (rgb :a3c)
    (rgb :aaa) (rgb :fff) (rgb :fff)))

(deftest color-divided-by-color
  (are [a b res] (= res (/ a b))
    (rgb :abc) (rgb :010101) (rgb :abc)
    (rgb :804) (rgb :444) (rgb :020001)))

(deftest color+number
  (are [a b res] (= res (+ a b))
    (rgb :abc) 0 (rgb :abc)
    (rgb :abc) 2 (rgb :acbdce)
    (rgb :abc) 200 (rgb :fff)))

(deftest number+color
  (are [a b res] (= res (+ a b))
    0 (rgb :abc) (rgb :abc)
    2 (rgb :abc) (rgb :acbdce)
    200 (rgb :abc) (rgb :fff)))

(deftest color-number
  (are [a b res] (= res (- a b))
    (rgb :abc) 0 (rgb :abc)
    (rgb :987) 2 (rgb :978675)
    (rgb :abc) 250 (rgb :000)))

(deftest number-color
  (are [a b res] (= res (- a b))
    0 (rgb :abc) (rgb :000)
    255 (rgb :010203) (rgb :fefdfc)
    10 (rgb :000) (rgb :0a0a0a)))

(deftest color*number
  (are [a b res] (= res (* a b))
    (rgb :abc) 0 (rgb :000)
    (rgb :123) 2 (rgb :224466)
    (rgb :abc) 250 (rgb :fff)))

(deftest number*color
  (are [a b res] (= res (* a b))
    0 (rgb :abc) (rgb :000)
    2 (rgb :123) (rgb :224466)
    250 (rgb :abc) (rgb :fff)))

(deftest color-divided-by-number
  (are [a b res] (= res (/ a b))
    (rgb :000) 10 (rgb :000)
    (rgb :abc) 1 (rgb :abc)
    (rgb :864) 2 (rgb :443322)))

(deftest number-divided-by-color
  (are [a b res] (= res (/ a b))
    12 (rgb :010203) (rgb :0c0604)
    0 (rgb :abc) (rgb :000)))
