(ns tacocat.view
  (:require [hiccup.page  :as    page]
            [hiccup.form  :as    form]
            [tacocat.util :refer :all]
            [tacocat.sql  :as    sql]
            [tacocat.intl :refer [get-string]]))

(defn make-link
  "Makes the link form"
  ([address text tag]
   [tag [:a {:href address} text]])
  ([address text]
   (make-link address text :h5)))

(def link-data
  ;["/" ["admin" ["list-users" ["add-new-user"]]
  ;              ["list-roles" ["add-new-role"]]
  ;              ["list-items" ["add-new-item"]
  ;                            ["add-new-menu-group"]]]
  ;     ["home" ["bills" ["old-bills"]
  ;                      ["new-bill"]]
  ;             ["accts" ["add-expense"]
  ;                      ["close-acct"]
  ;                      ["previous-closes"]]
  ;             ["services" ["add-services-expense"]
  ;                         ["closed-services"]]]
  ;     ["system" ["login"]
  ;               ["user-info"]
  ;               ["admin-options"]
  ;               ["log"]
  ;               ["intl"]]]
  {:general    [{:destination "/"
                 :string      "ln-home"}
                {:destination "/admin"
                 :string      "ln-admin"}
                {:destination "/system"
                 :string      "ln-system"}]
   :admin      [{:destination "/list-users"
                 :string      "ln-list-users"}
                {:destination "/list-roles"
                 :string      "ln-list-roles"}
                {:destination "/list-items"
                 :string      "ln-list-items"}]
   :list-users [{:destination "/add-new-user"
                 :string      "ln-add-new-user"}]
   :list-roles [{:destination "/add-new-role"
                 :string      "ln-add-new-role"}]
   :list-items [{:destination "/add-new-item"
                 :string      "ln-create-new-item"}
                {:destination "/add-new-menu-group"
                 :string      "ln-create-new-menu-group"}]
   :home       [{:destination "/bills"
                 :string      "ln-bills"}
                {:destination "/accts"
                 :string      "ln-accts"}
                {:destination "/services"
                 :string      "ln-services"}]
   :bills      [{:destination "/new-bill"
                 :string      "ln-new-bill"}
                {:destination "/old-bills"
                 :string      "ln-old-bills"}]
   :accts      [{:destination "/add-expense"
                 :string      "ln-add-expense"}
                {:destination "/close-acct"
                 :string      "ln-close-accounting"}
                {:destination "/previous-closes"
                 :string      "ln-previous-closes"}]
   :services   [{:destination "/add-services-expense"
                 :string      "ln-add-services-expense"}
                {:destination "/closed-services"
                 :string      "ln-closed-services"}]
   :system     [;{:destination "/login"
                ; :string      "ln-login"}
                ;{:destination "/user-info"
                ; :string      "ln-user-info"}
                {:destination "/admin-options"
                 :string      "ln-admin-options"}
                {:destination "/log"
                 :string      "ln-log"}
                {:destination "/intl"
                 :string      "ln-intl"}]
   :error      [{:destination "/user-info"
                 :string      "ln-user-info"}]})

#_(def link-data
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
               c       (count data)
               percent (float (/ 100 (if (= 0 c) 1 c)))]]
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
       (get-links lang (reverse sections))
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
      [:home])))

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
  ([date tag]
   [tag
    (-> (java.text.SimpleDateFormat. "yy-MM-dd@HH:mm:ss")
        (.format date))])
  ([date]
   (format-full-date date :h5)))

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
(def btn-enter             (partial btn "btn-enter"            ))
(def btn-delete            (partial btn "btn-delete"           ))
(def btn-add               (partial btn "btn-add"              ))
(def btn-charge            (partial btn "btn-charge"           ))
(def btn-change            (partial btn "btn-change"           ))
(def btn-close             (partial btn "btn-close"            ))
(def btn-create            (partial btn "btn-create"           ))
(def btn-enter             (partial btn "btn-enter"            ))
(def btn-remove            (partial btn "btn-remove"           ))
(def btn-view-translations (partial btn "btn-view-translations"))
(def btn-copy              (partial btn "btn-copy"             ))

(defn lbl
  "Make a localised label"
  [text id lang]
  (form/label {:for id} id (get-string text {} lang)))

(def lbl-charge       (partial lbl "lbl-charge"      ))
(def lbl-concept      (partial lbl "lbl-concept"     ))
(def lbl-extra-charge (partial lbl "lbl-extra-charge"))
(def lbl-filter       (partial lbl "lbl-filter"      ))
(def lbl-from         (partial lbl "lbl-from"        ))
(def lbl-full-name    (partial lbl "lbl-full-name"   ))
(def lbl-group        (partial lbl "lbl-group"       ))
(def lbl-in-stock     (partial lbl "lbl-in-stock"    ))
(def lbl-item         (partial lbl "lbl-item"        ))
(def lbl-lang         (partial lbl "lbl-lang"        ))
(def lbl-location     (partial lbl "lbl-location"    ))
(def lbl-name         (partial lbl "lbl-name"        ))
(def lbl-new-group    (partial lbl "lbl-new-group"   ))
(def lbl-new-option   (partial lbl "lbl-new-option"  ))
(def lbl-new-password (partial lbl "lbl-new-password"))
(def lbl-option       (partial lbl "lbl-option"      ))
(def lbl-password     (partial lbl "lbl-password"    ))
(def lbl-role         (partial lbl "lbl-role"        ))
(def lbl-to           (partial lbl "lbl-to"          ))
(def lbl-user-name    (partial lbl "lbl-user-name"   ))

(defn tf
  "Makes a text-field"
  ([id default]
   (form/text-field {:id id} id default))
  ([id]
   (tf id nil)))

(defn nf
  "Makes a number field"
  ([id default]
   (form/text-field {:id id :type "number" :step "0.01"}
                    id default))
  ([id]
   (nf id nil)))

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
         (lbl-user-name "user-name" lang)
         (tf "user-name" (:user_name user))
         [:br]
         (lbl-password "password" lang)
         (form/password-field {:id "password"} "password")
         [:br]
         (btn-enter lang))])))

(defn render-delete-user
  "Delete user page"
  [user id]
  (let [lang (:language user)
        u    (-> id int-or-null sql/retrieve-user-by-id)]
    (with-page (get-string "str-delete-user/name" u lang)
      user
      [:admin :list-users]
      [:h5
       (with-form "/list-users"
         (form/hidden-field {:value id} "delete-user")
         (btn-delete lang))])))

(defn render-change-user-name
  "Screen to change users full name"
  [user id]
  (let [u    (-> id int-or-null sql/retrieve-user-by-id)
        lang (:language user)]
    (with-page (get-string "str-change-user-name/name" u lang)
      user
      [:admin :list-users]
      [:h5
       (with-form (str "/user-info/" id)
         (form/hidden-field {:value id} "change-user-name")
         (lbl-full-name "name" lang)
         (tf "name" (:name u))
         [:br]
         (btn-change lang))])))

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
      [:h5
       (with-form (str "/user-info/" id)
         (form/hidden-field {:value id} "set-user-language")
         (lbl-lang "language" lang)
         (form/drop-down
           {:id "language"} "language"
           (map (fn [{n :name f :full_name}]
                  [(get-string f {} lang) n])
                langs)
           t-lang)
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
           [:system]
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
      [:admin :list-users]
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
      [:admin :list-roles]
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
      [:admin :list-roles]
      [:h5
       (with-form "/list-roles"
         (form/hidden-field {:value true} "add-role")
         (lbl-role "role" lang)
         (tf "role")
         [:br]
         (btn-add lang))])))

(defn render-delete-role
  "Delete role page"
  [user id]
  (with-page (get-string "str-delete-role/name"
                         (-> id int-or-null sql/retrieve-role-by-id)
                         (:language user))
    user
    [:admin :list-roles]
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
      [:admin :list-items]
      [:h5
       (with-form "/list-items"
         (form/hidden-field {:value true} "add-new-menu-group")
         (lbl-name "menu-group-name" lang)
         (tf "menu-group-name")
         [:br]
         (btn-add lang))])))

(defn render-add-new-item
  "New item page"
  [user]
  (let [lang (:language user)]
    (with-page (get-string "str-add-item" {} lang)
      user
      [:admin :list-items]
      [:h5
       (with-form "/list-items"
         (form/hidden-field {:value true} "add-new-item")
         (lbl-item "item-name" lang)
         (tf "item-name")
         [:br]
         (lbl-group "menu-group" lang)
         (form/drop-down {:id "menu-group"} "menu-group"
                         (map (fn [g] [g g])
                           (sort
                             (map :name
                               (sql/retrieve-menu-groups)))))
         [:br]
         (lbl-charge "amount" lang)
         (nf "amount")
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
      [:admin :list-items]
      [:h5
       (with-form (str "/view-item/" id)
         (form/hidden-field {:value id} "set-item-name")
         (lbl-item "name" lang)
         (tf "name" iname)
         (btn-change lang))
       
       (with-form (str "/view-item/" id)
         (form/hidden-field {:value id} "set-item-menu-group")
         (lbl-group "menu-group" lang)
         (form/drop-down {:id "menu-group"} "menu-group"
                         (map (fn [g] [g g])
                              (sort (map :name menu-groups)))
                         mgroup)
         (btn-change lang))
       
       (with-form (str "/view-item/" id)
         (form/hidden-field {:value id} "set-item-charge")
         (lbl-charge "amount" lang)
         (nf "amount" amount)
         (btn-change lang))
       
       (with-form (str "/view-item/" id)
         (form/hidden-field {:value id} "set-item-in-stock")
         (lbl-in-stock "in-stock" lang)
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
        (get-string "str-other-options" {} lang)]
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
      [:system]
      [:h5
       (with-form (str "/user-info/" id)
         (form/hidden-field {:value true} "change-user-password")
         (lbl-new-password "password" lang)
         (form/password-field {:id "password"} "password")
         [:br]
         (btn-change lang))])))

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
  (let [lang  (:language user)
        langs (sql/retrieve-langs)]
    (with-page (get-string "str-internationalisation" {} lang)
      user
      [:system]
      (with-form "/intl"
        (lbl-from "lang-from" lang)
        (form/drop-down {:id "lang-from"} "lang-from"
                        (map (fn [{l :full_name n :name}]
                               [(get-string l {} lang) n])
                             langs)
                        lang-from)
        [:br]
        (lbl-to "lang-to" lang)
        (form/drop-down {:id "lang-to"} "lang-to"
                        (map (fn [{l :full_name n :name}]
                               [(get-string l {} lang) n])
                             langs)
                        lang-to)
        [:br]
        (lbl-filter "filter" lang)
        (tf "filter" ffilter)
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
                (tf "value" v)
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
      [:system]
      [:h5
       (with-form "/admin-options"
         (form/hidden-field {:value true} "make-admin-changes")
         (with-table lang
           [:key      :val]
           ["str-key" "str-val"]
           [(fn [k _] [:h5 k])
            (fn [v o] [:h5 (tf (:key o) v)])]
           (sql/retrieve-app-data))
         (btn-change lang))])))

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
                     (format-bool b (:id i))
                     (btn-change lang)))
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
      (make-services-table user (sql/retrieve-current-services)))))

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
      [:home :bills]
      (get-existing-bills user))))

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
        [:h5 (btn-close lang)]))))

(defn render-add-services-expense
  "Makes a form to add a service expense"
  [user]
  (let [lang (:language user)]
    (with-page (get-string "str-add-services-charge" {} lang)
      user
      [:home :services]
      (with-form "/services"
        (form/hidden-field {:value true} "add-service-charge")
        [:h5
         (lbl-concept "concept" lang)
         (tf "concept")
         [:br]
         (lbl-charge "amount" lang)
         (nf "amount")
         [:br]
         (btn-add lang)]))))

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
       (fn [i c] [:div
                  (make-link (str "/set-bill-item/" (:id c)) i)
                  (if (= 0 (op-cnt (:id c)))
                    nil
                    (ln-fn (:id c) (:options c) lang))])
       (fn [m c] (make-link (str "/set-charge-override/" (:id c))
                            (format-money m)))
       (fn [i _] (with-form (str "/bill/" id)
                   (form/hidden-field {:value id} "replicate-bill-item")
                   (form/hidden-field {:value i} "id-item")
                   (btn-copy lang)))
       (fn [i _] (make-link (str "/delete-bill-item/" i)
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
      [:home :bills]
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
      [:home :bills]
      (with-form (str "/bill/" id)
        (form/hidden-field {:value id} "edit-bill-location")
        [:h5
         (tf "location" location)
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
      [:home :accts]
      (with-form "/accts"
        (form/hidden-field {:value true} "add-expense")
        [:h5
         (lbl-concept "concept" lang)
         (tf "concept")
         [:br]
         (lbl-charge "amount" lang)
         (nf "amount")
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
      [:home :bills]
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
      [:home :bills]
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
      [:home :bills]
      (with-form (str "/bill/" id-bill)
        (form/hidden-field {:value id} "id-bill-item")
        [:h5
         (form/drop-down
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
      [:home :bills]
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
      [:home :bills]
      (with-form (str "/bill/" id-bill)
        (form/hidden-field {:value id} "id-bill-item")
        (form/hidden-field {:value true} "set-options")
        [:h5
         (let [innerfn (fn [{n :option_name
                             i :id_option}]
                         [:h5
                          (form/label {:for i} n (str " " n))
                          (form/check-box
                            {:id i} i (contains? current-options i))])
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
      [:home :bills]
      (with-form (str "/bill/" id-bill)
        (form/hidden-field {:value id} "id-bill-item")
        [:h5
         (nf "charge-override" charge)
         [:br]
         (btn-change lang)]))))

(defn render-add-item
  "Displays a page where we add an item to a bill"
  [user id]
  (let [lang (:language user)]
    (with-page (get-string "ln-add-to-bill" {} lang)
      user
      [:home :bills]
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
      [:home :bills]
      (with-form "/bills"
        (form/hidden-field {:value true} "new-bill")
        [:h5
         (lbl-location "bill-location" lang)
         (tf "bill-location")
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
      [:home :bills]
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
      [:home :accts]
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
         (format-money
           total    (get-string "fmt-total" {} lang) :h2)
         (format-money
           expenses (get-string "fmt-expenses" {} lang))
         (format-money
           intakes  (get-string "fmt-intakes" {} lang))]))))

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
         (fn [i _] (map (fn [p] [:h6 p])
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
      [:h5
       (with-form (str "/view-item/" id)
         (form/hidden-field {:value true} "add-new-option-group")
         (lbl-new-group "new-option-group" lang)
         (tf "new-option-group")
         (btn-create lang))])))

  (defn render-new-option
  "Page to add an option"
  [user id]
  (let [lang (:language user)]
    (with-page (get-string "str-new-option" {} lang)
      user
      [:admin :list-items]
      [:h5
       (with-form (str "/view-item/" id)
         (form/hidden-field {:value true} "add-new-option")
         (lbl-new-option "new-option" lang)
         (tf "new-option")
         [:br]
         (lbl-group "option-group" lang)
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
      [:admin :list-items]
      [:h5
       (with-form post-page
         (form/hidden-field {:value true} "set-option-name")
         (lbl-option "option-name" lang)
         (tf "option-name" oname)
         (btn-change lang))
       (with-form post-page
         (form/hidden-field {:value true} "set-option-group")
         (lbl-group "option-group" lang)
         (form/drop-down {:id "option-group"} "option-group"
                         (sort
                           (map :name
                                all-option-groups))
                         option-group)
         (btn-change lang))
       (with-form post-page
         (form/hidden-field {:value true} "set-option-charge")
         (lbl-extra-charge "option-charge" lang)
         (nf "option-charge" extra-charge)
         (btn-change lang))
       (with-form post-page
         (form/hidden-field {:value true} "set-option-in-stock")
         (lbl-in-stock "option-in-stock" lang)
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
      [:admin :list-items]
      [:h5
       (with-form (str "/view-item/" id)
         (form/hidden-field {:value id} "set-item-menu-group")
         (lbl-group "menu-group" lang)
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
      [:admin :list-items]
      [:h5
       (with-form (str "/view-item/" id)
         (form/hidden-field {:value id} "set-item-charge")
         (lbl-charge "amount" lang)
         (nf "amount" charge)
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
      [:admin :list-items]
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
      [:admin :list-users]
      [:h5
       (with-form "/list-users"
         (form/hidden-field {:value true} "add-user")
         (lbl-user-name "username" lang)
         (tf "username")
         [:br]
         (lbl-full-name "name" lang)
         (tf "name")
         [:br]
         (lbl-password "password" lang)
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
      [:home :accts]
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
      [:system]
      [:p (get-string "str-entries-limit" {} lang)]
      (with-table lang
        [:date      :id_app_user  :action      :details]
        ["str-date" "str-user"    "str-action" "str-detail"]
        [(fn [d _] [:small (format-full-date d :p)])
         (fn [i l] [:small (let [u (sql/retrieve-user-by-id i)]
                             (make-link (str "/view-user/" i)
                                        (:user_name u) :p))])
         (fn [a _] [:small a])
         (fn [d _] [:small d])]
        (sql/retrieve-log)))))
