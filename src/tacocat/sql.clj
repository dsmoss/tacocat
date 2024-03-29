(ns tacocat.sql
  (:gen-class)
  (:require [clojure.java.jdbc :as j]
            [tacocat.str-var   :refer [translate-vars]]
            [tacocat.log       :refer [log]]
            [tacocat.util      :refer :all]))

(def connection-string
  "postgresql://tacocat:Tacocat2019@localhost:5432/tacocat")

(def db-spec
  {:dbtype     "postgresql"
   :dbname     "tacocat"
   :host       "localhost"
   :port       5432
   :user       "tacocat"
   :password   "Tacocat2019"})

(defn to-byte-array
  "Takes in a file path and turns it into a
  byte array containing the data in the file"
  [file]
  ;create table t (i int primary key, d bytea);
  ;(j/insert! db-spec :t
  ;  {:i 1
  ;   :d (to-byte-array "/path/to/test.txt")})
  ;and
  ;(j/query db-spec
  ;  ["select d from t where i = ?" 1])
  (let [f (if (= java.io.File (type file))
            file
            (java.io.File. file))
        a (byte-array (.length f))
        i (java.io.FileInputStream. f)]
    (.read i a)
    (.close i)
    a))

(defn log-action
  "Logs an action"
  [db id_user action details]
  (j/insert! db :app_activity_log
    {:id_app_user id_user
     :action action
     ; truncate at 1024 for stack traces
     :details (subs details 0 (min (count details) 1024))}))

(defn retrieve-error-log
  "Finds all error log items"
  []
  (j/query db-spec ["select id
                          , date
                          , id_cause
                          , error_type
                     from   error_log
                     order
                       by   date desc
                     limit  1000"]))

(defn retrieve-error-log-by-id
  "Finds an error log item"
  [id]
  (first (j/query db-spec ["select date
                                 , error_type
                                 , message
                                 , stack_trace
                                 , id_cause
                            from   error_log
                            where  id = ?"
                           id])))

(defmacro ins
  "Makes an insert"
  [user db & stuff]
  ;(log (:name user) db stuff)
  `(j/with-db-transaction [t-con# ~db]
     (let [ret# (j/insert! t-con# ~@stuff)]
       (log-action t-con# (:id ~user) "insert" (str ~@stuff))
       ret#)))

(defmacro del
  "makes a delete"
  [user db & stuff]
  `(j/with-db-transaction [t-con# ~db]
     (let [ret# (j/delete! t-con# ~@stuff)]
       (log-action t-con# (:id ~user) "delete" (str ~@stuff))
       ret#)))

(defmacro upd
  "makes an update"
  [user db & stuff]
  `(j/with-db-transaction [t-con# ~db]
     (let [ret# (j/update! t-con# ~@stuff)]
       (log-action t-con# (:id ~user) "update" (str ~@stuff))
       ret#)))

(defn insert-new-intake
  "Inserts a bill into the intake table.
   This results in the bill being considered as closed"
  [user id charge]
  (ins user db-spec :intakes {:id_bill id :amount charge}))

(defn update-person-for-bill-item
  "Sets the person for a bill item"
  [user id person]
  (upd user db-spec :bill_item {:person person} ["id = ?" id]))

(defn update-item-for-bill-item
  "Sets the item for a bill_item object erasing charge override and options"
  [user id id-item]
  (j/with-db-transaction [t-con db-spec]
    (del user t-con :bill_item_option ["id_bill_item = ?" id])
    (upd user t-con :bill_item {:charge_override nil :id_item id-item} ["id = ?" id])))

(defn update-option-in-stock
  "Update in_stock flag for option"
  [user id in-stock?]
  (upd user db-spec :option {:in_stock in-stock?} ["id = ?" id]))

(defn update-option-charge
  "Sets the charge for an option"
  [user id charge]
  (upd user db-spec :option {:extra_charge charge} ["id = ?" id]))

(defn update-option-group
  "Sets the group for an option"
  [user id group]
  (upd user db-spec :option {:option_group group} ["id = ?" id]))

(defn update-option-name
  "Sets the name for an option"
  [user id oname]
  (upd user db-spec :option {:name oname} ["id = ?" id]))

(defn insert-item-option
  "Insert an item option"
  ([user it op db]
   (ins user db :item_option {:id_item it :id_option op}))
  ([user it op]
   (insert-item-option user it op db-spec)))

(defn delete-item-option
  "Deletes an item option"
  [user it op]
  (del user db-spec :item_option ["id_item = ? and id_option = ?" it op]))

(defn retrieve-all-option-groups
  "Gets all the option groups"
  []
  (j/query db-spec ["select name from option_groups order by name"]))

(defn retrieve-all-options
  "Gets all options from the db"
  []
  (j/query db-spec ["select id
                          , name
                          , option_group
                          , extra_charge
                          , in_stock
                     from   option
                     order
                       by   option_group
                          , name"]))

(defn retrieve-valid-options
  "Finds valid options for an item"
  [id]
  (j/query db-spec ["select id_option
                          , option_name
                          , option_group
                     from   v_item_options
                     where  id_item = ?
                     order
                       by   option_group
                          , option_name"
                    id]))

(defn retrieve-valid-options-in-stock
  "Finds valid options in stock for an item"
  [id]
  (j/query db-spec ["select id_option
                          , option_name
                          , option_group
                     from   v_item_options
                     where  id_item = ?
                       and  option_in_stock
                     order
                       by   option_group
                         ,  option_name"
                    id]))

(defn retrieve-current-options
  "Finds the current options for a bill item"
  [id]
  (j/query db-spec ["select id_option
                     from   bill_item_option
                     where  id_bill_item = ?"
                    id]))

(defn update-options-for-bill-item
  "Sets the options for a bill item"
  [user id options]
  (j/with-db-transaction [t-con db-spec]
    ; The checkbox behaviour is weird... Seems to be the case that we *only*
    ; get checked boxes {45 true, 67 true, ...} as opposed to getting both
    ; checked and unchecked like {45 true, 67 false, ...} regardless of whether
    ; they have been changed. In order to deal with this we will first delete
    ; all options, and then re-insert the checked boxes
    (del user t-con :bill_item_option ["id_bill_item = ?" id])
    (dorun
      (for [[k v] options]
        (if v ; This should still work, seeing as we have no false values and all
              ; true ones are selected in the form
          (ins user t-con :bill_item_option {:id_bill_item id :id_option k})
          (del user t-con :bill_item_option ["id_bill_item = ? and id_option = ?" id k]))))))

(defn delete-bill-item-by-id
  "Delete bill-item"
  [user id]
  (del user db-spec :bill_item ["id = ?" id]))

(defn insert-new-expense
  "Insert an expense"
  [user concept amount image]
  (let [receipt (if (empty? (:filename image))
                  {:filename nil
                   :tempfile (:tempfile image) ; 0B file
                   :content-type nil}
                  image)]
    (ins user db-spec :expenses
         {:concept          concept
          :amount           amount
          :receipt          (to-byte-array
                              (:tempfile   receipt))
          :receipt_filename (:filename     receipt)
          :receipt_filetype (:content-type receipt)})))

(defn update-charge-override-for-bill-item
  "Sets the charge override for a bill item"
  [user id charge]
  (upd user db-spec :bill_item {:charge_override charge} ["id = ?" id]))

(defn insert-new-bill
  "Inserts a new bill to the system"
  ([user location db]
   (ins user db :bill {:location location}))
  ([user location]
   (insert-new-bill user location db-spec)))

(defn insert-new-bill-item
  "Inserts an item to a bill"
  ([user id-bill id-item db]
   (ins user db :bill_item {:id_bill id-bill :id_item id-item}))
  ([user id-bill id-item]
   (insert-new-bill-item user id-bill id-item db-spec)))

(defn insert-populated-bill
  "Insert a new bill and populate it with given data"
  [user location data]
  (j/with-db-transaction [t-con db-spec]
    (let [bill    (first (insert-new-bill user location t-con))
          id-bill (:id bill)]
      ;(log "0=>" id-bill bill location data)
      (doall
        (for [[i c o] data]
          (do ;(log "  1=>" i c o)
          (doall
            (for [n (range 0 c)
                  :let [bi    (first (insert-new-bill-item
                                       user id-bill i t-con))
                        id-bi (:id bi)]]
              (do ;(log "    2=>" n id-bi)
              (doall
                (for [[oi c] o]
                  (do ;(log "      3=>" oi c (< n c))
                  (if (< n c)
                    (ins user t-con :bill_item_option
                         {:id_bill_item id-bi
                          :id_option    oi}))))))))))))))

(defn update-bill-location
  "Updates the location for a bill"
  [user id-bill location]
  (upd user db-spec :bill {:location location} ["id = ?" id-bill]))

(defn insert-copy-of-bill-item
  "Copies a bill item and inserts it into the bill"
  [user id-bill-item]
  (j/with-db-transaction [t-con db-spec]
    (let [bill-item    (first
                         (j/query t-con ["select id_bill
                                               , person
                                               , id_item
                                               , charge_override
                                          from   bill_item
                                          where  id = ?"
                                         id-bill-item]))
          item-options (j/query t-con ["select id_option
                                        from   bill_item_option
                                        where  id_bill_item = ?"
                                       id-bill-item])
          {id-new :id} (first
                         (ins user t-con :bill_item bill-item))]
      (doall
        (for [{id-op :id_option} item-options]
          (ins user t-con :bill_item_option
               {:id_bill_item id-new :id_option id-op}))))))

(defn retrieve-app-data-val
  "Finds a value from the app_data table"
  ([k spec]
  (:val
    (first
      (j/query spec ["select val from app_data where key = ?" k]))))
  ([k]
   (retrieve-app-data-val k db-spec)))

(defn insert-new-close
  "Inserts a new close"
  [user str-register str-close]
  (j/with-db-transaction [t-con db-spec]
    (let [lang                     (retrieve-app-data-val "default-language" t-con)
          {total    :total
           intakes  :intakes
           expenses :expenses}     (first
                                     (j/query t-con
                                              ["select (select coalesce(sum(amount), 0)
                                                        from   v_accounting
                                                        where  id_close is null) as total
                                                     , (select coalesce(sum(amount), 0)
                                                        from   intakes
                                                        where  id_close is null) as intakes
                                                     , (select coalesce(sum(amount), 0)
                                                        from   expenses
                                                        where  id_close is null) as expenses"]))
          services-share           (float-or-null (retrieve-app-data-val "profit-services-share" t-con))
          services-amount          (* services-share total)
          business-share           (float-or-null (retrieve-app-data-val "profit-business-share" t-con))
          business-total           (* business-share total)
          partners-share           (float-or-null (retrieve-app-data-val "profit-partners-share" t-con))
          partners-total           (* partners-share total)
          partners-count           (int-or-null (retrieve-app-data-val "partner-count" t-con))
          partner-take             (/ partners-total partners-count)
          {id-close :id}           (first
                                     (ins user t-con :close
                                                {:expense_amount expenses
                                                 :intake_amount  intakes
                                                 :earnings       total
                                                 :partner_take   partner-take
                                                 :services_share services-amount
                                                 :partners_share partners-total
                                                 :business_share business-total}))
          {running :running_total} (first
                                     (j/query t-con
                                              ["select running_total
                                                from   services
                                                where   date = (select max(date)
                                                                from   services)"]))]
      (upd user t-con :intakes  {:id_close id-close} ["id_close is null"])
      (upd user t-con :expenses {:id_close id-close} ["id_close is null"])
      (upd user t-con :services {:id_close id-close} ["id_close is null"])
      (ins user t-con :expenses {:concept str-register
                                 :amount  business-total})
      (ins user t-con :services {:amount        services-amount
                                  :running_total (+ services-amount
                                                    (if (nil? running) 0 running))
                                  :concept       (translate-vars str-close {:number id-close})}))))

(defn insert-services-charge
  "Insert a service payment to db"
  [user concept amount image]
  (let [receipt (if (empty? (:filename image))
                  {:filename nil
                   :tempfile (:tempfile image) ; 0B file
                   :content-type nil}
                  image)]
    (j/with-db-transaction [t-con db-spec]
      (let [{running :running_total} (first
                                       (j/query t-con
                                                ["select running_total
                                                  from   services
                                                  where  date = (select max(date)
                                                  from   services)"]))]
        (ins user t-con :services
             {:amount amount
              :concept concept
              :running_total (+ amount (if (nil? running) 0 running))
              :receipt          (to-byte-array
                                  (:tempfile   receipt))
              :receipt_filename (:filename     receipt)
              :receipt_filetype (:content-type receipt)})))))

(defn retrieve-previous-closes
  "Gets the previous closes date and id"
  []
  (j/query db-spec ["select id, date from close order by date desc"]))

(defn retrieve-current-services
  "Gets the current services"
  []
  (j/query db-spec ["select id
                          , date
                          , amount
                          , running_total
                          , concept
                          , receipt_filename
                          , id_close
                     from   services
                     where  id_close is null
                     order
                       by   date desc"]))

(defn retrieve-services-for-close
  "Finds the services for a given close"
  [id-close]
  (j/query db-spec ["select id
                          , date
                          , amount
                          , running_total
                          , concept
                          , receipt_filename
                          , id_close
                     from   services
                     where  id_close = ?
                     order
                       by   date desc"
                    id-close]))

(defn retrieve-items-in-stock
  "Gets a list of valid items in the menu"
  []
  (j/query db-spec ["select id
                          , name
                          , charge
                          , in_stock
                          , menu_group
                     from   item
                     where  in_stock
                     order
                       by   menu_group desc
                          , name"]))

(defn retrieve-items
  "Gets a list of items in the menu"
  []
  (j/query db-spec ["select id
                          , name
                          , charge
                          , in_stock
                          , menu_group
                     from   item
                     order
                       by   menu_group desc
                          , name"]))

(defn retrieve-items-in-stock-in-group
  "Finds all items in a specific menu group"
  [group]
  (j/query db-spec ["select id
                          , name
                          , charge
                          , in_stock
                          , menu_group
                     from   item
                     where  menu_group = ?
                       and  in_stock
                     order
                       by   menu_group desc
                          , name"
                    group]))

(defn retrieve-full-avaliable-menu
  "Gets every menu item and its options"
  []
  (j/query db-spec ["select i.id           as id_item
                          , i.name         as item_name
                          , i.charge       as item_charge
                          , i.menu_group   as menu_group
                          , o.extra_charge as option_charge
                          , o.id           as id_option
                          , o.name         as option_name
                          , o.option_group as option_group
                     from   item           as i
                     left   outer
                     join   item_option    as io
                       on   io.id_item   = i.id
                     left   outer
                     join   option         as o
                       on   io.id_option = o.id
                     where  i.in_stock
                       and  (  o.in_stock
                            or o.in_stock  is null)"]))

(defn update-item-menu-group
  "Change an items menu_group"
  [user id group]
  (upd user db-spec :item {:menu_group group} ["id = ?" id]))

(defn update-item-charge
  "Change an items charge"
  [user id charge]
  (upd user db-spec :item {:charge charge} ["id = ?" id]))

(defn update-item-name
  "Updates the name of an item"
  [user id iname]
  (upd user db-spec :item {:name iname} ["id = ?" id]))

(defn retrieve-item-by-id
  "Gets an item from the menu"
  [id]
  (first
    (j/query db-spec ["select id
                            , name
                            , charge
                            , in_stock
                            , menu_group
                       from   item
                       where  id = ?
                       order
                         by   menu_group desc"
                      id])))

(defn update-bill-item-bill-id-and-delete-bill
  "Changes the items over and deletes newly empty bill"
  [user id-from id-to]
  (j/with-db-transaction [t-con db-spec]
    (upd user t-con :bill_item
         {:id_bill id-to} ["id_bill = ?" id-from])
    (del user t-con :bill ["id = ?" id-from])))

(defn retrieve-bills
  "Finds all the open bills in the database"
  []
  (j/query db-spec ["select id
                          , date
                          , location
                          , charge
                     from   v_bills
                     where  not closed
                     order
                       by   date desc"]))

(defn retrieve-closed-bills
  "Finds all closed bills in the database"
  []
  (j/query db-spec ["select id
                          , date
                          , location
                          , charge
                     from   v_bills
                     where  closed
                     order
                       by   date desc"]))

(defn retrieve-bill
  "Finds a bill"
  [id]
  (first (j/query db-spec ["select id
                                 , date
                                 , location
                                 , charge
                            from   v_bills
                            where  id = ?
                            order
                              by   date desc"
                           id])))

(defn retrieve-bill-charges-per-person
  "Gets a list of charges itemised by person"
  [id]
  (j/query db-spec ["select person
                          , sum(charge) as charge
                     from   v_bill_items
                     where  id_bill = ?
                     group
                       by   person
                     order
                       by   person"
                    id]))

(defn retrieve-bill-items
  "Finds all the stuff in a bill"
  [id]
  (j/query db-spec ["select id
                          , date
                          , person
                          , item
                          , id_item
                          , options
                          , charge
                     from   v_bill_items
                     where  id_bill = ?
                     order
                       by   date desc"
                    id]))

(defn retrieve-bill-item
  "Finds a single bill item"
  [id]
  (first (j/query db-spec ["select person
                                 , item
                                 , id_item
                                 , options
                                 , id_bill
                                 , charge
                            from   v_bill_items
                            where  id = ?"
                           id])))

(defn retrieve-receipt
  "Retrieves a receipt data from the db"
  [id]
  (first
    (j/query db-spec ["select receipt
                            , receipt_filename
                            , receipt_filetype
                            , id_close
                       from   expenses
                       where  id = ?"
                      id])))

(defn retrieve-services-receipt
  "Retrieves a receipt from the services db"
  [id]
  (first
    (j/query db-spec ["select receipt
                            , receipt_filename
                            , receipt_filetype
                            , id_close
                       from   services
                       where  id = ?"
                      id])))

(defn update-services-receipt
  "Update value of services receipt"
  [user id image]
  (let [receipt (if (empty? (:filename image))
                  {:filename nil
                   :tempfile (:tempfile image) ; 0B file
                   :content-type nil}
                  image)]
    (upd user db-spec :services
         {:receipt          (to-byte-array
                              (:tempfile   receipt))
          :receipt_filename (:filename     receipt)
          :receipt_filetype (:content-type receipt)}
         ["id = ?" id])))

(defn update-expense-receipt
  "Update value of expense receipt"
  [user id image]
  (let [receipt (if (empty? (:filename image))
                  {:filename nil
                   :tempfile (:tempfile image) ; 0B file
                   :content-type nil}
                  image)]
    (upd user db-spec :expenses
         {:receipt          (to-byte-array
                              (:tempfile   receipt))
          :receipt_filename (:filename     receipt)
          :receipt_filetype (:content-type receipt)}
         ["id = ?" id])))

(defn retrieve-closed-accounting
  "Finds the accounting for the current period"
  [id-close]
  (j/query db-spec ["select concept
                          , date
                          , amount
                          , receipt_filename
                          , id_expense
                          , id_intake
                     from   v_accounting
                     where  id_close = ?
                     order
                       by   date desc"
                    id-close]))

(defn retrieve-single-close
  "Finds a close item"
  [id]
  (first
    (j/query db-spec ["select id
                            , date
                            , expense_amount
                            , intake_amount
                            , earnings
                            , business_share
                            , partners_share
                            , services_share
                            , partner_take
                       from   close
                       where  id = ?
                       order
                         by   date desc"
                      id])))

(defn retrieve-current-accounting
  "Finds the accounting for the current period"
  []
  (j/query db-spec ["select concept
                          , date
                          , amount
                          , receipt_filename
                          , id_expense
                          , id_intake
                     from   v_accounting
                     where  id_close is null
                     order
                       by   date desc"]))

(defn retrieve-previous-closes
  "Finds the data for previous closing periods"
  []
  (j/query db-spec ["select id
                          , date
                          , expense_amount
                          , intake_amount
                          , earnings
                     from   close
                     order
                       by   date desc"]))

(defn retrieve-current-accounting-totals
  "finds the totals for the current period"
  []
  (first (j/query db-spec ["select (select coalesce(sum(amount), 0)
                                    from   v_accounting
                                    where  id_close is null) as total
                                 , (select coalesce(sum(amount), 0)
                                    from   intakes
                                    where  id_close is null) as intakes
                                 , (select coalesce(sum(amount), 0)
                                    from   expenses
                                    where  id_close is null) as expenses"])))

(defn retrieve-item-option-count
  "Finds the number of options an item supports"
  [id]
  (:count
    (first
      (j/query db-spec ["select count(1) as count
                         from   item_option
                         where  id_item = ?"
                        id]))))

(defn retrieve-bill-item-option-count
  "Finds the number of options an item supports"
  [id]
  (:count
    (first
      (j/query db-spec ["select count(1) as count
                         from   item_option
                         where  id_item = (select id_item
                                           from   bill_item
                                           where  id = ?)"
                        id]))))

(defn retrieve-app-data
  "Finds all values from the app_data table"
  ([c]
   (j/query c ["select key
                     , val
                     , data_type
                from   app_data
                order
                  by   key"]))
  ([]
   (retrieve-app-data db-spec)))

(defn retrieve-app_data_list_values
  "Gets the list values for an app_data entry"
  [k]
  (j/query db-spec ["select val
                     from   app_data_valid_list_values
                     where  key = ?"
                    k]))

(defn update-app-options
  "Changes the application settings"
  [user options]
  (j/with-db-transaction [t-con db-spec]
    (doall
      (map #(upd user t-con :app_data {:val (options %)} ["key = ?" %])
           (map :key (retrieve-app-data t-con))))))

(defn insert-app-login
  "Log-in a user at a specified ip-address
  NOTE: We may not be able to use the logger"
  [user-name password ip-addr]
  (j/with-db-transaction [t-con db-spec]
    (let [{correct-pw :correct_password
           enabled    :enabled
           user-id    :id} (first
                             (j/query t-con
                               ["select id
                                      , enabled
                                      , produce_password_hash( ? , salt) =
                                        password_hash as correct_password
                                 from   app_user
                                 where  user_name = ?"
                                password
                                user-name]))]
      (if enabled
        (do
          ; Log out existing users if any
          (del {:id user-id} t-con :app_user_ip_addr
               ["ip_addr = ?" ip-addr])
          (if correct-pw
            ; Log in new user
            (ins {:id user-id} t-con :app_user_ip_addr
                 {:ip_addr ip-addr :id_app_user user-id})))
        (log "User" user-id "tred to log in, but is not enabled")))))

(defn retrieve-user-permissions
  "Finds permissions allocated to user"
  ([id]
   retrieve-user-permissions id db-spec)
  ([id db]
   (into #{}
         (map :name
              (j/query db ["select distinct
                                   p.name
                            from   app_user        as au
                            join   app_user_role   as aur
                              on   aur.id_app_user = au.id
                            join   role            as r
                              on   aur.id_role = r.id
                            join   role_permission as rp
                              on   rp.id_role = r.id
                            join   permission      as p
                              on   rp.id_permission = p.id
                            where  au.id = ?"
                           id])))))

(defn update-user-picture
  "Updates a user image"
  [user id image]
  (let [user-img (if (empty? (:filename image))
                   {:filename nil
                    :tempfile (:tempfile image) ; 0B file
                    :content-type nil}
                   image)]
    (upd user db-spec :app_user
         {:picture          (to-byte-array
                              (:tempfile   user-img))
          :picture_filename (:filename     user-img)
          :picture_filetype (:content-type user-img)}
         ["id = ?" id])))

(defn retrieve-user-picture
  "Finds a user image"
  [id]
  (first
    (j/query db-spec ["select picture
                            , picture_filename
                            , picture_filetype
                       from   app_user
                       where  id = ?"
                      id])))

(defn retrieve-user-by-id
  "Finds a user"
  ([id db]
   (conj
     (first
       (j/query db ["select id
                          , user_name
                          , name
                          , salt
                          , language
                     from   app_user
                     where  id = ?"
                    id]))
     [:permissions (retrieve-user-permissions id db)]))
  ([id]
   (retrieve-user-by-id id db-spec)))

(defn update-user-language
  "Update the language of a user"
  [user id language]
  (upd user db-spec :app_user {:language language} ["id = ?" id]))

(defn set-password-for-user
  "Sets a users password"
  [user id password]
  (j/with-db-transaction [t-con db-spec]
    (let [{salt  :salt} (retrieve-user-by-id id t-con)
          {phash :hash} (first
                          (j/query t-con
                            ["select produce_password_hash(?, ?)
                                     as hash"
                                    password
                                    salt]))]
      (upd user t-con :app_user
           {:password_hash phash} ["id = ?" id]))))

(defn update-user-enabled
  "update enabled flag"
  [user id enabled]
  (j/with-db-transaction [t-con db-spec]
    (upd user t-con :app_user {:enabled enabled} ["id = ?" id])
    (if (not enabled) ; log user out of everythin if disabled
      (del user t-con :app_user_ip_addr ["id_app_user = ?" id]))))

(defn insert-new-user
  "Add a user" 
  [user username uname password user-img]
  (j/with-db-transaction [t-con db-spec]
    (let [user-img      (if (empty? (:filename user-img))
                          {:filename nil
                           :tempfile (:tempfile user-img) ; 0B file
                           :content-type nil}
                          user-img)
          {id    :id
           salt  :salt} (first
                          (ins user t-con
                                     :app_user
                                     {:name uname
                                      :user_name username
                                      :password_hash ""}))
          {phash :hash} (first
                          (j/query t-con
                                   ["select produce_password_hash(?, ?) as hash"
                                    password
                                    salt]))]
      (upd user t-con :app_user
           {:password_hash    phash
            :picture          (to-byte-array
                                (:tempfile   user-img))
            :picture_filename (:filename     user-img)
            :picture_filetype (:content-type user-img)}
           ["id = ?" id]))))

(defn retrieve-logged-in-to
  "Gets machines the user is logged in to"
  [id]
  (into #{}
        (map :ip_addr
             (j/query db-spec ["select ip_addr
                                from   app_user_ip_addr
                                where  id_app_user = ?
                                  and  now() between created
                                                 and expires"
                               id]))))

(defn retrieve-registered-users
  "Finds all registered users"
  []
  (j/query db-spec ["select id
                          , user_name
                          , name
                          , language
                          , enabled
                     from   app_user
                     order
                       by   name"]))

(defn retrieve-role-by-id
  "Finds a specific role"
  [id]
  (first (j/query db-spec ["select id, name from role where id = ?" id])))

(defn retrieve-all-roles
  "Finds all the registered roles"
  []
  (j/query db-spec ["select id, name from role order by name"]))

(defn retrieve-all-items
  "Finds all items from the database"
  []
  (j/query db-spec ["select id, name, menu_group, charge, in_stock from item order by menu_group, name"]))

(defn update-item-set-in-stock
  "Sets item's in-stock flag"
  [user id-item set-true?]
  (upd user db-spec :item {:in_stock set-true?} ["id = ?" id-item]))

(defn retrieve-all-permissions
  "Finds all permissions"
  []
  (j/query db-spec ["select id, name from permission order by name"]))

(defn delete-permission-from-role
  "Remove a permission"
  [user id-role id-perm]
  (del user db-spec :role_permission
             ["id_role = ? and id_permission = ?" id-role id-perm]))

(defn insert-permission-to-role
  "Insert a permission"
  [user id-role id-perm]
  (ins user db-spec :role_permission
             {:id_role id-role :id_permission id-perm}))

(defn insert-role
  "Insert a role"
  [user role]
  (ins user db-spec :role {:name role}))

(defn delete-role
  "Erase a role"
  [user id]
  (j/with-db-transaction [t-con db-spec]
    (del user t-con :role_permission ["id_role = ?" id])
    (del user t-con :role ["id = ?" id])))

(defn retrieve-menu-groups
  "Find menu_group enum values"
  []
  (j/query db-spec ["select name from menu_groups"]))

(defn insert-menu-group
  "Adds a new menu group"
  [user menu-group-name]
  (ins user db-spec :menu_groups {:name menu-group-name}))

(defn delete-item
  "Delete item"
  [user id]
  (del user db-spec :item ["id = ?" id]))

(defn insert-new-item
  "Insert an item"
  [user item-name menu-group amount]
  (ins user db-spec :item {:name item-name :menu_group menu-group :charge amount}))

(defn insert-option-group
  "Insert an option group"
  [user og-name]
  (ins user db-spec :option_groups {:name og-name}))

(defn retrieve-option-groups
  "Gets all option groups"
  []
  (j/query db-spec ["select name from option_groups order by name"]))

(defn insert-option
  "Insert a new option"
  [user o-name o-group]
  (ins user db-spec :option {:name o-name :option_group o-group}))

(defn retrieve-option-by-id
  "Finds an option"
  [id]
  (first
    (j/query db-spec ["select id
                            , name
                            , option_group
                            , extra_charge
                            , in_stock
                       from   option
                       where  id = ?"
                      id])))

(defn retrieve-permissions-for-role
  "Finds all the permissions for a roel"
  [id]
  (into #{}
        (map :name
             (j/query db-spec ["select p.name
                                from   permission      as p
                                join   role_permission as rp
                                  on   rp.id_permission = p.id
                                join   role            as r
                                  on   rp.id_role = r.id
                                where  r.id = ?"
                               id]))))

(defn retrieve-roles-for-user
  "Finds all the roles asigned to a user"
  [id]
  (into #{}
        (j/query db-spec ["select distinct
                                  r.id
                               ,  r.name
                           from   app_user as au
                           join   app_user_role   as aur
                             on   aur.id_app_user = au.id
                           join   role            as r
                             on   aur.id_role = r.id
                           where  au.id = ?"
                          id])))

(defn set-user-roles
  "Set the roles for a user"
  [user id roles]
  (j/with-db-transaction [t-con db-spec]
    (del user t-con :app_user_role ["id_app_user = ?" id])
    (doall
      (map #(if (second %)
              (ins user t-con :app_user_role
                         {:id_role (first %) :id_app_user id}))
           roles))))

(defn update-user-name
  "Set name for app_user"
  [user id uname]
  (upd user db-spec :app_user {:name uname} ["id = ?" id]))

(defn delete-user
  "Deletes a user from the database"
  [user id]
  (j/with-db-transaction [t-con db-spec]
    (del user t-con :app_user_ip_addr ["id_app_user = ?" id])
    (del user t-con :app_user_role    ["id_app_user = ?" id])
    (del user t-con :app_user         ["id = ?" id])))

(defn retrieve-logged-in-user
  "Finds a logged in user for a machine.
  If session exists but is expired, the entry is deleted"
  [addr]
  (j/with-db-transaction [t-con db-spec]
    (let [{id-user :id_app_user
           created :created
           expires :expires
           valid   :valid
           :as     entry} (first
                            (j/query t-con ["select id_app_user
                                                  , created
                                                  , expires
                                                  , now() between created
                                                              and expires as valid 
                                             from   app_user_ip_addr
                                             where  ip_addr = ?"
                                            addr]))]
      (cond
        (empty? entry) nil
        valid          (retrieve-user-by-id id-user t-con)
        :else          (do
                         (j/delete! t-con :app_user_ip_addr ["ip_addr = ?" addr])
                         nil)))))

(defn retrieve-log
  "Retrieve 1000 most recent entries of the log"
  []
  (j/query db-spec ["select date
                          , id_app_user
                          , action
                          , details
                     from   app_activity_log
                     order
                       by   date desc
                     limit  1000"]))

(defn insert-error-log
  "Insert an object to the error log"
  ([user t msg stack-trace cause]
   (ins user db-spec :error_log
        {:error_type  t           :message  msg
         :stack_trace stack-trace :id_cause cause}))
  ([user t msg stack-trace]
   (insert-error-log user t msg stack-trace nil)))

(defn retrieve-langs
  "gets all the languages registered for internationalisation"
  []
  (j/query db-spec ["select name
                          , fallback
                          , full_name
                     from   intl_lang
                     order
                       by   length(name)
                          , name"]))

(defn insert-intl
  "Inserts an internationalised string"
  [user key-name lang value]
  (j/with-db-transaction [t-con db-spec]
    (del user t-con :intl ["key = ? and lang = ?" key-name lang])
    (ins user t-con :intl {:key key-name :lang lang :val value})))

(defn retrieve-internationalised-string
  "Find the value of an internationalised string"
  ([k lang default db]
   ;(log "k" k "lang" lang "default" default)
   (let [l (if (nil? lang)
             (retrieve-app-data-val "default-language" db)
             lang)
         {v :val
          f :fallback} (first
                         (j/query db ["select l.fallback
                                            , i.val
                                       from   intl_lang as l
                                       join   intl_key  as k
                                         on   1 = 1
                                       left   outer
                                       join   intl      as i
                                         on   i.lang = l.name
                                         and  i.key  = k.name
                                       where  l.name = ?
                                         and  k.name = ?"
                                      l
                                      k]))]
     (if (nil? v)
       (if (nil? f)
         default
         (recur k f default db))
       v)))
  ([k lang default]
   (retrieve-internationalised-string k lang default db-spec))
  ([k lang]
   (retrieve-internationalised-string k lang k))
  ([k]
   (retrieve-internationalised-string k nil)))

(defn retrieve-intl
  "Gets all internationalised strings"
  [src-lang dest-lang key-filter]
  ; Weird-ass select.... not to mention the product is massive
  (j/query db-spec ["select k.name    as key
                          , sl.name   as src_lang
                          , dl.name   as dst_lang
                          , s.val     as src_val
                          , d.val     as dst_val
                     from   intl_key  as k
                     join   intl_lang as sl
                       on   1 = 1
                     join   intl_lang as dl
                       on   sl.name != dl.name
                     left   outer
                     join   intl      as s
                       on   sl.name = s.lang
                       and  k.name  = s.key
                     left   outer
                     join   intl      as d
                       on   dl.name = d.lang
                       and  k.name = d.key
                     where  sl.name = ?
                       and  dl.name = ?
                       and  (  k.name like ?
                            or s.val  like ?
                            or d.val  like ? )
                     order
                       by   key"
                    src-lang
                    dest-lang
                    key-filter
                    key-filter
                    key-filter]))

(defn retrieve-debt-summary
  "Finds a summary of all the debts owed by creditor"
  []
  (j/query db-spec ["select c.id          as id_creditor
                          , c.name        as creditor
                          , sum(d.amount) as amount
                     from   debt          as d
                     join   creditor      as c
                       on   d.id_creditor = c.id
                     group
                       by   c.id
                          , c.name
                     order
                       by   c.name"]))

(defn retrieve-debt-for-creditor
  "Retrieves the breakdown and total of a debt"
  [id-creditor]
  (j/with-db-transaction [t-con db-spec]
    (conj
      {:breakdown (j/query t-con ["select date
                                        , amount
                                        , concept
                                   from   debt
                                   where  id_creditor = ?
                                   order
                                     by   date desc"
                                  id-creditor])}
      (first
        (j/query t-con ["select sum(amount) as total
                         from   debt
                         where  id_creditor = ?"
                        id-creditor])))))

(defn insert-creditor
  "Inserts a creditor"
  [user creditor]
  (ins user db-spec :creditor {:name creditor}))

(defn retrieve-creditors
  "Finds all creditors"
  []
  (j/query db-spec ["select id, name from creditor order by name"]))

(defn retrieve-creditor-by-id
  "Finds a specific creditor"
  [id]
  (first
    (j/query db-spec
             ["select name from creditor where id = ?" id])))

(defn insert-debt
  "Inserts a debt to the db"
  [user id-creditor amount concept]
  ;(log (:name user) id-creditor amount concept)
  (let [concept (if (empty? concept)
                  (retrieve-internationalised-string "str-debt")
                  concept)]
    ;(log user id-creditor amount concept)
    (ins user db-spec :debt
         {:id_creditor id-creditor
          :amount      amount
          :concept     concept})))

(defn retrieve-sales-breakdown
  []
  (j/with-db-transaction [t-con db-spec]
    (doall
      (map (fn [{id :id_item :as sb}]
             {:item sb
              :options (j/query t-con
                                ["select sold
                                       , option
                                       , id_option
                                  from   v_sales_option_breakdown
                                  where  id_item = ?
                                  order
                                    by   sold desc
                                       , option"
                                 id])})
           (j/query t-con ["select sold
                                 , item
                                 , id_item
                            from   v_sales_breakdown
                            order
                              by   sold desc
                                 , item"])))))
