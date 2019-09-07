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
;

insert into permission (name)
values ('view-error-list')
     , ('view-debts')
     , ('add-creditor')
     , ('add-debt')
     , ('add-debt-payment')
     , ('view-debt-detail')
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

