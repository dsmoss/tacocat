insert into app_upgrade (upgrade) values ('upgrade-00003.sql');

insert into intl_key (name) values ('btn-copy')
                                 , ('ln-system')
;

insert into intl (key       , lang, val)
          values ('btn-copy', 'en', 'Copy')
	       , ('btn-copy', 'es', 'Copiar')
	       , ('ln-system', 'en', 'System')
	       , ('ln-system', 'es', 'Sistema')
;

alter table item_option
drop  constraint item_option_id_item_fkey,
  add constraint item_option_id_item_fkey
      foreign key (id_item)
      references item(id)
      on delete cascade;