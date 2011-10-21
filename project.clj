(defproject cssgen "0.2.5-SNAPSHOT"
  :description "Generate CSS from clojure code. Like an embedded sass."
  :url "http://github.com/paraseba/cssgen"
  :dependencies [[org.clojure/clojure "[1.2.0,1.3.0]"]
                 [org.clojure/algo.generic "0.1.0-SNAPSHOT"]]
  :dev-dependencies [[lein-multi "1.0.0"]]
  :multi-deps {"1.2" [[org.clojure/clojure "1.2.1"]
                      [org.clojure/algo.generic "0.1.0-SNAPSHOT"]]}
  :repositories {"sonatype" "http://oss.sonatype.org/content/repositories/snapshots"})
