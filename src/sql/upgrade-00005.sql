insert into app_upgrade (upgrade) values ('upgrade-00005.sql');

update intl
  set  val  = 'Change Stock Status'
where  key  = 'prm-change-stock-status'
  and  lang = 'en';

update intl
  set  val  = 'Print Bill'
where  key  = 'prm-print-bill'
  and  lang = 'en';

update intl   as d
  set  val    =  s.val
from   intl   as s
where  d.key  =  'prm-set-services-receipt'
  and  s.key  =  d.key
  and  s.lang != d.lang;

update intl
  set  val  = 'Active'
where  key  = 'str-enabled'
  and  lang = 'en';

insert into intl (key, lang, val)
values ('str-enabled', 'es', 'Activo');

update intl
  set  val = replace(val, ' &nbsp;', '');

insert into intl_key (name)
values ('ln-menu')
     , ('ln-merge')
     , ('prm-merge-bill')
     , ('str-merge/location')
     , ('btn-merge')
;

insert into intl (key, lang, val)
values ('ln-menu'           , 'es', 'Menú')
     , ('ln-menu'           , 'en', 'Menu')
     , ('ln-merge'          , 'es', 'Unir')
     , ('ln-merge'          , 'en', 'Merge')
     , ('prm-merge-bill'    , 'es', 'Unir Comandas')
     , ('prm-merge-bill'    , 'en', 'Merge Bills')
     , ('str-merge/location', 'es', 'Unir Comanda de "%location"')
     , ('str-merge/location', 'en', 'Merge "%location" Bill')
     , ('btn-merge'         , 'es', 'Unir')
     , ('btn-merge'         , 'en', 'Merge')
;

insert into permission (name)
values ('merge-bill')
;

insert into role_permission (id_role, id_permission)
select r.id
     , p.id
from   role       as r
cross
join   permission as p
where  r.name = 'Admin'
  and  p.name in
    ( 'merge-bill'
);

create or replace view v_bill_items as
select bi.id                                      as id,
       bi.id_bill                                 as id_bill,
       i.id                                       as id_item,
       bi.date                                    as date,
       i.name                                     as item,
       bi.person                                  as person,
       string_agg(o.name, ', ' order by o.name)   as options,
       coalesce(bi.charge_override,
                i.charge + coalesce(
                             sum(o.extra_charge),
                             0))                  as charge
from   bill_item                                  as bi
join   item                                       as i
  on   bi.id_item = i.id
  left outer
join   bill_item_option                           as bio
  on   bio.id_bill_item = bi.id
  left outer
join   option                                     as o
  on   bio.id_option = o.id
group
  by   bi.id_bill,
       bi.id,
       i.name,
       i.charge,
       i.id;
