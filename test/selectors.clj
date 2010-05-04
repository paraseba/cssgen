(ns selectors
  (:use cssgen clojure.test))

(defn- extract-selectors [rule]
  (map second (re-seq #"(?m)^(.*) \{" (css rule))))

(deftest nested-selectors
  (are [selectors the-rule] (= selectors (extract-selectors the-rule))

    ["body"] (rule "body")
    ["body" "body table"] (rule "body" (rule "table"))
    ["body" "body table"] (rule "body" (mixin (rule "table")))
    ["body" "body table"] (rule "body" (mixin (mixin (mixin (rule "table")))))
    ["body" "body table" "body table tr"] (rule "body" (rule "table" (rule "tr")))
    ["body" "body table" "body table tr"] (rule "body" (mixin (rule "table" (mixin (rule "tr")))))

    ["a" "a:hover"] (rule "a" (rule "&:hover"))
    ["a" "a:hover"] (rule "a" (mixin (rule "&:hover")))
    ["body a" "body a:hover, body a:visited"] (rule "body a" (rule "&:hover, &:visited"))
    ["body a" "body a:hover, body a:visited"] (rule "body a" (mixin (rule "&:hover, &:visited")))
    ["a" "html a:hover"] (rule "a" (rule "html &:hover"))
    ["a" "html a:hover"] (rule "a" (mixin (rule "html &:hover")))

    ["body" "body a, body input"] (rule "body" (rule "a, input"))
    ["body" "body a, body input"] (rule "body" (mixin (rule "a, input")))
    ["body, #footer" "body a, #footer a"] (rule "body  ,  #footer" (rule "  a  "))
    ["body, #footer" "body a, #footer a"] (rule "body  ,  #footer" (mixin (rule "  a  ")))
    ["body, #footer" "body a, body input, #footer a, #footer input"] (rule " body  ,  #footer " (rule "  a , input "))
    ["body, #footer" "body a, body input, #footer a, #footer input"] (rule " body  ,  #footer " (mixin (rule "  a , input ")))

    ["body" "body a, body:hover"] (rule "body" (rule "a, &:hover"))
    ["body" "body a, body:hover"] (rule "body" (mixin (rule "a, &:hover")))
    ["#foo, .bar" "#foo:hover, .bar:hover"] (rule "#foo, .bar" (rule "&:hover"))
    ["#foo, .bar" "#foo:hover, .bar:hover"] (rule "#foo, .bar" (mixin (rule "&:hover")))
    ["#foo, .bar" "#foo:hover, #foo:visited, .bar:hover, .bar:visited"] (rule "#foo, .bar" (rule "&:hover, &:visited"))
    ["#foo, .bar" "#foo:hover, #foo:visited, .bar:hover, .bar:visited"] (rule "#foo, .bar" (mixin (rule "&:hover, &:visited")))

    ["div" "div span" "div span a"] (rule "div" (rule "span" (rule "a")))
    ["div" "div span" "div span a"] (rule "div" (mixin (rule "span" (mixin (rule "a")))))))
