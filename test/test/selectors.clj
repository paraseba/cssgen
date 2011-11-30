(ns test.selectors
  (:use cssgen clojure.test
        [clojure.string :only [trim]]))

(defn- extract-selectors [rule]
  (map (comp trim second) (re-seq #"(?m)^(.*) \{" (css rule))))

(deftest nested-selectors
  (are [selectors the-rule] (= selectors (extract-selectors the-rule))

    ["body"] ["body"]
    ["body" "body table"] ["body" ["table"]]
    ["body" "body table"] ["body" (list ["table"])]
    ["body" "body table"] ["body" (list (list (list ["table"])))]
    ["body" "body table" "body table tr"] ["body" ["table" ["tr"]]]
    ["body" "body table" "body table tr"] ["body" (list ["table" (list ["tr"])])]

    ["a" "a:hover"] ["a" ["&:hover"]]
    ["a" "a:hover"] ["a" (list ["&:hover"])]
    ["body a" "body a:hover, body a:visited"] ["body a" ["&:hover, &:visited"]]
    ["body a" "body a:hover, body a:visited"] ["body a" (list ["&:hover, &:visited"])]
    ["a" "html a:hover"] ["a" ["html &:hover"]]
    ["a" "html a:hover"] ["a" (list ["html &:hover"])]

    ["body" "body a, body input"] ["body" ["a, input"]]
    ["body" "body a, body input"] ["body" (list ["a, input"])]
    ["body, #footer" "body a, #footer a"] ["body  ,  #footer" ["  a  "]]
    ["body, #footer" "body a, #footer a"] ["body  ,  #footer" (list ["  a  "])]
    ["body, #footer" "body a, body input, #footer a, #footer input"] [" body  ,  #footer " ["  a , input "]]
    ["body, #footer" "body a, body input, #footer a, #footer input"] [" body  ,  #footer " (list ["  a , input "])]

    ["body" "body a, body:hover"] ["body" ["a, &:hover"]]
    ["body" "body a, body:hover"] ["body" (list ["a, &:hover"])]
    ["#foo, .bar" "#foo:hover, .bar:hover"] ["#foo, .bar" ["&:hover"]]
    ["#foo, .bar" "#foo:hover, .bar:hover"] ["#foo, .bar" (list ["&:hover"])]
    ["#foo, .bar" "#foo:hover, #foo:visited, .bar:hover, .bar:visited"] ["#foo, .bar" ["&:hover, &:visited"]]
    ["#foo, .bar" "#foo:hover, #foo:visited, .bar:hover, .bar:visited"] ["#foo, .bar" (list ["&:hover, &:visited"])]

    ["div" "div span" "div span a"] ["div" ["span" ["a"]]]
    ["div" "div span" "div span a"] ["div" (list ["span" (list ["a"])])]))
