(ns tacocat.main
  (:gen-class)
  (:require [tacocat.system :refer [init-system start!]]))

(defn -main [& args]
  (init-system)
  (start!))
