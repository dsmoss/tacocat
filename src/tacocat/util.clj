(ns tacocat.util)

(defn int-or-null
  "Returns an int or nil"
  [n]
  (cond (integer? n)          n
        (empty? n)            nil
        (re-matches #"\d+" n) (Integer/parseInt n)
        :else                 nil))

(defn float-or-null
  "Returns a float or null"
  [n]
  ; 9, 9.9, 9., .9
  (if (re-matches #"(\.\d+)|(\d+(\.(\d+)?)?)" n)
    (Float/parseFloat n)
    nil))

(defn p-or-n-float-or-null
  "Returns a float or null"
  [n]
  ; 9, 9.9, 9., .9 + or -
  (if (re-matches #"([\+|\-]?)((\.\d+)|(\d+(\.(\d+)?)?))" n)
    (Float/parseFloat n)
    nil))

(defn bool-or-null
  "Returns a bool or nil"
  [b]
  (cond (boolean? b)                     b
        (empty? b)                       false
        (re-matches #"(true)|(false)" b) (Boolean/parseBoolean b)
        :else                            nil))

