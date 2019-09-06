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
     , ('theme', 'red')
     , ('theme', 'pink')
     , ('theme', 'purple')
     , ('theme', 'deep-purple')
     , ('theme', 'indigo')
     , ('theme', 'blue')
     , ('theme', 'light-blue')
     , ('theme', 'cyan')
     , ('theme', 'teal')
     , ('theme', 'green')
     , ('theme', 'light-green')
     , ('theme', 'lime')
     , ('theme', 'khaki')
     , ('theme', 'yellow')
     , ('theme', 'amber')
     , ('theme', 'orange')
     , ('theme', 'deep-orange')
     , ('theme', 'blue-grey')
     , ('theme', 'brown')
     , ('theme', 'grey')
     , ('theme', 'dark-grey')
     , ('theme', 'black')
;

insert into intl_key (name)
values ('str-cause')
     , ('str-none')
     , ('dta-environment')
     , ('dta-theme')
;

insert into intl (key, lang, val)
values ('str-cause'      , 'en', 'Cause')
     , ('str-cause'      , 'es', 'Causa')
     , ('str-none'       , 'en', 'None')
     , ('str-none'       , 'es', 'Ninguna')
     , ('dta-environment', 'en', 'Environment')
     , ('dta-environment', 'es', 'Entorno')
     , ('dta-theme'      , 'en', 'Theme')
     , ('dta-theme'      , 'es', 'Tema')
;
