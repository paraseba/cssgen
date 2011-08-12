(ns test.cssgen
  (:use cssgen)
  (:use clojure.test))

(deftest simple-rules-test
  (are [the-rule result] (= result (css the-rule))
    [:a :color :red]
"a {
  color: red;
}"
    [:a.foo :color :#aaa "font-weigth" "bold"]
"a.foo {
  color: #aaa;
  font-weigth: bold;
}"

    ["div.klass#id, tr > td" :padding "1px"]
"div.klass#id, tr > td {
  padding: 1px;
}"

    [:#sidebar 'width 4005/10]
"#sidebar {
  width: 400.5;
}"))

(deftest sequence-values
  (are [the-rule result] (= result (css the-rule))
    [:.foo :border '("5px" :solid "red")]
".foo {
  border: 5px solid red;
}"

    [:.foo :border ["5px" :solid "red"]]
".foo {
  border: 5px solid red;
}"))

(deftest sub-rules
  (are [the-rule result] (= result (css the-rule))
    [:tr
     :background-color :#fff
     [:td
      :color :black
      [:a :color :red]
      :background-color :#ccc]
     :color "blue"]
"tr {
  background-color: #fff;
  color: blue;
}
  tr td {
    color: black;
    background-color: #ccc;
  }
    tr td a {
      color: red;
    }"

    ["#content, #sidebar"
     :background-color :white
     [:a :color :blue
      ["&:hover" :color :#00e]]]
"#content, #sidebar {
  background-color: white;
}
  #content a {
    color: blue;
  }
    #content a:hover {
      color: #00e;
    }
  #sidebar a {
    color: blue;
  }
    #sidebar a:hover {
      color: #00e;
    }"))
