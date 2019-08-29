(ns tacocat.view
  (:require [hiccup.page  :as page]
            [hiccup.form  :as form]
            [tacocat.util :refer :all]
            [tacocat.sql  :as sql]))

(defn make-link
  "Makes the link form"
  [address text]
  [:h5 [:a {:href address} text]])

(def links
  {:general [:tr
             [:th {:width "20%"} (make-link "/"         "Inicio")]
             [:th {:width "20%"} (make-link "/bills"    "Comandas")]
             [:th {:width "20%"} (make-link "/accts"    "Cuentas")]
             [:th {:width "20%"} (make-link "/services" "Servicios")]
             [:th {:width "20%"} (make-link "/admin"    "Admin")]]
   :main    [:tr
             [:th {:width "33%"} (make-link "/old-bills"       "Entradas")]
             [:th {:width "34%"} (make-link "/closed-services" "Servicios Pasados")]
             [:th {:width "33%"} (make-link "/previous-closes" "Cierres")]]
   :admin   [:tr
             [:th {:width "20%"} (make-link "/admin-options" "Opciones")]
             [:th {:width "20%"} (make-link "/list-users"    "Usuarios")]
             [:th {:width "20%"} (make-link "/list-roles"    "Roles")]
             [:th {:width "20%"} (make-link "/list-items"    "Productos")]
             [:th {:width "20%"} (make-link "/log"           "Registro")]]
   :error   [:tr
             [:th {:width "100%"} (make-link "/user-info" "Información del Usuario")]]})

(defn with-page
  "Adds content to a page"
  [header user section & content]
  (page/html5
    [:head
     [:link {:rel  "apple-touch-icon"       :sizes "180x180" :href "/apple-touch-icon.png"}]
     [:link {:rel  "icon" :type "image/png" :sizes "32x32"   :href "/favicon-32x32.png"}]
     [:link {:rel  "icon" :type "image/png" :sizes "16x16"   :href "/favicon-16x16.png"}]
     [:link {:rel  "manifest"                                :href "/site.webmanifest"}]
     [:link {:rel  "mask-icon"                               :href "/safari-pinned-tab.svg" :color "#5bbad5"}]
     [:meta {:name "msapplication-TileColor" :content "#da532c"}]
     [:meta {:name "theme-color"             :content "#ffffff"}]
     [:meta {:name "viewport"                :content "width=device-width, initial-scale=1.0"}]
     [:meta {:charset "UTF-8"}]
     (page/include-css "https://www.w3schools.com/w3css/4/w3pro.css"
                       "https://www.w3schools.com/lib/w3-theme-grey.css"
                       "/css/style.css"
                       "/fonts/style.css")
     [:style (str "h1, h2, h3, h4, h5, h6, div, p, th, td, tr {font-family: "
                  (let [font (sql/retrieve-app-data-val "default-font")]
                    (if (or (nil? font) (empty? font))
                      ""
                      (str \" font "\", ")))
                  "Verdana, sans-serif;}")]
     [:title header]]
    [:body
     [:center
      [:table {:width "100%" :style "w3-cell w3-container" :cellpadding 10}
       (:general links)]
      [:table {:width "100%" :style "w3-cell w3-container" :cellpadding 10}
       (section  links)]]
     [:header {:class "w3-container w3-card w3-theme-l4"} [:center [:h1 header]]]
     [:center content]
     [:center
      [:table {:width "100%" :style "w3-cell w3-container" :cellpadding 10}
       (section  links)]
      [:table {:width "100%" :style "w3-cell w3-container" :cellpadding 10}
       (:general links)]]
     [:header {:class "w3-container w3-card w3-theme-l4"}
      [:center
       [:h5
        (if (nil? user)
          (make-link "/login" "Entrar")
          [:span
           (make-link "/user-info" user)
           (make-link "/login" "Cambiar")])]]]]))

(defn with-printing-page
  "Makes a page suitable for printing"
  [header id & content]
  (page/html5
    [:head
     [:link {:rel  "apple-touch-icon"       :sizes "180x180" :href "/apple-touch-icon.png"}]
     [:link {:rel  "icon" :type "image/png" :sizes "32x32"   :href "/favicon-32x32.png"}]
     [:link {:rel  "icon" :type "image/png" :sizes "16x16"   :href "/favicon-16x16.png"}]
     [:link {:rel  "manifest"                                :href "/site.webmanifest"}]
     [:link {:rel  "mask-icon"                               :href "/safari-pinned-tab.svg" :color "#5bbad5"}]
     [:meta {:name "msapplication-TileColor" :content "#da532c"}]
     [:meta {:name "theme-color"             :content "#ffffff"}]
     [:meta {:name "viewport"                :content "width=device-width, initial-scale=1.0"}]
     [:meta {:charset "UTF-8"}]
     (page/include-css ;"https://www.w3schools.com/w3css/4/w3pro.css"
                       ;"https://www.w3schools.com/lib/w3-theme-grey.css"
                       "/css/style.css"
                       "/fonts/style.css"
                       "/css/printing-style.css")
     [:style (str "h1, h2, h3, h4, h5, h6, div, p, th, td, tr {font-family: "
                  (let [font (sql/retrieve-app-data-val "default-font")]
                    (if (or (nil? font) (empty? font))
                      ""
                      (str \" font "\", ")))
                  "Verdana, sans-serif;}")]
     [:title header]] 
    [:body
     [:center
      [:header {:class "w3-container w3-card"}
       [:h1 (sql/retrieve-app-data-val "business-name")]]
      [:small
       [:p
        (sql/retrieve-app-data-val "business-address") ", "
        (sql/retrieve-app-data-val "business-post-code") ", "
        (sql/retrieve-app-data-val "business-state")
        [:br]        
        "Tel: " (sql/retrieve-app-data-val "business-telephone")
        [:br]
        (sql/retrieve-app-data-val "business-website")]]
      [:header {:class "w3-container w3-card"}
       [:h2 "Cuenta para: " header " (#" id ")"]]
      content]
     [:script {:language "javascript"} "window.print();"]]))

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
  ;(NOT-ALLOWED (:name user)))
  (with-page (sql/retrieve-app-data-val "business-name")
    (:name user)
    :main
    (make-link "/bills"    "Comandas")
    (make-link "/accts"    "Cuentas" )
    (make-link "/services" "Servicios")
    (make-link "/admin"    "Admin")))

(defn headers
  "Make the [:th ...] section of a table"
  [hh]
  (-> (fn [h] [:th h])
      (map hh)
      (conj :tr)
      vec))

(defn with-table
  "Makes a [:table [:tr [:td ... ]]] structure out of a list of maps
  by applying the respective function to each.
 
  Each function must be able to take two arguments: The column content and the context"
  [columns column-display functions list-of-maps]
  (-> (fn [d]
        (-> (fn [c f] [:td {:valign "top"} (f (get d c) d)])
            (map columns functions)
            (conj :tr)
            vec))
      (map list-of-maps)
      (conj (headers column-display)
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
  (with-page "Acción Prohibida"
    (:name user)
    :error
    [:h2 {:style "color: red;"} "Permisos Insuficientes"]
    (if (not (empty? permissions-required))
      (with-table
        [:permissions]
        ["Permisos"]
        [(fn [p _] (map (fn [p] [:h6 p]) (sort p)))]
        [{:permissions permissions-required}]))
    (make-link "/login" "Cambiar Usuario")))

(defn format-bill-list
  "Formats a list of bills"
  [bill-link-root bill-data editable?]
  (with-table
    [:date   :location :charge :id]
    ["Fecha" "Mesa"    "Monto" ""]
    [(fn [d _] (format-date d))
     (fn [l c] (if editable?
                 (make-link (str "/edit-location/" (:id c)) l)
                 [:h5 l]))
     (fn [c _] (format-money c))
     (fn [i _] (make-link (str bill-link-root i) "Mostrar"))]
    bill-data))

(defn make-services-table
  "Makes a services table"
  [services-resultset]
  (with-table
    [:date   :concept   :amount :running_total]
    ["Fecha" "Concepto" "Monto" "Nuevo Balance"]
    [(fn [d _] (format-date d))
     (fn [c _] [:h5 c])
     (fn [m _] (format-money m))
     (fn [m _] (format-money m))]
    services-resultset))

(defn render-login
  "Show the login page"
  [user]
  (with-page "Registro de Usuario"
    (:name user)
    :error
    [:h5
     (with-form "/user-info"
       (form/hidden-field {:value true} "perform-login")
       (form/label "usuario" "Usuario: ")
       (form/text-field "user-name" (:user_name user))
       [:br]
       (form/label "contraseña" "Contraseña: ")
       (form/password-field "password")
       [:br]
       (form/submit-button "Entrar"))]))

(defn render-delete-user
  "Delete user page"
  [user id]
  (with-page (str "Borrar Usuario " (-> id int-or-null sql/retrieve-user-by-id :name))
    (:name user)
    :admin
    [:h5
     (with-form "/list-users"
       (form/hidden-field {:value id} "delete-user")
       (form/submit-button "Borrar"))]))

(defn render-change-user-name
  "Screen to change users full name"
  [user id]
  (with-page (str "Cambiar Nombre de" (-> id int-or-null sql/retrieve-user-by-id :name))
    (:name user)
    :admin
    [:h5
     (with-form (str "/user-info/" id)
       (form/hidden-field {:value id} "change-user-name")
       (form/label {:for id} "nombre" "Nombre Completo: ")
       (form/text-field {:id id} "name")
       [:br]
       (form/submit-button "Cambiar"))]))

(defn render-user-info
  "Shows user info page or login error"
  ([user id]
   (if (empty? user)
     (with-page "Error"
       nil
       :error
       [:h2 {:style "color: red;"} "Usuario o Contraseña incorrecta"])
     (let [id                         (int-or-null id)
           id                         (if (nil? id) (:id user) id)
           {uname       :name
            user-name   :user_name
            permissions :permissions} (sql/retrieve-user-by-id id) ;user
           user-roles                 (sql/retrieve-roles-for-user id)
           machines                   (sql/retrieve-logged-in-to id)]
       (with-page "Información de Ususario"
         (:name user)
         :admin
         [:h5 "Nombre de Usuario: " user-name]
         (make-link (str "/change-user-name/"  id) (str "Nombre Completo: " uname))
         (make-link (str "/change-password/"   id) "Cambiar Contraseña")
         (make-link (str "/change-user-roles/" id) "Cambiar Roles")
         (with-table
           [:roles  :permissions :machines]
           ["Roles" "Permisos"   "Máquinas"]
           [(fn [r _] (map (fn [r] (make-link (str "/view-role/" (:id r)) (:name r))) r))
            (fn [p _] (map (fn [p] [:h6 p]) (sort p)))
            (fn [m _] (map (fn [m] [:h5 m]) (sort m)))]
           [{:roles user-roles :machines machines :permissions permissions}])))))
  ([user]
   (render-user-info user (:id user))))

(defn render-change-user-roles
  "Role selection and assignment page for a user"
  [user id]
  (let [id            (int-or-null id)
        id-user-roles (into #{} (map :id (sql/retrieve-roles-for-user id)))
        all-roles     (sql/retrieve-all-roles)
        edit-user     (sql/retrieve-user-by-id id)]
    (with-page (str "Roles para " (:name edit-user))
      (:name user)
      :admin
      [:h5
       (with-form (str "/user-info/" id)
         (form/hidden-field {:value true} "set-user-roles")
         (with-table
           [:name :id]
           ["Rol" ""]
           [(fn [n r] (form/label {:for (:id r)} (:name r) (:name r)))
            (fn [i _] (form/check-box {:id i} i (contains? id-user-roles i)))]
           all-roles)
         (form/submit-button "Cambiar"))])))

(defn render-view-role
  "Role page"
  [user id]
  (let [id            (int-or-null id)
        {rname :name} (sql/retrieve-role-by-id id)
        all-perms     (sql/retrieve-all-permissions)
        role-perms    (sql/retrieve-permissions-for-role id)]
    (with-page rname
      (:name user)
      :admin
      (with-table
        [:name     :id]
        ["Permiso" ""]
        [(fn [n _] [:h5 n])
         (fn [i r] (with-form (str "/view-role/" id)
                     (form/hidden-field {:value i} "change-permission")
                     (form/check-box i (contains? role-perms (:name r)))
                     (form/submit-button "Cambiar")))]
        all-perms))))

(defn render-add-new-role
  "Add role page"
  [user]
  (with-page "Añadir Rol"
    (:name user)
    :admin
    [:h5
     (with-form "/list-roles"
       (form/hidden-field {:value true} "add-role")
       (form/label {:for "role"} "role" "Rol: ")
       (form/text-field {:id "role"} "role")
       [:br]
       (form/submit-button "Añadir"))]))

(defn render-delete-role
  "Delete role page"
  [user id]
  (with-page (str "Borrar Rol: " (-> id int-or-null sql/retrieve-role-by-id :name))
    (:name user)
    :admin
    [:h5
     (with-form "/list-roles"
       (form/hidden-field {:value id} "delete-role")
       (form/submit-button "Borrar"))]))

(defn render-add-new-menu-group
  "New Menu Group page"
  [user]
  (with-page "Añadir Grupo de Menú"
    (:name user)
    :admin
    [:h5
     (with-form"/list-items"
       (form/hidden-field {:value true} "add-new-menu-group")
       (form/label {:for "menu-group-name"} "menu-group-name" "Nombre: ")
       (form/text-field {:id "menu-group-name"} "menu-group-name")
       [:br]
       (form/submit-button "Añadir"))]))

(defn render-add-new-item
  "New item page"
  [user]
  (with-page "Añadir Producto"
    (:name user)
    :admin
    [:h5
     (with-form "/list-items"
       (form/hidden-field {:value true} "add-new-item")
       (form/label {:for "item-name"} "item-name" "Producto: ")
       (form/text-field {:id "item-name"} "item-name")
       [:br]
       (form/label {:for "menu-group"} "menu-group" "Grupo: ")
       (form/drop-down {:id "menu-group"} "menu-group"
                       (map (fn [g] [g g])
                            (sort
                              (map :name
                                   (sql/retrieve-menu-groups)))))
       [:br]
       (form/label {:for "amount"} "amount" "Monto: ")
       (form/text-field {:id "amount" :type "number" :step "0.01"} "amount")
       [:br]
       (form/submit-button "Añadir"))]))

(defn with-options-table
  "Make a table for showing item options"
  [id options action]
  (with-table
    [:name    :in_stock       :id]
    ["Opción" "En Inventario" ""]
    [(fn [n {i :id g :option_group m :extra_charge}]
       (make-link (str "/view-option/" i) (str g "/" n " ($" m ")")))
     (fn [b o] (with-form (str "/view-item/" id)
                 (form/hidden-field {:value (:id o)} "set-option-in-stock")
                 (form/check-box "set-option" b)
                 (form/submit-button "Cambiar")))
     action]
    options))

(defn render-view-item
  "Shows an items details"
  [user id]
  (let [{id        :id
         iname     :name
         mgroup    :menu_group
         amount    :charge
         in-stock? :in_stock} (sql/retrieve-item-by-id (int-or-null id))
        menu-groups           (sql/retrieve-menu-groups)
        item-options          (into #{} (map :id_option (sql/retrieve-valid-options id)))
        all-options           (sql/retrieve-all-options)]
    (with-page iname
      (:name user)
      :admin
      [:h5
       (with-form (str "/view-item/" id)
         (form/hidden-field {:value id} "set-item-name")
         (form/label {:for "name"} "name" "Producto: ")
         (form/text-field {:id "name"} "name" iname)
         (form/submit-button "Cambiar"))
       (with-form (str "/view-item/" id)
         (form/hidden-field {:value id} "set-item-menu-group")
         (form/label {:for "menu-group"} "menu-group" "Grupo: ")
         (form/drop-down {:id "menu-group"} "menu-group"
                         (map (fn [g] [g g])
                              (sort (map :name menu-groups)))
                         mgroup)
         (form/submit-button "Cambiar"))
       (with-form (str "/view-item/" id)
         (form/hidden-field {:value id} "set-item-charge")
         (form/label {:for "amount"} "amount" "Monto: ")
         (form/text-field {:id "amount" :type "number" :step "0.01"} "amount" amount)
         (form/submit-button "Cambiar"))
       (with-form (str "/view-item/" id)
         (form/hidden-field {:value id} "set-item-in-stock")
         (form/label {:for "in-stock"} "in-stock" "En Inventario: ")  
         (form/check-box {:id "in-stock"} "in-stock" in-stock?)
         (form/submit-button "Cambiar"))
       (make-link (str "/create-new-option-group/" id) "Crear Nuevo Grupo de Opciones")
       (make-link (str "/create-new-option/"       id) "Crear Nueva Opción")
       [:h4 {:class "w3-container w3-card"} "Opciones del Producto"]
       (with-options-table id
         (filter (fn [{i :id}] (contains? item-options i)) all-options)
         (fn [i _] (with-form (str "/view-item/" id)
                      (form/hidden-field {:value i} "remove-option-from-item")
                      (form/submit-button "Remover"))))
       [:h4 {:class "w3-container w3-card"} "Resto de las Opciones"]
       (with-options-table id
         (filter (fn [{i :id}] (not (contains? item-options i))) all-options)
         (fn [i _] (with-form (str "/view-item/" id)
                      (form/hidden-field {:value i} "add-option-to-item")
                      (form/submit-button "Añadir"))))
       ])))

(defn render-change-user-password
  "User password change screen"
  [user id]
  (let [u (sql/retrieve-user-by-id (int-or-null id))]
    (with-page (str "Change Password for " (:name u))
      (:name user)
      :admin
      [:h5
       (with-form (str "/user-info/" id)
         (form/hidden-field {:value true} "change-user-password")
         (form/label "password" "Nueva Contraseña: ")
         (form/password-field "password")
         [:br]
         (form/submit-button "Cambiar"))])))

(defn render-admin
  "Gets the admin page"
  [user]
  (with-page "Admin"
    (:name user)
    :admin
    (make-link "/admin-options" "Opciones de la Aplicación")
    (make-link "/list-users"    "Usuarios")
    (make-link "/list-roles"    "Roles")
    (make-link "/list-items"    "Productos")
    (make-link "/log"           "Registro")))

(defn render-app-options
  "Gets the app options page"
  [user]
  (with-page "Opciones de la Aplicación"
    (:name user)
    :admin
    [:h5
     (with-form "/admin-options"
       (form/hidden-field {:value true} "make-admin-changes")
       (with-table
         [:key     :val]
         ["Opción" "Valor"]
         [(fn [k _] [:h5 k])
          (fn [v o] [:h5 (form/text-field (:key o) v)])]
         (sql/retrieve-app-data))
       (form/submit-button "Cambiar"))]))

(defn render-list-items
  "Gets the product page"
  [user]
  (with-page "Productos"
    (:name user)
    :admin
    (make-link "/add-new-item"       "Añadir Producto")
    (make-link "/add-new-menu-group" "Añadir Grupo de Menú")
    (with-table
      [:name      :menu_group     :charge      :in_stock       :id]
      ["Producto" "Grupo de Menú" "Costo Base" "En Inventario" ""]
      [(fn [p i] (make-link (str "/view-item/"              (:id i)) p))
       (fn [g i] (make-link (str "/change-item-menu-group/" (:id i)) g))
       (fn [m i] (make-link (str "/change-item-charge/"     (:id i)) (format-money m)))
       (fn [b i] (with-form "/list-items"
                   (form/hidden-field {:value (:id i)} "change-in-stock")
                   (format-bool b (:id i))
                   (form/submit-button "Cambiar")))
       (fn [i _] (make-link (str "/delete-item/"            i)       "Borrar"))]
      (sql/retrieve-all-items))
    (make-link "/add-new-item"       "Añadir Producto")
    (make-link "/add-new-menu-group" "Añadir Grupo de Menú")))

(defn render-services
  "Services screen"
  [user]
  (with-page "Servicios"
    (:name user)
    :main
    (make-link "/add-services-expense" "Añadir Cargo de Servicios")
    (make-services-table (sql/retrieve-current-services))
    (make-link "/add-services-expense" "Añadir Cargo de Servicios")))

(defn render-closed-services
  "Services for prior closes"
  [user]
  (with-page "Servicios de Cierres Anteriores"
    (:name user)
    :main
    (with-table
      [:date   :id]
      ["Fecha" "Cierre"]
      [(fn [d _] (format-date d))
       (fn [i _] (make-link (str "/services-for-close/" i) (str "Cierre " i)))]
      (sql/retrieve-previous-closes))))

(defn render-services-for-close
  "Services page for a specific close"
  [user id]
  (with-page (str "Servicios de Cierre " id)
    (:name user)
    :main
    (make-services-table (sql/retrieve-services-for-close id))))

(defn get-existing-bills
  "Returns a structure containing existing bills"
  []
  (format-bill-list "/bill/" (sql/retrieve-bills) true))

(defn get-closed-bills
  "Returns a structure containing existing bills"
  []
  (format-bill-list "/closed-bill/" (sql/retrieve-closed-bills) false))

(defn render-bills
  "Shows the bills page"
  [user]
  (with-page "Comandas"
    (:name user)
    :main
    (make-link "new-bill"  "Nueva Comanda")
    (get-existing-bills)
    (make-link "new-bill"  "Nueva Comanda")))

(defn render-previous-closes
  "Shows the previous closes"
  [user]
  (with-page "Cierres"
    (:name user)
    :main
    (with-table
      [:date   :expense_amount :intake_amount :earnings  :id]
      ["Fecha" "Gastos"        "Entradas"     "Ganancia" ""]
      [(fn [d _] (format-date d))
       (fn [m _] (format-money m))
       (fn [m _] (format-money m))
       (fn [m _] (format-money m))
       (fn [i _] (make-link (str "/close/" i) "Mostrar"))]
      (sql/retrieve-previous-closes))))

(defn render-close-acct
  "Shows the close current accounting period page"
  [user]
  (with-page "Cierre de Cuentas"
    (:name user)
    :main
    (with-form "/previous-closes"
      (form/hidden-field {:value true} "make-close")
      [:h5 (form/submit-button "Cerrar")])))

(defn render-add-services-expense
  "Makes a form to add a service expense"
  [user]
  (with-page "Añadir Cargo de Servicios"
    (:name user)
    :main
    (with-form "/services"
      (form/hidden-field {:value true} "add-service-charge")
      [:h5
       (form/label "Concepto: " "Concepto: ")
       (form/text-field "concept")
       [:br]
       (form/label "Monto: " "Monto: ")
       (form/text-field {:type "number" :step "0.01"} "amount")
       [:br]
       (form/submit-button "Añadir")])))

(defn render-old-bills
  "Shows the closed bills page"
  [user]
  (with-page "Comandas Cerradas"
    (:name user)
    :main
    (get-closed-bills)))

(defn get-bill-items
  "Returns a form with the bill items of a bill"
  [id]
  (with-table
    [:date  :person   :item      :charge :nil]
    ["Hora" "Persona" "Producto" "Monto" ""]
    [(fn [t _] (format-time t))
     (fn [p c] (make-link (str "/set-person/" (:id c))
                          (if (nil? p)
                            "Asignar"
                            (str "Persona " p))))
     (fn [i c] [:div
                (make-link (str "/set-bill-item/" (:id c)) i)
                (if (= 0 (sql/retrieve-bill-item-option-count (:id c)))
                  nil
                  (let [options (:options c)]
                    [:span
                     (make-link (str "/set-bill-item-options/" (:id c))
                                [:small
                                 (if (nil? options)
                                   "Añadir Opciones"
                                   options)])]))])
     (fn [m c] (make-link (str "/set-charge-override/" (:id c))
                          (format-money m)))
     (fn [_ c] (make-link (str "/delete-bill-item/" (:id c)) "Borrar"))]
    (sql/retrieve-bill-items id)))

(defn render-delete-bill-item
  "Returns a form where a bill_item can be deleted"
  [user id]
  (let [{person  :person
         item    :item
         options :options
         id-bill :id_bill} (sql/retrieve-bill-item id)]
    (with-page (str "Borrar " item)
      (:name user)
      :main
      [:h5
       (with-form (str "/bill/" id-bill)
         (form/hidden-field {:value id} "delete-bill-item")
         (form/submit-button "Borrar"))])))

(defn render-edit-bill-location
  "Returns the edit location for a bill form"
  [user id]
  (let [{location :location} (sql/retrieve-bill id)]
    (with-page "Cambiar Mesa"
      (:name user)
      :main
      (with-form (str "/bill/" id)
        (form/hidden-field {:value id} "edit-bill-location")
        [:h5
         (form/text-field "location" location)
         [:br]
         (form/submit-button "Cambiar")]))))

(defn get-old-bill-items
  "Returns a form with the bill items of a closed bill"
  ([id tag sep]
   (with-table
     [:date  :person :item      :charge]
     [""     "P#"    "Producto" "Monto"]
     [(fn [t _] (format-time t tag))
      (fn [p _] [tag (if (nil? p) "" [:center p])])
      (fn [i c] [tag i " " 
                 (let [options (:options c)]
                   (if (nil? options)
                     nil
                     [sep [:small (:options c)]]))])
      (fn [m c] (format-money m "" tag))]
     (sql/retrieve-bill-items id)))
  ([id tag]
   (get-old-bill-items id tag tag))
  ([id]
   (get-old-bill-items id :h5)))

(defn render-new-expense
  "Renders a new expense form"
  [user]
  (with-page "Añadir Gasto"
    (:name user)
    :main
    (with-form "/accts"
      (form/hidden-field {:value true} "add-expense")
      [:h5
       (form/label "" "Concepto: ")
       (form/text-field "concept")
       [:br]
       (form/label "" "Monto: ")
       (form/text-field {:type "number" :step "0.01"} "amount")
       [:br]
       (form/submit-button "Añadir")])))

(defn render-closed-bill
  "Renders a single closed bill"
  [user id]
  (let [{date     :date
         location :location
         charge   :charge}  (sql/retrieve-bill id)]
    (with-page location
      (:name user)
      :main
      (make-link (str "/print-bill/" id) "Imprimir")
      (get-old-bill-items id)
      (format-money charge "Total:" :h2)
      (with-table
        [:person   :charge]
        ["Persona" "Total"]
        [(fn [p _] [:h5 (if (nil? p)
                          ""
                          (str "Persona " p))])
         (fn [m _] (format-money m))]
        (sql/retrieve-bill-charges-per-person id))
      [:p date]
      (make-link (str "/print-bill/" id) "Imprimir"))))

(defn render-print-bill
  [user id]
  (let [id                  (int-or-null id)
        {date     :date
         location :location
         charge   :charge}  (sql/retrieve-bill id)]
    (with-printing-page location id
      (get-old-bill-items id :p :span)
      [:header {:class "w3-container w3-card"}
       [:center (format-money charge "Total:" :h2)]]
      (let [per-person (sql/retrieve-bill-charges-per-person id)]
        (if (= 1 (count per-person))
          nil
          (with-table
            [:person   :charge]
            ["Persona" "Total"]
            [(fn [p _] [:p (if (nil? p)
                             ""
                             (str "Persona " p))])
             (fn [m _] (format-money m "" :p))]
            per-person)))
      [:p date])))

(defn render-bill
  "Renders a single bill"
  [user id]
  (let [{date     :date
         location :location
         charge   :charge}  (sql/retrieve-bill id)
        mult                (float-or-null (sql/retrieve-app-data-val "card-multiplicative"))]
    (with-page location
      (:name user)
      :main
      (make-link (str "/add-item/" id) "Añadir a comanda")
      (make-link (str "/charge-bill/" id) "Cobrar")
      (get-bill-items id)
      (format-money charge "Total:" :h2)
      (format-money (* mult charge) "Con Tarjeta:")
      (make-link (str "/add-item/" id) "Añadir a comanda")
      (make-link (str "/charge-bill/" id) "Cobrar")
      [:p date])))

(defn render-set-person
  "Renders the page where we set the person for a bill item"
  [user id]
  (let [{person  :person
         item    :item
         options :options
         id-bill :id_bill} (sql/retrieve-bill-item id)]
    (with-page (str "Asignar " item
                    (if (nil? options)
                      ""
                      (str " (" options ")")))
      (:name user)
      :main
      (with-form (str "/bill/" id-bill)
        (form/hidden-field {:value id} "id-bill-item")
        [:h5
         (form/drop-down "set-person"
                         (map (fn [n] [(if (= "null" n) "Nadie" (str "Persona " n)) n])
                              (conj (range 1 50) "null"))
                         (if (nil? person) "null" person))
         [:br]
         (form/submit-button "Cambiar")]))))

(defn render-set-bill-item
  "Renders the page where we set the a bill item and its options"
  [user id]
  (let [{person  :person
         item    :item
         options :options
         id-bill :id_bill
         item-id :id_item}     (sql/retrieve-bill-item id)]
    (println "Item:" item "id:" item-id)
    (with-page (str "Cambiar " item
                    (if (nil? options)
                      ""
                      (str " (" options ")")))
      (:name user)
      :main
      (with-form (str "/bill/" id-bill)
        (form/hidden-field {:value id} "id-bill-item")
        [:h5
         (form/drop-down "set-item"
                         (map (fn [{id :id
                                    nm :name}] [nm id])
                              (sql/retrieve-items))
                         item-id)
         [:br]
         (form/submit-button "Cambiar")]))))

(defn render-set-bill-item-options
  "Renders the bill item optiions page"
  [user id]
  (let [{person  :person
         item    :item
         options :options
         id-bill :id_bill
         item-id :id_item} (sql/retrieve-bill-item id)
        valid-options      (sql/retrieve-valid-options item-id)
        current-options    (set (flatten (map vals (sql/retrieve-current-options id))))]
    (with-page (str "Opciones para " item
                    (if (nil? options)
                      ""
                      (str " (" options ")")))
      (:name user)
      :main
      (with-form (str "/bill/" id-bill)
        (form/hidden-field {:value id} "id-bill-item")
        (form/hidden-field {:value true} "set-options")
        [:h5
         (let [ordered-options (into (sorted-map)
                                     (map (fn [i] {(key i)
                                                   (map (fn [{n :option_name
                                                              i :id_option}]
                                                          [:h5
                                                           (form/check-box i (contains? current-options i))
                                                           (form/label n (str " " n))])
                                                        (val i))})
                                          (group-by :option_group valid-options)))
               option-groups (keys ordered-options)]
           (with-table
             option-groups
             option-groups
             (repeat (count ordered-options)
                     (fn [k _] k))
             [ordered-options]))
         (form/submit-button "Cambiar")]))))

(defn render-set-charge-override
  "Displays a page where we can select a change to the charge for a bill item"
  [user id]
  (let [{person  :person
         item    :item
         options :options
         id-bill :id_bill
         item-id :id_item
         charge  :charge} (sql/retrieve-bill-item id)]
    (println "Charge for" id)
    (with-page (str "Monto de " item
                    (if (nil? options)
                      ""
                      (str " (" options ")")))
      (:name user)
      :main
      (with-form (str "/bill/" id-bill)
        (form/hidden-field {:value id} "id-bill-item")
        [:h5
         (form/text-field {:type "number" :step "0.01"} "charge-override" charge)
         [:br]
         (form/submit-button "Cambiar")]))))

(defn render-add-item
  "Displays a page where we add an item to a bill"
  [user id]
  (with-page "Añadir a comanda"
    (:name user)
    :main
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
                 (form/submit-button "Añadir")])])
           (group-by :menu_group (sql/retrieve-items))))))

(defn render-new-bill
  "Shows a page where we can create a new bill"
  [user]
  (with-page "Nueva Comanda"
    (:name user)
    :main
    (with-form "/bills"
      (form/hidden-field {:value true} "new-bill")
      [:h5
       (form/label :mesa "Mesa: ")
       (form/text-field {:type "text"} "bill-location")
       [:br]
       (form/submit-button "Crear")])))

(defn render-charge-bill
  "Displays a page where a bill is paid"
  [user id]
  (let [{date     :date
         location :location
         charge   :charge}  (sql/retrieve-bill id)]
    (with-page (str "Cobro para " location)
      (:name user)
      :main
      [:p date]
      (with-form (str "/closed-bill/" id)
        (form/hidden-field {:value charge} "bill-charge")
        (form/hidden-field {:value id}     "bill-id")
        (format-money charge "Total:" :h2)
        [:h5 (form/submit-button "Cobrar")]))))

(defn render-accts
  "Displays the accounts page"
  [user]
  (with-page "Cuentas"
    (:name user)
    :main
    (make-link "/add-expense"     "Añadir Gasto")
    (make-link "/close-acct"      "Hacer Cierre")
    (with-table
      [:date   :concept   :amount]
      ["Fecha" "Concepto" "Monto"]
      [(fn [d _] (format-date d))
       (fn [c _] [:h5 c])
       (fn [m _] (format-money m))]
      (sql/retrieve-current-accounting))
    (let [{total    :total
           expenses :expenses
           intakes  :intakes} (sql/retrieve-current-accounting-totals)]
      [:span
       (format-money total "Total:" :h2)
       (format-money expenses "Gastos:")
       (format-money intakes "Entradas:")])
    (make-link "/add-expense"     "Añadir Gasto")
    (make-link "/close-acct"      "Hacer Cierre")))

(defn render-list-users
  "Page listing all registered users"
  [user]
  (with-page "Usuarios Registrados"
    (:name user)
    :admin
    (make-link "/add-new-user" "Añadir Usuario")
    (with-table
      [:user_name :name             :id          :id      :id]
      ["Usuario"  "Nombre Completo" "Contraseña" "Roles"  ""]
      [(fn [u _] [:h5 u])
       (fn [n u] (make-link (str "/user-info/" (:id u)) n))
       (fn [i _] (make-link (str "/change-password/"   i) "Cambiar Contraseña"))
       (fn [i _]
         (make-link (str "/change-user-roles/" i)
                    (apply str
                           (interpose ", "
                                      (sort
                                        (map :name
                                             (sql/retrieve-roles-for-user i)))))))
       (fn [i _] (make-link (str "/delete-user/"       i) "Borrar"))]
      (sql/retrieve-registered-users))
    (make-link "/add-new-user" "Añadir Usuario")))

(defn render-list-roles
  "Gets the list roles page"
  [user]
  (with-page "Roles Registrados"
    (:name user)
    :admin
    (make-link "/add-new-role" "Añadir Nuevo Rol")
    (with-table
      [:name :id        :id]
      ["Rol" "Permisos" ""]
      [(fn [n r] (make-link (str "/view-role/" (:id r)) n))
       (fn [i _] (map (fn [p] [:h6 p])
                      (sort (sql/retrieve-permissions-for-role i))))
       (fn [i _] (make-link (str "/delete-role/" i) "Borrar"))]
      (sql/retrieve-all-roles))
    (make-link "/add-new-role" "Añadir Nuevo Rol")))

(defn render-new-option-group
  "Page to add an option group"
  [user id]
  (with-page "Nuevo Grupo de Opciones"
    (:name user)
    :admin
    [:h5
     (with-form (str "/view-item/" id)
       (form/hidden-field {:value true} "add-new-option-group")
       (form/label {:for "new-option-group"} "new-option-group" "Nuevo Grupo: ")
       (form/text-field {:id "new-option-group"} "new-option-group")
       (form/submit-button "Crear"))]))

(defn render-new-option
  "Page to add an option"
  [user id]
  (with-page "Nueva Opción"
    (:name user)
    :admin
    [:h5
     (with-form (str "/view-item/" id)
       (form/hidden-field {:value true} "add-new-option")
       (form/label {:for "new-option"} "new-option" "Nueva Opción: ")
       (form/text-field {:id "new-option"} "new-option")
       [:br]
       (form/label {:for "option-group"} "option-group" "Grupo: ")
       (form/drop-down {:id "option-group"} "option-group"
                       (map (fn [{g :name}] [g g])
                            (sql/retrieve-option-groups)))
       [:br]
       (form/submit-button "Crear"))]))

(defn render-view-option
  "Shows an option"
  [user id]
  (let [{oname        :name
         option-group :option_group
         extra-charge :extra_charge
         in-stock?    :in_stock} (sql/retrieve-option-by-id (int-or-null id))
        all-option-groups        (sql/retrieve-all-option-groups)
        post-page                (str "/view-option/" id)]
    (with-page (str option-group "/" oname)
      (:name user)
      :admin
      [:h5
       (with-form post-page
         (form/hidden-field {:value true} "set-option-name")
         (form/label {:for "option-name"} "option-name" "Opción: ")
         (form/text-field {:id "option-name"} "option-name" oname)
         (form/submit-button "Cambiar"))
       (with-form post-page
         (form/hidden-field {:value true} "set-option-group")
         (form/label {:for "option-group"} "option-group" "Grupo: ")
         (form/drop-down {:id "option-group"} "option-group" (map :name all-option-groups) option-group)
         (form/submit-button "Cambiar"))
       (with-form post-page
         (form/hidden-field {:value true} "set-option-charge")
         (form/label {:for "option-charge"} "option-charge" "Extra Cargo: ")
         (form/text-field {:id "option-charge" :type "number" :step "0.01"} "option-charge" extra-charge)
         (form/submit-button "Cambiar"))
       (with-form post-page
         (form/hidden-field {:value true} "set-option-in-stock")
         (form/label {:for "option-in-stock"} "option-in-stock" "En Inventario: ")
         (form/check-box "option-in-stock" in-stock?)
         (form/submit-button "Cambiar"))])))

(defn render-change-item-menu-group
  "Page to change an items menu group"
  [user id]
  (let [id                  (int-or-null id)
        {iname :name
         group :menu_group} (sql/retrieve-item-by-id id)
        all-groups          (map :name (sql/retrieve-menu-groups))]
    (with-page iname
      (:name user)
      :admin
      [:h5
       (with-form (str "/view-item/" id)
         (form/hidden-field {:value id} "set-item-menu-group")
         (form/label {:for "menu-group"} "menu-group" "Grupo: ")
         (form/drop-down {:id "menu-group"} "menu-group"
                         (map (fn [g] [g g]) (sort all-groups))
                         group)
         [:br]
         (form/submit-button "Cambiar"))])))

(defn render-change-item-charge
  "Page to set price of an item"
  [user id]
  (let [id               (int-or-null id)
        {iname :name
         charge :charge} (sql/retrieve-item-by-id id)]
    (with-page iname
      (:name user)
      :admin
      [:h5
       (with-form (str "/view-item/" id)
         (form/hidden-field {:value id} "set-item-charge")
         (form/label {:for "amount"} "amount" "Monto: ")
         (form/text-field {:id "amount" :type "number" :step "0.01"} "amount" charge)
         [:br]
         (form/submit-button "Cambiar"))])))

(defn render-delete-item
  "Page to confirm item deletion"
  [user id]
  (let [id            (int-or-null id)
        {iname :name} (sql/retrieve-item-by-id id)]
    (with-page (str "Borrar " iname)
      (:name user)
      :admin
      [:h5 "En la mayor parte de los casos cambiar la existencia
            va a tener el efecto deseado. Esta acción fallará si
            el producto ha sido anteriormente añadido a una
            comanda."
       (with-form "/list-items"
         (form/hidden-field {:value id} "delete-item")
         (form/submit-button "Borrar"))])))

(defn render-add-user
  "User registration page"
  [user]
  (with-page "Registro de Usuario"
    (:name user)
    :admin
    [:h5
     (with-form "/list-users"
       (form/hidden-field {:value true} "add-user")
       (form/label "username" "Nombre de Usuario: ")
       (form/text-field "username")
       [:br]
       (form/label "name" "Nombre Completo: ")
       (form/text-field "name")
       [:br]
       (form/label "password" "Contraseña: ")
       (form/password-field "password")
       [:br]
       (form/submit-button "Crear"))]))

(defn render-close
  "Displays a close"
  [user id]
  (with-page (str "Cierre " id)
    (:name user)
    :main
    (let [{date         :date
           expenses     :expense_amount
           intakes      :intake_amount
           total        :earnings
           business     :business_share
           partners     :partners_share
           services     :services_share
           partner-take :partner_take} (sql/retrieve-single-close id)]
      [:span
       [:p date]
       (with-table
         [:date   :concept   :amount]
         ["Fecha" "Concepto" "Monto"]
         [(fn [d _] (format-date d))
          (fn [c _] [:h5 c])
          (fn [m _] (format-money m))]
         (sql/retrieve-closed-accounting id))
       (format-money total        "Total:" :h2)
       (format-money expenses     "Gastos:")
       (format-money intakes      "Entradas:")
       (format-money business     "Negocio:")
       (format-money services     "Servicios:")
       (format-money partners     "Socios:")
       (format-money partner-take "Cada Socio:")])))

(defn render-log
  "Log page"
  [user]
  (with-page "Registro"
    (:name user)
    :admin
    [:p "Últimas 1000 entradas"]
    (with-table 
      [:date   :id_app_user :action  :details]
      ["Fecha" "Usuario"    "Acción" "Detalles"]
      [(fn [d _] (format-full-date d))
       (fn [i l] (let [u (sql/retrieve-user-by-id i)]
                   (make-link (str "/view-user/" i)
                              (:user_name u))))
       (fn [a _] a)
       (fn [d _] d)]
      (sql/retrieve-log))))
