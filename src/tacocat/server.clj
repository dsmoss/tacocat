(ns tacocat.server
  (:require [com.stuartsierra.component :as    component]
            [bidi.ring                  :refer [make-handler resources-maybe resources]]
            [aleph.http                 :as    http]
            [ring.util.response         :as    res]
            [ring.util.request          :as    req]
            [ring.middleware.params     :refer [wrap-params]]
            [tacocat.view               :as    view]
            [tacocat.store              :as    store]
            [tacocat.util               :refer :all]
            [tacocat.controller         :as    controller]))

(defn get-user
  [request]
  (-> request
      :remote-addr
      controller/find-logged-in-user))

(defmacro response
  "Returns the response form"
  [request fun]
  `(-> ~request
       :remote-addr
       controller/find-logged-in-user
       ~fun
       res/response))

(defn get-permissions
  "Gets the permissions to the logged-in user"
  [request]
  (-> request
      :remote-addr
      controller/find-logged-in-user
      :permissions))

(defmacro not-allowed
  "Return not allowed page"
  [request & permissions]
  `(response ~request (view/NOT-ALLOWED ~@permissions)))

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

(def handle-icons (resources-maybe {:prefix "ico/"}))
(def handle-css   (resources {:prefix "/css"}))
(def handle-fonts (resources {:prefix "/fonts"}))

(defn handle-index
  "Index Page"
  [request]
  ;(println request)
  (response request view/render-index))

(defn request-id
  "Get the id of a request"
  [request]
  (int-or-null (:id (:route-params request))))

(defn handle-bills
  "Display the bills page"
  [request]
  ;(println request)
  (let [{location "bill-location"} (:params request)]
    (with-check-permissions
      request "list-bills" view/render-bills
      {:trigger    "new-bill"
       :permission "create-new-bill"
       :action     (controller/add-bill
                     (get-user request) location)})))

(defn handle-single-bill
  "Shows data for a particular bill"
  [request]
  ;(println request)
  (let [params                           (:params request)
        {person          "set-person"
         id-item         "set-item"
         charge-override "charge-override"
         ebl             "edit-bill-location"
         location        "location"
         id-bill         "id-bill"
         new-item        "new-item"
         delete-item     "delete-bill-item"
         id-bill-item    "id-bill-item"} params
        filtered-options (filter 
                           #(re-matches #"\d+" (name (key %)))
                           params)]
    (with-check-permissions
      request "view-open-bill" (view/render-bill (request-id request))
      {:trigger    "edit-bill-location"
       :permission "change-bill-location"
       :action     (controller/edit-bill-location
                     (get-user request) ebl location)}
      {:trigger    "delete-bill-item"
       :permission "delete-bill-item"
       :action     (controller/delete-bill-item 
                     (get-user request) delete-item)}
      {:trigger    "new-item"
       :permission "create-bill-item"
       :action     (controller/add-item
                     (get-user request) id-bill new-item)}
      {:trigger    "set-item"
       :permission "change-bill-item"
       :action     (controller/set-item 
                     (get-user request) id-bill-item id-item)}
      {:trigger    "set-options"
       :permission "set-options-to-bill-item"
       :action     (controller/set-options 
                     (get-user request) id-bill-item filtered-options)}
      {:trigger    "charge-override"
       :permission "change-bill-item-price"
       :action     (controller/set-charge-override 
                     (get-user request) id-bill-item charge-override)}
      {:trigger    "set-person"
       :permission "assign-bill-item-to-person"
       :action     (controller/set-person 
                     (get-user request) id-bill-item person)})))

(defn handle-set-person
  "Changes the person a bill item is asigned to"
  [request]
  ;(println request)
  (with-check-permissions
    request "assign-bill-item-to-person"
    (view/render-set-person (request-id request))))

(defn handle-charge-override
  "Changes the charge for a bill item"
  [request]
  ;(println request)
  (with-check-permissions
    request "change-bill-item-price"
    (view/render-set-charge-override (request-id request))))

(defn handle-set-bill-item
  "Changes an item in the bill"
  [request]
  ;(println request)
  (with-check-permissions
    request "change-bill-item"
    (view/render-set-bill-item (request-id request))))

(defn handle-set-bill-item-options
  "Changes the options for a bill item"
  [request]
  ;(println request)
  (with-check-permissions
    request "set-options-to-bill-item"
    (view/render-set-bill-item-options (request-id request))))

(defn handle-delete-bill-item
  "Shows the page to delete an item from the bill"
  [request]
  ;(println request)
  (with-check-permissions
    request "delete-bill-item"
    (view/render-delete-bill-item (request-id request))))

(defn handle-add-item
  "Adds an item to the bill"
  [request]
  ;(println request)
  (with-check-permissions
    request "create-bill-item"
    (view/render-add-item (request-id request))))

(defn handle-charge-bill
  "Closes a bill"
  [request]
  ;(println request)
  (with-check-permissions
    request "charge-bill"
    (view/render-charge-bill (request-id request))))

(defn handle-new-expense
  "Show the expense addition page"
  [request]
  ;(println request)
  (with-check-permissions
    request "add-expense" view/render-new-expense))

(defn handle-new-bill
  "Add a new bill to the system"
  [request]
  ;(println request)
  (with-check-permissions
    request "create-new-bill" view/render-new-bill))

(defn handle-old-bills
  "Show closed bills"
  [request]
  ;(println request)
  (with-check-permissions
    request "list-closed-bills" view/render-old-bills))

(defn handle-closed-bill
  "Show closed bills"
  [request]
  ;(println request)
  (let [{charge   "bill-charge"
         id-bill  "bill-id"}    (:params request)]
    (with-check-permissions
      request "view-closed-bill"
      (view/render-closed-bill (request-id request))
      {:trigger    "bill-charge"
       :permission "charge-bill"
       :action     (controller/charge-bill
                     (get-user request) id-bill charge)})))

(defn handle-accts
  "Show the accounts page"
  [request]
  ;(println request)
  (let [{concept "concept"
         amount  "amount"} (:params request)]
    (with-check-permissions
      request "view-accounts" view/render-accts
      {:trigger    "add-expense"
       :permission "add-expense"
       :action     (controller/add-expense
                     (get-user request) concept amount)})))

(defn handle-edit-bill-location
  "Show the edit bill location"
  [request]
  ;(println request)
  (with-check-permissions
    request "change-bill-location"
    (view/render-edit-bill-location (request-id request))))

(defn handle-close-acct
  "Show the close account page"
  [request]
  ;(println request)
  (with-check-permissions
    request "close-accounts" view/render-close-acct))

(defn handle-previous-closes
  "Shows the previous closes page"
  [request]
  ;(println request)
  (with-check-permissions
    request "list-closed-accounts" view/render-previous-closes
    {:trigger    "make-close"
     :permission "close-accounts"
     :action     (controller/make-close (get-user request))}))

(defn handle-single-close
  "Shows a single close item"
  [request]
  ;(println request)
  (with-check-permissions
    request "view-closed-account"
    (view/render-close (request-id request))))

(defn handle-services
  "Shows the services screen"
  [request]
  ;(println request)
  (let [{concept "concept"
         amount  "amount"} (:params request)]
    (with-check-permissions
      request "list-services" view/render-services
      {:trigger    "add-service-charge"
       :permission "add-services-expense"
       :action     (controller/add-service-charge
                     (get-user request) concept amount)})))

(defn handle-closed-services
  "Shows the services screen"
  [request]
  ;(println request)
  (with-check-permissions
    request "list-closed-services" view/render-closed-services))

(defn handle-services-for-close
  "Shows services linked to a past close"
  [request]
  ;(println request)
  (with-check-permissions
    request "view-closed-services"
    (view/render-services-for-close (request-id request))))

(defn handle-add-services-expense
  "Shows the form to add a services expense"
  [request]
  ;(println request)
  (with-check-permissions
    request "add-services-expense" view/render-add-services-expense))

(defn handle-admin-options
  "Shows the admin options page"
  [request]
  ;(println request)
  (with-check-permissions
    request "view-app-values" view/render-app-options
    {:trigger    "make-admin-changes"
     :permission "change-app-values"
     :action     (controller/change-app-options
                   (get-user request) (:params request))}))

(defn handle-admin
  "Shows the admin page"
  [request]
  ;(println request)
  (response request view/render-admin))

(defn handle-login
  "Shows the login page"
  [request]
  ;(println request)
  (response request view/render-login))

(defn handle-user-info
  "Shows the logged-in user info or an error page"
  [request]
  ;(println request)
  ; :params {set-user-roles , 1 true, 3 true, 5 true, :id 1}
  (let [params                 (:params request)
        {user-name "user-name"
         c-user    "change-user-name"
         uname     "name"
         password  "password"
         language  "language"} params]
    ; First log in, so that info is accurate
    (if (contains? params "perform-login")
      (controller/perform-login
        user-name password (:remote-addr request)))
    (let [roles          (filter 
                           #(re-matches #"\d+"
                                        (name (key %)))
                           params)
          id             (:id params)
          logged-in-user (controller/get-logged-in-user
                           (:remote-addr request))
          permissions    (get-permissions request)]
      (if (or (empty? id) (= (int-or-null id)
                             (:id logged-in-user)))
        (do
          (if (contains? params "set-user-language")
            (controller/change-user-language
              (get-user request) (:id logged-in-user) language))
          (if (contains? params "change-user-password")
            (controller/change-user-password
              (get-user request) (:id logged-in-user) password))
          (if (contains? params "change-user-name")
            (controller/change-user-name
              (get-user request) (:id logged-in-user) uname))
          (if (and (contains? params "set-user-roles")
                   (contains? permissions "assign-user-roles"))
            (controller/assign-user-roles
              (get-user request) (:id logged-in-user) roles))
          (response request 
                    (view/render-user-info
                      (:id logged-in-user))))
        (with-check-permissions request "view-other-users"
          (view/render-user-info id)
          {:trigger    "set-user-language"
           :permission "change-other-users-language"
           :action     (controller/change-user-language
                         (get-user request) id language)}
          {:trigger    "change-user-name"
           :permission "change-other-users-name"
           :action     (controller/change-user-name 
                         (get-user request) id uname)}
          {:trigger    "set-user-roles"
           :permission "assign-user-roles"
           :action     (controller/assign-user-roles 
                         (get-user request) id roles)}
          {:trigger    "change-user-password"
           :permission "change-other-users-password"
           :action     (controller/change-user-password 
                         (get-user request) id password)})))))

(defn handle-list-users
  "Shows a page with all the registered users and related actions"
  [request]
  ;(println request)
  (let [{username   "username"
         uname      "name"
         password   "password"
         delete     "delete-user"
         id-user-ch "change-user-enabled"
         enabled    "enabled"}            (:params request)]
    (with-check-permissions
      request "list-users" view/render-list-users
      {:trigger    "change-user-enabled"
       :permission "change-user-enabled"
       :action     (controller/change-user-enabled
                     (get-user request) id-user-ch enabled)}
      {:trigger    "delete-user"
       :permission "delete-user"
       :action     (controller/delete-user 
                     (get-user request) delete)}
      {:trigger    "add-user"
       :permission "add-user"
       :action     (controller/add-new-user 
                     (get-user request) username uname password)})))

(defn handle-list-roles
  "Shows a page with all roles"
  [request]
  ;(println request)
  (let [params                  (-> request :params)
        {role    "role"
         rm-role "delete-role"} params]
    (with-check-permissions
      request "list-roles" view/render-list-roles
      {:trigger    "delete-role"
       :permission "delete-roles"
       :action     (controller/delete-role 
                     (get-user request) rm-role)}
      {:trigger    "add-role"
       :permission "add-user-roles"
       :action     (controller/add-new-role 
                     (get-user request) role)})))

(defn handle-list-items
  "Shows the items page"
  [request]
  ;(println request)
  ; Let form weird because data is like: {change-in-stock 31, 31 true}
  (let [{change-in-stock "change-in-stock"
         set-true?       change-in-stock
         item-name       "item-name"
         menu-group      "menu-group"
         amount          "amount"
         menu-group-name "menu-group-name"
         delete-item     "delete-item"} (:params request)]
    (with-check-permissions request "list-items"
      view/render-list-items
      {:trigger    "add-new-menu-group"
       :permission "add-new-menu-group"
       :action     (controller/add-new-menu-group 
                     (get-user request) menu-group-name)}
      {:trigger    "add-new-item"
       :permission "add-new-item"
       :action     (controller/add-new-item 
                     (get-user request) item-name menu-group amount)}
      {:trigger    "delete-item"
       :permission "delete-item"
       :action     (controller/delete-item 
                     (get-user request) delete-item)}
      {:trigger    "change-in-stock"
       :permission "change-stock-status"
       :action     (controller/change-stock-status 
                     (get-user request) change-in-stock set-true?)})))

(defn handle-add-new-user
  "Shows the user registration page"
  [request]
  (with-check-permissions request "add-user" view/render-add-user))

(defn handle-change-password
  "Changes the password for a user"
  [request]
  ; If user id == id -> change login
  ; otherwise check permissions
  (let [params (:params request)
        id     (:id params)]
    ; User can change own password
    (if (= (int-or-null id)  ; Current user id == request id
           (-> request :remote-addr controller/get-logged-in-user :id))
      (response request (view/render-change-user-password id))
      (with-check-permissions request "change-other-users-password"
        (view/render-change-user-password id)))))

(defn handle-change-user-language
  "Change the language of the user"
  [request]
  ; If user id == id -> change
  ; otherwise check permissions
  (let [params (:params request)
        id     (:id params)]
    ; User can change own lang
    (if (= (int-or-null id)  ; Current user id == request id
           (-> request :remote-addr controller/get-logged-in-user :id))
      (response request (view/render-change-user-language id))
      (with-check-permissions request "change-other-users-language"
        (view/render-change-user-language id)))))

(defn handle-change-user-name
  "Change full name of user"
  [request]
  (let [r-id (-> request :params :id int-or-null)
        u-id (-> request :remote-addr controller/get-logged-in-user :id)]
    (if (= r-id u-id) ; Current user id == request id
      (response request (view/render-change-user-name u-id))
      (with-check-permissions request "change-other-users-name"
        (view/render-change-user-name r-id)))))

(defn handle-change-user-roles
  "Show the role assignment page"
  [request]
  (with-check-permissions request "assign-user-roles"
    (view/render-change-user-roles (-> request :params :id))))

(defn handle-delete-user
  "Shows the deete user screen"
  [request]
  (with-check-permissions request "delete-user"
    (view/render-delete-user (-> request :params :id))))

(defn handle-view-role
  "Shows a roles info"
  [request]
  ;(println requet)
  (let [params                         (-> request :params)
        id                             (-> params :id)
        {perm-id   "change-permission"
         set-true? perm-id}            params]
    (with-check-permissions request "view-role"
      (view/render-view-role id)
      {:permission "assign-role-permissions"
       :trigger    "change-permission"
       :action     (controller/set-role-permission 
                     (get-user request) id perm-id set-true?)})))

(defn handle-add-new-role
  "Get the role addition page"
  [request]
  (with-check-permissions request "add-user-roles"
    view/render-add-new-role))

(defn handle-delete-role
  "Gets the delete role page"
  [request]
  (with-check-permissions request "delete-roles"
    (view/render-delete-role (-> request :params :id))))

(defn handle-add-new-item
  "Gets the item addition page"
  [request]
  (with-check-permissions request "add-new-item"
    view/render-add-new-item))

(defn handle-add-new-menu-group
  "Gets the menu group addition page"
  [request]
  (with-check-permissions request "add-new-menu-group"
    view/render-add-new-menu-group))

(defn handle-view-item
  "Shows the item screen"
  [request]
  ;(println request)
  (let [params                                 (-> request :params)
        id                                     (-> params :id)
        {charge      "amount"
         menu-group  "menu-group"
         in-stock?   "in-stock"
         iname       "name"
         option      "set-option-in-stock"
         add-op      "add-option-to-item"
         rm-op       "remove-option-from-item"
         o-in-stock? "set-option"
         og-name     "new-option-group"
         o-name      "new-option"
         o-group     "option-group"}          params]
    (with-check-permissions request "view-item" 
      (view/render-view-item id)
      {:trigger    "add-new-option-group"
       :permission "create-new-option-group"
       :action     (controller/add-option-group 
                     (get-user request) og-name)}
      {:trigger    "add-new-option"
       :permission "create-new-option"
       :action     (controller/add-option 
                     (get-user request) id o-name o-group)}
      {:trigger    "set-option-in-stock"
       :permission "set-option-in-stock"
       :action     (controller/set-option-in-stock 
                     (get-user request) option o-in-stock?)}
      {:trigger    "add-option-to-item"
       :permission "add-option-to-item"
       :action     (controller/add-option-to-item 
                     (get-user request) id add-op)}
      {:trigger    "remove-option-from-item"
       :permission "remove-option-from-item"
       :action     (controller/remove-option-from-item 
                     (get-user request) id rm-op)}
      {:trigger    "set-item-name"
       :permission "set-item-name"
       :action     (controller/change-item-name 
                     (get-user request) id iname)}
      {:trigger    "set-item-in-stock"
       :permission "change-stock-status"
       :action     (controller/change-stock-status 
                     (get-user request) id in-stock?)}
      {:trigger    "set-item-menu-group"
       :permission "set-item-menu-group"
       :action     (controller/set-item-menu-group 
                     (get-user request) id menu-group)}
      {:trigger    "set-item-charge"
       :permission "set-item-charge"
       :action     (controller/set-item-charge 
                     (get-user request) id charge)})))

(defn handle-new-option-group
  "Gets the new option group creation page"
  [request]
  (with-check-permissions request "create-new-option-group"
    (view/render-new-option-group (-> request :params :id))))

(defn handle-new-option
  "Creates a new option"
  [request]
  (with-check-permissions request "create-new-option"
    (view/render-new-option (-> request :params :id))))

(defn handle-view-option
  "View option page"
  [request]
  (let [params                           (-> request :params)
        id                               (-> params :id)
        {oname        "option-name"
         option-group "option-group"
         charge       "option-charge"
         in-stock?    "option-in-stock"} params]
    (with-check-permissions request "view-option"
      (view/render-view-option id)
      {:trigger    "set-option-name"
       :permission "set-option-name"
       :action     (controller/set-option-name 
                     (get-user request) id oname)}
      {:trigger    "set-option-group"
       :permission "set-option-group"
       :action     (controller/set-option-group 
                     (get-user request) id option-group)}
      {:trigger    "set-option-charge"
       :permission "set-option-charge"
       :action     (controller/set-option-charge 
                     (get-user request) id charge)}
      {:trigger    "set-option-in-stock"
       :permission "set-option-in-stock"
       :action     (controller/set-option-in-stock 
                     (get-user request) id in-stock?)})))

(defn handle-change-item-menu-group
  "Change an items menu group"
  [request]
  (with-check-permissions request "set-item-menu-group"
    (view/render-change-item-menu-group (-> request :params :id))))

(defn handle-change-item-charge
  "Change base price of an item"
  [request]
  (with-check-permissions request "set-item-charge"
    (view/render-change-item-charge (-> request :params :id))))

(defn handle-delete-item
  [request]
  (with-check-permissions request "delete-item"
    (view/render-delete-item (-> request :params :id))))

(defn handle-print-bill
  "Especial page suitable for printing"
  [request]
  (with-check-permissions request "print-bill"
    (view/render-print-bill (-> request :params :id))))

(defn handle-log
  "Shows the app log"
  [request]
  (with-check-permissions request "view-log" view/render-log))

(defn handle-intl
  "Shows the internationalisation screen"
  [request]
  ;(println request)
  ; {translate btn-view-translations,
  ;  lang-from es,
  ;  lang-to   en,
  ;  value     some}
  (let [{lang-to   "lang-to"
         lang-from "lang-from"
         key-name  "translate"
         value     "value"} (-> request :params)]
    (with-check-permissions request "can-translate"
      (view/render-intl lang-from lang-to)
      {:trigger    "translate"
       :permission "can-translate"
       :action     (controller/set-translation
                     (get-user request) key-name lang-to value)})))

(def id [#"\d+" :id])

(def handler
  "Get the handler function for our routes."
  (make-handler
    ["/"
     [[""                              handle-icons]                 ; done
      ["css"                           handle-css]                   ; done
      ["fonts"                         handle-fonts]                 ; done
      [""                              handle-index]                 ; done
      ["admin"                     {"" handle-admin}]                ; done
      ["admin-options"             {"" handle-admin-options}]        ; done
      ["list-users"                {"" handle-list-users}]           ; done
      ["add-new-user"              {"" handle-add-new-user}]         ; done
      ["list-roles"                {"" handle-list-roles}]           ; done
      ["list-items"                {"" handle-list-items}]           ; done
      ["add-new-item"              {"" handle-add-new-item}]         ; done
      ["add-new-menu-group"        {"" handle-add-new-menu-group}]   ; done
      [["view-item/"               id] handle-view-item]             ; done
      [["create-new-option-group/" id] handle-new-option-group]      ; done
      [["create-new-option/"       id] handle-new-option]            ; done
      [["view-option/"             id] handle-view-option]           ; done
      [["change-item-menu-group/"  id] handle-change-item-menu-group]; done
      [["change-item-charge/"      id] handle-change-item-charge]    ; done
      [["delete-item/"             id] handle-delete-item]           ; done
      ["login"                     {"" handle-login}]                ; done
      ["user-info"                 {"" handle-user-info              ; done
       ["/"                        id] handle-user-info}]            ; done
      [["change-user-name/"        id] handle-change-user-name]      ; done
      [["change-password/"         id] handle-change-password]       ; done
      [["change-user-roles/"       id] handle-change-user-roles]     ; done
      [["change-user-language/"    id] handle-change-user-language]
      [["view-role/"               id] handle-view-role]             ; done
      ["add-new-role"              {"" handle-add-new-role}]         ; done
      [["delete-role/"             id] handle-delete-role]           ; done
      [["delete-user/"             id] handle-delete-user]           ; done
      ["bills"                     {"" handle-bills}]                ; done
      [["edit-location/"           id] handle-edit-bill-location]    ; done
      ["new-bill"                  {"" handle-new-bill}]             ; done
      [["bill/"                    id] handle-single-bill]           ; done
      [["set-person/"              id] handle-set-person]            ; done
      [["set-bill-item/"           id] handle-set-bill-item]         ; done
      [["set-bill-item-options/"   id] handle-set-bill-item-options] ; done
      [["delete-bill-item/"        id] handle-delete-bill-item]      ; done
      [["set-charge-override/"     id] handle-charge-override]       ; done
      [["add-item/"                id] handle-add-item]              ; done
      [["charge-bill/"             id] handle-charge-bill]           ; done
      ["add-expense"               {"" handle-new-expense}]          ; done
      ["old-bills"                 {"" handle-old-bills}]            ; done
      [["closed-bill/"             id] handle-closed-bill]           ; done
      [["print-bill/"              id] handle-print-bill]            ; done
      [["close/"                   id] handle-single-close]          ; done
      ["close-acct"                {"" handle-close-acct}]           ; done
      ["previous-closes"           {"" handle-previous-closes}]      ; done
      ["closed-services"           {"" handle-closed-services}]      ; done
      [["services-for-close/"      id] handle-services-for-close]    ; done
      ["add-services-expense"      {"" handle-add-services-expense}] ; done
      ["services"                  {"" handle-services}]             ; done
      ["accts"                     {"" handle-accts}]                ; done
      ["log"                       {"" handle-log}]
      ["intl"                      {"" handle-intl}]
      [true (fn [req] {:status 404 :body "404 not found"})]]]))

(defn app
  [store]
  (-> handler
      wrap-params))

(defrecord HttpServer [server]

  component/Lifecycle

  (start [this]
    (assoc this :server (http/start-server (app (:store this)) {:port 8080})))

  (stop [this]
    (dissoc this :server)))

(defn make-server
  []
  (map->HttpServer {}))
