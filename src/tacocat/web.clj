(ns tacocat.web)

(defn handler [request]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "<h1>Hello World</h1>"})
