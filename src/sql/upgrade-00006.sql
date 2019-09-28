insert into app_upgrade (upgrade) values ('upgrade-00006.sql');

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

insert into app_data (key, val, data_type)
values ('use-cache', 'true', 'boolean')
;

insert into intl_key (name)
values ('dta-use-cache')
;

insert into intl (key, lang, val)
values ('dta-use-cache', 'es', 'Almacenar en Caché')
     , ('dta-use-cache', 'en', 'Use Cache')
;
