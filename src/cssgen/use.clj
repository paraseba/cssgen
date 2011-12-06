(ns cssgen.use)

(defmacro css-ns [ns-name & forms]
  `(ns ~ns-name
     ~'(:refer-clojure :exclude [+ - * /])
     ~'(:use cssgen cssgen.types
             [clojure.algo.generic.arithmetic :only (+ - * /)])
     ~@forms))
