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

(deftest sequence-expansion
  (let [inner '(:color :blue :margin "auto" ["&:hover" :border ["1px" "solid" :black]])]
    (are [the-rule result] (= result (css the-rule))
      [:#block
       :background-color :#fff
       inner
       :width "100%"]
"#block {
  background-color: #fff;
  color: blue;
  margin: auto;
  width: 100%;
}
  #block:hover {
    border: 1px solid black;
  }")))

(deftest lengths-test
  (are [s l] (= (str "div {\n  width: " s ";\n}") (css [:div :width l]))
    "5em"  (em 5)
    "5ex"  (ex 5)
    "5px"  (px 5)
    "5in"  (in 5)
    "5cm"  (cm 5)
    "5mm"  (mm 5)
    "5pt"  (pt 5)
    "5pc"  (pc 5)
    "5%"   (% 5)
    "5deg" (deg 5)))

(deftest make-color-test
  (are [s l] (= (str "div {\n  color: " s ";\n}") (css [:div :color l]))
    "#FFFFFF" (rgb 255 255 255)
    "#FFFFFF" (rgb "ffffff")
    "#FFFFFF" (rgb "#ffffff")
    "#FFFFFF" (rgb "fff")
    "#FFFFFF" (rgb "#fff")
    "#1122AA" (rgb 17 34 170)
    "#1122AA" (rgb "1122AA")
    "#1122AA" (rgb "#1122AA")
    "#1122AA" (rgb "12A")
    "#1122AA" (rgb "#12A")))
