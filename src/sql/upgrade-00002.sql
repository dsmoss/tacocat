insert into app_upgrade (upgrade) values ('upgrade-00002.sql');

-- Internationalisation schema
-- See: https://en.wikipedia.org/wiki/Language_localisation
drop table if exists intl_lang cascade;

create table intl_lang 
  ( name     varchar(8)    primary key
  , fallback varchar(8)    null references intl_lang(name)
  , full_name varchar(128) not null
);

-- Fallback to english when no translations available.
-- Be careful not to set a fallback for 'en' least you
-- cause an endless loop
insert into intl_lang (name   , fallback, full_name)
               values ('es'   , 'en'    , 'str-lang-es')
	            , ('es-MX', 'es'    , 'str-lang-es-MX')
		    , ('en'   , null    , 'str-lang-en')
		    , ('en-GB', 'en'    , 'str-lang-en-GB')
;

drop table if exists intl_key cascade;

create table intl_key ( name varchar(128) primary key );

drop table if exists intl cascade;

create table intl
  ( key   varchar(128)  not null references intl_key(name)
  , lang  varchar(8)    not null references intl_lang(name)
  , val   varchar(4096) not null default ''
  , primary key (key, lang)
);

insert into permission (name)
                values ('can-translate')
		     , ('change-other-users-language')
;

insert into role_permission (id_role, id_permission)
select r.id
     , p.id
from   role       as r
join   permission as p
  on   1 = 1
where  r.name = 'Admin'
  and  p.name
  in ( 'can-translate'
     , 'change-other-users-language'
);

alter table app_user
add   column language
varchar(8)
not null
references intl_lang(name)
default 'en';

insert into app_data (key, val)
values ('default-language', 'en');

insert into intl_key (name)
              values ('btn-view-translations')
	           , ('ln-home')
		   , ('ln-bills')
		   , ('ln-accts')
		   , ('ln-services')
                   , ('ln-admin')
                   , ('ln-old-bills')
                   , ('ln-closed-services')
                   , ('ln-previous-closes')
                   , ('ln-admin-options')
                   , ('ln-list-users')
                   , ('ln-list-roles')
		   , ('ln-list-items')
                   , ('ln-log')
                   , ('ln-intl')
                   , ('ln-user-info')
                   , ('str-tel/number')
                   , ('str-bill-for/location/id')
                   , ('ln-login')
                   , ('ln-change-user')
                   , ('str-forbidden-action')
                   , ('str-insufficient-permissions')
                   , ('str-permissions')
                   , ('str-date')
                   , ('str-location')
                   , ('str-charge')
                   , ('ln-view')
                   , ('str-concept')
                   , ('str-new-balance')
                   , ('str-user-login')
                   , ('lbl-user-name')
                   , ('lbl-password')
                   , ('btn-enter')
                   , ('str-delete-user/name')
                   , ('btn-delete')
                   , ('str-change-user-name/name')
                   , ('lbl-full-name')
                   , ('btn-change')
                   , ('str-lang-es')
                   , ('str-lang-es-MX')
                   , ('str-lang-en')
                   , ('str-lang-en-GB')
                   , ('str-lang')
                   , ('lbl-lang')
                   , ('str-error')
                   , ('str-wrong-user-or-password')
                   , ('str-user-info/name')
                   , ('ln-full-name/name')
                   , ('ln-change-password')
                   , ('ln-change-roles')
                   , ('ln-change-language/language')
                   , ('str-roles')
                   , ('str-machines')
                   , ('str-roles-for/name')
                   , ('str-role')
                   , ('str-permission')
                   , ('str-add-role')
                   , ('lbl-role')
                   , ('btn-add')
                   , ('str-delete-role/name')
                   , ('str-add-new-menu-group')
                   , ('lbl-name')
                   , ('str-add-item')
                   , ('lbl-item')
                   , ('lbl-group')
                   , ('lbl-charge')
                   , ('str-option')
                   , ('str-in-stock')
                   , ('lbl-in-stock')
                   , ('ln-create-new-option-group')
                   , ('str-item-options')
                   , ('ln-create-new-option')
                   , ('btn-remove')
                   , ('str-other-options')
                   , ('str-change-password/name')
                   , ('lbl-new-password')
                   , ('str-internationalisation')
                   , ('lbl-from')
                   , ('lbl-to')
                   , ('lbl-filter')
                   , ('str-label')
                   , ('str-default')
                   , ('str-app-options')
                   , ('str-key')
                   , ('str-val')
                   , ('ln-create-new-item')
		   , ('ln-create-new-menu-group')
                   , ('str-items')
                   , ('str-item')
                   , ('str-menu-group')
		   , ('ln-add-services-expense')
                   , ('s-filter-instructions')
                   , ('str-close')
                   , ('str-close/number')
		   , ('str-services-for-close/number')
                   , ('ln-new-bill')
                   , ('str-closes')
                   , ('str-expenses')
		   , ('str-intake')
                   , ('str-earning')
                   , ('str-add-services-charge')
		   , ('lbl-concept')
                   , ('str-close-of-accounting')
                   , ('str-closed-bills')
                   , ('str-time')
		   , ('str-person')
                   , ('ln-assign')
                   , ('ln-person/number')
                   , ('ln-add-options')
		   , ('str-delete/name')
                   , ('str-change-location')
                   , ('str-p#')
                   , ('str-add-expense')
		   , ('ln-print')
                   , ('fmt-total')
                   , ('str-person/number')
                   , ('ln-add-to-bill')
		   , ('ln-charge')
                   , ('str-card-payment')
                   , ('str-assign-item/item/options')
                   , ('str-nobody')
		   , ('str-change-item/item/options')
                   , ('str-options-for-item/item/options')
                   , ('str-charge-for/item/options')
                   , ('lbl-location')
                   , ('btn-create')
                   , ('str-charge-for/location')
                   , ('btn-charge')
                   , ('ln-add-expense')
                   , ('ln-close-accounting')
                   , ('str-amount')
                   , ('str-total')
                   , ('fmt-expenses')
                   , ('fmt-intakes')
                   , ('str-registered-users')
                   , ('ln-add-new-user')
                   , ('str-user')
                   , ('str-full-name')
                   , ('str-password')
                   , ('str-enabled')
                   , ('str-registerd-roles')
                   , ('ln-add-new-role')
                   , ('str-new-option-group')
                   , ('lbl-new-group')
                   , ('str-new-option')
                   , ('lbl-new-option')
                   , ('lbl-option')
                   , ('lbl-extra-charge')
                   , ('str-wrn-on-item-delete')
                   , ('str-register-user')
                   , ('fmt-business')
                   , ('fmt-services')
                   , ('fmt-partners')
                   , ('fmt-per-partner')
                   , ('str-entries-limit')
		   , ('str-action')
                   , ('str-detail')
;

COPY public.intl (key, lang, val) FROM stdin;
btn-view-translations	en	Show Translations
ln-accts	es	Cuentas
ln-user-info	es	Información de Usuario
ln-services	es	Servicios
ln-admin	es	Admin
ln-admin-options	es	Opciones
ln-bills	es	Comandas
ln-closed-services	es	Servicios Pasados
ln-home	es	Inicio
ln-intl	es	Traducir
ln-list-items	es	Productos
ln-list-roles	es	Roles
ln-list-users	es	Usuarios
ln-old-bills	es	Entradas
ln-log	es	Registro
ln-accts	en	Accounts
ln-admin	en	Admin
ln-admin-options	en	Settings
ln-closed-services	en	Past Utilities
ln-home	en	Home
ln-intl	en	Translate
ln-list-items	en	Items
ln-list-roles	en	Roles
ln-list-users	en	Users
ln-log	en	Log
ln-services	en	Utilities
ln-user-info	en	User Info
str-tel/number	es	Tel: %number
str-tel/number	en	Tel: %number
str-bill-for/location/id	en	Bill for %location (#%{id})
str-bill-for/location/id	es	Cuenta para %location (#%{id})
ln-change-user	en	Switch User
ln-login	en	Log-in
ln-change-user	es	Cambiar Usuario
ln-login	es	Entrar
btn-view-translations	es	Ver Traducciones
str-forbidden-action	es	Acción Prohibida
str-insufficient-permissions	es	Permisos Insuficientes
str-permissions	es	Permisos
str-insufficient-permissions	en	Insufficient Permissions
str-permissions	en	Permissions
ln-view	en	View
ln-view	es	Mostrar
str-charge	en	Charge
str-charge	es	Monto
str-location	en	Table
str-location	es	Mesa
str-date	en	Date
str-date	es	Fecha
str-concept	es	Concepto
str-concept	en	Reason
str-new-balance	es	Nuevo Balance
str-new-balance	en	New Balance
btn-enter	es	Entrar
btn-enter	en	Enter
lbl-user-name	en	Username: 
lbl-password	es	Contraseña: 
lbl-password	en	Password: 
lbl-user-name	es	Usuario: 
str-user-login	es	Registro de Usuario
str-user-login	en	User Log-in
btn-delete	es	Borrar
btn-delete	en	Delete
str-delete-user/name	es	Borrar Usuario: %name
str-delete-user/name	en	Delete User: %name
btn-change	es	Cambiar
btn-change	en	Change
str-change-user-name/name	es	Cambiar Nombre a: %name
str-change-user-name/name	en	Change Name for: %name
lbl-full-name	es	Nombre Completo: 
lbl-full-name	en	Full Name: 
lbl-lang	es	Lenguaje: 
lbl-lang	en	Language: 
str-lang	en	Language
str-lang-en	en	English
str-lang-en-GB	en	British English
str-lang-es	en	Spanish
str-lang-es-MX	en	Mexican Spanish
str-lang	es	Lenguaje
str-lang-en	es	Inglés
str-lang-en-GB	es	Inglés Britanico
str-lang-es	es	Español
str-lang-es-MX	es	Español Mexicano
ln-change-language/language	es	Cambiar Lenguaje: %language
ln-change-password	es	Cambiar Contraseña
ln-change-roles	es	Cambiar Roles
ln-full-name/name	es	Nombre Completo: %name
str-machines	es	Máquinas
str-error	es	Error
str-roles	es	Roles
str-user-info/name	es	Información del Usuario: %name
str-wrong-user-or-password	es	Usuario o Contraseña incorrecta
ln-change-language/language	en	Change Language: %language
ln-change-password	en	Change Password
ln-full-name/name	en	Full Name: %name
ln-change-roles	en	Change Roles
str-error	en	Error
str-machines	en	Devices
str-roles	en	Roles
str-user-info/name	en	User Info for: %name
str-wrong-user-or-password	en	Wrong Username or Password
str-role	en	Role
str-role	es	Rol
str-roles-for/name	es	Roles para: %name
str-roles-for/name	en	Roles for: %name
str-permission	en	Permission
str-permission	es	Permiso
btn-add	es	Añadir
btn-add	en	Add
lbl-role	es	Rol: 
lbl-role	en	Role: 
str-add-role	es	Añadir Rol
str-add-role	en	Add Role
str-delete-role/name	en	Delete Role: %name
str-delete-role/name	es	Borrar Rol: %name
lbl-charge	es	Monto: 
lbl-group	es	Grupo: 
lbl-item	es	Producto: 
lbl-name	es	Nombre: 
str-add-item	es	Añadir Producto
lbl-charge	en	Charge: 
lbl-group	en	Group: 
lbl-item	en	Item: 
str-add-item	en	Add Item
btn-create	en	Create
btn-create	es	Crear
str-add-new-menu-group	es	Añadir Grupo de Menú
str-add-new-menu-group	en	Add Menu Group
str-in-stock	en	In Stock
btn-remove	en	Remove
lbl-in-stock	en	In Stock: 
ln-create-new-option	en	Create New Option
ln-create-new-option-group	en	Create New Option Group
str-option	en	Option
str-other-options	en	Other Options
str-item-options	en	Item Options
ln-create-new-option	es	Crear Nueva Opción
ln-create-new-option-group	es	Crear Nuevo Grupo de Opciones
btn-remove	es	Remover
lbl-in-stock	es	En Inventario: 
str-in-stock	es	En Inventario
str-item-options	es	Opciones del Producto
str-option	es	Opción
str-other-options	es	Otras Opciones
lbl-new-password	es	Nueva Contraseña: 
str-change-password/name	es	Cambiar Contraseña de %name
lbl-new-password	en	New Password: 
str-change-password/name	en	Change Password for %name
str-internationalisation	en	Internationalisation
str-internationalisation	es	Internacionalización
lbl-from	es	De: 
lbl-to	es	A: 
lbl-from	en	From: 
lbl-to	en	To: 
lbl-filter	en	Filter: 
lbl-filter	es	Filtro: 
str-default	es	En Falta
str-label	es	Etiqueta
str-default	en	Default
str-label	en	Label
str-key	en	Key
str-val	en	Value
str-app-options	en	Aplication Options
str-app-options	es	Opciones de la Aplicación
str-key	es	Opción
str-val	es	Valor
str-menu-group	es	Grupo
str-menu-group	en	Group
ln-create-new-item	en	Create New Item
ln-create-new-menu-group	en	Create New Menu Group
ln-create-new-item	es	Crear Nuevo Producto
ln-create-new-menu-group	es	Crear Nuevo Grupo de Menú
str-item	es	Producto
str-items	es	Productos
str-item	en	Item
str-items	en	Items
ln-add-services-expense	en	Add Utilities Expense
ln-add-services-expense	es	Añadir Cargo de Servicios
str-close	en	Close
str-close/number	en	Close %number
str-close	es	Cierre
str-close/number	es	Cierre %number
str-earning	es	Entradas
str-expenses	es	Gastos
str-earning	en	Earnings
str-expenses	en	Expenses
str-intake	en	Intake
str-intake	es	Entarda
ln-new-bill	es	Nueva Comanda
ln-new-bill	en	New Bill
str-closes	es	Cierres
str-closes	en	Closes
s-filter-instructions	en	Use '%' for any number of characters, and '_' for any single character.
s-filter-instructions	es	Use '%' para cualquier número de letras, y '_' para una letra en particular.
str-add-services-charge	es	Añadir Cargo de Servicios
str-add-services-charge	en	Add Utilities Charge
lbl-concept	en	Reason: 
lbl-concept	es	Concepto: 
btn-close	es	Cerrar
btn-close	en	Close
str-closed-bills	es	Comandas Cerradas
str-close-of-accounting	es	Cierre de Cuentas
str-close-of-accounting	en	Accounting Close
ln-previous-closes	es	Cierres Pasados
ln-previous-closes	en	Past Closes
str-time	en	Time
str-time	es	Hora
str-person	en	Person
str-person	es	Persona
ln-assign	es	Asignar
ln-assign	en	Assign
ln-person/number	en	Person %number
ln-person/number	es	Persona %number
ln-add-options	es	Añadir Opciones
ln-add-options	en	Add Options
str-delete/name	en	Delete %name
str-delete/name	es	Borrar %name
str-change-location	es	Cambiar Mesa
str-change-location	en	Change Sitting
str-p#	en	P#
str-p#	es	P#
str-add-expense	es	Añadir Gasto
str-add-expense	en	Add Expense
ln-print	en	Print
ln-print	es	Imprimir
ln-add-to-bill	es	Añadir a comanda
ln-add-to-bill	en	Add to Bill
ln-charge	en	Charge
ln-charge	es	Cobrar
str-card-payment	es	Con Tarjeta:
str-card-payment	en	Card Payment:
str-assign-item/item/options	en	Assign %{item}%{options}
str-assign-item/item/options	es	Asignar %{item}%{options}
str-nobody	es	Nadie
str-nobody	en	Nobody
str-change-item/item/options	en	Change %{item}%{options}
str-change-item/item/options	es	Cambiar %{item}%{options}
str-options-for-item/item/options	es	Opciones para %{item}%{options}
str-options-for-item/item/options	en	Options for %{item}%{options}
str-charge-for/item/options	en	Charge for %{item}%{options}
str-charge-for/item/options	es	Monto de %{item}%{options}
lbl-location	es	Mesa: 
lbl-location	en	Sitting: 
str-charge-for/location	es	Cobro para %location
str-charge-for/location	en	Charge for %location
btn-charge	en	Charge
btn-charge	es	Cobrar
ln-close-accounting	es	Hacer Cierre
ln-close-accounting	en	Close Accounting
ln-add-expense	en	Add Expense
ln-add-expense	es	Añadir Gasto
str-amount	es	Monto
str-amount	en	Amount
fmt-total	es	Total:
fmt-total	en	Total:
str-total	en	Total
str-total	es	Total
fmt-expenses	es	Gastos:
fmt-expenses	en	Expenses:
fmt-intakes	en	Intakes:
fmt-intakes	es	Entradas
str-registered-users	es	Usuarios Registrados
str-registered-users	en	Registered Users
ln-add-new-user	en	Add New User
ln-add-new-user	es	Añadir Usuario
str-user	es	Usuario
str-user	en	Username
str-full-name	en	Full Name
str-full-name	es	Nombre Completo
str-password	es	Contraseña
str-password	en	Password
str-enabled	en	Activo
str-registerd-roles	en	Registered Roles
ln-add-new-role	en	Add New Role
ln-add-new-role	es	Añadir Nuevo Rol
str-registerd-roles	es	Roles Registrados
str-new-option-group	es	Nuevo Grupo de Opciones
str-new-option-group	en	New Option Group
lbl-new-group	en	New Group: 
lbl-new-group	es	Nuevo Grupo: 
str-new-option	es	Nueva Opción
str-new-option	en	New Option
lbl-new-option	en	New Option: 
lbl-new-option	es	Nueva Opción: 
lbl-option	es	Opción: 
lbl-option	en	Option: 
lbl-extra-charge	en	Extra Charge: 
lbl-extra-charge	es	Extra Cargo: 
str-wrn-on-item-delete	es	En la mayor parte de los casos cambiar la existencia va a tener el efecto deseado. Esta acción fallará si el producto ha sido anteriormente añadido a una comanda.
str-wrn-on-item-delete	en	In most cases changing the inventory status will have the desired result. This acction will fail should the item be added to any bill present or past.
str-register-user	en	Register User
str-register-user	es	Registro de Usuario
fmt-business	es	Negocio:
fmt-services	es	Servicios:
fmt-partners	es	Socios:
fmt-per-partner	es	Cada Socio:
fmt-business	en	Business Take:
fmt-partners	en	Partners Take:
fmt-per-partner	en	Per-partner Take:
fmt-services	en	Utilities Take:
str-entries-limit	en	Latest 1000 entries.
str-entries-limit	es	Últimas 1000 entradas.
str-action	es	Acción
str-forbidden-action	en	Forbidden Action
str-action	en	Action
str-detail	en	Details
str-detail	es	Detalles
ln-bills	en	Bills
ln-old-bills	en	Closed Bills
str-closed-bills	en	Closed Bills
str-person/number	en	Person %number
str-person/number	es	Persona %number
str-register	es	Caja
str-register	en	Cash Register
str-services-for-close/number	es	Servicios para Cierre %number
str-services-for-close/number	en	Utilities for Close %number
lbl-name	en	Name: 
\.


