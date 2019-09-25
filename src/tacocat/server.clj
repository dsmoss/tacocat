(ns tacocat.server
  (:gen-class)
  (:require [tacocat.server.util        :refer :all]
            [com.stuartsierra.component :as    component]
            [ring.middleware.params     :refer [wrap-params]]
            [ring.middleware.multipart-params
                                        :refer [wrap-multipart-params]]
            [aleph.http                 :as    http]
            [ring.util.response         :as    res]
            [bidi.ring                  :refer [make-handler]]
            [ring.util.request          :as    req]
            [tacocat.view               :as    view]
            [tacocat.log                :refer [log]]
            [tacocat.util               :refer :all]
            [tacocat.controller         :as    controller]
            [tacocat.intl               :refer [get-string]]))

(defn handle-index
  "Index Page"
  [request]
  ;(log request)
  (response request view/render-index))

(defn handle-bills
  "Display the bills page"
  [request]
  ;(log request)
  (let [{location   "bill-location"
         merge-into "merge-location"
         merge-bill "merge-bill"} (:params request)

        user                       (get-user request)]
    (with-check-permissions
      request "list-bills" view/render-bills
      {:trigger    "merge-bill"
       :permission "merge-bill"
       :action     (controller/merge-bills
                     user merge-bill merge-into)}
      {:trigger    "add-new-bill-from-menu"
       :permission "create-new-bill"
       :action     (controller/create-populated-bill
                     user (-> request :params))}
      {:trigger    "new-bill"
       :permission "create-new-bill"
       :action     (controller/add-bill user location)})))

(defn handle-single-bill
  "Shows data for a particular bill"
  [request]
  ;(log request)
  (let [params                           (:params request)
        {person          "set-person"
         id-item         "set-item"
         charge-override "charge-override"
         ebl             "edit-bill-location"
         location        "location"
         id-bill         "id-bill"
         new-item        "new-item"
         delete-item     "delete-bill-item"
         id-bill-item    "id-bill-item"
         cp-bill-item    "id-item"} params
        filtered-options (filter 
                           #(re-matches #"\d+" (name (key %)))
                           params)]
    (with-check-permissions
      request "view-open-bill" (view/render-bill (request-id request))
      {:trigger    "replicate-bill-item"
       :permission "create-bill-item"
       :action     (controller/copy-bill-item
                     (get-user request) cp-bill-item)}
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
  ;(log request)
  (with-check-permissions
    request "assign-bill-item-to-person"
    (view/render-set-person (request-id request))))

(defn handle-charge-override
  "Changes the charge for a bill item"
  [request]
  ;(log request)
  (with-check-permissions
    request "change-bill-item-price"
    (view/render-set-charge-override (request-id request))))

(defn handle-set-bill-item
  "Changes an item in the bill"
  [request]
  ;(log request)
  (with-check-permissions
    request "change-bill-item"
    (view/render-set-bill-item (request-id request))))

(defn handle-set-bill-item-options
  "Changes the options for a bill item"
  [request]
  ;(log request)
  (with-check-permissions
    request "set-options-to-bill-item"
    (view/render-set-bill-item-options (request-id request))))

(defn handle-delete-bill-item
  "Shows the page to delete an item from the bill"
  [request]
  ;(log request)
  (with-check-permissions
    request "delete-bill-item"
    (view/render-delete-bill-item (request-id request))))

(defn handle-add-item
  "Adds an item to the bill"
  [request]
  ;(log request)
  (with-check-permissions
    request "create-bill-item"
    (view/render-add-item (request-id request))))

(defn handle-charge-bill
  "Closes a bill"
  [request]
  ;(log request)
  (with-check-permissions
    request "charge-bill"
    (view/render-charge-bill (request-id request))))

(defn handle-new-expense
  "Show the expense addition page"
  [request]
  ;(log request)
  (with-check-permissions
    request "add-expense" view/render-new-expense))

(defn handle-new-bill
  "Add a new bill to the system"
  [request]
  ;(log request)
  (with-check-permissions
    request "create-new-bill" view/render-new-bill))

(defn handle-old-bills
  "Show closed bills"
  [request]
  ;(log request)
  (with-check-permissions
    request "list-closed-bills" view/render-old-bills))

(defn handle-closed-bill
  "Show closed bills"
  [request]
  ;(log request)
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
  ;(log request)
  (let [{concept "concept"
         image   "image"
         amount  "amount"} (:params request)]
    (with-check-permissions
      request "view-accounts" view/render-accts
      {:trigger    "add-expense"
       :permission "add-expense"
       :action     (controller/add-expense
                     (get-user request) concept amount image)})))

(defn handle-edit-bill-location
  "Show the edit bill location"
  [request]
  ;(log request)
  (with-check-permissions
    request "change-bill-location"
    (view/render-edit-bill-location (request-id request))))

(defn handle-close-acct
  "Show the close account page"
  [request]
  ;(log request)
  (with-check-permissions
    request "close-accounts" view/render-close-acct))

(defn handle-previous-closes
  "Shows the previous closes page"
  [request]
  ;(log request)
  (with-check-permissions
    request "list-closed-accounts" view/render-previous-closes
    {:trigger    "make-close"
     :permission "close-accounts"
     ; Use system language
     :action     (controller/make-close
                   (get-user request)
                   (get-string "str-register" {})
                   (get-string "str-close/number" {}))}))

(defn handle-single-close
  "Shows a single close item"
  [request]
  ;(log request)
  (with-check-permissions
    request "view-closed-account"
    (view/render-close (request-id request))))

(defn handle-services
  "Shows the services screen"
  [request]
  ;(log request)
  (let [{concept "concept"
         image   "image"
         amount  "amount"} (:params request)]
    (with-check-permissions
      request "list-services" view/render-services
      {:trigger    "add-service-charge"
       :permission "add-services-expense"
       :action     (controller/add-service-charge
                     (get-user request) concept amount image)})))

(defn handle-closed-services
  "Shows the services screen"
  [request]
  ;(log request)
  (with-check-permissions
    request "list-closed-services" view/render-closed-services))

(defn handle-services-for-close
  "Shows services linked to a past close"
  [request]
  ;(log request)
  (with-check-permissions
    request "view-closed-services"
    (view/render-services-for-close (request-id request))))

(defn handle-add-services-expense
  "Shows the form to add a services expense"
  [request]
  ;(log request)
  (with-check-permissions
    request "add-services-expense" view/render-add-services-expense))

(defn handle-admin-options
  "Shows the admin options page"
  [request]
  ;(log request)
  (with-check-permissions
    request "view-app-values" view/render-app-options
    {:trigger    "make-admin-changes"
     :permission "change-app-values"
     :action     (controller/change-app-options
                   (get-user request) (:params request))}))

(defn handle-admin
  "Shows the admin page"
  [request]
  ;(log request)
  (response request view/render-admin))

(defn handle-login
  "Shows the login page"
  [request]
  ;(log request)
  (response request view/render-login))

(defn handle-user-info
  "Shows the logged-in user info or an error page"
  [request]
  ;(log request)
  ; :params {set-user-roles , 1 true, 3 true, 5 true, :id 1}
  (let [params                 (:params request)
        {user-name "user-name"
         c-user    "change-user-name"
         uname     "name"
         image     "image"
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
          (if (contains? params "set-user-picture")
            (controller/change-user-picture
              (get-user request) (:id logged-in-user) image))
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
          {:trigger    "set-user-picture"
           :permission "change-other-users-picture"
           :action     (controller/change-user-picture
                         (get-user request) id image)}
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
  ;(log request)
  (let [{username   "username"
         user-img   "img"
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
                     (get-user request)
                     username uname password user-img)})))

(defn handle-list-roles
  "Shows a page with all roles"
  [request]
  ;(log request)
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
  ;(log request)
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
  ;(log requet)
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
  ;(log request)
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
  ;(log request)
  (let [{lang-to   "lang-to"
         lang-from "lang-from"
         key-name  "translate"
         ffilter   "filter"
         value     "value"} (-> request :params)]
    (with-check-permissions request "can-translate"
      (view/render-intl lang-from lang-to ffilter)
      {:trigger    "translate"
       :permission "can-translate"
       :action     (controller/set-translation
                     (get-user request) key-name lang-to value)})))

(defn handle-system
  "Shows the System page"
  [request]
  ;(log request)
  (response request view/render-system))

(defn handle-error-log
  "Shows the error log details"
  [request]
  (with-check-permissions request "view-error-log"
    (view/render-error-log (-> request :params :id))))

(defn handle-error-list
  "Shows list of all errors registered"
  [request]
  (with-check-permissions request "view-error-list"
    view/render-error-list))

(defn handle-404
  "404/Not Found page"
  [request]
  (-> request (response (view/render-404)) (res/status 404)))

(defn handle-debts
  "Shows the debts page"
  [request]
  ;(log request)
  (let [user                 (get-user request)
        {creditor "creditor"
         amount   "amount"
         image    "image"
         concept  "concept"} (-> request :params)]
    (with-check-permissions request "view-debts"
      view/render-debts
      {:trigger    "add-debt"
       :permission "add-debt"
       :action     (controller/add-debt user creditor amount)}
      {:trigger    "add-debt-payment"
       :permission "add-debt-payment"
       :action     (controller/add-debt-payment
                     user creditor amount concept image)}
      {:trigger    "add-creditor"
       :permission "add-creditor"
       :action     (controller/add-creditor user creditor)})))

(defn handle-add-creditor
  "Shows the add creditor form"
  [request]
  (with-check-permissions request "add-creditor"
    view/render-add-creditor))

(defn handle-add-debt
  "Shows the debt addition page"
  [request]
  (with-check-permissions request "add-debt"
    view/render-add-debt))

(defn handle-add-debt-payment
  "Shows the page to add a debt payment"
  [request]
  (with-check-permissions request "add-debt-payment"
    view/render-add-debt-payment))

(defn handle-debt-detail
  "Shows the details of a debt"
  [request]
  (with-check-permissions request "view-debt-detail"
    (view/render-debt-detaill (-> request :params :id))))

(defn handle-sales
  "Shows the sales breakdown for the current period"
  [request]
  (with-check-permissions request "view-sales" view/render-sales))

(defn handle-set-user-image
  "Shows the user image acquisition form"
  [request]
  (let [r-id (-> request :params :id int-or-null)
        u-id (-> request :remote-addr controller/get-logged-in-user :id)]
    (if (= r-id u-id) ; Current user id == request id
      (response request (view/render-set-user-image u-id))
      (with-check-permissions request "change-other-users-picture"
        (view/render-set-user-image r-id)))))

(defn handle-view-receipt
  "Shows a receipt image"
  [request]
  (let [user                  (get-user request)
        {id-exp "set-receipt"
         image  "image"}      (-> request :params)]
    (with-check-permissions request "view-receipt"
      (view/render-view-receipt (-> request :params :id))
      {:trigger    "set-receipt"
       :permission "set-receipt"
       :action     (controller/set-receipt user id-exp image)})))

(defn handle-view-services-receipt
  "Shows a receipt for services"
  [request]
  (let [user                  (get-user request)
        {id-srv "set-receipt"
         image  "image"}      (-> request :params)]
    (with-check-permissions request "view-services-receipt"
      (view/render-view-services-receipt (-> request :params :id))
      {:trigger    "set-receipt"
       :permission "set-services-receipt"
       :action     (controller/set-services-receipt
                     user id-srv image)})))

(defn handle-set-expense-receipt
  "Shows the form to upload a receipt"
  [request]
  (with-check-permissions request "set-receipt"
    (view/render-set-expense-receipt (-> request :params :id))))

(defn handle-set-services-receipt
  "Shows the services file upload page"
  [request]
  (with-check-permissions request "set-services-receipt"
    (view/render-set-services-receipt (-> request :params :id))))

(defn handle-menu
  [request]
  (response request view/render-menu))

(defn handle-merge
  "Merge two bills together"
  [request]
  (with-check-permissions request "merge-bill"
    (view/render-merge (-> request :params :id))))

(def handler
  "Get the handler function for our routes."
  (make-handler
    ["/"
     [[""                              handle-icons]
      ["css"                           handle-css]
      ["fonts"                         handle-fonts]
      [""                              handle-index]
      ["system"                        handle-system]
      ["admin"                         handle-admin]
      ["admin-options"                 handle-admin-options]
      ["list-users"                    handle-list-users]
      ["add-new-user"                  handle-add-new-user]
      ["list-roles"                    handle-list-roles]
      ["list-items"                    handle-list-items]
      ["add-new-item"                  handle-add-new-item]
      ["add-new-menu-group"            handle-add-new-menu-group]
      [["view-item/"               id] handle-view-item]
      [["create-new-option-group/" id] handle-new-option-group]
      [["create-new-option/"       id] handle-new-option]
      [["view-option/"             id] handle-view-option]
      [["change-item-menu-group/"  id] handle-change-item-menu-group]
      [["change-item-charge/"      id] handle-change-item-charge]
      [["delete-item/"             id] handle-delete-item]
      ["login"                         handle-login]
      ["user-info"                 {"" handle-user-info
       ["/"                        id] handle-user-info}]
      [["change-user-name/"        id] handle-change-user-name]
      [["change-password/"         id] handle-change-password]
      [["change-user-roles/"       id] handle-change-user-roles]
      [["change-user-language/"    id] handle-change-user-language]
      [["set-user-image/"          id] handle-set-user-image]
      [["view-role/"               id] handle-view-role]
      ["add-new-role"                  handle-add-new-role]
      [["delete-role/"             id] handle-delete-role]
      [["delete-user/"             id] handle-delete-user]
      ["bills"                         handle-bills]
      [["edit-location/"           id] handle-edit-bill-location]
      [["merge-bill/"              id] handle-merge]
      ["new-bill"                      handle-new-bill]
      [["bill/"                    id] handle-single-bill]
      [["set-person/"              id] handle-set-person]
      [["set-bill-item/"           id] handle-set-bill-item]
      [["set-bill-item-options/"   id] handle-set-bill-item-options]
      [["delete-bill-item/"        id] handle-delete-bill-item]
      [["set-charge-override/"     id] handle-charge-override]
      [["add-item/"                id] handle-add-item]
      [["charge-bill/"             id] handle-charge-bill]
      ["add-expense"                   handle-new-expense]
      [["set-expense-receipt/"     id] handle-set-expense-receipt]
      ["old-bills"                     handle-old-bills]
      [["closed-bill/"             id] handle-closed-bill]
      [["print-bill/"              id] handle-print-bill]
      [["close/"                   id] handle-single-close]
      ["close-acct"                    handle-close-acct]
      ["previous-closes"               handle-previous-closes]
      ["closed-services"               handle-closed-services]
      [["services-for-close/"      id] handle-services-for-close]
      ["add-services-expense"          handle-add-services-expense]
      ["services"                      handle-services]
      [["set-services-receipt/"    id] handle-set-services-receipt]
      [["view-services-receipt/"   id] handle-view-services-receipt]
      ["accts"                         handle-accts]
      [["view-receipt/"            id] handle-view-receipt]
      ["log"                           handle-log]
      ["intl"                          handle-intl]
      ["error-log"                 {"" handle-error-list
       ["/"                        id] handle-error-log}]
      ["debts"                         handle-debts]
      ["add-creditor"                  handle-add-creditor]
      ["add-debt-payment"              handle-add-debt-payment]
      ["add-debt"                      handle-add-debt]
      [["debt-detail/"             id] handle-debt-detail]
      ["sales"                         handle-sales]
      ["menu"                          handle-menu]
      [true                            handle-404]]])) 

(defn app
  [store]
  (-> handler
      wrap-params
      wrap-multipart-params
      wrap-exception-handling))

(defrecord HttpServer [server]

  component/Lifecycle

  (start [this]
    (assoc this :server (http/start-server
                          (app (:store this))
                          {:port 8080})))

  (stop [this]
    (dissoc this :server)))

(defn make-server
  []
  (map->HttpServer {}))
