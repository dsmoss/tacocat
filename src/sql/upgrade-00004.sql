insert into app_upgrade (upgrade) values ('upgrade-00004.sql');

alter table  error_log
  add column id_cause int null references error_log(id);

alter table  app_data
  add column data_type varchar(32) not null default 'string'
check (data_type in ('string', 'float', 'list', 'boolean', 'int'));

insert into app_data (key, val)
values ('environment', 'dev')
     , ('theme'      , 'grey')
;

update app_data
  set  data_type = 'float'
where  key
  in   ( 'profit-business-share'
       , 'profit-services-share'
       , 'profit-partners-share'
       , 'card-multiplicative'
);

update app_data
  set  data_type = 'int'
where  key
  in   ('partner-count');

update app_data
  set  data_type = 'list'
where  key
  in  ( 'default-language'
      , 'environment'
      , 'theme'
);

drop table if exists app_data_valid_list_values cascade;

create table app_data_valid_list_values
  ( key varchar(255) not null references app_data(key)
  , val varchar(255) not null
  , primary key (key, val)
);

insert into app_data_valid_list_values (key, val)
select 'default-language'
     , name
from   intl_lang;

insert into app_data_valid_list_values (key, val)
values ('environment', 'dev')
     , ('environment', 'qa')
     , ('environment', 'test')
     , ('environment', 'prod')
     , ('theme'      , 'red')
     , ('theme'      , 'pink')
     , ('theme'      , 'purple')
     , ('theme'      , 'deep-purple')
     , ('theme'      , 'indigo')
     , ('theme'      , 'blue')
     , ('theme'      , 'light-blue')
     , ('theme'      , 'cyan')
     , ('theme'      , 'teal')
     , ('theme'      , 'green')
     , ('theme'      , 'light-green')
     , ('theme'      , 'lime')
     , ('theme'      , 'khaki')
     , ('theme'      , 'yellow')
     , ('theme'      , 'amber')
     , ('theme'      , 'orange')
     , ('theme'      , 'deep-orange')
     , ('theme'      , 'blue-grey')
     , ('theme'      , 'brown')
     , ('theme'      , 'grey')
     , ('theme'      , 'dark-grey')
     , ('theme'      , 'black')
;

insert into intl_key (name)
values ('str-cause')
     , ('str-none')
     , ('dta-environment')
     , ('dta-theme')
     , ('prm-view-error-list')
     , ('ln-error-list')
     , ('ln-errors')
     , ('str-404')
     , ('str-404-msg')
     , ('ln-debts')
     , ('prm-view-debts')
     , ('ln-add-debt-payment')
     , ('ln-add-debt')
     , ('ln-add-creditor')
     , ('str-creditor')
     , ('ln-debt-detail')
     , ('prm-add-creditor')
     , ('prm-add-debt')
     , ('lbl-creditor')
     , ('str-debt')
     , ('prm-add-debt-payment')
     , ('prm-view-debt-detail')
     , ('str-debt-for/name')
     , ('ln-sales')
     , ('prm-view-sales')
     , ('str-options')
     , ('ln-get-user-image')
     , ('prm-change-other-users-picture')
     , ('ln-view-receipt')
     , ('prm-view-receipt')
     , ('ln-no-receipt-image')
     , ('prm-set-receipt')
     , ('str-upload-receipt')
     , ('prm-set-services-receipt')
     , ('prm-view-services-receipt')
;

insert into intl (key, lang, val)
values ('str-cause'           , 'en', 'Cause')
     , ('str-cause'           , 'es', 'Causa')
     , ('str-none'            , 'en', 'None')
     , ('str-none'            , 'es', 'Ninguna')
     , ('dta-environment'     , 'en', 'Environment')
     , ('dta-environment'     , 'es', 'Entorno')
     , ('dta-theme'           , 'en', 'Theme')
     , ('dta-theme'           , 'es', 'Tema')
     , ('prm-view-error-list' , 'es', 'Ver Lista de Errores')
     , ('prm-view-error-list' , 'en', 'View Error List')
     , ('ln-error-list'       , 'es', 'Registro de Errores')
     , ('ln-error-list'       , 'en', 'Error Log')
     , ('ln-errors'           , 'es', 'Errores')
     , ('ln-errors'           , 'en', 'Errors')
     , ('str-404'             , 'es', 'Error 404')
     , ('str-404'             , 'en', '404 Error')
     , ('str-404-msg'         , 'es', 'La página no existe o ha sido movida.')
     , ('str-404-msg'         , 'en', 'The requested page does not exist or has been moved.')
     , ('ln-debts'            , 'es', 'Deudas')
     , ('ln-debts'            , 'en', 'Debts')
     , ('prm-view-debts'      , 'en', 'View Debts')
     , ('prm-view-debts'      , 'es', 'Ver Deudas')
     , ('ln-add-debt-payment' , 'en', 'Pay Debt')
     , ('ln-add-debt-payment' , 'es', 'Pagar Deuda')
     , ('ln-add-debt'         , 'en', 'Add Debt')
     , ('ln-add-debt'         , 'es', 'Añadir Deuda')
     , ('ln-add-creditor'     , 'en', 'Add Creditor')
     , ('ln-add-creditor'     , 'es', 'Añadir Acreedor')
     , ('str-creditor'        , 'es', 'Acreedor')
     , ('str-creditor'        , 'en', 'Creditor')
     , ('ln-debt-detail'      , 'es', 'Ver Detalles')
     , ('ln-debt-detail'      , 'en', 'View Details')
     , ('prm-add-creditor'    , 'es', 'Añadir Acreedor')
     , ('prm-add-creditor'    , 'en', 'Add Creditor')
     , ('prm-add-debt'        , 'es', 'Añadir Deuda')
     , ('prm-add-debt'        , 'en', 'Add Debt')
     , ('lbl-creditor'        , 'es', 'Acreedor: &nbsp;')
     , ('lbl-creditor'        , 'en', 'Creditor: &nbsp;')
     , ('str-debt'            , 'es', 'Adeudo')
     , ('str-debt'            , 'en', 'Debt Incurred')
     , ('prm-add-debt-payment', 'en', 'Add Debt Payment')
     , ('prm-add-debt-payment', 'es', 'Añadir Pago de Deuda')
     , ('prm-view-debt-detail', 'es', 'Ver Detalle de Deuda')
     , ('prm-view-debt-detail', 'en', 'View Debt Detail')
     , ('str-debt-for/name'   , 'es', 'Deuda con %name')
     , ('str-debt-for/name'   , 'en', '%name Debt')
     , ('ln-sales'            , 'es', 'Ventas')
     , ('ln-sales'            , 'en', 'Sales')
     , ('prm-view-sales'      , 'es', 'Ver Ventas')
     , ('prm-view-sales'      , 'en', 'View Sales')
     , ('str-options'         , 'es', 'Opciones')
     , ('str-options'         , 'en', 'Options')
     , ('ln-get-user-image'   , 'en', 'Set User Picture')
     , ('ln-get-user-image'   , 'es', 'Adquirir Imagen')
     , ('prm-change-other-users-picture'
	                      , 'en', 'Change Other Users Picture')
     , ('prm-change-other-users-picture'
	                      , 'es', 'Cambiar Imagen de Otros Usuarios')
     , ('ln-view-receipt'     , 'es', 'Ver Recibo')
     , ('ln-view-receipt'     , 'en', 'View Receipt')
     , ('prm-view-receipt'    , 'es', 'Ver Recibo')
     , ('prm-view-receipt'    , 'en', 'View Receipt')
     , ('ln-no-receipt-image' , 'es', 'Sin Recibo')
     , ('ln-no-receipt-image' , 'en', 'No Receipt')
     , ('prm-set-receipt'     , 'es', 'Subir Recibo')
     , ('prm-set-receipt'     , 'en', 'Set Receipt')
     , ('str-upload-receipt'  , 'es', 'Subir Recibo')
     , ('str-upload-receipt'  , 'en', 'Receipt Upload')
     , ('prm-set-services-receipt', 'en', 'Subir Recibo de Servicios')
     , ('prm-set-services-receipt', 'es', 'Set Utilities Receipt')
     , ('prm-view-services-receipt', 'es', 'Ver Recibo de Servicios')
     , ('prm-view-services-receipt', 'en', 'View Utilities Receipt')
;

insert into permission (name)
values ('view-error-list')
     , ('view-debts')
     , ('add-creditor')
     , ('add-debt')
     , ('add-debt-payment')
     , ('view-debt-detail')
     , ('view-sales')
     , ('change-other-users-picture')
     , ('view-receipt')
     , ('set-receipt')
     , ('set-services-receipt')
     , ('view-services-receipt')
;

insert into role_permission (id_role, id_permission)
select r.id, p.id
from   role as r
join   permission as p
  on   true
where  r.name = 'Admin'
  and  p.name in ( 'view-error-list'
	         , 'view-debts'
		 , 'add-creditor'
		 , 'add-debt'
		 , 'add-debt-payment'
		 , 'view-debt-detail'
		 , 'view-sales'
		 , 'change-other-users-picture'
		 , 'view-receipt'
		 , 'set-receipt'
		 , 'set-services-receipt'
		 , 'view-services-receipt'
);

drop table if exists creditor cascade;

create table creditor
  ( id   serial       primary key
  , name varchar(256) not null unique
);

drop table if exists debt cascade;

create table debt
  ( id          serial        primary key
  , date        timestamp     not null default now()
  , id_creditor int           not null references creditor(id)
  , amount      numeric(10,2) not null
  , concept     varchar(255)  not null
);

drop view if exists v_sales_breakdown cascade;

create view v_sales_breakdown as
select count(1)  as sold
     , it.name   as item
     , it.id     as id_item
from   intakes   as i
join   bill      as b
  on   i.id_bill = b.id
join   bill_item as bi
  on   b.id = bi.id_bill
join   item      as it
  on   bi.id_item = it.id
where  i.id_close is null
group
  by   it.name
     , it.id;

drop view if exists v_sales_option_breakdown cascade;

create view v_sales_option_breakdown as
select count(1)         as sold
     , it.name          as item
     , it.id            as id_item
     , o.name           as option
     , o.id             as id_option
from   intakes          as i
join   bill             as b
  on   i.id_bill = b.id
join   bill_item        as bi
  on   b.id = bi.id_bill
join   item             as it
  on   bi.id_item = it.id
left   outer
join   bill_item_option as bio
  on   bio.id_bill_item = bi.id
left   outer
join   option           as o
  on   o.id = bio.id_option
where  i.id_close is null
group
  by  it.name
    , o.name
    , it.id
    , o.id;

alter table  expenses
  add column receipt          bytea        null,
  add column receipt_filename varchar(255) null,
  add column receipt_filetype varchar(15)  null;

alter table app_user
  add column picture          bytea        null,
  add column picture_filename varchar(255) null,
  add column picture_filetype varchar(32)  null;

alter table services
  add column receipt          bytea        null,
  add column receipt_filename varchar(255) null,
  add column receipt_filetype varchar(15)  null;

create or replace view v_accounting as
select b.id || ' (' || b.location || ')' as concept
     , i.date                            as date
     , i.amount                          as amount
     , i.id_close                        as id_close
     , null                              as receipt_filename
     , null                              as id_expense
     , i.id_bill                         as id_intake
from   intakes                           as i
join   bill                              as b
  on   i.id_bill = b.id
union
select e.concept                         as concept
     , e.date                            as date
     , e.amount                          as amount
     , e.id_close                        as id_close
     , e.receipt_filename                as receipt_filename
     , e.id                              as id_expense
     , null                              as id_intake
from   expenses                          as e
order
  by   date desc;

