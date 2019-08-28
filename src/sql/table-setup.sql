drop table if exists app_data cascade;

-- Just a key/value store of configurable data
create table app_data (
  key varchar(255) not null unique primary key,
  val varchar(255) null
);

grant all on table app_data to tacocat;

insert into app_data (key, val)
              values ('business-name'        , 'TacOcaT')
	           , ('profit-business-share', '0.5')
		   , ('profit-services-share', '0.1')
		   , ('profit-partners-share', '0.4')
		   , ('partner-count'        , '3')
	           , ('card-multiplicative'  , '1.05')
		   , ('business-address'     , 'Some Street 101, Some Town')
	           , ('business-state'       , 'My State')
                   , ('business-post-code'   , '12345')
                   , ('business-telephone'   , '01800 TACOCAT')
	           , ('business-website'     , 'my-site.com')
		   , ('default-font'         , null)
		;

drop table if exists menu_groups cascade;

create table menu_groups ( name varchar(255) primary key );

grant all on table menu_groups to tacocat;

drop table if exists item cascade;

create table item (
  id         serial        primary key,
  name       varchar(255)  not null unique,
  menu_group varchar(255)  not null references menu_groups(name),
  charge     numeric(10,2) not null,
  in_stock   bool          not null default true
);

grant all on table item to tacocat;

drop table if exists option_groups cascade;

create table option_groups ( name varchar(255) primary key );

grant all on table option_groups to tacocat;

drop table if exists option cascade;

create table option (
  id           serial        primary key,
  name         varchar(255)  not null,
  option_group varchar(255)  not null references option_groups(name),
  extra_charge numeric(10,2) not null default 0,
  in_stock     bool          not null default true
);

grant all on table option to tacocat;

drop table if exists item_option cascade;

create table item_option (
  id_item   int not null references item(id),
  id_option int not null references option(id),
  primary key (id_item, id_option)
);

grant all on table item_option to tacocat;

drop table if exists bill cascade;

create table bill (
  id       serial       primary key,
  date     timestamp    not null default now(),
  location varchar(255) not null
);

grant all on table bill to tacocat;

drop table if exists bill_item cascade;

create table bill_item (
  id              serial        primary key,
  date            timestamp     not null default now(),
  id_bill         int           references bill(id) not null,
  person          int           null,
  id_item         int           references item(id) not null,
  charge_override numeric(10,2) null
);

grant all on table bill_item to tacocat;

drop table if exists close cascade;

create table close (
  id             serial        primary key,
  date           timestamp     not null default now(),
  expense_amount numeric(10,2) not null,
  intake_amount  numeric(10,2) not null,
  earnings       numeric(10,2) not null,
  business_share numeric(10,2) not null,
  partners_share numeric(10,2) not null,
  services_share numeric(10,2) not null,
  partner_take   numeric(10,2) not null
);

grant all on table close to tacocat;

drop table if exists intakes cascade;

create table intakes (
  id_bill  int           unique primary key references bill(id),
  date     timestamp     null default now(),
  amount   numeric(10,2) not null,
  id_close int           null references close(id)
);

grant all on table intakes to tacocat;

drop table if exists expenses cascade;

create table expenses (
  id       serial        primary key,
  date     timestamp     not null default now(),
  concept  varchar(255)  not null,
  amount   numeric(10,2) not null,
  id_close int           null references close(id)
);

grant all on table expenses to tacocat;

drop table if exists services cascade;

create table services (
  id            serial        primary key,
  date          timestamp     not null default now(),
  amount        numeric(10,2) not null,
  running_total numeric(10,2) not null,
  id_close      int           null references close(id),
  concept       varchar(255)  not null
);

grant all on table services to tacocat;

drop table if exists bill_item_option cascade;

create table bill_item_option (
  id_bill_item int not null references bill_item(id) on delete cascade,
  id_option    int not null references option(id),
  primary key (id_bill_item, id_option)
);

drop view if exists v_bill_items cascade;

create view v_bill_items as
select bi.id                                      as id,
       bi.id_bill                                 as id_bill,
       i.id                                       as id_item,
       bi.date                                    as date,
       i.name                                     as item,
       bi.person                                  as person,
       string_agg(o.name, ', ')                   as options,
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

drop view if exists v_bills cascade;

create view v_bills as
select b.id                                  as id,
       b.date                                as date,
       b.location                            as location,
       coalesce((select amount from intakes as i where i.id_bill = b.id)
	      , (select sum(charge)
                 from   v_bill_items as vvi
                 where  b.id = vvi.id_bill)
              , 0)                           as charge,
       b.id in (select id_bill from intakes) as closed
from   bill                                  as b;

drop view if exists v_item_options cascade;

create view v_item_options as
select io.id_item     as id_item
     , io.id_option   as id_option
     , o.name         as option_name
     , o.in_stock     as option_in_stock
     , i.name         as item_name
     , i.in_stock     as item_in_stock
     , o.option_group as option_group
from   item_option    as io
join   option         as o
  on   io.id_option = o.id
join   item           as i
  on   io.id_item = i.id;

drop view if exists v_accounting cascade;

create view v_accounting as
select b.id || ' (' || b.location || ')' as concept
     , i.date                            as date
     , i.amount                          as amount
     , i.id_close                        as id_close
from   intakes                           as i
join   bill                              as b
  on   i.id_bill = b.id
union
select e.concept                         as concept
     , e.date                            as date
     , e.amount                          as amount
     , e.id_close                        as id_close
from   expenses                          as e
order
  by   date desc;

-- Security schema

drop table if exists app_user cascade;

create table app_user
  ( id            serial       primary key
  , user_name     varchar(255) not null unique
  , name          varchar(255) not null
  , salt          uuid         not null default uuid_generate_v4()
  , password_hash varchar(128) not null
);

create or replace function
produce_password_hash(password varchar, salt uuid)
returns varchar(128)
language plpgsql
as $body$begin
	return encode(digest(digest(password  , 'sha512')
                          || digest(salt::text, 'sha512')
                           , 'sha512')
                    , 'hex');
end;$body$;

-- Default User
insert into app_user (user_name, name      , password_hash)
              values ('tacocat', 'Taco Cat', '');

update app_user set password_hash = (
	select produce_password_hash('TacOcaT2019', salt)
	from   app_user
	where  user_name = 'tacocat')
where user_name = 'tacocat';

drop table if exists app_user_ip_addr cascade;

create table app_user_ip_addr
  ( id_app_user int         references app_user(id)
  , ip_addr     varchar(39) not null primary key -- ipv6 NNNN:NNNN:NNNN:NNNN:NNNN:NNNN:NNNN:NNNN
  , created     timestamp   not null default now()
  , expires     timestamp   not null default now() + interval '8 hour'
  , active      bool        not null default true
);

drop table if exists role cascade;

create table role
  ( id   serial       primary key
  , name varchar(255) not null unique
);

-- Admin Role
insert into role (name)
          values ('Admin');

drop table if exists app_user_role cascade;

create table app_user_role
  ( id_app_user int not null references app_user(id)
  , id_role     int not null references role(id)
  , primary key (id_app_user, id_role)
);

-- Grant Admin to tacocat
insert into app_user_role (id_app_user, id_role)
select au.id
     , r.id
from   app_user as au
join   role     as r
  on   1 = 1
where  au.user_name = 'tacocat'
  and  r.name = 'Admin';
	
drop table if exists permission cascade;

create table permission
  ( id   serial       primary key
  , name varchar(255) not null unique);

-- Insert app permissions
insert into permission (name)
                values ('close-accounts')
		     , ('charge-bill')
		     , ('add-expense')
		     , ('add-services-expense')
		     , ('create-new-bill')
		     , ('view-open-bill')
		     , ('view-closed-bill')
		     , ('list-closed-bills')
		     , ('delete-bill-item')
		     , ('set-options-to-bill-item')
		     , ('change-bill-item')
		     , ('assign-bill-item-to-person')
		     , ('change-bill-item-price')
		     , ('change-app-values')
		     , ('add-user-roles')
		     , ('assign-user-roles')
		     , ('assign-role-permissions')
		     , ('create-bill-item')
		     , ('change-bill-location')
		     , ('list-bills')
		     , ('view-closed-account')
		     , ('list-closed-accounts')
		     , ('list-closed-services')
		     , ('view-closed-services')
		     , ('list-services')
		     , ('view-accounts')
		     , ('list-users')
		     , ('view-other-users')
		     , ('list-roles')
		     , ('list-items')
		     , ('change-stock-status')
		     , ('view-app-values')
		     , ('add-user')
		     , ('change-other-users-password')
		     , ('delete-user')
		     , ('change-other-users-name')
		     , ('view-role')
		     , ('delete-roles')
		     , ('view-item')
		     , ('set-item-menu-group')
		     , ('set-item-charge')
		     , ('set-item-name')
		     , ('add-new-item')
		     , ('set-option-in-stock')
		     , ('add-option-to-item')
		     , ('remove-option-from-item')
		     , ('create-new-option-group')
		     , ('create-new-option')
		     , ('view-option')
		     , ('set-option-name')
		     , ('set-option-group')
		     , ('set-option-charge')
		     , ('delete-item')
		     , ('print-bill')
		     , ('view-log')
		;

drop table if exists role_permission cascade;

create table role_permission
  ( id_role       int not null references role(id)
  , id_permission int not null references permission(id)
  , primary key (id_role, id_permission)
);

-- Grant all permissions to Admin
insert into role_permission (id_role, id_permission)
select r.id
     , p.id
from   role       as r
join   permission as p
  on   1 = 1
where  r.name = 'Admin';

drop table if exists app_activity_log cascade;

create table app_activity_log
  ( id          serial        primary key
  , date        timestamp     not null default now()
  , id_app_user int           not null references app_user(id)
  , action      varchar(6)    not null check (action in ('insert', 'update', 'delete'))
  , details     varchar(1024) not null
);
