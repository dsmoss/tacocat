(defproject tacocat "0.2.0"
  :description  "Cuentas y Comandas"
  :url          "http://example.com/FIXME"
  :license      {:name "Eclipse Public License"
                 :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :main         tacocat.main
  :dependencies [[org.clojure/clojure        "1.10.1"]
                 [org.clojure/java.jdbc      "0.7.9"]
                 [org.postgresql/postgresql  "42.2.6"]
                 [com.stuartsierra/component "0.3.2"]
                 [clojure.java-time          "0.3.2"]
                 [ring                       "1.7.0"]
                 [aleph                      "0.4.6"]
                 [hiccup                     "1.0.5"]
                 [bidi                       "2.1.6"]])
