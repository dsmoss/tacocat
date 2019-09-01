(ns tacocat.str-var)

(defn extract-name
  "Make a keyword out of an embedded variable"
  [s]
  (let [[_ r] (re-matches #"%[\{]?([\w\d+_-]+)[\}]?" s)]
    (keyword r)))

(defn translate-vars
  "Takes a string and a map and replaces incidences of %var
   and %{var} for (:var m)"
  [s m]
  (if (empty? m)
    s
    (let [[_ b v a
           :as x] (re-matches
                    #"(.*)(%(?:[\w\d+_-]+|\{[\w\d+_-]+\}))(.*)" s)]
      (if x
        (str (translate-vars b m)
             ((extract-name v) m)
             (translate-vars a m))
        s))))

