(ns tacocat.intl
  (:require [tacocat.sql :refer [retrieve-internationalised-string]]))

(defn extract-name
  "Make a keyword out of an embedded variable"
  [s]
  (let [[_ r] (re-matches #"%[\{]?([\w\d+_-]+)[\}]?" s)]
    (keyword r)))

(defn translate-vars
  "Takes a string and a map and replaces incidences of %var
   and %{var} for (:var m)"
  [s m]
  (let [[_ b v a
         :as x] (re-matches
                  #"(.*)(%(?:[\w\d+_-]+|\{[\w\d+_-]+\}))(.*)" s)]
    (if x
      (str (translate-vars b m)
           ((extract-name v) m)
           (translate-vars a m))
      s)))

(defn get-string
  "Gets a string in a specific language from the db"
  ([k lang m]
   (translate-vars
     (retrieve-internationalised-string k lang)
     m))
  ([k lang]
   (get-string k lang {})))
