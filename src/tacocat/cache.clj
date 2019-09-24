(ns tacocat.cache
  (:require [tacocat.log :refer [log]]))

(def store (atom {}))

(defn with-cache
  "Returns the value of a cunction or returns the previously
  stored value from the cache"
  [context fun & funargs]
  (let [cached (get @store [context funargs])]
    (if cached
      (do
        (log "From cache" context)
        cached)
      (do
        (log "Into cache" context funargs)
        (let [res (apply fun funargs)]
          (do
            (swap! store assoc [context funargs] res)
            res))))))
