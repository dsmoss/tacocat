--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


--
-- Name: pgcrypto; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA public;


--
-- Name: EXTENSION pgcrypto; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION pgcrypto IS 'cryptographic functions';


--
-- Name: uuid-ossp; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS "uuid-ossp" WITH SCHEMA public;


--
-- Name: EXTENSION "uuid-ossp"; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION "uuid-ossp" IS 'generate universally unique identifiers (UUIDs)';


SET search_path = public, pg_catalog;

--
-- Name: produce_password_hash(character varying, uuid); Type: FUNCTION; Schema: public; Owner: tacocat
--

CREATE FUNCTION produce_password_hash(password character varying, salt uuid) RETURNS character varying
    LANGUAGE plpgsql
    AS $$begin
	return encode(digest(digest(password  , 'sha512')
                          || digest(salt::text, 'sha512')
                           , 'sha512')
                    , 'hex');
end;$$;


ALTER FUNCTION public.produce_password_hash(password character varying, salt uuid) OWNER TO tacocat;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: app_activity_log; Type: TABLE; Schema: public; Owner: tacocat; Tablespace: 
--

CREATE TABLE app_activity_log (
    id integer NOT NULL,
    date timestamp without time zone DEFAULT now() NOT NULL,
    id_app_user integer NOT NULL,
    action character varying(6) NOT NULL,
    details character varying(1024) NOT NULL,
    CONSTRAINT app_activity_log_action_check CHECK (((action)::text = ANY ((ARRAY['insert'::character varying, 'update'::character varying, 'delete'::character varying])::text[])))
);


ALTER TABLE app_activity_log OWNER TO tacocat;

--
-- Name: app_activity_log_id_seq; Type: SEQUENCE; Schema: public; Owner: tacocat
--

CREATE SEQUENCE app_activity_log_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE app_activity_log_id_seq OWNER TO tacocat;

--
-- Name: app_activity_log_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tacocat
--

ALTER SEQUENCE app_activity_log_id_seq OWNED BY app_activity_log.id;


--
-- Name: app_data; Type: TABLE; Schema: public; Owner: tacocat; Tablespace: 
--

CREATE TABLE app_data (
    key character varying(255) NOT NULL,
    val character varying(255)
);


ALTER TABLE app_data OWNER TO tacocat;

--
-- Name: app_user; Type: TABLE; Schema: public; Owner: tacocat; Tablespace: 
--

CREATE TABLE app_user (
    id integer NOT NULL,
    user_name character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    salt uuid DEFAULT uuid_generate_v4() NOT NULL,
    password_hash character varying(128) NOT NULL
);


ALTER TABLE app_user OWNER TO tacocat;

--
-- Name: app_user_id_seq; Type: SEQUENCE; Schema: public; Owner: tacocat
--

CREATE SEQUENCE app_user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE app_user_id_seq OWNER TO tacocat;

--
-- Name: app_user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tacocat
--

ALTER SEQUENCE app_user_id_seq OWNED BY app_user.id;


--
-- Name: app_user_ip_addr; Type: TABLE; Schema: public; Owner: tacocat; Tablespace: 
--

CREATE TABLE app_user_ip_addr (
    id_app_user integer,
    ip_addr character varying(39) NOT NULL,
    created timestamp without time zone DEFAULT now() NOT NULL,
    expires timestamp without time zone DEFAULT (now() + '08:00:00'::interval) NOT NULL,
    active boolean DEFAULT true NOT NULL
);


ALTER TABLE app_user_ip_addr OWNER TO tacocat;

--
-- Name: app_user_role; Type: TABLE; Schema: public; Owner: tacocat; Tablespace: 
--

CREATE TABLE app_user_role (
    id_app_user integer NOT NULL,
    id_role integer NOT NULL
);


ALTER TABLE app_user_role OWNER TO tacocat;

--
-- Name: bill; Type: TABLE; Schema: public; Owner: tacocat; Tablespace: 
--

CREATE TABLE bill (
    id integer NOT NULL,
    date timestamp without time zone DEFAULT now() NOT NULL,
    location character varying(255) NOT NULL
);


ALTER TABLE bill OWNER TO tacocat;

--
-- Name: bill_id_seq; Type: SEQUENCE; Schema: public; Owner: tacocat
--

CREATE SEQUENCE bill_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE bill_id_seq OWNER TO tacocat;

--
-- Name: bill_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tacocat
--

ALTER SEQUENCE bill_id_seq OWNED BY bill.id;


--
-- Name: bill_item; Type: TABLE; Schema: public; Owner: tacocat; Tablespace: 
--

CREATE TABLE bill_item (
    id integer NOT NULL,
    date timestamp without time zone DEFAULT now() NOT NULL,
    id_bill integer NOT NULL,
    person integer,
    id_item integer NOT NULL,
    charge_override numeric(10,2)
);


ALTER TABLE bill_item OWNER TO tacocat;

--
-- Name: bill_item_id_seq; Type: SEQUENCE; Schema: public; Owner: tacocat
--

CREATE SEQUENCE bill_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE bill_item_id_seq OWNER TO tacocat;

--
-- Name: bill_item_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tacocat
--

ALTER SEQUENCE bill_item_id_seq OWNED BY bill_item.id;


--
-- Name: bill_item_option; Type: TABLE; Schema: public; Owner: tacocat; Tablespace: 
--

CREATE TABLE bill_item_option (
    id_bill_item integer NOT NULL,
    id_option integer NOT NULL
);


ALTER TABLE bill_item_option OWNER TO tacocat;

--
-- Name: close; Type: TABLE; Schema: public; Owner: tacocat; Tablespace: 
--

CREATE TABLE close (
    id integer NOT NULL,
    date timestamp without time zone DEFAULT now() NOT NULL,
    expense_amount numeric(10,2) NOT NULL,
    intake_amount numeric(10,2) NOT NULL,
    earnings numeric(10,2) NOT NULL,
    business_share numeric(10,2) NOT NULL,
    partners_share numeric(10,2) NOT NULL,
    services_share numeric(10,2) NOT NULL,
    partner_take numeric(10,2) NOT NULL
);


ALTER TABLE close OWNER TO tacocat;

--
-- Name: close_id_seq; Type: SEQUENCE; Schema: public; Owner: tacocat
--

CREATE SEQUENCE close_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE close_id_seq OWNER TO tacocat;

--
-- Name: close_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tacocat
--

ALTER SEQUENCE close_id_seq OWNED BY close.id;


--
-- Name: expenses; Type: TABLE; Schema: public; Owner: tacocat; Tablespace: 
--

CREATE TABLE expenses (
    id integer NOT NULL,
    date timestamp without time zone DEFAULT now() NOT NULL,
    concept character varying(255) NOT NULL,
    amount numeric(10,2) NOT NULL,
    id_close integer
);


ALTER TABLE expenses OWNER TO tacocat;

--
-- Name: expenses_id_seq; Type: SEQUENCE; Schema: public; Owner: tacocat
--

CREATE SEQUENCE expenses_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE expenses_id_seq OWNER TO tacocat;

--
-- Name: expenses_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tacocat
--

ALTER SEQUENCE expenses_id_seq OWNED BY expenses.id;


--
-- Name: intakes; Type: TABLE; Schema: public; Owner: tacocat; Tablespace: 
--

CREATE TABLE intakes (
    id_bill integer NOT NULL,
    date timestamp without time zone DEFAULT now(),
    amount numeric(10,2) NOT NULL,
    id_close integer
);


ALTER TABLE intakes OWNER TO tacocat;

--
-- Name: item; Type: TABLE; Schema: public; Owner: tacocat; Tablespace: 
--

CREATE TABLE item (
    id integer NOT NULL,
    name character varying(255) NOT NULL,
    menu_group character varying(255) NOT NULL,
    charge numeric(10,2) NOT NULL,
    in_stock boolean DEFAULT true NOT NULL
);


ALTER TABLE item OWNER TO tacocat;

--
-- Name: item_id_seq; Type: SEQUENCE; Schema: public; Owner: tacocat
--

CREATE SEQUENCE item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE item_id_seq OWNER TO tacocat;

--
-- Name: item_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tacocat
--

ALTER SEQUENCE item_id_seq OWNED BY item.id;


--
-- Name: item_option; Type: TABLE; Schema: public; Owner: tacocat; Tablespace: 
--

CREATE TABLE item_option (
    id_item integer NOT NULL,
    id_option integer NOT NULL
);


ALTER TABLE item_option OWNER TO tacocat;

--
-- Name: menu_groups; Type: TABLE; Schema: public; Owner: tacocat; Tablespace: 
--

CREATE TABLE menu_groups (
    name character varying(255) NOT NULL
);


ALTER TABLE menu_groups OWNER TO tacocat;

--
-- Name: option; Type: TABLE; Schema: public; Owner: tacocat; Tablespace: 
--

CREATE TABLE option (
    id integer NOT NULL,
    name character varying(255) NOT NULL,
    option_group character varying(255) NOT NULL,
    extra_charge numeric(10,2) DEFAULT 0 NOT NULL,
    in_stock boolean DEFAULT true NOT NULL
);


ALTER TABLE option OWNER TO tacocat;

--
-- Name: option_groups; Type: TABLE; Schema: public; Owner: tacocat; Tablespace: 
--

CREATE TABLE option_groups (
    name character varying(255) NOT NULL
);


ALTER TABLE option_groups OWNER TO tacocat;

--
-- Name: option_id_seq; Type: SEQUENCE; Schema: public; Owner: tacocat
--

CREATE SEQUENCE option_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE option_id_seq OWNER TO tacocat;

--
-- Name: option_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tacocat
--

ALTER SEQUENCE option_id_seq OWNED BY option.id;


--
-- Name: permission; Type: TABLE; Schema: public; Owner: tacocat; Tablespace: 
--

CREATE TABLE permission (
    id integer NOT NULL,
    name character varying(255) NOT NULL
);


ALTER TABLE permission OWNER TO tacocat;

--
-- Name: permission_id_seq; Type: SEQUENCE; Schema: public; Owner: tacocat
--

CREATE SEQUENCE permission_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE permission_id_seq OWNER TO tacocat;

--
-- Name: permission_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tacocat
--

ALTER SEQUENCE permission_id_seq OWNED BY permission.id;


--
-- Name: role; Type: TABLE; Schema: public; Owner: tacocat; Tablespace: 
--

CREATE TABLE role (
    id integer NOT NULL,
    name character varying(255) NOT NULL
);


ALTER TABLE role OWNER TO tacocat;

--
-- Name: role_id_seq; Type: SEQUENCE; Schema: public; Owner: tacocat
--

CREATE SEQUENCE role_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE role_id_seq OWNER TO tacocat;

--
-- Name: role_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tacocat
--

ALTER SEQUENCE role_id_seq OWNED BY role.id;


--
-- Name: role_permission; Type: TABLE; Schema: public; Owner: tacocat; Tablespace: 
--

CREATE TABLE role_permission (
    id_role integer NOT NULL,
    id_permission integer NOT NULL
);


ALTER TABLE role_permission OWNER TO tacocat;

--
-- Name: services; Type: TABLE; Schema: public; Owner: tacocat; Tablespace: 
--

CREATE TABLE services (
    id integer NOT NULL,
    date timestamp without time zone DEFAULT now() NOT NULL,
    amount numeric(10,2) NOT NULL,
    running_total numeric(10,2) NOT NULL,
    id_close integer,
    concept character varying(255) NOT NULL
);


ALTER TABLE services OWNER TO tacocat;

--
-- Name: services_id_seq; Type: SEQUENCE; Schema: public; Owner: tacocat
--

CREATE SEQUENCE services_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE services_id_seq OWNER TO tacocat;

--
-- Name: services_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tacocat
--

ALTER SEQUENCE services_id_seq OWNED BY services.id;


--
-- Name: v_accounting; Type: VIEW; Schema: public; Owner: tacocat
--

CREATE VIEW v_accounting AS
 SELECT (((b.id || ' ('::text) || (b.location)::text) || ')'::text) AS concept,
    i.date,
    i.amount,
    i.id_close
   FROM (intakes i
     JOIN bill b ON ((i.id_bill = b.id)))
UNION
 SELECT e.concept,
    e.date,
    e.amount,
    e.id_close
   FROM expenses e
  ORDER BY 2 DESC;


ALTER TABLE v_accounting OWNER TO tacocat;

--
-- Name: v_bill_items; Type: TABLE; Schema: public; Owner: tacocat; Tablespace: 
--

CREATE TABLE v_bill_items (
    id integer,
    id_bill integer,
    id_item integer,
    date timestamp without time zone,
    item character varying(255),
    person integer,
    options text,
    charge numeric
);

ALTER TABLE ONLY v_bill_items REPLICA IDENTITY NOTHING;


ALTER TABLE v_bill_items OWNER TO tacocat;

--
-- Name: v_bills; Type: VIEW; Schema: public; Owner: tacocat
--

CREATE VIEW v_bills AS
 SELECT b.id,
    b.date,
    b.location,
    COALESCE(( SELECT i.amount
           FROM intakes i
          WHERE (i.id_bill = b.id)), ( SELECT sum(vvi.charge) AS sum
           FROM v_bill_items vvi
          WHERE (b.id = vvi.id_bill)), (0)::numeric) AS charge,
    (b.id IN ( SELECT intakes.id_bill
           FROM intakes)) AS closed
   FROM bill b;


ALTER TABLE v_bills OWNER TO tacocat;

--
-- Name: v_item_options; Type: VIEW; Schema: public; Owner: tacocat
--

CREATE VIEW v_item_options AS
 SELECT io.id_item,
    io.id_option,
    o.name AS option_name,
    o.in_stock AS option_in_stock,
    i.name AS item_name,
    i.in_stock AS item_in_stock,
    o.option_group
   FROM ((item_option io
     JOIN option o ON ((io.id_option = o.id)))
     JOIN item i ON ((io.id_item = i.id)));


ALTER TABLE v_item_options OWNER TO tacocat;

--
-- Name: id; Type: DEFAULT; Schema: public; Owner: tacocat
--

ALTER TABLE ONLY app_activity_log ALTER COLUMN id SET DEFAULT nextval('app_activity_log_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: tacocat
--

ALTER TABLE ONLY app_user ALTER COLUMN id SET DEFAULT nextval('app_user_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: tacocat
--

ALTER TABLE ONLY bill ALTER COLUMN id SET DEFAULT nextval('bill_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: tacocat
--

ALTER TABLE ONLY bill_item ALTER COLUMN id SET DEFAULT nextval('bill_item_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: tacocat
--

ALTER TABLE ONLY close ALTER COLUMN id SET DEFAULT nextval('close_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: tacocat
--

ALTER TABLE ONLY expenses ALTER COLUMN id SET DEFAULT nextval('expenses_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: tacocat
--

ALTER TABLE ONLY item ALTER COLUMN id SET DEFAULT nextval('item_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: tacocat
--

ALTER TABLE ONLY option ALTER COLUMN id SET DEFAULT nextval('option_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: tacocat
--

ALTER TABLE ONLY permission ALTER COLUMN id SET DEFAULT nextval('permission_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: tacocat
--

ALTER TABLE ONLY role ALTER COLUMN id SET DEFAULT nextval('role_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: tacocat
--

ALTER TABLE ONLY services ALTER COLUMN id SET DEFAULT nextval('services_id_seq'::regclass);


--
-- Data for Name: app_activity_log; Type: TABLE DATA; Schema: public; Owner: tacocat
--

COPY app_activity_log (id, date, id_app_user, action, details) FROM stdin;
\.


--
-- Name: app_activity_log_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tacocat
--

SELECT pg_catalog.setval('app_activity_log_id_seq', 1, false);


--
-- Data for Name: app_data; Type: TABLE DATA; Schema: public; Owner: tacocat
--

COPY app_data (key, val) FROM stdin;
business-name	TacOcaT
profit-business-share	0.5
profit-services-share	0.1
profit-partners-share	0.4
partner-count	3
card-multiplicative	1.05
business-address	Some Street 101, Some Town
business-state	My State
business-post-code	12345
business-telephone	01800 TACOCAT
business-website	my-site.com
\.


--
-- Data for Name: app_user; Type: TABLE DATA; Schema: public; Owner: tacocat
--

COPY app_user (id, user_name, name, salt, password_hash) FROM stdin;
1	tacocat	Taco Cat	dac87755-ef29-4236-bf97-3aabfa428941	fa56b0c5aff504e177a4ce55a9d837370dec38ce1801f42fd36c6e17f979a083185e06c0ad5e20f92bb3f175be5fbb2f3bf14a2690ec548aec1801907dccafdd
\.


--
-- Name: app_user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tacocat
--

SELECT pg_catalog.setval('app_user_id_seq', 1, true);


--
-- Data for Name: app_user_ip_addr; Type: TABLE DATA; Schema: public; Owner: tacocat
--

COPY app_user_ip_addr (id_app_user, ip_addr, created, expires, active) FROM stdin;
\.


--
-- Data for Name: app_user_role; Type: TABLE DATA; Schema: public; Owner: tacocat
--

COPY app_user_role (id_app_user, id_role) FROM stdin;
1	1
\.


--
-- Data for Name: bill; Type: TABLE DATA; Schema: public; Owner: tacocat
--

COPY bill (id, date, location) FROM stdin;
\.


--
-- Name: bill_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tacocat
--

SELECT pg_catalog.setval('bill_id_seq', 1, false);


--
-- Data for Name: bill_item; Type: TABLE DATA; Schema: public; Owner: tacocat
--

COPY bill_item (id, date, id_bill, person, id_item, charge_override) FROM stdin;
\.


--
-- Name: bill_item_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tacocat
--

SELECT pg_catalog.setval('bill_item_id_seq', 1, false);


--
-- Data for Name: bill_item_option; Type: TABLE DATA; Schema: public; Owner: tacocat
--

COPY bill_item_option (id_bill_item, id_option) FROM stdin;
\.


--
-- Data for Name: close; Type: TABLE DATA; Schema: public; Owner: tacocat
--

COPY close (id, date, expense_amount, intake_amount, earnings, business_share, partners_share, services_share, partner_take) FROM stdin;
\.


--
-- Name: close_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tacocat
--

SELECT pg_catalog.setval('close_id_seq', 1, false);


--
-- Data for Name: expenses; Type: TABLE DATA; Schema: public; Owner: tacocat
--

COPY expenses (id, date, concept, amount, id_close) FROM stdin;
\.


--
-- Name: expenses_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tacocat
--

SELECT pg_catalog.setval('expenses_id_seq', 1, false);


--
-- Data for Name: intakes; Type: TABLE DATA; Schema: public; Owner: tacocat
--

COPY intakes (id_bill, date, amount, id_close) FROM stdin;
\.


--
-- Data for Name: item; Type: TABLE DATA; Schema: public; Owner: tacocat
--

COPY item (id, name, menu_group, charge, in_stock) FROM stdin;
\.


--
-- Name: item_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tacocat
--

SELECT pg_catalog.setval('item_id_seq', 1, false);


--
-- Data for Name: item_option; Type: TABLE DATA; Schema: public; Owner: tacocat
--

COPY item_option (id_item, id_option) FROM stdin;
\.


--
-- Data for Name: menu_groups; Type: TABLE DATA; Schema: public; Owner: tacocat
--

COPY menu_groups (name) FROM stdin;
\.


--
-- Data for Name: option; Type: TABLE DATA; Schema: public; Owner: tacocat
--

COPY option (id, name, option_group, extra_charge, in_stock) FROM stdin;
\.


--
-- Data for Name: option_groups; Type: TABLE DATA; Schema: public; Owner: tacocat
--

COPY option_groups (name) FROM stdin;
\.


--
-- Name: option_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tacocat
--

SELECT pg_catalog.setval('option_id_seq', 1, false);


--
-- Data for Name: permission; Type: TABLE DATA; Schema: public; Owner: tacocat
--

COPY permission (id, name) FROM stdin;
1	close-accounts
2	charge-bill
3	add-expense
4	add-services-expense
5	create-new-bill
6	view-open-bill
7	view-closed-bill
8	list-closed-bills
9	delete-bill-item
10	set-options-to-bill-item
11	change-bill-item
12	assign-bill-item-to-person
13	change-bill-item-price
14	change-app-values
15	add-user-roles
16	assign-user-roles
17	assign-role-permissions
18	create-bill-item
19	change-bill-location
20	list-bills
21	view-closed-account
22	list-closed-accounts
23	list-closed-services
24	view-closed-services
25	list-services
26	view-accounts
27	list-users
28	view-other-users
29	list-roles
30	list-items
31	change-stock-status
32	view-app-values
33	add-user
34	change-other-users-password
35	delete-user
36	change-other-users-name
37	view-role
38	delete-roles
39	view-item
40	set-item-menu-group
41	set-item-charge
42	set-item-name
43	add-new-item
44	set-option-in-stock
45	add-option-to-item
46	remove-option-from-item
47	create-new-option-group
48	create-new-option
49	view-option
50	set-option-name
51	set-option-group
52	set-option-charge
53	delete-item
54	print-bill
\.


--
-- Name: permission_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tacocat
--

SELECT pg_catalog.setval('permission_id_seq', 54, true);


--
-- Data for Name: role; Type: TABLE DATA; Schema: public; Owner: tacocat
--

COPY role (id, name) FROM stdin;
1	Admin
\.


--
-- Name: role_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tacocat
--

SELECT pg_catalog.setval('role_id_seq', 1, true);


--
-- Data for Name: role_permission; Type: TABLE DATA; Schema: public; Owner: tacocat
--

COPY role_permission (id_role, id_permission) FROM stdin;
1	1
1	2
1	3
1	4
1	5
1	6
1	7
1	8
1	9
1	10
1	11
1	12
1	13
1	14
1	15
1	16
1	17
1	18
1	19
1	20
1	21
1	22
1	23
1	24
1	25
1	26
1	27
1	28
1	29
1	30
1	31
1	32
1	33
1	34
1	35
1	36
1	37
1	38
1	39
1	40
1	41
1	42
1	43
1	44
1	45
1	46
1	47
1	48
1	49
1	50
1	51
1	52
1	53
1	54
\.


--
-- Data for Name: services; Type: TABLE DATA; Schema: public; Owner: tacocat
--

COPY services (id, date, amount, running_total, id_close, concept) FROM stdin;
\.


--
-- Name: services_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tacocat
--

SELECT pg_catalog.setval('services_id_seq', 1, false);


--
-- Name: app_activity_log_pkey; Type: CONSTRAINT; Schema: public; Owner: tacocat; Tablespace: 
--

ALTER TABLE ONLY app_activity_log
    ADD CONSTRAINT app_activity_log_pkey PRIMARY KEY (id);


--
-- Name: app_data_pkey; Type: CONSTRAINT; Schema: public; Owner: tacocat; Tablespace: 
--

ALTER TABLE ONLY app_data
    ADD CONSTRAINT app_data_pkey PRIMARY KEY (key);


--
-- Name: app_user_ip_addr_pkey; Type: CONSTRAINT; Schema: public; Owner: tacocat; Tablespace: 
--

ALTER TABLE ONLY app_user_ip_addr
    ADD CONSTRAINT app_user_ip_addr_pkey PRIMARY KEY (ip_addr);


--
-- Name: app_user_pkey; Type: CONSTRAINT; Schema: public; Owner: tacocat; Tablespace: 
--

ALTER TABLE ONLY app_user
    ADD CONSTRAINT app_user_pkey PRIMARY KEY (id);


--
-- Name: app_user_role_pkey; Type: CONSTRAINT; Schema: public; Owner: tacocat; Tablespace: 
--

ALTER TABLE ONLY app_user_role
    ADD CONSTRAINT app_user_role_pkey PRIMARY KEY (id_app_user, id_role);


--
-- Name: app_user_user_name_key; Type: CONSTRAINT; Schema: public; Owner: tacocat; Tablespace: 
--

ALTER TABLE ONLY app_user
    ADD CONSTRAINT app_user_user_name_key UNIQUE (user_name);


--
-- Name: bill_item_option_pkey; Type: CONSTRAINT; Schema: public; Owner: tacocat; Tablespace: 
--

ALTER TABLE ONLY bill_item_option
    ADD CONSTRAINT bill_item_option_pkey PRIMARY KEY (id_bill_item, id_option);


--
-- Name: bill_item_pkey; Type: CONSTRAINT; Schema: public; Owner: tacocat; Tablespace: 
--

ALTER TABLE ONLY bill_item
    ADD CONSTRAINT bill_item_pkey PRIMARY KEY (id);


--
-- Name: bill_pkey; Type: CONSTRAINT; Schema: public; Owner: tacocat; Tablespace: 
--

ALTER TABLE ONLY bill
    ADD CONSTRAINT bill_pkey PRIMARY KEY (id);


--
-- Name: close_pkey; Type: CONSTRAINT; Schema: public; Owner: tacocat; Tablespace: 
--

ALTER TABLE ONLY close
    ADD CONSTRAINT close_pkey PRIMARY KEY (id);


--
-- Name: expenses_pkey; Type: CONSTRAINT; Schema: public; Owner: tacocat; Tablespace: 
--

ALTER TABLE ONLY expenses
    ADD CONSTRAINT expenses_pkey PRIMARY KEY (id);


--
-- Name: intakes_pkey; Type: CONSTRAINT; Schema: public; Owner: tacocat; Tablespace: 
--

ALTER TABLE ONLY intakes
    ADD CONSTRAINT intakes_pkey PRIMARY KEY (id_bill);


--
-- Name: item_name_key; Type: CONSTRAINT; Schema: public; Owner: tacocat; Tablespace: 
--

ALTER TABLE ONLY item
    ADD CONSTRAINT item_name_key UNIQUE (name);


--
-- Name: item_option_pkey; Type: CONSTRAINT; Schema: public; Owner: tacocat; Tablespace: 
--

ALTER TABLE ONLY item_option
    ADD CONSTRAINT item_option_pkey PRIMARY KEY (id_item, id_option);


--
-- Name: item_pkey; Type: CONSTRAINT; Schema: public; Owner: tacocat; Tablespace: 
--

ALTER TABLE ONLY item
    ADD CONSTRAINT item_pkey PRIMARY KEY (id);


--
-- Name: menu_groups_pkey; Type: CONSTRAINT; Schema: public; Owner: tacocat; Tablespace: 
--

ALTER TABLE ONLY menu_groups
    ADD CONSTRAINT menu_groups_pkey PRIMARY KEY (name);


--
-- Name: option_groups_pkey; Type: CONSTRAINT; Schema: public; Owner: tacocat; Tablespace: 
--

ALTER TABLE ONLY option_groups
    ADD CONSTRAINT option_groups_pkey PRIMARY KEY (name);


--
-- Name: option_pkey; Type: CONSTRAINT; Schema: public; Owner: tacocat; Tablespace: 
--

ALTER TABLE ONLY option
    ADD CONSTRAINT option_pkey PRIMARY KEY (id);


--
-- Name: permission_name_key; Type: CONSTRAINT; Schema: public; Owner: tacocat; Tablespace: 
--

ALTER TABLE ONLY permission
    ADD CONSTRAINT permission_name_key UNIQUE (name);


--
-- Name: permission_pkey; Type: CONSTRAINT; Schema: public; Owner: tacocat; Tablespace: 
--

ALTER TABLE ONLY permission
    ADD CONSTRAINT permission_pkey PRIMARY KEY (id);


--
-- Name: role_name_key; Type: CONSTRAINT; Schema: public; Owner: tacocat; Tablespace: 
--

ALTER TABLE ONLY role
    ADD CONSTRAINT role_name_key UNIQUE (name);


--
-- Name: role_permission_pkey; Type: CONSTRAINT; Schema: public; Owner: tacocat; Tablespace: 
--

ALTER TABLE ONLY role_permission
    ADD CONSTRAINT role_permission_pkey PRIMARY KEY (id_role, id_permission);


--
-- Name: role_pkey; Type: CONSTRAINT; Schema: public; Owner: tacocat; Tablespace: 
--

ALTER TABLE ONLY role
    ADD CONSTRAINT role_pkey PRIMARY KEY (id);


--
-- Name: services_pkey; Type: CONSTRAINT; Schema: public; Owner: tacocat; Tablespace: 
--

ALTER TABLE ONLY services
    ADD CONSTRAINT services_pkey PRIMARY KEY (id);


--
-- Name: _RETURN; Type: RULE; Schema: public; Owner: tacocat
--

CREATE RULE "_RETURN" AS
    ON SELECT TO v_bill_items DO INSTEAD  SELECT bi.id,
    bi.id_bill,
    i.id AS id_item,
    bi.date,
    i.name AS item,
    bi.person,
    string_agg((o.name)::text, ', '::text) AS options,
    COALESCE(bi.charge_override, (i.charge + COALESCE(sum(o.extra_charge), (0)::numeric))) AS charge
   FROM (((bill_item bi
     JOIN item i ON ((bi.id_item = i.id)))
     LEFT JOIN bill_item_option bio ON ((bio.id_bill_item = bi.id)))
     LEFT JOIN option o ON ((bio.id_option = o.id)))
  GROUP BY bi.id_bill, bi.id, i.name, i.charge, i.id;


--
-- Name: app_activity_log_id_app_user_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tacocat
--

ALTER TABLE ONLY app_activity_log
    ADD CONSTRAINT app_activity_log_id_app_user_fkey FOREIGN KEY (id_app_user) REFERENCES app_user(id);


--
-- Name: app_user_ip_addr_id_app_user_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tacocat
--

ALTER TABLE ONLY app_user_ip_addr
    ADD CONSTRAINT app_user_ip_addr_id_app_user_fkey FOREIGN KEY (id_app_user) REFERENCES app_user(id);


--
-- Name: app_user_role_id_app_user_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tacocat
--

ALTER TABLE ONLY app_user_role
    ADD CONSTRAINT app_user_role_id_app_user_fkey FOREIGN KEY (id_app_user) REFERENCES app_user(id);


--
-- Name: app_user_role_id_role_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tacocat
--

ALTER TABLE ONLY app_user_role
    ADD CONSTRAINT app_user_role_id_role_fkey FOREIGN KEY (id_role) REFERENCES role(id);


--
-- Name: bill_item_id_bill_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tacocat
--

ALTER TABLE ONLY bill_item
    ADD CONSTRAINT bill_item_id_bill_fkey FOREIGN KEY (id_bill) REFERENCES bill(id);


--
-- Name: bill_item_id_item_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tacocat
--

ALTER TABLE ONLY bill_item
    ADD CONSTRAINT bill_item_id_item_fkey FOREIGN KEY (id_item) REFERENCES item(id);


--
-- Name: bill_item_option_id_bill_item_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tacocat
--

ALTER TABLE ONLY bill_item_option
    ADD CONSTRAINT bill_item_option_id_bill_item_fkey FOREIGN KEY (id_bill_item) REFERENCES bill_item(id) ON DELETE CASCADE;


--
-- Name: bill_item_option_id_option_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tacocat
--

ALTER TABLE ONLY bill_item_option
    ADD CONSTRAINT bill_item_option_id_option_fkey FOREIGN KEY (id_option) REFERENCES option(id);


--
-- Name: expenses_id_close_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tacocat
--

ALTER TABLE ONLY expenses
    ADD CONSTRAINT expenses_id_close_fkey FOREIGN KEY (id_close) REFERENCES close(id);


--
-- Name: intakes_id_bill_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tacocat
--

ALTER TABLE ONLY intakes
    ADD CONSTRAINT intakes_id_bill_fkey FOREIGN KEY (id_bill) REFERENCES bill(id);


--
-- Name: intakes_id_close_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tacocat
--

ALTER TABLE ONLY intakes
    ADD CONSTRAINT intakes_id_close_fkey FOREIGN KEY (id_close) REFERENCES close(id);


--
-- Name: item_menu_group_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tacocat
--

ALTER TABLE ONLY item
    ADD CONSTRAINT item_menu_group_fkey FOREIGN KEY (menu_group) REFERENCES menu_groups(name);


--
-- Name: item_option_id_item_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tacocat
--

ALTER TABLE ONLY item_option
    ADD CONSTRAINT item_option_id_item_fkey FOREIGN KEY (id_item) REFERENCES item(id);


--
-- Name: item_option_id_option_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tacocat
--

ALTER TABLE ONLY item_option
    ADD CONSTRAINT item_option_id_option_fkey FOREIGN KEY (id_option) REFERENCES option(id);


--
-- Name: option_option_group_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tacocat
--

ALTER TABLE ONLY option
    ADD CONSTRAINT option_option_group_fkey FOREIGN KEY (option_group) REFERENCES option_groups(name);


--
-- Name: role_permission_id_permission_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tacocat
--

ALTER TABLE ONLY role_permission
    ADD CONSTRAINT role_permission_id_permission_fkey FOREIGN KEY (id_permission) REFERENCES permission(id);


--
-- Name: role_permission_id_role_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tacocat
--

ALTER TABLE ONLY role_permission
    ADD CONSTRAINT role_permission_id_role_fkey FOREIGN KEY (id_role) REFERENCES role(id);


--
-- Name: services_id_close_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tacocat
--

ALTER TABLE ONLY services
    ADD CONSTRAINT services_id_close_fkey FOREIGN KEY (id_close) REFERENCES close(id);


--
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- Name: app_data; Type: ACL; Schema: public; Owner: tacocat
--

REVOKE ALL ON TABLE app_data FROM PUBLIC;
REVOKE ALL ON TABLE app_data FROM tacocat;
GRANT ALL ON TABLE app_data TO tacocat;


--
-- Name: bill; Type: ACL; Schema: public; Owner: tacocat
--

REVOKE ALL ON TABLE bill FROM PUBLIC;
REVOKE ALL ON TABLE bill FROM tacocat;
GRANT ALL ON TABLE bill TO tacocat;


--
-- Name: bill_item; Type: ACL; Schema: public; Owner: tacocat
--

REVOKE ALL ON TABLE bill_item FROM PUBLIC;
REVOKE ALL ON TABLE bill_item FROM tacocat;
GRANT ALL ON TABLE bill_item TO tacocat;


--
-- Name: close; Type: ACL; Schema: public; Owner: tacocat
--

REVOKE ALL ON TABLE close FROM PUBLIC;
REVOKE ALL ON TABLE close FROM tacocat;
GRANT ALL ON TABLE close TO tacocat;


--
-- Name: expenses; Type: ACL; Schema: public; Owner: tacocat
--

REVOKE ALL ON TABLE expenses FROM PUBLIC;
REVOKE ALL ON TABLE expenses FROM tacocat;
GRANT ALL ON TABLE expenses TO tacocat;


--
-- Name: intakes; Type: ACL; Schema: public; Owner: tacocat
--

REVOKE ALL ON TABLE intakes FROM PUBLIC;
REVOKE ALL ON TABLE intakes FROM tacocat;
GRANT ALL ON TABLE intakes TO tacocat;


--
-- Name: item; Type: ACL; Schema: public; Owner: tacocat
--

REVOKE ALL ON TABLE item FROM PUBLIC;
REVOKE ALL ON TABLE item FROM tacocat;
GRANT ALL ON TABLE item TO tacocat;


--
-- Name: item_option; Type: ACL; Schema: public; Owner: tacocat
--

REVOKE ALL ON TABLE item_option FROM PUBLIC;
REVOKE ALL ON TABLE item_option FROM tacocat;
GRANT ALL ON TABLE item_option TO tacocat;


--
-- Name: menu_groups; Type: ACL; Schema: public; Owner: tacocat
--

REVOKE ALL ON TABLE menu_groups FROM PUBLIC;
REVOKE ALL ON TABLE menu_groups FROM tacocat;
GRANT ALL ON TABLE menu_groups TO tacocat;


--
-- Name: option; Type: ACL; Schema: public; Owner: tacocat
--

REVOKE ALL ON TABLE option FROM PUBLIC;
REVOKE ALL ON TABLE option FROM tacocat;
GRANT ALL ON TABLE option TO tacocat;


--
-- Name: option_groups; Type: ACL; Schema: public; Owner: tacocat
--

REVOKE ALL ON TABLE option_groups FROM PUBLIC;
REVOKE ALL ON TABLE option_groups FROM tacocat;
GRANT ALL ON TABLE option_groups TO tacocat;


--
-- Name: services; Type: ACL; Schema: public; Owner: tacocat
--

REVOKE ALL ON TABLE services FROM PUBLIC;
REVOKE ALL ON TABLE services FROM tacocat;
GRANT ALL ON TABLE services TO tacocat;


--
-- PostgreSQL database dump complete
--

