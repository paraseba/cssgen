(ns rules
  (:use cssgen clojure.test))

(deftest single-prop-rules
  (are [the-rule css] (= css (rule-css the-rule))

(rule "a"
      (prop [:color "#aaa"]))
"a {
  color: #aaa;
}
"

(rule "div.klass#id, tr > td"
  (prop [:padding 'px1 'px2 'em5.5 'cm-3.3]))
"div.klass#id, tr > td {
  padding: 1px 2px 5.5em -3.3cm;
}
"))

(deftest multiple-prop-rules
  (are [the-rule css] (= css (rule-css the-rule))

(rule "a"
  (prop [:color "#aaa"])
  (prop [:background-color "#fff"]))
"a {
  color: #aaa;
  background-color: #fff;
}
"

(rule "a"
  (prop [:color "#aaa"] [:background-color "#fff"]))
"a {
  color: #aaa;
  background-color: #fff;
}
"))

(deftest nested-rule
  (are [the-rule css] (= css (rule-css the-rule))

(rule "tr"
  (prop ["background-color" "#fff"])
  (rule "td"
    (prop ["color" "black"])))
"tr {
  background-color: #fff;
}
tr td {
  color: black;
}
"

(rule "tr"
  (prop [:background-color "#fff"]
        [:color "black"])
  (rule "td"
    (prop [:color "red"])
    (prop [:width '%50])))
"tr {
  background-color: #fff;
  color: black;
}
tr td {
  color: red;
  width: 50%;
}
"))

(def prop1
  (prop [:color "#fff"]
        [:background-color "black"]))

(def prop2
  (prop [:width '%100]
        [:display "block"]))

(def prop3
  (prop [:height 'px100]))

(deftest inner-prop
  (are [the-rule css] (= css (rule-css the-rule))
(rule "tr"
  (prop [:padding 0] prop1)
  (prop prop2 prop3)
  (prop [:border "none"]))
"tr {
  padding: 0;
  color: #fff;
  background-color: black;
  width: 100%;
  display: block;
  height: 100px;
  border: none;
}
"))

(defn mixin1 []
  (mixin
    (prop [:padding 0])
    (rule "a"
      (prop [:color :blue]))))

(deftest multiple-rules-and-props
  (are [the-rule css] (= css (rule-css the-rule))
(rule ".block, .group"
  (mixin1))
".block, .group {
  padding: 0;
}
.block a, .group a {
  color: blue;
}
"))

(deftest mixin-with-nil
  (are [the-rule css] (= css (rule-css the-rule))
(rule "a"
  (mixin
    (prop [:color "blue"])
    nil
    (prop [:font-size 'mm2])))
"a {
  color: blue;
  font-size: 2mm;
}
"))

