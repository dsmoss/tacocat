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
		   , ('')
                   , ('')
                   , ('')
                   , ('')
		   , ('')
                   , ('')
                   , ('')
                   , ('')
		   , ('')
                   , ('')
                   , ('')
                   , ('')
		   , ('')
                   , ('')
                   , ('')
                   , ('')
                   , ('')
                   , ('')
                   , ('')
                   , ('')
;
