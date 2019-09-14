(ns tacocat.view
  (:gen-class)
  (:require [tacocat.view.util :refer :all]
            [hiccup.form       :as    form]
            [hiccup.core       :refer [html]]
            [base64-clj.core   :as    b64]
            [tacocat.util      :refer :all]
            [tacocat.sql       :as    sql]
            [tacocat.intl      :refer [get-string]]))

(defn render-index
  "Index page of the application"
  [user]
  (let [lang (if (empty? user)
               (sql/retrieve-app-data-val "default-language")
               (:language user))]
    (with-page (sql/retrieve-app-data-val "business-name")
      user
      [:home])))

(defn render-exception
  "Shows an exception"
  [user msg id]
  (let [lang (:language user)]
    (with-page (get-string "str-error-ocurred" {} lang)
      user
      [:error]
      (html
        [:h5 {:style "color: red;"} msg]
        (with-form-table nil nil
          [(make-link
             (str "/error-log/" id)
             (get-string "ln-error-logged/number"
                         {:number id} lang))])))))

(defn NOT-ALLOWED
  [user & permissions-required]
  (let [lang (if (empty? user)
               (sql/retrieve-app-data-val "default-language")
               (:language user))]
    (with-page (get-string "str-forbidden-action" {} lang)
      user
      [:error]
      (html
        [:h2 {:style "color: red;"}
         (get-string "str-insufficient-permissions" {} lang)]
        (if (not (empty? permissions-required))
          (with-table lang
            [:permissions]
            [(get-string "str-permissions" {} lang)]
            [(fn [p _] (map (fn [p]
                              (html
                                [:h6 (get-string
                                       (str "prm-" p) {} lang)]))
                            (sort p)))]
            [{:permissions permissions-required}]))
        (with-form-table nil nil
          [(make-link
             "/login"
             (get-string "ln-change-user" {} lang))])))))

(defn render-login
  "Show the login page"
  [user]
  (let [lang (if (empty? user)
               (sql/retrieve-app-data-val "default-language")
               (:language user))]
    (with-page (get-string "str-user-login" {} lang)
      user
      [:error]
      (html
        [:h5
         (with-form "/user-info"
           (form/hidden-field {:value true} "perform-login")
           (with-form-table [nil nil nil [2]] nil
             [(lbl-user-name "user-name" lang)
              (tf "user-name" (:user_name user))]
             [(lbl-password "password" lang)
              (form/password-field {:id "password"} "password")]
             [(btn-enter lang)]))]))))

(defn render-delete-user
  "Delete user page"
  [user id]
  (let [lang (:language user)
        u    (-> id int-or-null sql/retrieve-user-by-id)]
    (with-page (get-string "str-delete-user/name" u lang)
      user
      [:admin :list-users]
      (html
        [:h5
         (with-form "/list-users"
           (form/hidden-field {:value id} "delete-user")
           (with-form-table nil nil
             [(btn-delete lang)]))]))))

(defn render-change-user-name
  "Screen to change users full name"
  [user id]
  (let [u    (-> id int-or-null sql/retrieve-user-by-id)
        lang (:language user)]
    (with-page (get-string "str-change-user-name/name" u lang)
      user
      [:admin :list-users]
      (html
        [:h5
         (with-form (str "/user-info/" id)
           (form/hidden-field {:value id} "change-user-name")
           (with-form-table [nil nil [2]] nil
             [(lbl-full-name "name" lang)
              (tf "name" (:name u))]
             [(btn-change lang)]))]))))

(defn render-change-user-language
  "Language selection screen"
  [user id]
  (let [lang    (:language user)
        t-lang  (-> id
                    int-or-null
                    sql/retrieve-user-by-id
                    :language)
        langs (sql/retrieve-langs)]
    (with-page (get-string "str-lang" {} lang)
      user
      [:admin :list-users]
      (html
        [:h5
         (with-form (str "/user-info/" id)
           (form/hidden-field {:value id} "set-user-language")
           (with-form-table [nil nil [2]] nil
             [(lbl-lang "language" lang)
              (form/drop-down
                {:id "language"} "language"
                (map (fn [{n :name f :full_name}]
                       [(get-string f {} lang) n])
                     langs)
                t-lang)]
             [(btn-change lang)]))]))))

(defn render-set-user-image
  "Form where we set user image"
  [user id]
  (let [lang (:language user)]
    (with-page (get-string "ln-get-user-image" {} lang)
      user
      [:admin :list-users]
      (with-form (str "/user-info/" id)
        (form/hidden-field {:value id} "set-user-picture")
        (with-form-table nil nil
          [(finput "image")]
          [(btn-change lang)])))))

(defn render-user-info
  "Shows user info page or login error"
  ([user id]
   (let [lang (if (empty? user)
                (sql/retrieve-app-data-val "default-language")
                (:language user))]
     (if (empty? user)
       (with-page (get-string "str-error" {} lang)
         nil
         [:error]
         (html
           [:h2 {:style "color: red;"}
            (get-string "str-wrong-user-or-password" {} lang)]))
       (let [id              (int-or-null id)
             id              (if (nil? id) (:id user) id)
             {permissions :permissions
              :as         u} (sql/retrieve-user-by-id     id)
             user-roles      (sql/retrieve-roles-for-user id)
             machines        (sql/retrieve-logged-in-to   id)
             picture         (sql/retrieve-user-picture   id)]
         (with-page (get-string "str-user-info/name" u lang)
           user
           [:system]
           (html
             (with-form-table nil nil
               [(make-link
                  (str "/set-user-image/" id)
                  (if (nil? (:picture_filetype picture))
                    (get-string "ln-get-user-image" {} lang)
                    (html [:img
                           {:height "190px"
                            :src
                            (str "data:" (:picture_filetype picture)
                                 ";base64,"
                                 (-> picture
                                     :picture
                                     b64/encode-bytes
                                     String.))}])))

                (with-form-table nil
                  [[:h2 (get-string "str-username/user_name" u lang)]]
                  [(make-link (str "/change-user-name/"  id)
                              (get-string "ln-full-name/name" u lang))]
                  [(make-link (str "/change-password/"   id)
                              (get-string "ln-change-password" {} lang))]
                  [(make-link (str "/change-user-roles/" id)
                              (get-string "ln-change-roles" {} lang))]
                  [(make-link (str "/change-user-language/" id)
                              (get-string
                                "ln-change-language/language" u lang))])])
             (with-table lang
               [:roles      :permissions      :machines]
               ["str-roles" "str-permissions" "str-machines"]
               [(fn [r _] (map (fn [r]
                                 (make-link
                                   (str "/view-role/" (:id r))
                                   (:name r)))
                               r))
                (fn [p _] (map (fn [p]
                                 (html
                                   [:h6
                                    (get-string (str "prm-" p)
                                                {} lang)]))
                               (sort p)))
                (fn [m _] (map (fn [m] (html [:h5 m])) (sort m)))]
               [{:roles       user-roles
                 :machines    machines
                 :permissions permissions}])))))))
  ([user]
   (render-user-info user (:id user))))

(defn render-change-user-roles
  "Role selection and assignment page for a user"
  [user id]
  (let [id            (int-or-null id)
        id-user-roles (into #{}
                            (map :id
                                 (sql/retrieve-roles-for-user id)))
        all-roles     (sql/retrieve-all-roles)
        edit-user     (sql/retrieve-user-by-id id)
        lang          (:language user)]
    (with-page (get-string "str-roles-for/name" edit-user lang)
      user
      [:admin :list-users]
      (html
        [:h5
         (with-form (str "/user-info/" id)
           (form/hidden-field {:value true} "set-user-roles")
           (with-table lang
             [:name      :id]
             ["str-role" ""]
             [(fn [n r] (lbl n (:id r) lang))
              (fn [i _] (format-bool (contains? id-user-roles i) i))]
             all-roles)
           (with-form-table nil nil
             [(btn-change lang)]))]))))

(defn render-view-role
  "Role page"
  [user id]
  (let [id            (int-or-null id)
        {rname :name} (sql/retrieve-role-by-id id)
        all-perms     (sql/retrieve-all-permissions)
        role-perms    (sql/retrieve-permissions-for-role id)
        lang          (:language user)]
    (with-page rname
      user
      [:admin :list-roles]
      (with-table lang
        [:name            :id]
        ["str-permission" ""]
        [(fn [n r] 
           (html [:h5 (lbl (str "prm-" n) (:id r) lang)]))
         (fn [i r]
           (html
             [:h5
              (with-form (str "/view-role/" id)
                (form/hidden-field
                  {:value i} "change-permission")
                (with-form-table nil nil
                  [(format-bool (contains? role-perms (:name r)) i)
                   (btn-change lang)]))]))]
        all-perms))))

(defn render-add-new-role
  "Add role page"
  [user]
  (let [lang (:language user)]
    (with-page (get-string "str-add-role" {} lang)
      user
      [:admin :list-roles]
      (html
        [:h5
         (with-form "/list-roles"
           (form/hidden-field {:value true} "add-role")
           (with-form-table [nil nil [2]] nil
             [(lbl-role "role" lang)
              (tf "role")]
             [(btn-add lang)]))]))))

(defn render-delete-role
  "Delete role page"
  [user id]
  (let [lang (:language user)
        role (sql/retrieve-role-by-id (int-or-null id))]
    (with-page (get-string "str-delete-role/name" role lang)
    user
    [:admin :list-roles]
    (html
      [:h5
       (with-form "/list-roles"
         (form/hidden-field {:value id} "delete-role")
         (with-form-table nil nil
           [(btn-delete lang)]))]))))

(defn render-add-new-menu-group
  "New Menu Group page"
  [user]
  (let [lang (:language user)]
    (with-page (get-string "str-add-new-menu-group" {} lang)
      user
      [:admin :list-items]
      (html
        [:h5
         (with-form "/list-items"
           (form/hidden-field {:value true} "add-new-menu-group")
           (with-form-table [nil nil [2]] nil
             [(lbl-name "menu-group-name" lang)
              (tf "menu-group-name")]
             [(btn-add lang)]))]))))

(defn render-add-new-item
  "New item page"
  [user]
  (let [lang (:language user)]
    (with-page (get-string "str-add-item" {} lang)
      user
      [:admin :list-items]
      (html
        [:h5
         (with-form "/list-items"
           (form/hidden-field {:value true} "add-new-item")
           (with-form-table [nil nil nil nil [2]] nil
             [(lbl-item "item-name" lang)
              (tf "item-name")]
             [(lbl-group "menu-group" lang)
              (form/drop-down
                {:id "menu-group"} "menu-group"
                (map (fn [g] [g g])
                     (sort
                       (map :name
                            (sql/retrieve-menu-groups)))))]
             [(lbl-charge "amount" lang)
              (nf "amount")]
             [(btn-add lang)]))]))))

(defn render-view-item
  "Shows an items details"
  [user id]
  (let [{id        :id
         iname     :name
         mgroup    :menu_group
         amount    :charge
         in-stock? :in_stock} (sql/retrieve-item-by-id
                                (int-or-null id))
        menu-groups           (sql/retrieve-menu-groups)
        item-options          (into #{}
                                (map :id_option
                                  (sql/retrieve-valid-options id)))
        all-options           (sql/retrieve-all-options)
        lang                  (:language user)
        lt         (make-link-table
                     [{:destination (str "/create-new-option-group/"
                                         id)
                       :string "ln-create-new-option-group"}
                      {:destination (str "/create-new-option/" id)
                       :string "ln-create-new-option"}]
                     lang)]
    (with-page iname
      user
      [:admin :list-items]
      lt
      (html
        [:h5
         [:table {:style "border: 0; padding: 0;"}
          [:tr {:style "border: 0; padding: 0;"}
           (with-form (str "/view-item/" id)
             [:td {:style "border: 0; padding: 0;"} 
              (form/hidden-field {:value id} "set-item-name")
              (lbl-item "name" lang)]
             [:td {:style "border: 0; padding: 0;"}
              (tf "name" iname)]
             [:td {:style "border: 0; padding: 0;"}
              (btn-change lang)])]
         [:tr {:style "border: 0; padding: 0;"}
          (with-form (str "/view-item/" id)
            [:td {:style "border: 0; padding: 0;"}
             (form/hidden-field {:value id} "set-item-menu-group")
             (lbl-group "menu-group" lang)]
            [:td {:style "border: 0; padding: 0;"}
             (form/drop-down {:id "menu-group"} "menu-group"
                             (map (fn [g] [g g])
                                  (sort (map :name menu-groups)))
                             mgroup)]
            [:td {:style "border: 0; padding: 0;"}
             (btn-change lang)])]
         [:tr {:style "border: 0; padding: 0;"}
          (with-form (str "/view-item/" id)
            [:td {:style "border: 0; padding: 0;"}
             (form/hidden-field {:value id} "set-item-charge")
             (lbl-charge "amount" lang)]
            [:td {:style "border: 0; padding: 0;"}
             (nf "amount" amount)]
            [:td {:style "border: 0; padding: 0;"}
             (btn-change lang)])]
         [:tr {:style "border: 0; padding: 0;"}
          (with-form (str "/view-item/" id)
            [:td {:style "border: 0; padding: 0;"}
             (form/hidden-field {:value id} "set-item-in-stock")
             (lbl-in-stock "in-stock" lang)]
            [:td {:style "border: 0; padding: 0;"}
             (format-bool in-stock? "in-stock")]
            [:td {:style "border: 0; padding: 0;"}
             (btn-change lang)])]]
         
         [:h4 {:class "w3-container w3-card"}
          (get-string "str-item-options" {} lang)]
         (with-options-table lang id
           (filter (fn [{i :id}] (contains? item-options i))
                   all-options)
           (fn [i _]
             (with-form (str "/view-item/" id)
               (form/hidden-field {:value i} "remove-option-from-item")
               (btn-remove lang))))
         
         [:h4 {:class "w3-container w3-card"}
          (get-string "str-other-options" {} lang)]
         (with-options-table lang id
           (filter (fn [{i :id}]
                     (not (contains? item-options i)))
                   all-options)
           (fn [i _]
             (with-form (str "/view-item/" id)
               (form/hidden-field {:value i} "add-option-to-item")
               (btn-add lang))))]
        lt))))

(defn render-change-user-password
  "User password change screen"
  [user id]
  (let [u    (sql/retrieve-user-by-id (int-or-null id))
        lang (:language user)]
    (with-page (get-string "str-change-password/name" u lang)
      user
      [:system]
      (html
        [:h5
         (with-form (str "/user-info/" id)
           (form/hidden-field {:value true} "change-user-password")
           (with-form-table [nil nil [2]] nil
             [(lbl-new-password "password" lang)
              (form/password-field {:id "password"} "password")]
             [(btn-change lang)]))]))))

(defn render-system
  "Gets the system page"
  [user]
  (let [lang (if (empty? user)
               (sql/retrieve-app-data-val "default-language")
               (:language user))]
    (with-page (get-string "ln-system" {} lang)
      user
      [:system])))

(defn render-admin
  "Gets the admin page"
  [user]
  (let [lang (if (empty? user)
               (sql/retrieve-app-data-val "default-language")
               (:language user))]
    (with-page (get-string "ln-admin" {} lang)
      user
      [:admin])))

(defn render-intl
  "Renders the internationalisation page"
  [user lang-from lang-to ffilter]
  (let [lang   (:language user)
        langs  (sql/retrieve-langs)
        gr     (group-by :name langs)
        name-f (-> gr (get lang-from) first :full_name)
        name-t (-> gr (get lang-to)   first :full_name)]
    (with-page (get-string "str-internationalisation" {} lang)
      user
      [:system]
      (html
        (with-form "/intl"
          (with-form-table nil nil
            [(lbl-from "lang-from" lang)
             (form/drop-down
               {:id "lang-from"} "lang-from"
               (map (fn [{l :full_name n :name}]
                      [(get-string l {} lang) n])
                    langs)
               lang-from)]
            [(lbl-to "lang-to" lang)
             (form/drop-down
               {:id "lang-to"} "lang-to"
               (map (fn [{l :full_name n :name}]
                      [(get-string l {} lang) n])
                    langs)
               lang-to)]
            [(lbl-filter "filter" lang)
             (tf "filter" ffilter)])
          [:p (get-string "s-filter-instructions" {} lang)]
          (with-form-table nil nil
            [(btn-view-translations lang)]))
        (if (and (not (nil? lang-from))
                 (not (nil? lang-to))
                 (not (= lang-from lang-to)))
          (with-table lang
            [:key        :src_val :key          :dst_val]
            ["str-label" name-f   "str-default" name-t]
            [(fn [k _] (html [:small k]))
             (fn [v i] (html
                         [:small
                          (if (nil? v)
                            (sql/retrieve-internationalised-string
                              (:key i) lang-from)
                            v)]))
             (fn [k _] (html
                         [:small
                          (sql/retrieve-internationalised-string
                            k lang-to)]))
             (fn [v i] 
               (with-form "/intl"
                 (form/hidden-field {:value (:key i)}  "translate")
                 (form/hidden-field {:value ffilter}   "filter")
                 (form/hidden-field {:value lang-from} "lang-from")
                 (form/hidden-field {:value lang-to}   "lang-to")
                 (html
                   [:small
                    (with-form-table nil nil
                      [(tf "value" v)
                       (btn-change lang :span)])])))]
            (sql/retrieve-intl lang-from lang-to
                               (if (empty? ffilter)
                                 "%"
                                 ffilter))))))))

(defn render-app-options
  "Gets the app options page"
  [user]
  (let [lang (:language user)]
    (with-page (get-string "str-app-options" {} lang)
      user
      [:system]
      (html
        [:h5
         (with-form "/admin-options"
           (form/hidden-field {:value true} "make-admin-changes")
           (apply
             (partial
               with-form-table nil [(get-string "str-key" {} lang)
                                    (get-string "str-val" {} lang)])
             (map (fn [{k :key v :val :as o}]
                    [(lbl (str "dta-" k) k lang)
                     (get-app-option-control o)])
                  (sql/retrieve-app-data)))
           (with-form-table nil nil
             [(btn-change lang)]))]))))

(defn render-list-items
  "Gets the product page"
  [user]
  (let [lang (:language user)]
    (with-page (get-string "str-items" {} lang)
      user
      [:admin :list-items]
      (with-table lang
        [:name      :menu_group      :charge      :in_stock      :id]
        ["str-item" "str-menu-group" "str-charge" "str-in-stock" ""]
        [(fn [p i] (make-link
                     (str "/view-item/" (:id i)) p))
         (fn [g i] (make-link
                     (str "/change-item-menu-group/" (:id i)) g))
         (fn [m i] (make-link
                     (str "/change-item-charge/" (:id i))
                     (format-money m)))
         (fn [b i] (with-form "/list-items"
                     (form/hidden-field {:value (:id i)}
                                        "change-in-stock")
                     (with-form-table nil nil
                       [(format-bool b (:id i))
                        (btn-change lang)])))
         (fn [i _] (make-link (str "/delete-item/" i)
                              (get-string "btn-delete" {} lang)))]
        (sql/retrieve-all-items)))))

(defn render-services
  "Services screen"
  [user]
  (let [lang (:language user)]
    (with-page (get-string "ln-services" {} lang)
      user
      [:home :services]
      (make-services-table lang (sql/retrieve-current-services)))))

(defn render-closed-services
  "Services for prior closes"
  [user]
  (let [lang (:language user)]
    (with-page (get-string "ln-closed-services" {} lang)
      user
      [:home :services]
      (with-table lang
        [:date      :id]
        ["str-date" "str-close"]
        [(fn [d _] (format-date d))
         (fn [i _] (make-link (str "/services-for-close/" i)
                              (get-string "str-close/number"
                                          {:number i} lang)))]
        (sql/retrieve-previous-closes)))))

(defn render-services-for-close
  "Services page for a specific close"
  [user id]
  (let [lang (:language user)]
    (with-page (get-string "str-services-for-close/number"
                           {:number id} lang)
      user
      [:home :services]
      (make-services-table
        lang (sql/retrieve-services-for-close id)))))

(defn render-set-services-receipt
  "File upload for utilities receipt"
  [user id]
  (let [lang (:language user)]
    (with-page (get-string "str-upload-receipt" {} lang)
      user
      [:home :services]
      (with-form (str "/view-services-receipt/" id)
        (form/hidden-field {:value id} "set-receipt")
        (with-form-table nil nil
          [(finput "image")]
          [(btn-change lang)])))))

(defn render-view-services-receipt
  "Show a receipt"
  [user id]
  (let [lang                   (:language user)
        id                     (int-or-null id)
        {rc :receipt
         rf :receipt_filename
         rt :receipt_filetype
         cl :id_close}         (sql/retrieve-services-receipt id)
        editable?              (nil? cl)]
    (with-page (get-string "ln-view-receipt" {} lang)
      user
      [:home :services]
      (with-form-table nil nil
        [((if editable?
            (partial make-link (str "/set-services-receipt/" id))
            identity)
          (if (nil? rf)
            (get-string "ln-no-receipt-image" {} lang)
            (html
              [:img
               {:width "100%"
                :src (str "data:" rt
                          ";base64,"
                          (-> rc
                              b64/encode-bytes
                              String.))}])))]))))

(defn render-bills
  "Shows the bills page"
  [user]
  (let [lang (:language user)]
    (with-page (get-string "ln-bills" {} lang)
      user
      [:home :bills]
      (get-existing-bills user))))

(defn render-merge
  "Shows a form in which to merge bills"
  [user id]
  (let [lang  (:language user)
        id    (int-or-null id)
        bill  (sql/retrieve-bill id)
        bills (filter (fn [{i :id}] (not (= i id)))
                      (sql/retrieve-bills))]
    (with-page (get-string "str-merge/location" bill lang)
      user
      [:home :bills]
      (with-form "/bills"
        (form/hidden-field {:value id} "merge-bill")
        (html
          [:h5
           (with-form-table [nil nil [2]] nil
             [(lbl-location "merge-location" lang)
              (form/drop-down {:id "merge-location"} "merge-location"
                              (for [b bills]
                                [(:location b) (:id b)]))]
             [(btn-merge lang)])])))))

(defn render-previous-closes
  "Shows the previous closes"
  [user]
  (let [lang (:language user)]
    (with-page (get-string "str-closes" {} lang)
      user
      [:home :accts]
      (with-table lang
        [:date      :expense_amount :intake_amount :earnings     :id]
        ["str-date" "str-expenses"  "str-intake"   "str-earning" ""]
        [(fn [d _] (format-date d))
         (fn [m _] (format-money m))
         (fn [m _] (format-money m))
         (fn [m _] (format-money m))
         (fn [i _] (make-link (str "/close/" i)
                              (get-string "ln-view" {} lang)))]
        (sql/retrieve-previous-closes)))))

(defn render-close-acct
  "Shows the close current accounting period page"
  [user]
  (let [lang (:language user)]
    (with-page (get-string "str-close-of-accounting" {} lang)
      user
      [:home :accts]
      (with-form "/previous-closes"
        (form/hidden-field {:value true} "make-close")
        (html
          [:h5
           (with-form-table nil nil
             [(btn-close lang)])])))))

(defn render-add-services-expense
  "Makes a form to add a service expense"
  [user]
  (let [lang (:language user)]
    (with-page (get-string "str-add-services-charge" {} lang)
      user
      [:home :services]
      (with-form "/services"
        (form/hidden-field {:value true} "add-service-charge")
        (html
          [:h5
           (with-form-table [nil [2] nil nil [2]] nil
             [(finput "image")]
             [(lbl-concept "concept" lang)
              (tf "concept")]
             [(lbl-charge "amount" lang)
              (nf "amount")]
             [(btn-add lang)])])))))

(defn render-old-bills
  "Shows the closed bills page"
  [user]
  (with-page (get-string "str-closed-bills" {} (:language user))
    user
    [:home :bills]
    (get-closed-bills user)))

(defn get-bill-items
  "Returns a form with the bill items of a bill"
  [user id]
  (let [lang   (:language user)
        ln-fn  (fn [id options lang]
                (make-link
                  (str "/set-bill-item-options/" id)
                  (if (nil? options)
                    (get-string "ln-add-options" {} lang)
                    options)
                  :small))
        op-cnt #(sql/retrieve-bill-item-option-count %)]
    (html
      (with-table lang
        [:date      :person      :item      :charge      :id :id]
        ["str-time" "str-person" "str-item" "str-charge" ""  ""]
        [(fn [t _] (format-time t))
         (fn [p c] (make-link
                     (str "/set-person/" (:id c))
                     (if (nil? p)
                       (get-string "ln-assign" {} lang)
                       (get-string
                         "ln-person/number" {:number p} lang))))
         (fn [i c] (html
                     (make-link (str "/set-bill-item/" (:id c)) i)
                     (if (= 0 (op-cnt (:id c)))
                       nil
                       (ln-fn (:id c) (:options c) lang))))
         (fn [m c] (make-link (str "/set-charge-override/" (:id c))
                              (format-money m)))
         (fn [i _] (with-form (str "/bill/" id)
                     (form/hidden-field {:value id} "replicate-bill-item")
                     (form/hidden-field {:value i} "id-item")
                     (html [:h5 (btn-copy lang)])))
         (fn [i _] (make-link (str "/delete-bill-item/" i)
                              (get-string "btn-delete" {} lang)))]
        (sql/retrieve-bill-items id)))))

(defn render-delete-bill-item
  "Returns a form where a bill_item can be deleted"
  [user id]
  (let [{person  :person
         item    :item
         options :options
         id-bill :id_bill} (sql/retrieve-bill-item id)
        lang               (:language user)]
    (with-page (get-string "str-delete/name" {:name item} lang)
      user
      [:home :bills]
      (html
        [:h5
         (with-form (str "/bill/" id-bill)
           (form/hidden-field {:value id} "delete-bill-item")
           (with-form-table nil nil
             [(btn-delete lang)]))]))))

(defn render-edit-bill-location
  "Returns the edit location for a bill form"
  [user id]
  (let [location (:location (sql/retrieve-bill id))
        lang     (:language user)]
    (with-page (get-string "str-change-location" {} lang)
      user
      [:home :bills]
      (with-form (str "/bill/" id)
        (form/hidden-field {:value id} "edit-bill-location")
        (html
          [:h5
           (with-form-table [nil nil [2]] nil
             [(lbl-location "location" lang)
              (tf "location" location)]
             [(btn-change lang)])])))))

(defn get-old-bill-items
  "Returns a form with the bill items of a closed bill"
  ([user id tag sep]
   (with-table (:language user)
     [:date  :person  :item      :charge]
     [""     "str-p#" "str-item" "str-charge"]
     [(fn [t _] (format-time t tag))
      (fn [p _] (html [tag [:center (if (nil? p) "-" p)]]))
      (fn [i c] (html
                  [tag i " " 
                   (let [options (:options c)]
                     (if (nil? options)
                       nil
                       [sep [:small (:options c)]]))]))
      (fn [m c] (format-money m "" tag))]
     (sql/retrieve-bill-items id)))
  ([user id tag]
   (get-old-bill-items user id tag tag))
  ([user id]
   (get-old-bill-items user id :h5)))

(defn render-new-expense
  "Renders a new expense form"
  [user]
  (let [lang (:language user)]
    (with-page (get-string "str-add-expense" {} lang)
      user
      [:home :accts]
      (with-form "/accts"
        (form/hidden-field {:value true} "add-expense")
        (html
          [:h5
           (with-form-table [nil [2] nil nil [2]] nil
             [(finput "image")]
             [(lbl-concept "concept" lang)
              (tf "concept")]
             [(lbl-charge "amount" lang)
              (nf "amount")]
             [(btn-add lang)])])))))

(defn render-closed-bill
  "Renders a single closed bill"
  [user id]
  (let [{date     :date
         location :location
         charge   :charge}  (sql/retrieve-bill id)
        lang                (:language user)
        lt                  (make-link-table
                              [{:destination (str "/print-bill/" id)
                                :string      "ln-print"}]
                              lang)]
    (with-page location
      user
      [:home :bills]
      (html
        lt
        [:p date]
        (format-money charge (get-string "fmt-total" {} lang) :h2)
        (get-old-bill-items user id)
        (with-table lang
          [:person      :charge]
          ["str-person" "str-charge"]
          [(fn [p _] (html
                       [:h5 (if (nil? p)
                              "-"
                              (get-string
                                "str-person/number"
                                {:number p} lang))]))
           (fn [m _] (format-money m))]
          (sql/retrieve-bill-charges-per-person id))
        lt))))

(defn render-print-bill
  [user id]
  (let [id                  (int-or-null id)
        {date     :date
         location :location
         charge   :charge}  (sql/retrieve-bill id)
        ; Printing always happens on system language
        lang                (sql/retrieve-app-data-val
                              "default-language")]
    (with-printing-page location id
      (html
        (get-old-bill-items {:language lang} id :p :span)
        [:header {:class "w3-container w3-card"}
         [:center (format-money
                    charge (get-string "fmt-total" {} lang) :h2)]]
        (let [per-person (sql/retrieve-bill-charges-per-person id)]
          (if (= 1 (count per-person))
            nil
            (with-table lang
              [:person      :charge]
              ["str-person" "str-charge"]
              [(fn [p _] (html
                           [:p (if (nil? p)
                                 "-"
                                 (get-string "str-person/number"
                                             {:number p} lang))]))
               (fn [m _] (format-money m "" :p))]
              per-person)))
        (html [:p date])))))

(defn render-bill
  "Renders a single bill"
  [user id]
  (let [{date     :date
         location :location
         charge   :charge}  (sql/retrieve-bill id)
        mult                (float-or-null
                              (sql/retrieve-app-data-val
                                "card-multiplicative"))
        lang                (:language user)
        lt                  (make-link-table
                              [{:destination (str "/add-item/" id)
                                :string      "ln-add-to-bill"}
                               {:destination (str "/charge-bill/" id)
                                :string      "ln-charge"}]
                              lang)]
    (with-page location
      user
      [:home :bills]
      (html
        lt
        [:p date]
        (format-money charge
                      (get-string "fmt-total" {} lang) :h2)
        (format-money (* mult charge)
                      (get-string "str-card-payment" {} lang))
        (get-bill-items user id)
        lt))))

(defn render-set-person
  "Renders the page where we set the person for a bill item"
  [user id]
  (let [{person  :person
         item    :item
         options :options
         id-bill :id_bill} (sql/retrieve-bill-item id)
        lang               (:language user)]
    (with-page (get-string "str-assign-item/item/options"
                           {:item    item
                            :options (make-option-string options)}
                           lang)
      user
      [:home :bills]
      (with-form (str "/bill/" id-bill)
        (form/hidden-field {:value id} "id-bill-item")
        (html
          [:h5
           (with-form-table [nil nil [2]] nil
             [(lbl-person "set-person" lang)
              (form/drop-down {:id "set-person"}
                              "set-person"
                              (map (fn [n]
                                     [(if (= "null" n)
                                        (get-string "str-nobody" {} lang)
                                        (get-string
                                          "str-person/number" {:number n} lang))
                                      n])
                                   (conj (range 1 50) "null"))
                              (if (nil? person)
                                "null"
                                person))]
             [(btn-change lang)])])))))

(defn render-set-bill-item
  "Renders the page where we set the a bill item and its options"
  [user id]
  (let [{person  :person
         item    :item
         options :options
         id-bill :id_bill
         item-id :id_item} (sql/retrieve-bill-item id)
        item-group         (:menu_group
                             (sql/retrieve-item-by-id item-id))
        items              (sql/retrieve-items-in-stock-in-group
                             item-group)
        lang               (:language user)]
    (with-page (get-string "str-change-item/item/options"
                           {:item    item
                            :options (make-option-string options)}
                           lang)
      user
      [:home :bills]
      (with-form (str "/bill/" id-bill)
        (form/hidden-field {:value id} "id-bill-item")
        (html
          [:h5
           (with-form-table [nil nil [2]] nil
             [(lbl-item "set-item" lang)
              (form/drop-down "set-item"
                              (map (fn [{id :id nm :name}] [nm id])
                                   items)
                              item-id)]
             [(btn-change lang)])])))))

(defn render-set-bill-item-options
  "Renders the bill item optiions page"
  [user id]
  (let [{person  :person
         item    :item
         options :options
         id-bill :id_bill
         item-id :id_item} (sql/retrieve-bill-item id)
        valid-options      (sql/retrieve-valid-options-in-stock
                             item-id)
        current-options    (set
                             (flatten
                               (map vals
                                 (sql/retrieve-current-options id))))
        lang               (:language user)]
    (with-page (get-string "str-options-for-item/item/options"
                          {:item    item
                           :options (make-option-string options)}
                          lang)
      user
      [:home :bills]
      (with-form (str "/bill/" id-bill)
        (form/hidden-field {:value id} "id-bill-item")
        (form/hidden-field {:value true} "set-options")
        (html
          [:h5
           (let [innerfn (fn [{n :option_name
                               i :id_option}]
                           (html
                             [:h5
                              (with-form-table nil nil
                                [(form/label {:for i}
                                             n (str n "&nbsp;"))
                                 (format-bool
                                   (contains? current-options i) i)])]))
                 oo (into (sorted-map)
                          (map (fn [i]
                                 {(key i) (map innerfn (val i))})
                               (group-by :option_group
                                         valid-options)))
                 option-groups (keys oo)]
             (with-table lang
               option-groups
               option-groups
               (repeat (count oo) (fn [k _] k))
               [oo]))
           (with-form-table nil nil
             [(btn-change lang)])])))))

(defn render-set-charge-override
  "Displays a page where we can select a change
   to the charge for a bill item"
  [user id]
  (let [{person  :person
         item    :item
         options :options
         id-bill :id_bill
         item-id :id_item
         charge  :charge} (sql/retrieve-bill-item id)
        lang              (:language user)]
    (with-page (get-string "str-charge-for/item/options"
                          {:item    item
                           :options (make-option-string options)}
                          lang)
      user
      [:home :bills]
      (with-form (str "/bill/" id-bill)
        (form/hidden-field {:value id} "id-bill-item")
        (html
          [:h5
           (with-form-table nil nil
             [(nf "charge-override" charge)
              (btn-change lang)])])))))

(defn render-add-item
  "Displays a page where we add an item to a bill"
  [user id]
  (let [lang (:language user)]
    (with-page (get-string "ln-add-to-bill" {} lang)
      user
      [:home :bills]
      (html
        [:h5
         [:table {:style "border: 0; padding: 0;"}
          (for [m (sort (group-by :menu_group
                                  (sql/retrieve-items-in-stock)))]
              [:tr {:style "border: 0; padding: 0;"}
               (with-form (str "/bill/" id)
                 [:td {:style "border: 0; padding: 0;"}
                  (lbl (str (key m) ":") "new-item" lang)]
                 [:td {:style "border: 0; padding: 0;"}
                  (form/hidden-field {:value id} "id-bill")
                  (form/drop-down
                    "new-item"
                    (sort
                      (for [{id :id nm :name} (val m)]
                        [nm id])))]
                 [:td {:style "border: 0; padding: 0;"}
                  (btn-add lang)])])]]))))

(defn render-new-bill
  "Shows a page where we can create a new bill"
  [user]
  (let [lang (:language user)]
    (with-page (get-string "ln-new-bill" {} lang)
      user
      [:home :bills]
      (with-form "/bills"
        (form/hidden-field {:value true} "new-bill")
        (html
          [:h5
           (with-form-table [nil nil [2]] nil
             [(lbl-location "bill-location" lang)
              (tf "bill-location")]
             [(btn-create lang)])])))))

(defn render-charge-bill
  "Displays a page where a bill is paid"
  [user id]
  (let [{date     :date
         location :location
         charge   :charge}  (sql/retrieve-bill id)
        lang                (:language user)]
    (with-page (get-string "str-charge-for/location"
                           {:location location} lang)
      user
      [:home :bills]
      (html
        [:p date]
        (with-form (str "/closed-bill/" id)
          (form/hidden-field {:value charge} "bill-charge")
          (form/hidden-field {:value id}     "bill-id")
          (format-money charge (get-string "fmt-total" {} lang) :h2)
          [:h5
           (with-form-table nil nil
             [(btn-charge lang)])])))))

(defn render-set-expense-receipt
  "Form to upload a receipt for an expense"
  [user id]
  (let [lang (:language user)]
    (with-page (get-string "str-upload-receipt" {} lang)
      user
      [:home :accts]
      (with-form (str "/view-receipt/" id)
        (form/hidden-field {:value id} "set-receipt")
        (with-form-table nil nil
          [(finput "image")]
          [(btn-change lang)])))))

(defn render-view-receipt
  "Show a receipt"
  [user id]
  (let [lang                   (:language user)
        id                     (int-or-null id)
        {rc :receipt
         rf :receipt_filename
         rt :receipt_filetype
         cl :id_close}         (sql/retrieve-receipt id)
        editable?              (nil? cl)]
    (with-page (get-string "ln-view-receipt" {} lang)
      user
      [:home :accts]
      (with-form-table nil nil
        [((if editable?
            (partial make-link (str "/set-expense-receipt/" id))
            identity)
          (if (nil? rf)
            (get-string "ln-no-receipt-image" {} lang)
            (html
              [:img
               {:width "100%"
                :src (str "data:" rt
                          ";base64,"
                          (-> rc
                              b64/encode-bytes
                              String.))}])))]))))

(defn render-accts
  "Displays the accounts page"
  [user]
  (let [lang                (:language user)
        {total    :total
         expenses :expenses
         intakes  :intakes} (sql/retrieve-current-accounting-totals)]
    (with-page (get-string "ln-accts" {} lang)
      user
      [:home :accts]
      (format-money total (get-string "fmt-total" {} lang) :h2)
      (format-money expenses (get-string "fmt-expenses" {} lang))
      (format-money intakes (get-string "fmt-intakes" {} lang))
      (with-table lang
        [:date      :concept      :amount      :receipt_filename]
        ["str-date" "str-concept" "str-amount" ""]
        [(fn [d _] (format-date d))
         (fn [c _] (html [:h5 c]))
         (fn [m _] (format-money m))
         (fn [f a]
           (cond
             (and (nil? f) (nil? (:id_expense a)))
               (make-link
                 (str "/closed-bill/" (:id_intake a))
                 (get-string "ln-view" {} lang))
             (nil? f)
               (make-link
                 (str "/set-expense-receipt/" (:id_expense a))
                 (get-string "ln-no-receipt-image" {} lang))
             :else
               (make-link
                 (str "/view-receipt/" (:id_expense a))
                 (get-string "ln-view-receipt" {} lang))))]
        (sql/retrieve-current-accounting)))))

(defn render-list-users
  "Page listing all registered users"
  [user]
  (let [lang (:language user)]
    (with-page (get-string "str-registered-users" {} lang)
      user
      [:admin :list-users]
      (let
        [make-role-string (fn [roles]
                            (apply str
                              (interpose ", "
                                (sort
                                  (map :name roles)))))]
        (with-table lang
          [:user_name :name           :id
           :id         :enabled       :id]
          ["str-user" "str-full-name" "str-password"
           "str-roles" "str-enabled"  ""]
          [(fn [u _] (html [:h5 u]))
           (fn [n u] (make-link (str "/user-info/" (:id u)) n))
           (fn [i _] (make-link (str "/change-password/"   i)
                                (get-string
                                  "ln-change-password" {} lang)))
           (fn [i _]
             (make-link (str "/change-user-roles/" i)
                        (let [rs (make-role-string
                                   (sql/retrieve-roles-for-user i))]
                          (if (empty? rs)
                            (get-string "ln-assign" {} lang)
                            rs))))
           (fn [b u] (with-form "/list-users"
                       (form/hidden-field {:value (:id u)}
                                          "change-user-enabled")
                       (with-form-table nil nil
                         [(format-bool b "enabled")
                          (html [:h5 (btn-change lang)])])))
           (fn [i _] (make-link (str "/delete-user/" i)
                               (get-string "btn-delete" {} lang)))]
          (sql/retrieve-registered-users))))))

(defn render-list-roles
  "Gets the list roles page"
  [user]
  (let [lang (:language user)]
    (with-page (get-string "str-registerd-roles" {} lang)
      user
      [:admin :list-roles]
      (with-table lang
        [:name      :id               :id]
        ["str-role" "str-permissions" ""]
        [(fn [n r] (make-link (str "/view-role/" (:id r)) n))
         (fn [i _] (map (fn [p]
                          (html
                            [:h6
                             (get-string (str "prm-" p) {} lang)]))
                     (sort (sql/retrieve-permissions-for-role i))))
         (fn [i _] (make-link (str "/delete-role/" i)
                              (get-string "btn-delete" {} lang)))]
        (sql/retrieve-all-roles)))))

(defn render-new-option-group
  "Page to add an option group"
  [user id]
  (let [lang (:language user)]
    (with-page (get-string "str-new-option-group" {} lang)
      user
      [:admin :list-items]
      (html
        [:h5
         (with-form (str "/view-item/" id)
           (form/hidden-field {:value true} "add-new-option-group")
           (with-form-table [nil nil [2]] nil
             [(lbl-new-group "new-option-group" lang)
              (tf "new-option-group")]
             [(btn-create lang)]))]))))

(defn render-new-option
  "Page to add an option"
  [user id]
  (let [lang (:language user)]
    (with-page (get-string "str-new-option" {} lang)
      user
      [:admin :list-items]
      (html
        [:h5
         (with-form (str "/view-item/" id)
           (form/hidden-field {:value true} "add-new-option")
           (with-form-table [nil nil nil [2]] nil
             [(lbl-new-option "new-option" lang)
              (tf "new-option")]
             [(lbl-group "option-group" lang)
              (form/drop-down
                {:id "option-group"} "option-group"
                (map (fn [g] [g g])
                     (sort
                       (map :name
                            (sql/retrieve-option-groups)))))]
             [(btn-create lang)]))]))))

(defn render-view-option
  "Shows an option"
  [user id]
  (let [{oname        :name
         option-group :option_group
         extra-charge :extra_charge
         in-stock?    :in_stock} (sql/retrieve-option-by-id
                                   (int-or-null id))
        all-option-groups        (sql/retrieve-all-option-groups)
        post-page                (str "/view-option/" id)
        lang                     (:language user)]
    (with-page (str option-group "/" oname)
      user
      [:admin :list-items]
      (html
        [:h5
         (with-form post-page
           (form/hidden-field {:value true} "set-option-name")
           (with-form-table nil nil
             [(lbl-option "option-name" lang)
              (tf "option-name" oname)
              (btn-change lang)]))
         (with-form post-page
           (form/hidden-field {:value true} "set-option-group")
           (with-form-table nil nil
             [(lbl-group "option-group" lang)
              (form/drop-down {:id "option-group"} "option-group"
                              (sort
                                (map :name
                                     all-option-groups))
                              option-group)
              (btn-change lang)]))
         (with-form post-page
           (form/hidden-field {:value true} "set-option-charge")
           (with-form-table nil nil
             [(lbl-extra-charge "option-charge" lang)
              (nf "option-charge" extra-charge)
              (btn-change lang)]))
         (with-form post-page
           (form/hidden-field {:value true} "set-option-in-stock")
           (with-form-table nil nil
             [(lbl-in-stock "option-in-stock" lang)
              (format-bool in-stock? "option-in-stock")
              (btn-change lang)]))]))))

(defn render-change-item-menu-group
  "Page to change an items menu group"
  [user id]
  (let [id                  (int-or-null id)
        {iname :name
         group :menu_group} (sql/retrieve-item-by-id id)
        all-groups          (map :name (sql/retrieve-menu-groups))
        lang                (:language user)]
    (with-page iname
      user
      [:admin :list-items]
      (html
        [:h5
         (with-form (str "/view-item/" id)
           (form/hidden-field {:value id} "set-item-menu-group")
           (with-form-table [nil nil [2]] nil
             [(lbl-group "menu-group" lang)
              (form/drop-down {:id "menu-group"} "menu-group"
                              (map (fn [g] [g g]) (sort all-groups))
                              group)]
             [(btn-change lang)]))]))))

(defn render-change-item-charge
  "Page to set price of an item"
  [user id]
  (let [id               (int-or-null id)
        {iname :name
         charge :charge} (sql/retrieve-item-by-id id)
        lang             (:language user)]
    (with-page iname
      user
      [:admin :list-items]
      (html
        [:h5
         (with-form (str "/view-item/" id)
           (form/hidden-field {:value id} "set-item-charge")
           (with-form-table [nil nil [2]] nil
             [(lbl-charge "amount" lang)
              (nf "amount" charge)]
             [(btn-change lang)]))]))))

(defn render-delete-item
  "Page to confirm item deletion"
  [user id]
  (let [id            (int-or-null id)
        {iname :name} (sql/retrieve-item-by-id id)
        lang          (:language user)]
    (with-page (get-string "str-delete/name" {:name iname} lang)
      user
      [:admin :list-items]
      (html
        [:h5 (get-string "str-wrn-on-item-delete" {} lang)
         (with-form "/list-items"
           (form/hidden-field {:value id} "delete-item")
           (with-form-table nil nil
             [(btn-delete lang)]))]))))

(defn render-add-user
  "User registration page"
  [user]
  (let [lang (:language user)]
    (with-page (get-string "str-register-user" {} lang)
      user
      [:admin :list-users]
      (html
        [:h5
         (with-form "/list-users"
           (form/hidden-field {:value true} "add-user")
           (with-form-table [nil [2] nil nil nil [2]] nil
             [(finput "img")]
             [(lbl-user-name "username" lang)
              (tf "username")]
             [(lbl-full-name "name" lang)
              (tf "name")]
             [(lbl-password "password" lang)
              (form/password-field {:id "password"} "password")]
             [(btn-create lang)]))]))))

(defn render-close
  "Displays a close"
  [user id]
  (let [lang (:language user)
        {date         :date
         expenses     :expense_amount
         intakes      :intake_amount
         total        :earnings
         business     :business_share
         partners     :partners_share
         services     :services_share
         partner-take :partner_take} (sql/retrieve-single-close id)]
    (with-page (get-string "str-close/number" {:number id} lang)
      user
      [:home :accts]
      (html
        [:span
         [:p date]
         (format-money total
                       (get-string "fmt-total" {} lang) :h2)
         (format-money expenses
                       (get-string "fmt-expenses" {} lang))
         (format-money intakes
                       (get-string "fmt-intakes" {} lang))
         (format-money business
                       (get-string "fmt-business" {} lang))
         (format-money services
                       (get-string "fmt-services" {} lang))
         (format-money partners
                       (get-string "fmt-partners" {} lang))
         (format-money partner-take
                       (get-string "fmt-per-partner" {} lang))
         (with-table lang
           [:date      :concept      :amount      :receipt_filename]
           ["str-date" "str-concept" "str-amount" ""]
           [(fn [d _] (format-date d))
            (fn [c _] (html [:h5 c]))
            (fn [m _] (format-money m))
            (fn [f a]
             (cond
               (and (nil? f) (nil? (:id_expense a)))
                 (make-link
                   (str "/closed-bill/" (:id_intake a))
                   (get-string "ln-view" {} lang))
               (nil? f)
                 (html
                   [:h5 (get-string "ln-no-receipt-image" {} lang)])
               :else
                 (make-link
                   (str "/view-receipt/" (:id_expense a))
                   (get-string "ln-view-receipt" {} lang))))]
           (sql/retrieve-closed-accounting id))]))))

(defn render-log
  "Log page"
  [user]
  (let [lang (:language user)]
    (with-page (get-string "ln-log" {} lang)
      user
      [:system]
      (html
        [:p (get-string "str-entries-limit" {} lang)]
        (with-table lang
          [:date      :id_app_user  :action      :details]
          ["str-date" "str-user"    "str-action" "str-detail"]
          [(fn [d _] (html
                       [:small (format-full-date d :p)]))
           (fn [i l] (html
                       [:small (let [u (sql/retrieve-user-by-id i)]
                                 (make-link (str "/user-info/" i)
                                            (:user_name u) :p))]))
           (fn [a _] (html [:small a]))
           (fn [d _] (html [:small d]))]
          (sql/retrieve-log))))))

(defn render-error-list
  "Generate the error list"
  [user]
  (let [lang (:language user)]
    (with-page (get-string "ln-error-list" {} lang)
       user
      [:system]
      (html
        [:p (get-string "str-entries-limit" {} lang)]
        (with-table lang
          [:date      :error_type      :id_cause      :id]
          ["str-date" "str-error-type" "str-cause" ""]
          [(fn [d _] (html [:small (format-full-date d :p)]))
           (fn [t _] (html [:small t]))
           (fn [c _] (if c
                       (make-link (str "/error-log/" c)
                                  (get-string "ln-error-logged/number"
                                              {:number c} lang)
                                  :small)
                       (html [:small (get-string "str-none" {} lang)])))
           (fn [i _] (make-link (str "/error-log/" i)
                                (get-string "ln-error-logged/number"
                                            {:number i} lang)
                                :small))]
          (sql/retrieve-error-log))))))

(defn render-error-log
  "Show page with error log details"
  [user id]
  (let [lang (:language user)
        {ty :error_type
         st :stack_trace
         ms :message
         dt :date
         ca :id_cause}   (sql/retrieve-error-log-by-id
                           (int-or-null id))]
    (with-page (get-string "ln-error-logged/number"
                           {:number id} lang)
      user
      [:system]
      (html
        (with-form-table [nil nil nil nil nil [2]] nil
          [[:h5 {:class "right"}
            (get-string "str-error-type" {} lang) "&nbsp;"]
           [:h5 {:class "left"} ty]]
          [[:h5 {:class "right"}
            (get-string "str-message" {} lang) "&nbsp;"]
           [:h5 {:class "left"} ms]]
          [[:h5 {:class "right"}
            (get-string "str-date" {} lang) "&nbsp;"]
           [:h5 {:class "left"} dt]]
          [[:h5 {:class "right"}
            (get-string "str-cause" {} lang) "&nbsp;"]
           [:h5 {:class "left"}
            (if ca
              (with-form-table nil nil
                [(make-link (str "/error-log/" ca)
                            (get-string "ln-error-logged/number"
                                        {:number ca} lang))])
              (get-string "str-none" {} lang))]]
          [[:pre st]])))))

(defn render-debts
  "Render the debts summary page"
  [user]
  (let [lang (:language user)]
    (with-page (get-string "ln-debts" {} lang)
      user
      [:home :debts]
      (with-table lang
        [:creditor      :amount      :id_creditor]
        ["str-creditor" "str-amount" ""]
        [(fn [c _] (html [:h5 c]))
         (fn [a _] (format-money a))
         (fn [i _]
           (make-link (str "/debt-detail/" i)
                      (get-string "ln-debt-detail" {} lang)))]
        (sql/retrieve-debt-summary)))))

(defn render-add-creditor
  "Shows the creditor addition screen"
  [user]
  (let [lang (:language user)]
    (with-page (get-string "ln-add-creditor" {} lang)
      user
      [:home :debts]
      (with-form "/debts"
        (form/hidden-field {:value true} "add-creditor")
        (with-form-table [nil nil [2]] nil
          [(lbl-name "creditor" lang)
           (tf "creditor")]
          [(btn-add lang)])))))

(defn render-add-debt
  "Shows the add debt form"
  [user]
  (let [lang (:language user)]
    (with-page (get-string "ln-add-debt" {} lang)
      user
      [:home :debts]
      (with-form "/debts"
        (form/hidden-field {:value true} "add-debt")
        (with-form-table [nil nil nil [2]] nil
          [(lbl-creditor "creditor" lang)
           (form/drop-down
             {:id "creditor"} "creditor"
             (map (fn [{n :name i :id}] [n i])
                  (sql/retrieve-creditors)))]
          [(lbl-charge "amount" lang)
           (nf "amount" 0 0.01 0)]
          [(btn-add lang)])))))

(defn render-add-debt-payment
  "Shows the add debt payment form"
  [user]
  (let [lang (:language user)]
    (with-page (get-string "ln-add-debt-payment" {} lang)
      user
      [:home :debts]
      (with-form "/debts"
        (form/hidden-field {:value true} "add-debt-payment")
        (with-form-table [nil [2] nil nil nil [2]] nil
          [(finput "image")]
          [(lbl-creditor "creditor" lang)
           (form/drop-down
             {:id "creditor"} "creditor"
             (map (fn [{n :name i :id}] [n i])
                  (sql/retrieve-creditors)))]
          [(lbl-concept "concept" lang)
           (tf "concept")]
          [(lbl-charge "amount" lang)
           (nf "amount" 0 0.01 0)]
          [(btn-add lang)])))))

(defn render-debt-detaill
  "Page showing movements related to the debt"
  [user id-creditor]
  (let [lang        (:language user)
        id-creditor (int-or-null id-creditor)
        creditor    (sql/retrieve-creditor-by-id id-creditor)
        debt        (sql/retrieve-debt-for-creditor id-creditor)]
    (with-page (get-string "str-debt-for/name" creditor lang)
      user
      [:home :debts]
      (format-money (-> debt :total)
                    (get-string "fmt-total" {} lang)
                    :h2)
      (with-table lang
        [:date      :concept      :amount]
        ["str-date" "str-concept" "str-amount"]
        [(fn [d _] (format-full-date d))
         (fn [c _] (html [:h5 c]))
         (fn [m _] (format-money m))]
        (-> debt :breakdown)))))

(defn render-404
  "Shows a 404/not found page"
  [user]
  (let [lang (:language user)]
    (with-page (get-string "str-404" {} lang)
      user
      [:error]
      (html
        [:h5 {:style "color: red;"}
         (get-string "str-404-msg" {} lang)]))))

(defn render-sales
  "Shows the sales breakdown"
  [user]
  (let [lang (:language user)]
    (with-page (get-string "ln-sales" {} lang)
      user
      [:home :accts]
      (with-table lang
        [:item      :item        :options]
        ["str-item" "str-amount" "str-options"]
        [(fn [i _] (make-link (str "/view-item/" (-> i :id_item))
                              (-> i :item)))
         (fn [n _] (html
                     [:h5 {:class "expand"} (-> n :sold)]))
         (fn [o _] (html
                     [:h5 {:class "expand"}
                      (apply
                        (partial with-form-table nil
                                 [(get-string "str-option" {} lang)
                                  (get-string "str-amount" {} lang)])
                        (map (fn [{o :option i :id_option n :sold}]
                               [(html
                                  [:h5
                                   (if (nil? o)
                                     (get-string "str-none" {} lang)
                                     (make-link
                                       (str "/view-option/" i) o))])
                                (html [:h5 n])])
                             o))]))]
        (sql/retrieve-sales-breakdown)))))

(defn handle-menu
  "Shows the menu of available items"
  [user]
  (let [lang   (:language user)
        menu   (group-by :menu_group
                         (sql/retrieve-full-avaliable-menu))
        groups (sort (keys menu))]
    (with-page (get-string "ln-menu" {} lang)
      user
      [:home]
      (with-form "/bills"
        (form/hidden-field {:value true} "add-new-bill-from-menu")
        (html
          [:table 
           [:tr
            [:td
             (lbl-location "location" lang)]
            [:td {:colspan 3}
             (tf "location")]]
           (for [g groups]
             (list
               [:tr
                [:th {:colspan 4} g]]
               (for [item (sort (group-by :item_name (get menu g)))
                     :let [i-id (:id_item (first (val item)))
                           q-id (str "i-" i-id)]]
                 [:tr
                  [:td {:valign "top"}
                   (key item)]
                  [:td {:valign "top" :style "border-right: 0;"}
                   (lbl "Quantity:" q-id lang)]
                  [:td {:valign "top" :style "border-left: 0;"}
                   (form/drop-down {:id q-id}
                     q-id (for [x (range 0 100)] [x x]) 0)]
                  [:td {:valign "top"}
                   (for [og (sort
                              (group-by :option_group (val item)))]
                     (if (not (empty? (key og)))
                       [:table {:style "padding: 0;"}
                        [:tr {:style "padding: 0;"}
                         [:th {:style "padding: 0;" :colspan 3}
                          (key og)]]
                        (for [op (sort-by :option_name (val og))
                              :let [o-id (:id_option op)
                                    g-id (str "o-" i-id "-" o-id)]]
                          [:tr {:style "padding: 0;"}
                           [:td {:style "padding: 3;"}
                            (:option_name op)]
                           [:td {:style "border-right: 0;
                                         padding: 3;"}
                            (lbl "Quantity:" g-id lang)]
                           [:td {:style "border-left: 0;
                                         padding: 3;"}
                            (form/drop-down {:id g-id}
                              g-id (for [x (range 0 100)] [x x]) 0)]]
                          )] ; /table
                       ))]] ; /tr
                 ))) ; /for
           [:tr
            [:td {:colspan 4}
             (btn-create lang)]]])))))


;               (let [sect  (get menu g)
;                     items (group-by :option_group (get menu g))
;                     ogrs  (sort (keys items))]
;                 (println s menu sect items ogrs)
;                 (for [og ogrs]
;                   [:tr
;                    [:th og]
;                    [:td (:item_name )]]))))])))))
;
;              (lbl "Quantity:" "quantity" lang)
;              (form/drop-down {:id "quantity"} "quantity"
;                              (for [x (range 0 100)]
;                                [x x])
;                              0)])

