(ns tacocat.view
  (:require [hiccup.page  :as    page]
            [hiccup.form  :as    form]
            [tacocat.util :refer :all]
            [tacocat.sql  :as    sql]
            [tacocat.intl :refer [get-string]]))

(defn make-link
  "Makes the link form"
  [address text]
  [:h5 [:a {:href address} text]])

(def link-data
  {:general [{:destination "/"
              :string      "ln-home"}
             {:destination "/bills"
              :string      "ln-bills"}
             {:destination "/accts"
              :string      "ln-accts"}
             {:destination "/services"
              :string      "ln-services"}
             {:destination "/admin"
              :string      "ln-admin"}]
   :admin    [{:destination "/admin-options"
              :string      "ln-admin-options"}
             {:destination "/list-users"
              :string      "ln-list-users"}
             {:destination "/list-roles"
              :string      "ln-list-roles"}
             {:destination "/list-items"
              :string      "ln-list-items"}
             {:destination "/log"
              :string      "ln-log"}
             {:destination "/intl"
              :string      "ln-intl"}]
   :main    [{:destination "/old-bills"
              :string      "ln-old-bills"}
             {:destination "/closed-services"
              :string      "ln-closed-services"}
             {:destination "/previous-closes"
              :string      "ln-previous-closes"}]
   :error   [{:destination "/user-info"
              :string      "ln-user-info"}]})

(defn get-links
  "Gets the links for a section"
  [lang sections]
  [:center
   (for [s    sections
         :let [data    (s link-data)
               percent (float (/ 100 (count data)))]]
     [:table {:width "100%"
              :style "w3-cell w3-container"
              :cellpadding 10}
      [:tr
       (map (fn [{d :destination s :string}]
              [:th {:width (str percent "%")}
               (make-link d (get-string s {} lang))])
            data)]])])

(defn main-head
  "normal page head tag"
  [font header]
  [:head
   [:link {:rel     "apple-touch-icon"
           :sizes   "180x180"
           :href    "/apple-touch-icon.png"}]
   [:link {:rel     "icon"
           :type    "image/png"
           :sizes   "32x32"
           :href    "/favicon-32x32.png"}]
   [:link {:rel     "icon"
           :type    "image/png"
           :sizes   "16x16"
           :href    "/favicon-16x16.png"}]
   [:link {:rel     "manifest"
           :href    "/site.webmanifest"}]
   [:link {:rel     "mask-icon"
           :href    "/safari-pinned-tab.svg"
           :color   "#5bbad5"}]
   [:meta {:name    "msapplication-TileColor"
           :content "#da532c"}]
   [:meta {:name    "theme-color"
           :content "#ffffff"}]
   [:meta {:name    "viewport"
           :content "width=device-width, initial-scale=1.0"}]
   [:meta {:charset "UTF-8"}]
   (page/include-css "https://www.w3schools.com/w3css/4/w3pro.css"
                     "https://www.w3schools.com/lib/w3-theme-grey.css"
                     "/css/style.css"
                     "/fonts/style.css")
   [:style (str "h1, h2, h3, h4,
                h5, h6, div, p,
                th, td, tr {font-family: "
                (if (or (nil? font)
                        (empty? font))
                  ""
                  (str \" font "\", "))
                "Verdana, sans-serif;}")]
   [:title header]])

(defn with-page
  "Adds content to a page"
  [header user sections & content]
  (let [lang (if (empty? user)
               (sql/retrieve-app-data-val "default-language")
               (:language user))
        font (sql/retrieve-app-data-val "default-font")]
    (page/html5
      (main-head font header)
      [:body
       (get-links lang [:general])
       (get-links lang sections)
       [:header {:class "w3-container w3-card w3-theme-l4"}
        [:center [:h1 header]]]
       [:center content]
       (get-links lang sections)
       [:header {:class "w3-container w3-card w3-theme-l4"}
        [:center
         [:h5
          (if (empty? user)
            (make-link "/login" (get-string "ln-login" {} lang))
            [:span
             (make-link "/user-info" (:name user))
             (make-link "/login"
                        (get-string
                          "ln-change-user" {} lang))])]]]])))

(defn print-head
  "Head tag for printing"
  [font header]
  [:head
   [:link {:rel     "apple-touch-icon"
           :sizes   "180x180"
           :href    "/apple-touch-icon.png"}]
   [:link {:rel     "icon"
           :type    "image/png"
           :sizes   "32x32"
           :href    "/favicon-32x32.png"}]
   [:link {:rel     "icon"
           :type    "image/png"
           :sizes   "16x16"
           :href    "/favicon-16x16.png"}]
   [:link {:rel     "manifest"
           :href    "/site.webmanifest"}]
   [:link {:rel     "mask-icon"
           :href    "/safari-pinned-tab.svg"
           :color   "#5bbad5"}]
   [:meta {:name    "msapplication-TileColor"
           :content "#da532c"}]
   [:meta {:name    "theme-color"
           :content "#ffffff"}]
   [:meta {:name    "viewport"
           :content "width=device-width, initial-scale=1.0"}]
   [:meta {:charset "UTF-8"}]
   (page/include-css "/css/style.css"
                     "/fonts/style.css"
                     "/css/printing-style.css")
   [:style (str "h1, h2, h3, h4,
                h5, h6, div, p,
                th, td, tr {font-family: "
                (if (or (nil? font)
                        (empty? font))
                  ""
                  (str \" font "\", "))
                "Verdana, sans-serif;}")]
   [:title header]])

(defn with-printing-page
  "Makes a page suitable for printing"
  [header id & content]
  ; Printing only in system-wide language
  (let [lang (sql/retrieve-app-data-val "default-language")
        font (sql/retrieve-app-data-val "default-font")
        tel  (sql/retrieve-app-data-val "business-telephone")]
    (page/html5
      (print-head font header)
      [:body
       [:center
        [:header
         [:h1 (sql/retrieve-app-data-val "business-name")]]
        [:small
         [:p
          (sql/retrieve-app-data-val "business-address") ", "
          (sql/retrieve-app-data-val "business-post-code") ", "
          (sql/retrieve-app-data-val "business-state")
          [:br]
          ; Tel: 34567789
          (get-string "str-tel/number" {:number tel})
          [:br]
          (sql/retrieve-app-data-val "business-website")]]
        [:header
         [:h2 (get-string "str-bill-for/location/id" {:location header :id id})]]
        content]
       [:script {:language "javascript"} "window.print();"]])))

(defmacro with-form
  "Wraps a form"
  [post & forms]
  `(form/form-to [:post ~post] ~@forms))

(defn format-money
  "Gets a formatted money form"
  ([m]
   (format-money m ""))
  ([m s]
   (format-money m s :h5))
  ([m s t]
   [t s
    [:span {:class (if (< m 0) "red" "green")}
     (format (str " $%.2f") m)]]))

(defn format-bool
  "Makes a bool into a form"
  [b id]
  (form/check-box id b))

(defn render-index
  "Index page of the application"
  [user]
  ;(if (contains? (:permissions user) "view-index")
  ;  ...
  ;  (NOT-ALLOWED (:name user)))
  (let [lang (if (empty? user)
               (sql/retrieve-app-data-val "default-language")
               (:language user))]
    (with-page (sql/retrieve-app-data-val "business-name")
      user
      [:main]
      (make-link "/bills"    (get-string "ln-bills"    {} lang))
      (make-link "/accts"    (get-string "ln-accts"    {} lang))
      (make-link "/services" (get-string "ln-services" {} lang))
      (make-link "/admin"    (get-string "ln-admin"    {} lang)))))

(defn headers
  "Make the [:th ...] section of a table"
  [hh]
  (-> (fn [h] [:th h])
      (map hh)
      (conj :tr)
      vec))

(defn with-table
  "Makes a [:table [:tr [:td ... ]]] structure out of a list of
  maps by applying the respective function to each.
 
  Each function must be able to take two arguments:
  The column content and the context"
  [lang columns column-display functions list-of-maps]
  (-> (fn [d]
        (-> (fn [c f] [:td {:valign "top"} (f (get d c) d)])
            (map columns functions)
            (conj :tr)
            vec))
      (map list-of-maps)
      (conj (headers (map #(get-string % {} lang) column-display))
            {:cellspacing 0 :cellpadding 1}
            :table)
      vec))

(defn format-date
  "Gets the format for a date"
  [date]
  [:h5
   (-> (java.text.SimpleDateFormat. "MM/dd HH:mm")
       (.format date))])

(defn format-full-date
  "Full date format"
  [date]
  [:h5
   (-> (java.text.SimpleDateFormat. "yy:MM:dd:HH:mm:ss")
       (.format date))])

(defn format-time
  "Gets the format for a time"
  ([tme tag]
   [tag
    (-> (java.text.SimpleDateFormat. "HH:mm")
        (.format tme))])
  ([tme]
   (format-time tme :h5)))

(defn NOT-ALLOWED
  [user & permissions-required]
  (let [lang (if (empty? user)
               (sql/retrieve-app-data-val "default-language")
               (:language user))]
    (with-page (get-string "str-forbidden-action" {} lang)
      user
      [:error]
      [:h2 {:style "color: red;"}
       (get-string "str-insufficient-permissions" {} lang)]
      (if (not (empty? permissions-required))
        (with-table lang
          [:permissions]
          [(get-string "str-permissions" {} lang)]
          [(fn [p _] (map (fn [p] [:h6 p]) (sort p)))]
          [{:permissions permissions-required}]))
      (make-link "/login" (get-string "ln-change-user" {} lang)))))

(defn format-bill-list
  "Formats a list of bills"
  [user bill-link-root bill-data editable?]
  (with-table (:language user)
    [:date      :location      :charge      :id]
    ["str-date" "str-location" "str-charge" ""]
    [(fn [d _] (format-date d))
     (fn [l c] (if editable?
                 (make-link (str "/edit-location/" (:id c)) l)
                 [:h5 l]))
     (fn [c _] (format-money c))
     (fn [i _] (make-link
                 (str bill-link-root i)
                 (get-string "ln-view" {} (:language user))))]
    bill-data))

(defn make-services-table
  "Makes a services table"
  [user services-resultset]
  (with-table (:language user)
    [:date      :concept      :amount      :running_total]
    ["str-date" "str-concept" "str-charge" "str-new-balance"]
    [(fn [d _] (format-date d))
     (fn [c _] [:h5 c])
     (fn [m _] (format-money m))
     (fn [m _] (format-money m))]
    services-resultset))

(defn btn
  "Make a localised button"
  [text lang]
  (form/submit-button (get-string text {} lang)))

; Buttons
(def btn-enter             #(btn "btn-enter"             %))
(def btn-delete            #(btn "btn-delete"            %))
(def btn-add               #(btn "btn-add"               %))
(def btn-charge            #(btn "btn-charge"            %))
(def btn-change            #(btn "btn-change"            %))
(def btn-close             #(btn "btn-close"             %))
(def btn-create            #(btn "btn-create"            %))
(def btn-enter             #(btn "btn-enter"             %))
(def btn-remove            #(btn "btn-remove"            %))
(def btn-view-translations #(btn "btn-view-translations" %))

(defn render-login
  "Show the login page"
  [user]
  (let [lang (if (empty? user)
               (sql/retrieve-app-data-val "default-language")
               (:language user))]
    (with-page (get-string "str-user-login" {} lang)
      user
      [:error]
      [:h5
       (with-form "/user-info"
         (form/hidden-field {:value true} "perform-login")
         (form/label {:for "user-name"}
                     "user-name"
                     (get-string "lbl-user-name" {} lang))
         (form/text-field {:id "user-name"}
                          "user-name" (:user_name user))
         [:br]
         (form/label {:for "password"}
                     "password" (get-string "lbl-password" {} lang))
         (form/password-field {:id "password"} "password")
         [:br]
         (btn-enter lang))])))

(defn render-delete-user
  "Delete user page"
  [user id]
  (with-page (get-string "str-delete-user/name"
                         (-> id int-or-null sql/retrieve-user-by-id)
                         (:language user))
    user
    [:admin]
    [:h5
     (with-form "/list-users"
       (form/hidden-field {:value id} "delete-user")
       (btn-delete (:language user)))]))

(defn render-change-user-name
  "Screen to change users full name"
  [user id]
  (let [u    (-> id int-or-null sql/retrieve-user-by-id)
        lang (:language user)]
    (with-page (get-string "str-change-user-name/name" u lang)
      user
      [:admin]
      [:h5
       (with-form (str "/user-info/" id)
         (form/hidden-field {:value id} "change-user-name")
         (form/label {:for id} "nombre"
                     (get-string "lbl-full-name" {} lang))
         (form/text-field {:id id} "name" (:name u))
         [:br]
         (btn-change lang))])))

(defn render-change-user-language
  "Language selection screen"
  [user id]
  (let [lang  (-> id
                  int-or-null
                  sql/retrieve-user-by-id
                  :language)
        langs (sql/retrieve-langs)]
    (with-page (get-string "str-lang" {} lang)
      user
      [:admin]
      [:h5
       (with-form (str "/user-info/" id)
         (form/hidden-field {:value id} "set-user-language")
         (form/label {:for "language"} "language"
                     (get-string "lbl-lang" {} lang))
         (form/drop-down {:id "language"} "language"
                         (map (fn [{n :name f :full_name}]
                                [(get-string f {} lang) n])
                              langs)
                         lang)
         [:br]
         (btn-change lang))])))

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
         [:h2 {:style "color: red;"}
          (get-string "str-wrong-user-or-password" {} lang)])
       (let [id              (int-or-null id)
             id              (if (nil? id) (:id user) id)
             {permissions :permissions
              :as         u} (sql/retrieve-user-by-id id)
             user-roles      (sql/retrieve-roles-for-user id)
             machines        (sql/retrieve-logged-in-to id)]
         (with-page (get-string "str-user-info/name" u lang)
           user
           [:admin]
           [:h5 (get-string "str-username/user_name" u lang)]
           (make-link (str "/change-user-name/"  id)
                      (get-string "ln-full-name/name" u lang))
           (make-link (str "/change-password/"   id)
                      (get-string "ln-change-password" {} lang))
           (make-link (str "/change-user-roles/" id)
                      (get-string "ln-change-roles" {} lang))
           (make-link (str "/change-user-language/" id)
                      (get-string
                        "ln-change-language/language" u lang))
           (with-table lang
             [:roles      :permissions      :machines]
             ["str-roles" "str-permissions" "str-machines"]
             [(fn [r _] (map (fn [r]
                               (make-link
                                 (str "/view-role/" (:id r))
                                 (:name r)))
                             r))
              (fn [p _] (map (fn [p] [:h6 p]) (sort p)))
              (fn [m _] (map (fn [m] [:h5 m]) (sort m)))]
             [{:roles       user-roles
               :machines    machines
               :permissions permissions}]))))))
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
      [:admin]
      [:h5
       (with-form (str "/user-info/" id)
         (form/hidden-field {:value true} "set-user-roles")
         (with-table lang
           [:name      :id]
           ["str-role" ""]
           [(fn [n r] (form/label {:for (:id r)}
                                  (:name r) (:name r)))
            (fn [i _] (form/check-box {:id i} i
                                      (contains? id-user-roles i)))]
           all-roles)
         (btn-change lang))])))

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
      [:admin]
      (with-table lang
        [:name            :id]
        ["str-permission" ""]
        [(fn [n _] [:h5 n])
         (fn [i r] (with-form (str "/view-role/" id)
                     (form/hidden-field
                       {:value i} "change-permission")
                     (form/check-box
                       i (contains? role-perms (:name r)))
                     (form/submit-button
                       (get-string "btn-change" {} lang))))]
        all-perms))))

(defn render-add-new-role
  "Add role page"
  [user]
  (let [lang (:language user)]
    (with-page (get-string "str-add-role" {} lang)
      user
      [:admin]
      [:h5
       (with-form "/list-roles"
         (form/hidden-field {:value true} "add-role")
         (form/label {:for "role"} "role"
                     (get-string "lbl-role" {} lang))
         (form/text-field {:id "role"} "role")
         [:br]
         (btn-add lang))])))

(defn render-delete-role
  "Delete role page"
  [user id]
  (with-page (get-string "str-delete-role/name"
                         (-> id int-or-null sql/retrieve-role-by-id)
                         (:language user))
    user
    [:admin]
    [:h5
     (with-form "/list-roles"
       (form/hidden-field {:value id} "delete-role")
       (btn-delete (:language user)))]))

(defn render-add-new-menu-group
  "New Menu Group page"
  [user]
  (let [lang (:language user)]
    (with-page (get-string "str-add-new-menu-group" {} lang)
      user
      [:admin]
      [:h5
       (with-form "/list-items"
         (form/hidden-field {:value true} "add-new-menu-group")
         (form/label {:for "menu-group-name"} "menu-group-name"
                     (get-string "lbl-name" {} lang))
         (form/text-field {:id "menu-group-name"} "menu-group-name")
         [:br]
         (btn-add lang))])))

(defn render-add-new-item
  "New item page"
  [user]
  (let [lang (:language user)]
    (with-page (get-string "str-add-item" {} lang)
      user
      [:admin]
      [:h5
       (with-form "/list-items"
         (form/hidden-field {:value true} "add-new-item")
         (form/label {:for "item-name"} "item-name"
                     (get-string "lbl-item" {} lang))
         (form/text-field {:id "item-name"} "item-name")
         [:br]
         (form/label {:for "menu-group"} "menu-group"
                     (get-string "lbl-group" {} lang))
         (form/drop-down {:id "menu-group"} "menu-group"
                         (map (fn [g] [g g])
                           (sort
                             (map :name
                               (sql/retrieve-menu-groups)))))
         [:br]
         (form/label {:for "amount"} "amount"
                     (get-string "lbl-charge" {} lang))
         (form/text-field {:id "amount" :type "number" :step "0.01"}
                          "amount")
         [:br]
         (btn-add lang))])))

(defn with-options-table
  "Make a table for showing item options"
  [lang id options action]
  (with-table lang
    [:name        :in_stock       :id]
    ["str-option" "str-in-stock"  ""]
    [(fn [n {i :id g :option_group m :extra_charge}]
       (make-link (str "/view-option/" i)
                  (str g "/" n " ($" m ")")))
     (fn [b o]
       (with-form (str "/view-item/" id)
         (form/hidden-field {:value (:id o)} "set-option-in-stock")
         (form/check-box "set-option" b)
         (btn-change lang)))
     action]
    options))

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
        lang                  (:language user)]
    (with-page iname
      user
      [:admin]
      [:h5
       (with-form (str "/view-item/" id)
         (form/hidden-field {:value id} "set-item-name")
         (form/label {:for "name"} "name"
                     (get-string "lbl-item" {} lang))
         (form/text-field {:id "name"} "name" iname)
         (btn-change lang))
       
       (with-form (str "/view-item/" id)
         (form/hidden-field {:value id} "set-item-menu-group")
         (form/label {:for "menu-group"} "menu-group"
                     (get-string "lbl-group" {} lang))
         (form/drop-down {:id "menu-group"} "menu-group"
                         (map (fn [g] [g g])
                              (sort (map :name menu-groups)))
                         mgroup)
         (btn-change lang))
       
       (with-form (str "/view-item/" id)
         (form/hidden-field {:value id} "set-item-charge")
         (form/label {:for "amount"} "amount"
                     (get-string "lbl-charge" {} lang))
         (form/text-field {:id "amount" :type "number" :step "0.01"}
                          "amount" amount)
         (btn-change lang))
       
       (with-form (str "/view-item/" id)
         (form/hidden-field {:value id} "set-item-in-stock")
         (form/label {:for "in-stock"} "in-stock"
                     (get-string "lbl-in-stock" {} lang))
         (form/check-box {:id "in-stock"} "in-stock" in-stock?)
         (btn-change lang))
       
       (make-link (str "/create-new-option-group/" id)
                  (get-string "ln-create-new-option-group" {} lang))
       (make-link (str "/create-new-option/" id)
                  (get-string "ln-create-new-option" {} lang))
       
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
        (get-string "str-other-options")]
       (with-options-table lang id
         (filter (fn [{i :id}]
                   (not (contains? item-options i)))
                 all-options)
         (fn [i _]
           (with-form (str "/view-item/" id)
             (form/hidden-field {:value i} "add-option-to-item")
             (btn-add lang))))])))

(defn render-change-user-password
  "User password change screen"
  [user id]
  (let [u    (sql/retrieve-user-by-id (int-or-null id))
        lang (:language user)]
    (with-page (get-string "str-change-password/name" u lang)
      user
      [:admin]
      [:h5
       (with-form (str "/user-info/" id)
         (form/hidden-field {:value true} "change-user-password")
         (form/label {:for "password"} "password"
                     (get-string "lbl-new-password" {} lang))
         (form/password-field {:id "password"} "password")
         [:br]
         (btn-change lang))])))

(defn render-admin
  "Gets the admin page"
  [user]
  (let [lang (if (empty? user)
               (sql/retrieve-app-data-val "default-language")
               (:language user))]
    (with-page "Admin"
      user
      [:admin]
      (make-link
        "/admin-options" (get-string "ln-admin-options" {} lang))
      (make-link
        "/list-users" (get-string "ln-list-users" {} lang))
      (make-link
        "/list-roles" (get-string "ln-list-roles" {} lang))
      (make-link
        "/list-items" (get-string "ln-list-items" {} lang))
      (make-link
        "/log" (get-string "ln-log" {} lang))
      (make-link
        "/intl" (get-string "ln-intl" {} lang)))))

(defn render-intl
  "Renders the internationalisation page"
  [user lang-from lang-to ffilter]
  (let [lang  (:language user)
        langs (sql/retrieve-langs)]
    (with-page (get-string "str-internationalisation" {} lang)
      user
      [:admin]
      (with-form "/intl"
        (form/label {:for "lang-from"} "lang-from"
                    (get-string "lbl-from" {} lang))
        (form/drop-down {:id "lang-from"} "lang-from"
                        (map (fn [{l :full_name n :name}]
                               [(get-string l {} lang) n])
                             langs)
                        lang-from)
        [:br]
        (form/label {:for "lang-to"} "lang-to"
                    (get-string "lbl-to" {} lang))
        (form/drop-down {:id "lang-to"} "lang-to"
                        (map (fn [{l :full_name n :name}]
                               [(get-string l {} lang) n])
                             langs)
                        lang-to)
        [:br]
        (form/label {:for "filter"} "filter"
                    (get-string "lbl-filter" {} lang))
        (form/text-field {:id "filter"} "filter" ffilter)
        [:p (get-string "s-filter-instructions" {} lang)]
        (btn-view-translations lang))
      (if (and (not (nil? lang-from))
               (not (nil? lang-to))
               (not (= lang-from lang-to)))
        (with-table lang
          [:key        :src_val  :key          :dst_val]
          ["str-label" lang-from "str-default" lang-to]
          [(fn [k _] [:small k])
           (fn [v i] [:small
                      (if (nil? v)
                        (sql/retrieve-internationalised-string
                          (:key i) lang-from)
                        v)])
           (fn [k _] [:small
                      (sql/retrieve-internationalised-string
                        k lang-to)])
           (fn [v i] 
             (with-form "/intl"
               (form/hidden-field {:value (:key i)}  "translate")
               (form/hidden-field {:value ffilter}   "filter")
               (form/hidden-field {:value lang-from} "lang-from")
               (form/hidden-field {:value lang-to}   "lang-to")
               [:small
                (form/text-field "value" v)
                (btn-change lang)]))]
          (sql/retrieve-intl lang-from lang-to
                             (if (empty? ffilter)
                               "%"
                               ffilter)))))))

(defn render-app-options
  "Gets the app options page"
  [user]
  (let [lang (:language user)]
    (with-page (get-string "str-app-options" {} lang)
      user
      [:admin]
      [:h5
       (with-form "/admin-options"
         (form/hidden-field {:value true} "make-admin-changes")
         (with-table lang
           [:key      :val]
           ["str-key" "str-val"]
           [(fn [k _] [:h5 k])
            (fn [v o] [:h5 (form/text-field (:key o) v)])]
           (sql/retrieve-app-data))
         (btn-change lang))])))

(defn render-list-items
  "Gets the product page"
  [user]
  (let [lang (:language user)]
    (with-page (get-string "str-items")
      user
      [:admin]
      (make-link "/add-new-item"
                 (get-string "ln-create-new-item" {} lang))
      (make-link "/add-new-menu-group"
                 (get-string "ln-create-new-menu-group" {} lang))
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
                     (format-bool b (:id i))
                     (btn-change lang)))
         (fn [i _] (make-link (str "/delete-item/" i)
                              (get-string "btn-delete")))]
        (sql/retrieve-all-items))
      (make-link "/add-new-item"
                 (get-string "ln-create-new-item" {} lang))
      (make-link "/add-new-menu-group"
                 (get-string "ln-create-new-menu-group" {} lang)))))

(defn render-services
  "Services screen"
  [user]
  (let [lang (:language user)]
    (with-page (get-string "ln-services" {} lang)
      user
      [:main]
      (make-link "/add-services-expense"
                 (get-string "ln-add-services-expense" {} lang))
      (make-services-table user (sql/retrieve-current-services))
      (make-link "/add-services-expense"
                 (get-string "ln-add-services-expense" {} lang)))))

(defn render-closed-services
  "Services for prior closes"
  [user]
  (let [lang (:language user)]
    (with-page (get-string "ln-closed-services" {} lang)
      user
      [:main]
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
      [:main]
      (make-services-table
        user (sql/retrieve-services-for-close id)))))

(defn get-existing-bills
  "Returns a structure containing existing bills"
  [user]
  (format-bill-list user "/bill/" (sql/retrieve-bills) true))

(defn get-closed-bills
  "Returns a structure containing existing bills"
  [user]
  (format-bill-list user "/closed-bill/" (sql/retrieve-closed-bills) false))

(defn render-bills
  "Shows the bills page"
  [user]
  (let [lang (:language user)]
    (with-page (get-string "ln-bills" {} lang)
      user
      [:main]
      (make-link "new-bill" (get-string "ln-new-bill" {} lang))
      (get-existing-bills user)
      (make-link "new-bill" (get-string "ln-new-bill" {} lang)))))

(defn render-previous-closes
  "Shows the previous closes"
  [user]
  (let [lang (:language user)]
    (with-page (get-string "str-closes" {} lang)
      user
      [:main]
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
      [:main]
      (with-form "/previous-closes"
        (form/hidden-field {:value true} "make-close")
        [:h5 (btn-close lang)]))))

(defn render-add-services-expense
  "Makes a form to add a service expense"
  [user]
  (let [lang (:language user)]
    (with-page (get-string "str-add-services-charge" {} lang)
      user
      [:main]
      (with-form "/services"
        (form/hidden-field {:value true} "add-service-charge")
        [:h5
         (form/label {:for "concept"} "concept"
                     (get-string "lbl-concept" {} lang))
         (form/text-field {:id "concept"} "concept")
         [:br]
         (form/label {:for "amount"} "amount"
                     (get-string "lbl-charge" {} lang))
         (form/text-field {:id "amount" :type "number" :step "0.01"}
                          "amount")
         [:br]
         (btn-add lang)]))))

(defn render-old-bills
  "Shows the closed bills page"
  [user]
  (with-page (get-string "str-closed-bills" {} (:language user))
    user
    [:main]
    (get-closed-bills user)))

(defn get-bill-items
  "Returns a form with the bill items of a bill"
  [user id]
  (let [lang (:language user)]
    (with-table lang
      [:date      :person      :item      :charge      :nil]
      ["str-time" "str-person" "str-item" "str-charge" ""]
      [(fn [t _] (format-time t))
       (fn [p c] (make-link
                   (str "/set-person/" (:id c))
                   (if (nil? p)
                     (get-string "ln-assign" {} lang)
                     (get-string
                       "ln-person/number" {:number p} lang))))
       (fn [i c] [:div
                  (make-link (str "/set-bill-item/" (:id c)) i)
                  (if (= 0 (sql/retrieve-bill-item-option-count
                             (:id c)))
                    nil
                    (let [options (:options c)]
                      [:span
                       (make-link (str "/set-bill-item-options/"
                                       (:id c))
                                  [:small
                                   (if (nil? options)
                                     (get-string
                                       "ln-add-options" {} lang)
                                     options)])]))])
       (fn [m c] (make-link (str "/set-charge-override/" (:id c))
                            (format-money m)))
       (fn [_ c] (make-link (str "/delete-bill-item/" (:id c))
                            (get-string "btn-delete" {} lang)))]
      (sql/retrieve-bill-items id))))

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
      [:main]
      [:h5
       (with-form (str "/bill/" id-bill)
         (form/hidden-field {:value id} "delete-bill-item")
         (btn-delete lang))])))

(defn render-edit-bill-location
  "Returns the edit location for a bill form"
  [user id]
  (let [location (:location (sql/retrieve-bill id))
        lang     (:language user)]
    (with-page (get-string "str-change-location" {} lang)
      user
      [:main]
      (with-form (str "/bill/" id)
        (form/hidden-field {:value id} "edit-bill-location")
        [:h5
         (form/text-field "location" location)
         [:br]
         (btn-change lang)]))))

(defn get-old-bill-items
  "Returns a form with the bill items of a closed bill"
  ([user id tag sep]
   (with-table (:language user)
     [:date  :person  :item      :charge]
     [""     "str-p#" "str-item" "str-charge"]
     [(fn [t _] (format-time t tag))
      (fn [p _] [tag (if (nil? p) "" [:center p])])
      (fn [i c] [tag i " " 
                 (let [options (:options c)]
                   (if (nil? options)
                     nil
                     [sep [:small (:options c)]]))])
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
      [:main]
      (with-form "/accts"
        (form/hidden-field {:value true} "add-expense")
        [:h5
         (form/label {:for "concept"} "concept"
                     (get-string "lbl-concept" {} lang))
         (form/text-field {:id "concept"} "concept")
         [:br]
         (form/label {:for "amount"} "amount"
                     (get-string "lbl-charge" {} lang))
         (form/text-field {:id "amount" :type "number" :step "0.01"}
                          "amount")
         [:br]
         (btn-add lang)]))))

(defn render-closed-bill
  "Renders a single closed bill"
  [user id]
  (let [{date     :date
         location :location
         charge   :charge}  (sql/retrieve-bill id)
        lang                (:language user)]
    (with-page location
      user
      [:main]
      (make-link (str "/print-bill/" id)
                 (get-string "ln-print" {} lang))
      (get-old-bill-items user id)
      (format-money charge (get-string "fmt-total" {} lang) :h2)
      (with-table lang
        [:person      :charge]
        ["str-person" "str-charge"]
        [(fn [p _] [:h5 (if (nil? p)
                          ""
                          (get-string
                            "str-person/number" {:number p} lang))])
         (fn [m _] (format-money m))]
        (sql/retrieve-bill-charges-per-person id))
      [:p date]
      (make-link (str "/print-bill/" id)
                 (get-string "ln-print" {} lang)))))

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
            [(fn [p _] [:p (if (nil? p)
                             ""
                             (get-string "str-person/number"
                               {:number p} lang))])
             (fn [m _] (format-money m "" :p))]
            per-person)))
      [:p date])))

(defn render-bill
  "Renders a single bill"
  [user id]
  (let [{date     :date
         location :location
         charge   :charge}  (sql/retrieve-bill id)
        mult                (float-or-null
                              (sql/retrieve-app-data-val
                                "card-multiplicative"))
        lang                (:language user)]
    (with-page location
      user
      [:main]
      (make-link (str "/add-item/" id)
                 (get-string "ln-add-to-bill" {} lang))
      (make-link (str "/charge-bill/" id)
                 (get-string "ln-charge" {} lang))
      (get-bill-items user id)
      (format-money charge
                    (get-string "fmt-total" {} lang) :h2)
      (format-money (* mult charge)
                    (get-string "str-card-payment" {} lang))
      (make-link (str "/add-item/" id)
                 (get-string "ln-add-to-bill" {} lang))
      (make-link (str "/charge-bill/" id)
                 (get-string "ln-charge" {} lang))
      [:p date])))

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
                            :options (if (nil? options)
                                       ""
                                       (str " (" options ")"))}
                           lang)
      user
      [:main]
      (with-form (str "/bill/" id-bill)
        (form/hidden-field {:value id} "id-bill-item")
        [:h5
         (form/drop-down "set-person"
                         (map (fn [n]
                                [(if (= "null" n)
                                   (get-string "str-nobody" {} lang)
                                   (get-string "str-person/number"
                                               {:number n} lang))
                                 n])
                              (conj (range 1 50) "null"))
                         (if (nil? person)
                           "null"
                           person))
         [:br]
         (btn-change lang)]))))

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
        lang               (:language user)]
    (with-page (get-string "str-change-item/item/options"
                           {:item    item
                            :options (if (nil? options)
                                       ""
                                       (str " (" options ")"))}
                           lang)
      user
      [:main]
      (with-form (str "/bill/" id-bill)
        (form/hidden-field {:value id} "id-bill-item")
        [:h5
         (form/drop-down "set-item"
                         (map (fn [{id :id nm :name}] [nm id])
                              (sql/retrieve-items-in-stock-in-group
                                item-group))
                         item-id)
         [:br]
         (btn-change lang)]))))

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
                           :options (if (nil? options)
                                      ""
                                      (str " (" options ")"))}
                          lang)
      user
      [:main]
      (with-form (str "/bill/" id-bill)
        (form/hidden-field {:value id} "id-bill-item")
        (form/hidden-field {:value true} "set-options")
        [:h5
         (let [ordered-options (into (sorted-map)
                                 (map (fn [i] {(key i)
                                        (map (fn [{n :option_name
                                                   i :id_option}]
                                               [:h5
                                                (form/label {:for i}
                                                  n (str " " n))
                                                (form/check-box
                                                  {:id i} i
                                                  (contains?
                                                    current-options
                                                    i))])
                                               (val i))})
                                      (group-by :option_group
                                                valid-options)))
               option-groups (keys ordered-options)]
           (with-table lang
             option-groups
             option-groups
             (repeat (count ordered-options)
                     (fn [k _] k))
             [ordered-options]))
         (btn-change lang)]))))

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
                           :options (if (nil? options)
                                      ""
                                      (str " (" options ")"))}
                          lang)
      user
      [:main]
      (with-form (str "/bill/" id-bill)
        (form/hidden-field {:value id} "id-bill-item")
        [:h5
         (form/text-field {:type "number" :step "0.01"}
                          "charge-override" charge)
         [:br]
         (btn-change lang)]))))

(defn render-add-item
  "Displays a page where we add an item to a bill"
  [user id]
  (let [lang (:language user)]
    (with-page (get-string "ln-add-to-bill" {} lang)
      user
      [:main]
      (doall
        (map (fn [m]
               [:div
                [:h5 (key m)]
                (with-form (str "/bill/" id)
                  (form/hidden-field {:value id} "id-bill")
                  [:h5
                   (form/drop-down "new-item"
                                   (sort
                                     (map (fn [{id :id
                                                nm :name}] [nm id])
                                          (val m))))
                   (btn-add lang)])])
             (group-by :menu_group
                       (sql/retrieve-items-in-stock)))))))

(defn render-new-bill
  "Shows a page where we can create a new bill"
  [user]
  (let [lang (:language user)]
    (with-page (get-string "ln-new-bill" {} lang)
      user
      [:main]
      (with-form "/bills"
        (form/hidden-field {:value true} "new-bill")
        [:h5
         (form/label {:for "bill-location"} "bill-location"
                     (get-string "lbl-location"))
         (form/text-field {:id "bill-location" :type "text"}
                          "bill-location")
         [:br]
         (btn-create lang)]))))

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
      [:main]
      [:p date]
      (with-form (str "/closed-bill/" id)
        (form/hidden-field {:value charge} "bill-charge")
        (form/hidden-field {:value id}     "bill-id")
        (format-money charge (get-string "fmt-total" {} lang) :h2)
        [:h5 (btn-charge lang)]))))

(defn render-accts
  "Displays the accounts page"
  [user]
  (let [lang (:language user)]
    (with-page (get-string "ln-accts" {} lang)
      user
      [:main]
      (make-link "/add-expense"
                 (get-string "ln-add-expense" {} lang))
      (make-link "/close-acct"
                 (get-string "ln-close-accounting" {} lang))
      (with-table lang
        [:date      :concept      :amount]
        ["str-date" "str-concept" "str-amount"]
        [(fn [d _] (format-date d))
         (fn [c _] [:h5 c])
         (fn [m _] (format-money m))]
        (sql/retrieve-current-accounting))
      (let [{total    :total
             expenses :expenses
             intakes  :intakes} (sql/retrieve-current-accounting-totals)]
        [:span
         (format-money total (get-string "fmt-total" {} lang) :h2)
         (format-money expenses (get-string "fmt-expenses" {} lang))
         (format-money intakes (get-string "fmt-intakes" {} lang))])
      (make-link "/add-expense"
                 (get-string "ln-add-expense" {} lang))
      (make-link "/close-acct"
                 (get-string "ln-close-accounting" {} lang)))))

(defn render-list-users
  "Page listing all registered users"
  [user]
  (let [lang (:language user)]
    (with-page (get-string "str-registered-users" {} lang)
      user
      [:admin]
      (make-link "/add-new-user"
                 (get-string "ln-add-new-user" {} lang))
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
          [(fn [u _] [:h5 u])
           (fn [n u] (make-link (str "/user-info/" (:id u)) n))
           (fn [i _] (make-link (str "/change-password/"   i)
                                (get-string
                                  "ln-change-password" {} lang)))
           (fn [i _]
             (make-link (str "/change-user-roles/" i)
                        (make-role-string
                          (sql/retrieve-roles-for-user i))))
           (fn [b u] (with-form "/list-users"
                       (form/hidden-field {:value (:id u)}
                                          "change-user-enabled")
                       (form/check-box {:id "enabled"} "enabled" b)
                       (btn-change lang)))
           (fn [i _] (make-link (str "/delete-user/" i)
                               (get-string "btn-delete" {} lang)))]
          (sql/retrieve-registered-users)))
      (make-link "/add-new-user"
                 (get-string "ln-add-user" {} lang)))))

(defn render-list-roles
  "Gets the list roles page"
  [user]
  (let [lang (:language user)]
    (with-page (get-string "str-registerd-roles" {} lang)
      user
      [:admin]
      (make-link "/add-new-role"
                 (get-string "ln-add-new-role" {} lang))
      (with-table lang
        [:name      :id               :id]
        ["str-role" "str-permissions" ""]
        [(fn [n r] (make-link (str "/view-role/" (:id r)) n))
         (fn [i _] (map (fn [p] [:h6 p])
                     (sort (sql/retrieve-permissions-for-role i))))
         (fn [i _] (make-link (str "/delete-role/" i)
                              (get-string "btn-delete" {} lang)))]
        (sql/retrieve-all-roles))
      (make-link "/add-new-role" 
                 (get-string "ln-add-new-role" {} lang)))))

(defn render-new-option-group
  "Page to add an option group"
  [user id]
  (let [lang (:language user)]
    (with-page (get-string "str-new-option-group" {} lang)
      user
      [:admin]
      [:h5
       (with-form (str "/view-item/" id)
         (form/hidden-field {:value true} "add-new-option-group")
         (form/label {:for "new-option-group"} "new-option-group"
                    (get-string "lbl-new-group" {} lang))
         (form/text-field {:id "new-option-group"}
                          "new-option-group")
         (btn-create lang))])))

  (defn render-new-option
  "Page to add an option"
  [user id]
  (let [lang (:language user)]
    (with-page (get-string "str-new-option" {} lang)
      user
      [:admin]
      [:h5
       (with-form (str "/view-item/" id)
         (form/hidden-field {:value true} "add-new-option")
         (form/label {:for "new-option"} "new-option"
                     (get-string "lbl-new-option" {} lang))
         (form/text-field {:id "new-option"} "new-option")
         [:br]
         (form/label {:for "option-group"} "option-group"
                     (get-string "lbl-group" {} lang))
         (form/drop-down {:id "option-group"} "option-group"
                         (map (fn [g] [g g])
                           (sort
                             (map :name 
                               (sql/retrieve-option-groups)))))
         [:br]
         (btn-create lang))])))

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
      [:admin]
      [:h5
       (with-form post-page
         (form/hidden-field {:value true} "set-option-name")
         (form/label {:for "option-name"} "option-name"
                     (get-string "lbl-option" {} lang))
         (form/text-field {:id "option-name"} "option-name" oname)
         (btn-change lang))
       (with-form post-page
         (form/hidden-field {:value true} "set-option-group")
         (form/label {:for "option-group"} "option-group"
                     (get-string "lbl-group" {} lang))
         (form/drop-down {:id "option-group"} "option-group"
                         (sort
                           (map :name
                                all-option-groups))
                         option-group)
         (btn-change lang))
       (with-form post-page
         (form/hidden-field {:value true} "set-option-charge")
         (form/label {:for "option-charge"} "option-charge"
                     (get-string "lbl-extra-charge" {} lang))
         (form/text-field
           {:id "option-charge" :type "number" :step "0.01"}
           "option-charge" extra-charge)
         (btn-change lang))
       (with-form post-page
         (form/hidden-field {:value true} "set-option-in-stock")
         (form/label {:for "option-in-stock"} "option-in-stock"
                     (get-string "lbl-in-stock" {} lang))
         (form/check-box {:id "option-in-stock"}
                         "option-in-stock" in-stock?)
         (btn-change lang))])))

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
      [:admin]
      [:h5
       (with-form (str "/view-item/" id)
         (form/hidden-field {:value id} "set-item-menu-group")
         (form/label {:for "menu-group"} "menu-group"
                     (get-string "lbl-group" {} lang))
         (form/drop-down {:id "menu-group"} "menu-group"
                         (map (fn [g] [g g]) (sort all-groups))
                         group)
         [:br]
         (btn-change lang))])))

(defn render-change-item-charge
  "Page to set price of an item"
  [user id]
  (let [id               (int-or-null id)
        {iname :name
         charge :charge} (sql/retrieve-item-by-id id)
        lang             (:language user)]
    (with-page iname
      user
      [:admin]
      [:h5
       (with-form (str "/view-item/" id)
         (form/hidden-field {:value id} "set-item-charge")
         (form/label {:for "amount"} "amount"
                     (get-string "lbl-charge" {} lang))
         (form/text-field {:id "amount" :type "number" :step "0.01"}
                          "amount" charge)
         [:br]
         (btn-change lang))])))

(defn render-delete-item
  "Page to confirm item deletion"
  [user id]
  (let [id            (int-or-null id)
        {iname :name} (sql/retrieve-item-by-id id)
        lang          (:language user)]
    (with-page (get-string "str-delete/name" {:name iname} lang)
      user
      [:admin]
      [:h5 (get-string "str-wrn-on-item-delete" {} lang)
       (with-form "/list-items"
         (form/hidden-field {:value id} "delete-item")
         (btn-delete lang))])))

(defn render-add-user
  "User registration page"
  [user]
  (let [lang (:language user)]
    (with-page (get-string "str-register-user" {} lang)
      user
      [:admin]
      [:h5
       (with-form "/list-users"
         (form/hidden-field {:value true} "add-user")
         (form/label {:for "username"} "username"
                     (get-string "lbl-user-name" {} lang))
         (form/text-field {:id "username"} "username")
         [:br]
         (form/label {:for "name"} "name"
                     (get-string "lbl-full-name" {} lang))
         (form/text-field {:id "name"} "name")
         [:br]
         (form/label {:for "password"} "password"
                     (get-string "lbl-password" {} lang))
         (form/password-field {:id "password"} "password")
         [:br]
         (btn-create lang))])))

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
      [:main]
      [:span
       [:p date]
       (with-table lang
         [:date      :concept      :amount]
         ["str-date" "str-concept" "str-amount"]
         [(fn [d _] (format-date d))
          (fn [c _] [:h5 c])
          (fn [m _] (format-money m))]
         (sql/retrieve-closed-accounting id))
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
                     (get-string "fmt-per-partner" {} lang))])))

(defn render-log
  "Log page"
  [user]
  (let [lang (:language user)]
    (with-page (get-string "ln-log" {} lang)
      user
      [:admin]
      [:p (get-string "str-entries-limit" {} lang)]
      (with-table lang
        [:date      :id_app_user  :action      :details]
        ["str-date" "str-user"    "str-action" "str-detail"]
        [(fn [d _] (format-full-date d))
         (fn [i l] (let [u (sql/retrieve-user-by-id i)]
                     (make-link (str "/view-user/" i)
                                (:user_name u))))
         (fn [a _] a)
         (fn [d _] d)]
        (sql/retrieve-log)))))
