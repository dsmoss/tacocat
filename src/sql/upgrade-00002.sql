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
               values ('es'   , 'en'    , 'Español')
	            , ('es-MX', 'es'    , 'Español de México')
		    , ('en'   , null    , 'English')
		    , ('en-GB', 'en'    , 'British English')
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
;