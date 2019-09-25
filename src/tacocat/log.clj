(ns tacocat.log)

(defmacro log
  "Logs a message to stdout"
  [& stuff]
  `(println [(-> (java.text.SimpleDateFormat. "yy-MM-dd@HH:mm:ss")
                 (.format (java.util.Date.)))
             (-> (Throwable.) .getStackTrace first .getClassName)]
            ~@stuff))
