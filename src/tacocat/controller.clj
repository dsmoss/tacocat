(ns tacocat.controller
  (:require [tacocat.sql  :as    sql]
            [tacocat.util :refer :all]))

(defn find-logged-in-user
  "Finds which (if any) user is logged into this machine"
  [addr]
  (sql/retrieve-logged-in-user addr))

(defn make-close
  "Closes the current period"
  [user str-register str-close]
  (println "Closing current period")
  (sql/insert-new-close user str-register str-close))

(defn edit-bill-location
  "Edits the location for a bill"
  [user id location]
  (println "Changing bill" id "to" location)
  (sql/update-bill-location
    user
    (int-or-null id)
    (str location)))

(defn add-expense
  "Adds an expense to the database"
  [user concept amount]
  (println "Adding expense of" amount "for" concept)
  (sql/insert-new-expense
    user
    concept
    (* -1 (float-or-null amount))))

(defn add-bill
  "Adds a new bill to the system"
  [user location]
  (println "Adding a bill for" location)
  (sql/insert-new-bill user (str location)))

(defn delete-bill-item
  "Deletes a bill item"
  [user id]
  (println "Deleting bill item" id)
  (sql/delete-bill-item-by-id
    user
    (int-or-null id)))

(defn charge-bill
  "Sets a bill to paid and related actions"
  [user id-bill charge]
  (println "Paying" charge "for bill" id-bill)
  (sql/insert-new-intake
    user
    (int-or-null id-bill)
    (float-or-null charge)))

(defn set-options
  "Sets the options for a bill item"
  [user id-bill-item options]
  (println "Setting options" options "for" id-bill-item)
  (sql/update-options-for-bill-item
    user
    (int-or-null id-bill-item)
    (map (fn [[k v]] [(int-or-null k) (bool-or-null v)])
         options)))

(defn change-app-options
  "Changes the admin options"
  [user options]
  (println "Setting options" options)
  (sql/update-app-options user options))

(defn add-service-charge
  "Add a services charge"
  [user concept amount]
  (println "Adding" amount "services charge for" concept)
  (sql/insert-services-charge
    user
    concept
    (* -1 (float-or-null amount))))

(defn set-person
  "Sets the person attribute of a bill item"
  [user id-bill-item person]
  (println "Setting person for" id-bill-item "to" person)
  (sql/update-person-for-bill-item
    user
    (int-or-null id-bill-item)
    (int-or-null person)))

(defn add-item
  "Adds an item to a bill"
  [user id-bill id-item]
  (println "Adding item" id-item "to bill" id-bill)
  (sql/insert-new-bill-item
    user
    (int-or-null id-bill)
    (int-or-null id-item)))

(defn set-charge-override
  "Sets the price for a bill item"
  [user id charge-override]
  (println "Changing price for" id "to" charge-override)
  (sql/update-charge-override-for-bill-item
    user
    (int-or-null id)
    (float-or-null charge-override)))

(defn set-item
  "Sets the item attribute of a bill item.
  Note: this erases options and charge override"
  [user id-bill-item id-item]
  (println "Setting item for" id-bill-item "to" id-item)
  (sql/update-item-for-bill-item
    user
    (int-or-null id-bill-item)
    (int-or-null id-item)))

(defn perform-login
  "Log the user into the app"
  [user-name password ip-addr]
  (println "Login:" user-name "for" ip-addr)
  (sql/insert-app-login user-name password ip-addr))

(defn get-logged-in-user
  "Gets the currently logged-in user"
  [ip-addr]
  ;(println "Getting logged-in user")
  (sql/retrieve-logged-in-user ip-addr))

(defn change-stock-status
  "Set an item's is_stock flag"
  [user id-item set-true?]
  (println "Setting item" id-item "in-stock" set-true?)
  (sql/update-item-set-in-stock
    user
    (int-or-null id-item)
    (if (nil? set-true?)
      false
      (bool-or-null set-true?))))

(defn add-new-user
  "Adds a user to the database"
  [user username uname password]
  (println "Adding user" username "(" uname ")")
  (sql/insert-new-user user username uname password))

(defn change-user-password
  "Changes a users password"
  [user id password]
  (println "Changing password for" id)
  (sql/set-password-for-user user (int-or-null id) password))

(defn change-user-name
  "Changes the users full name"
  [user id uname]
  (println "Setting name of" id "to" uname)
  (sql/update-user-name user (int-or-null id) uname))

(defn assign-user-roles
  "Assign a set of roles to a uer"
  [user id roles]
  (println "Setting user" id "Roles" roles)
  (sql/set-user-roles 
    user
    (int-or-null id)
    (map (fn [[n v]]
           [(int-or-null n)
            (bool-or-null v)])
         roles)))

(defn set-role-permission
  "Sets a permission for a role"
  [user id-role id-perm set-true?]
  (println "Setting permission" id-perm
           "to role" id-role "as" set-true?)
  (let [set-true? (bool-or-null set-true?)
        id-role   (int-or-null id-role)
        id-perm   (int-or-null id-perm)]
    (if (or (nil? set-true?) (false? set-true?))
      (sql/delete-permission-from-role
        user id-role id-perm)
      (sql/insert-permission-to-role
        user id-role id-perm))))

(defn delete-user
  "Deletes a user"
  [user id]
  (println "Deleting user" id)
  (sql/delete-user user (int-or-null id)))

(defn add-new-role
  "Adds a role"
  [user role]
  (println "Adding role" role)
  (sql/insert-role user role))

(defn add-new-item
  "Adds an item"
  [user item-name menu-group amount]
  (println "Adding item" menu-group 
           "/" item-name "for" amount)
  (sql/insert-new-item
    user
    item-name
    menu-group
    (float-or-null amount)))

(defn add-new-menu-group"Adds a menu group"
  [user menu-group-name]
  (sql/insert-menu-group user menu-group-name))

(defn set-item-menu-group
  "Sets the menu group for an item"
  [user id menu-group]
  (println "Setting group" menu-group "for item" id)
  (sql/update-item-menu-group
    user
    (int-or-null id)
    menu-group))

(defn change-item-name
  "Set the name of an item"
  [user id iname]
  (println "Setting name of item" id "to" iname)
  (sql/update-item-name user (int-or-null id) iname))

(defn set-item-charge
  "Sets the price of an item"
  [user id charge]
  (println "Setting charge of" charge "for item" id)
  (sql/update-item-charge
    user
    (int-or-null id)
    (float-or-null charge)))

(defn set-option-in-stock
  "Sets the in_stock flag for an option"
  [user id in-stock?]
  (println "Setting option" id "in_stock to" in-stock?)
  (sql/update-option-in-stock
    user
    (int-or-null id)
    (bool-or-null in-stock?)))

(defn set-option-charge
  "Sets the extra charge for an option"
  [user id charge]
  (println "Setting extra charge for option" id "to" charge)
  (sql/update-option-charge
    user
    (int-or-null id)
    (p-or-n-float-or-null charge)))

(defn delete-item
  "Deletes an item"
  [user id]
  (println "Deleting item" id)
  (sql/delete-item user (int-or-null id)))

(defn set-option-group
  "Sets the option group for an option"
  [user id option-group]
  (println "Setting option group for" id "to" option-group)
  (sql/update-option-group
    user
    (int-or-null id)
    option-group))

(defn set-option-name
  "Sets the name for an option"
  [user id oname]
  (println "Setting option name to" oname "for option" id)
  (sql/update-option-name user (int-or-null id) oname))

(defn add-option-to-item
  "Adds an option to an item"
  [user id add-op]
  (println "Adding option" add-op "to item" id)
  (sql/insert-item-option
    user
    (int-or-null id)
    (int-or-null add-op)))

(defn remove-option-from-item
  "Removes an option from an item"
  [user id rm-op]
  (println "Removing option" rm-op "from item" id)
  (sql/delete-item-option
    user
    (int-or-null id)
    (int-or-null rm-op)))

(defn add-option-group
  "Create a new option group"
  [user og-name]
  (println "Adding option group" og-name)
  (sql/insert-option-group user og-name))

(defn change-user-enabled
  "Sets the enabled flag on a user"
  [user id enabled]
  (println "Setting enabled to" enabled "for user" id)
  (sql/update-user-enabled
    user (int-or-null id) (bool-or-null enabled)))

(defn change-user-language
  "Sets the language of a user"
  [user id language]
  (println "Setting language of" id "to" language)
  (sql/update-user-language user (int-or-null id) language))

(defn set-translation
  "Sets internationalisation for a key"
  [user key-name lang value]
  (println "Setting" key-name "(" lang ") to" value)
  (sql/insert-intl user key-name lang value))

(defn add-option
  "Add an option"
  [user id o-name o-group]
  (println "Adding option" o-name "in" o-group)
  (add-option-to-item
    user
    id
    (-> (sql/insert-option user o-name o-group)
        first
        :id)))

(defn delete-role
  "Deletes a role"
  [user id]
  (println "Deleting role" id)
  (sql/delete-role user (int-or-null id)))
