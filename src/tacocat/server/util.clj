(ns tacocat.server.util
  (:gen-class)
  (:require [tacocat.util           :refer :all]
            [ring.util.response     :as    res]
            [bidi.ring              :refer [resources-maybe
                                            resources]]
            [tacocat.view           :as    view]
            [tacocat.controller     :as    controller]))

(def handle-icons (resources-maybe {:prefix "ico/"}))
(def handle-css   (resources {:prefix "/css"}))
(def handle-fonts (resources {:prefix "/fonts"}))

(defmacro not-allowed
  "Return not allowed page"
  [request & permissions]
  `(response ~request (view/NOT-ALLOWED ~@permissions)))

(defn get-user
  [request]
  (-> request
      :remote-addr
      controller/find-logged-in-user))

(defmacro response
  "Returns the response form"
  [request fun]
  `(-> ~request
       get-user
       ~fun
       res/response))

(defn get-permissions
  "Gets the permissions to the logged-in user"
  [request]
  (-> request
      :remote-addr
      controller/find-logged-in-user
      :permissions))

(defmacro with-check-permissions
  [request main-perm main-fn & other-checks]
  `(cond (not (contains? (get-permissions ~request) ~main-perm))
         (not-allowed ~request ~main-perm)
         ~@(interleave
             (map (fn [{t :trigger}]
                    `(contains? (:params ~request) ~t))
                  other-checks)
             (map (fn [{p :permission
                        a :action}]
                    `(if (contains? (get-permissions ~request) ~p)
                       (do
                         ~a
                         (response ~request ~main-fn))
                       (not-allowed ~request ~p)))
                  other-checks))
         :else (response ~request ~main-fn)))

(defn request-id
  "Get the id of a request"
  [request]
  (int-or-null (:id (:route-params request))))

(def id [#"\d+" :id])

(defn wrap-exception-handling
  "Makes a nicer looking error page"
  [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception e
        (let [eid (controller/log-exception (get-user request) e)
              msg (.getMessage e)]
          ;(println request)
          (-> (response request (view/render-exception msg eid))
              (res/status 500)))))))
