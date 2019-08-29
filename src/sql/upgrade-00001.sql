alter table app_user add column enabled bool default true;

insert into permission (name)
values ('add-new-menu-group');

insert into role_permission (id_role, id_permission)
select r.id
     , p.id
from   role       as r
join   permission as p
  on   1 = 1
where  r.name = 'Admin'
  and  p.name = 'add-new-menu-group';
