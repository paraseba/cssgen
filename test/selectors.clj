(ns selectors
  (:use cssgen clojure.test))

(defn- extract-selectors [rule]
  (map second (re-seq #"(?m)^(.*) \{" (rule-css rule))))

(deftest nested-selectors
  (are [selectors the-rule] (= selectors (extract-selectors the-rule))

    ["body"] (rule "body")
    ["body" "body table"] (rule "body" (rule "table"))
    ["body" "body table" "body table tr"] (rule "body" (rule "table" (rule "tr")))

    ["a" "a:hover"] (rule "a" (rule "&:hover"))
    ["body a" "body a:hover, body a:visited"] (rule "body a" (rule "&:hover, &:visited"))
    ["a" "html a:hover"] (rule "a" (rule "html &:hover"))

    ["body" "body a, body input"] (rule "body" (rule "a, input"))
    ["body, #footer" "body a, #footer a"] (rule "body  ,  #footer" (rule "  a  "))
    ["body, #footer" "body a, body input, #footer a, #footer input"] (rule " body  ,  #footer " (rule "  a , input "))

    ["body" "body a, body:hover"] (rule "body" (rule "a, &:hover"))
    ["#foo, .bar" "#foo:hover, .bar:hover"] (rule "#foo, .bar" (rule "&:hover"))
    ["#foo, .bar" "#foo:hover, #foo:visited, .bar:hover, .bar:visited"] (rule "#foo, .bar" (rule "&:hover, &:visited"))))


