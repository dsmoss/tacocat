insert into app_upgrade (upgrade) values ('upgrade-00004.sql');

alter table  error_log
add   column id_cause int null references error_log(id);

insert into intl_key (name)
values ('str-cause')
     , ('str-none')
;

insert into intl (key, lang, val)
values ('str-cause', 'en', 'Cause')
     , ('str-cause', 'es', 'Causa')
     , ('str-none' , 'en', 'None')
     , ('str-none' , 'es', 'Ninguna')
;
