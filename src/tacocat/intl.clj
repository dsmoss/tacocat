(ns tacocat.intl
  (:require [tacocat.sql :refer [retrieve-internationalised-string]]
            [tacocat.str-var :refer [translate-vars]]))

(defn get-string
  "Gets a string in a specific language from the db"
  ([k m lang]
   (if (empty? k)
     k
     (translate-vars
       (retrieve-internationalised-string k lang)
       m)))
  ([k m]
   (get-string k m nil))
  ([k]
   (get-string k {})))
