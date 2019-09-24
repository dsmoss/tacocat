(ns tacocat.log)

(defmacro log
  "Logs a message to stdout"
  [& stuff]
  `(println (-> (java.util.Date.) str)
            (-> (Throwable.) .getStackTrace first .getClassName)
            ~@stuff))
