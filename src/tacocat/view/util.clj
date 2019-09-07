(ns tacocat.view.util
  (:gen-class)
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
  ;                      ["previous-closes"]
  ;                      ["sales"]]
  ;             ["services" ["add-services-expense"]
  ;                         ["closed-services"]]
  ;             ["debts" ["add-debt-payment"]
  ;                      ["add-debt"]
  ;                      ["add-creditor"]]]
  ;     ["system" ["admin-options"]
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
                 :string      "ln-services"}
                {:destination "/debts"
                 :string      "ln-debts"}]
   :debts      [{:destination "/add-debt-payment"
                 :string      "ln-add-debt-payment"}
                {:destination "/add-debt"
                 :string      "ln-add-debt"}
                {:destination "/add-creditor"
                 :string      "ln-add-creditor"}]
   :bills      [{:destination "/new-bill"
                 :string      "ln-new-bill"}
                {:destination "/old-bills"
                 :string      "ln-old-bills"}]
   :accts      [{:destination "/add-expense"
                 :string      "ln-add-expense"}
                {:string      "ln-sales"
                 :destination "/sales"}
                {:destination "/close-acct"
                 :string      "ln-close-accounting"}
                {:destination "/previous-closes"
                 :string      "ln-previous-closes"}]
   :services   [{:destination "/add-services-expense"
                 :string      "ln-add-services-expense"}
                {:destination "/closed-services"
                 :string      "ln-closed-services"}]
   :system     [{:destination "/admin-options"
                 :string      "ln-admin-options"}
                {:destination "/log"
                 :string      "ln-log"}
                {:destination "/error-log"
                 :string      "ln-errors"}
                {:destination "/intl"
                 :string      "ln-intl"}]
   :error      [{:destination "/user-info"
                 :string      "ln-user-info"}]})

(defn make-link-table
  "Makes a link table"
  ([data lang m]
   (let [c  (count data)
         pc (float (/ 100 (if (= 0 c) 1 c)))]
     [:table
      {:width "100%" :cellpadding 0 :cellspacing 0 :border 0}
      [:tr {:style "padding: 0; border: 0;"}
       (map (fn [{d :destination s :string}]
              [:th {:width (str pc "%")
                    :style "border: 0; padding: 0;"}
               (make-link d (get-string s m lang))])
            data)]]))
  ([data lang]
   (make-link-table data lang {}))
  ([data]
   (make-link-table
     data (sql/retrieve-app-data-val "default-language"))))

(defn get-links
  "Gets the links for a section"
  [lang sections]
  (for [s sections]
    (make-link-table (s link-data) lang)))

(defn with-form-table
  "Makes a table suitable for holding form items
   (with-form-table [[1 2] [1 2] [2 1] nil] [some some]
     [some some]
     [some some]
     [some some some])
   => [:table [:tr [:th some] [:th {:colspan 2} some]]
              [:tr [:td some] [:td {:colspan 2} some]]
              [:tr [:td {:colspan 2} some] [:td some]]
              [:tr [:td some] [:td some] [:td some]]]
   Or:
   (with-form-table nil nil
     [some some])
   => [:table [:tr [:td some] [:td some]]]"
  [[hs & ts] header & data]
  (let [row (fn [tag cs items]
              [:tr {:class "form-table"}
               (map (fn [c d]
                      [tag {:class "form-table"
                            :colspan (if (nil? c) 1 c)} d])
                    (if (nil? cs) (repeat (count items) 1) cs)
                    items)])]
    [:table {:class "form-table"}
     (if (not (empty? header))
       (row :th hs header))
     (map row
          (repeat (count data) :td)
          (if (empty? ts) (repeat (count data) nil) ts)
          data)]))

(defn main-head
  "normal page head tag"
  [font header theme]
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
                     (str "https://www.w3schools.com/lib/" theme)
                     (if (= (sql/retrieve-app-data-val "environment")
                            "dev")
                       (str "/css/style.css?" (rand))
                       "/css/style.css")
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
  (let [lang  (if (empty? user)
                (sql/retrieve-app-data-val "default-language")
                (:language user))
        theme (str "w3-theme-"
                   (sql/retrieve-app-data-val "theme") ".css")
        font  (sql/retrieve-app-data-val "default-font")]
    (page/html5
      (main-head font header theme)
      [:body
       (get-links lang [:general])
       (get-links lang sections)
       [:header {:class "w3-container w3-card w3-theme-l4"}
        [:center
         [:h1 header]
         (if (empty? user) "" [:h5 (:name user)])]]
       [:center content]
       (get-links lang (reverse sections))
       [:header {:class "w3-container w3-card w3-theme-l4"}
        [:center
         [:h5
          (if (empty? user)
            (with-form-table nil nil
              [(make-link "/login" (get-string "ln-login" {} lang))])
            (with-form-table nil nil
              [(make-link
                 "/user-info" (get-string "ln-user-info" {} lang))
               (make-link
                 "/login"
                 (get-string "ln-change-user" {} lang))]))]]]])))

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
          (get-string "str-tel/number" {:number tel})
          [:br]
          (sql/retrieve-app-data-val "business-website")]]
        [:header
         [:h2 (get-string "str-bill-for/location/id"
                          {:location header :id id})]]
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
   ; Always use system language for money
   (let [ms   (get-string "str-money-symbol")
         mf   (get-string "str-money-fmt")
         cash (format mf m)
         fmt  (get-string "str-money-fmt/symbol/ammount"
                           {:amount cash :symbol ms})]
     [t (if (not (empty? s)) (str s "&nbsp;"))
      [:span {:class (if (< m 0) "red" "green")} fmt]])))

(defn format-bool
  "Makes a bool into a form"
  [b id]
  [:h5 (form/check-box {:id id} id b)])

(defn make-option-string
  "Makes an option string"
  [options]
  (if (empty? options) "" (str " (" options ")")))

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
  ([text lang tag]
   [tag (form/submit-button (get-string text {} lang))])
  ([text lang]
   (btn text lang :h5)))

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

(def lbl-creditor     (partial lbl "lbl-creditor"    ))
(def lbl-person       (partial lbl "lbl-person"      ))
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
  ([id default step minimum]
   (form/text-field {:id id :type "number" :step step :min minimum}
                    id (if (nil? default) minimum default)))
  ([id default step]
   (nf id default step 0))
  ([id default]
   (nf id default "0.01"))
  ([id]
   (nf id nil)))

(defn with-options-table
  "Make a table for showing item options"
  [lang id options action]
  (with-table lang
    [:name        :extra_charge :in_stock       :id]
    ["str-option" "str-charge"  "str-in-stock"  ""]
    [(fn [n {i :id g :option_group}]
       (make-link (str "/view-option/" i) (str g "/" n)))
     (fn [m _] (format-money m))
     (fn [b o]
       (with-form (str "/view-item/" id)
         (form/hidden-field {:value (:id o)} "set-option-in-stock")
         (with-form-table nil nil
           [(format-bool b "set-option")
            (btn-change lang)])))
     action]
    options))

(defn get-app-option-control
  "Get correct control for an option"
  [{k :key dt :data_type v :val}]
  (case dt
    "int"    (nf k v 1)
    "float"  (nf k v)
    "list"   (form/drop-down {:id k}
               k
               (map :val (sql/retrieve-app_data_list_values k))
               v)
    "string" (tf k v)))

(defn get-existing-bills
  "Returns a structure containing existing bills"
  [user]
  (format-bill-list user "/bill/" (sql/retrieve-bills) true))

(defn get-closed-bills
  "Returns a structure containing existing bills"
  [user]
  (format-bill-list user "/closed-bill/" (sql/retrieve-closed-bills) false))

