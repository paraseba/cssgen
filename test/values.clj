(ns values
  (:use cssgen cssgen.types clojure.test))

(deftest single-prop-rules
  (are [the-rule result] (= result (css the-rule))

(rule "a" :color "#aaa")
"a {
  color: #aaa;
}
"

(rule "a" :color :#aaa)
"a {
  color: #aaa;
}
"

(rule "a" :color ($ :aaa))
"a {
  color: #AAAAAA;
}
"

(rule "a" :color ($ :010101))
"a {
  color: #010101;
}
"

(rule "a" :color ($ :#aaa))
"a {
  color: #AAAAAA;
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
  width: 1%;
}
"

(rule "a"
      :width (em :1)
      :width (ex :1)
      :width (px :1)
      :width (in :1)
      :width (cm :1)
      :width (mm :1)
      :width (pt :1)
      :width (pc :1)
      :width (% :1))
"a {
  width: 1em;
  width: 1ex;
  width: 1px;
  width: 1in;
  width: 1cm;
  width: 1mm;
  width: 1pt;
  width: 1pc;
  width: 1%;
}
"

(rule "a"
      :width (em "1")
      :width (ex "1")
      :width (px "1")
      :width (in "1")
      :width (cm "1")
      :width (mm "1")
      :width (pt "1")
      :width (pc "1")
      :width (% "1"))
"a {
  width: 1em;
  width: 1ex;
  width: 1px;
  width: 1in;
  width: 1cm;
  width: 1mm;
  width: 1pt;
  width: 1pc;
  width: 1%;
}
"
       ))
