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

