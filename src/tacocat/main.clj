(ns tacocat.main
  (:require [tacocat.system :refer [init-system start!]]))

(defn -main [& args]
  (init-system)
  (start!))
