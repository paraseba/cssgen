(ns values
  (:use cssgen cssgen.types clojure.test))

(deftest color-formats
  (is (not= (col :#aabbcc) ($ :#aabbcd)))
  (are [a b] (= a b)
    (col "#abc"   ) ($ "#abc"   )
    (col "abc"    ) ($ "abc"    )
    (col "#aabbcc") ($ "#aabbcc")
    (col "aabbcc" ) ($ "aabbcc" )
    (col "#0bcdef") ($ "#0bcdef")
    (col "0bcdef" ) ($ "0bcdef" )
    (col :0bcdef  ) ($ :0bcdef  )
    (col :#0bcdef ) ($ :#0bcdef )
    (col :#aaa    ) ($ :#aaa    )
    (col :#abc    ) (col :abc   )
    (col :#abc    ) (col :aabbcc)
    (col 10 10 10)  (col :0a0a0a)
    ($ 10 10 10)    ($ :0a0a0a)))

(def all-lengths [em ex px in cm mm pt pc %])

(deftest length-equality
  (doseq [unit all-lengths]
		(is (not= (unit 1) (unit 2)))
		(is (= (unit 1) (unit 1)))))


(deftest representation
  (are [the-rule result] (= result (css the-rule))

(rule "a" :color :#aaa)
"a {
  color: #aaa;
}
"

(rule "a" :color ($ :010101))
"a {
  color: #010101;
}
"

(rule "a" :color (col :aaa) :background-color ($ :1a2B3C))
"a {
  color: #AAAAAA;
  background-color: #1A2B3C;
}
"

(rule "a"
      :width (em 1)
      :width (ex 1)
      :width (px 1)
      :width (in 1)
      :width (cm 1)
      :width (mm 1)
      :width (pt 1)
      :width (pc 1)
      :width (deg 1)
      :width (% 1))
"a {
  width: 1em;
  width: 1ex;
  width: 1px;
  width: 1in;
  width: 1cm;
  width: 1mm;
  width: 1pt;
  width: 1pc;
  width: 1deg;
  width: 1%;
}
"
))
