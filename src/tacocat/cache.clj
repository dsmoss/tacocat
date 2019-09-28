(ns tacocat.cache
  (:require [tacocat.log  :refer [log]]
            [tacocat.sql  :as    sql]
            [tacocat.util :refer :all]))

(def store (atom {}))

(defn with-cache
  "Returns the value of a cunction or returns the previously
  stored value from the cache"
  [context fun & funargs]
  (let [cache? (-> "use-cache"
                   sql/retrieve-app-data-val
                   bool-or-null)
        cached (get @store [context funargs])]
    (if (and cache? cached)
      (do
        ;(log "Hit:" context)
        cached)
      (do
        (log "Miss:" context)
        (let [res (apply fun funargs)]
          (do
            (if cache? (swap! store assoc [context funargs] res))
            res))))))
