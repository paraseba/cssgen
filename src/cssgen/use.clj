(ns cssgen.use)

(defmacro css-ns [ns-name & forms]
  `(ns ~ns-name
     ~'(:refer-clojure :exclude [+ - * /])
     ~'(:use
         cssgen cssgen.types
         [clojure.contrib.generic.arithmetic :only (+ - * /)])
     ~@forms))
