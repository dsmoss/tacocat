-- we want to fail here if table exists
create table app_upgrade
  ( upgrade varchar(20) not null primary key
  , date    timestamp   not null default now()
);

-- normally this would be the first line of every upgrade script
insert into app_upgrade (upgrade)
                 values ('upgrade-00001.sql')
;

alter table app_user add column enabled bool default true;

insert into permission (name)
                values ('add-new-menu-group')
                     , ('change-user-enabled')
;

insert into role_permission (id_role, id_permission)
select r.id
     , p.id
from   role       as r
join   permission as p
  on   1 = 1
where  r.name = 'Admin'
  and  p.name in ( 'add-new-menu-group'
                 , 'change-user-enabled'
);
