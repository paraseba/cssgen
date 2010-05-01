(ns rules
  (:use cssgen clojure.test))

(deftest single-prop-rules
  (are [the-rule css] (= css (rule-css the-rule))

(rule "a"
      (prop :color :#aaa))
"a {
  color: #aaa;
}
"

(rule "a"
      (prop :color "#aaa"))
"a {
  color: #aaa;
}
"

(rule "a"
      (prop :color ($ :aaa)))
"a {
  color: #AAAAAA;
}
"

(rule "div.klass#id, tr > td"
  :padding [:1px :2px :5.5em :-3.3cm])
"div.klass#id, tr > td {
  padding: 1px 2px 5.5em -3.3cm;
}
"))

(deftest multiple-prop-rules
  (are [the-rule css] (= css (rule-css the-rule))

(rule "a"
  :color "#aaa"
  :background-color :#fafbfc)
"a {
  color: #aaa;
  background-color: #fafbfc;
}
"

(rule "a"
  :color :#aaa :background-color (col "fff"))
"a {
  color: #aaa;
  background-color: #FFFFFF;
}
"))

(deftest nested-rule
  (are [the-rule css] (= css (rule-css the-rule))

(rule "tr"
  :background-color :#fff
  (rule "td"
    :color "black"))
"tr {
  background-color: #fff;
}
tr td {
  color: black;
}
"

(rule "tr"
  :background-color :#fff
  :color "black"
  (rule "td"
    :color "red"
    :width "50%"))
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
  (prop :color :#fff
        :background-color "black"))

(def prop2
  (prop :width "100%"
        :display "block"))

(def prop3
  (prop :height :100px))

(deftest inner-prop
  (are [the-rule css] (= css (rule-css the-rule))
(rule "tr"
  :padding 0 prop1
  prop2 prop3
  :border "none")
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
    :padding 0
    (rule "a"
      :color :blue)))

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
    :color "blue"
    nil
    :font-size :2mm))
"a {
  color: blue;
  font-size: 2mm;
}
"))

