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
    password_hash character varying(255) NOT NULL
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
    ip_addr character varying(15) NOT NULL,
    created timestamp without time zone DEFAULT now() NOT NULL,
    expires timestamp without time zone DEFAULT (now() + '08:00:00'::interval) NOT NULL
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
-- Name: t; Type: TABLE; Schema: public; Owner: tacocat; Tablespace: 
--

CREATE TABLE t (
    n interval
);


ALTER TABLE t OWNER TO tacocat;

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
1	2019-08-27 22:23:36.718459	11	delete	:app_user_ip_addr["ip_addr = ?" "192.168.100.109"]
2	2019-08-27 22:24:47.746303	11	delete	:app_user_ip_addr["ip_addr = ?" "192.168.100.109"]
3	2019-08-27 22:24:47.746303	11	insert	:app_user_ip_addr{:ip_addr "192.168.100.109", :id_app_user 11}
7	2019-08-27 23:03:51.021939	11	insert	:role_permission{:id_role 4, :id_permission 32}
8	2019-08-27 23:05:48.035041	11	insert	:role_permission{:id_role 4, :id_permission 20}
9	2019-08-27 23:06:03.145419	11	insert	:role_permission{:id_role 4, :id_permission 22}
10	2019-08-27 23:06:09.084067	11	insert	:role_permission{:id_role 4, :id_permission 8}
11	2019-08-27 23:06:24.473187	11	insert	:role_permission{:id_role 4, :id_permission 23}
12	2019-08-27 23:06:30.795012	11	insert	:role_permission{:id_role 4, :id_permission 31}
13	2019-08-27 23:06:41.502124	11	insert	:role_permission{:id_role 4, :id_permission 25}
14	2019-08-27 23:07:03.755405	11	insert	:role_permission{:id_role 4, :id_permission 44}
15	2019-08-27 23:07:15.028365	11	delete	:role_permission["id_role = ? and id_permission = ?" 4 44]
16	2019-08-27 23:07:56.429381	11	insert	:role_permission{:id_role 4, :id_permission 46}
17	2019-08-27 23:08:07.336945	11	insert	:role_permission{:id_role 4, :id_permission 26}
18	2019-08-27 23:08:13.400935	11	insert	:role_permission{:id_role 4, :id_permission 33}
19	2019-08-27 23:08:34.205321	11	insert	:role_permission{:id_role 4, :id_permission 21}
20	2019-08-27 23:08:41.673776	11	insert	:role_permission{:id_role 4, :id_permission 7}
21	2019-08-27 23:08:48.360259	11	insert	:role_permission{:id_role 4, :id_permission 24}
22	2019-08-27 23:09:12.158061	11	insert	:role_permission{:id_role 4, :id_permission 6}
23	2019-08-27 23:09:36.067778	11	insert	:app_user{:name "Bar", :user_name "bar", :password_hash ""}
24	2019-08-27 23:09:36.067778	11	update	:app_user{:password_hash nil}["id = ?" 23]
25	2019-08-27 23:13:30.429904	11	insert	:app_user{:name "Cocina", :user_name "cocina", :password_hash ""}
26	2019-08-27 23:13:30.429904	11	update	:app_user{:password_hash nil}["id = ?" 25]
27	2019-08-27 23:17:17.77232	11	insert	:app_user{:name "Gerencia", :user_name "gerencia", :password_hash ""}
28	2019-08-27 23:17:17.77232	11	update	:app_user{:password_hash nil}["id = ?" 27]
29	2019-08-27 23:20:08.793525	11	insert	:app_user{:name "Limpieza", :user_name "limpieza", :password_hash ""}
30	2019-08-27 23:20:08.793525	11	update	:app_user{:password_hash nil}["id = ?" 29]
31	2019-08-27 23:22:46.464119	11	insert	:app_user{:name "Mesero", :user_name "mesero", :password_hash ""}
32	2019-08-27 23:22:46.464119	11	update	:app_user{:password_hash nil}["id = ?" 31]
33	2019-08-27 23:26:51.33841	11	insert	:app_user{:name "Registro", :user_name "registro", :password_hash ""}
34	2019-08-27 23:26:51.33841	11	update	:app_user{:password_hash "48fe887efa7051f2db1dc85cf6708cfcca1d1402f1d8b919471813d6d93b60767be31f4fcbd2f1c93d6349888a6446ab6396531b7ec400df7b7f9ecc6fbdd469"}["id = ?" 17]
35	2019-08-27 23:30:31.239256	11	update	:app_user{:password_hash "84b35bc82fb14d288e9676988f4543297a7bbda824b049564a5bb2fb05b5e88cddcdd1b3649281f30091a0a91fcf8433047cfb1e4f7594501d5622c223f50ccc"}["id = ?" 16]
36	2019-08-27 23:31:19.416078	11	update	:app_user{:password_hash "57f3d269710598b69faf7bbda6eed38596eb72570e648e2330153ae9a4330030e2ef213d1dccba6473f6aed4ce781ff615811ea53007ffcada94fdb91caa75a2"}["id = ?" 15]
37	2019-08-27 23:31:38.160499	11	update	:app_user{:password_hash "d5e5c65fe0740ba2e64427ae2b6b8b950529e3448093bc1f3176edba497789af69f45cb6d1a7483e6e03c73c737c63beeb4ec9c9e109cb31588bada943ca6830"}["id = ?" 14]
38	2019-08-27 23:32:17.547868	11	update	:app_user{:password_hash "493f628ac21ffd82e5d0200fecfb7235a5ac552206147f1bfbe20b4113eb7670760bb75b5e410421bb6b4f5e4475b5df9dacb1b70e1d72d006b35d998716d697"}["id = ?" 13]
39	2019-08-27 23:32:33.96289	11	update	:app_user{:password_hash "73d032de3272df4da9b5229bc83fc4d97ba9b223972c764d18c2c232210ba91bee9312cafcf2865d77e423f34633c3be5e5c01f06eb731e69e079bf4e3997302"}["id = ?" 12]
40	2019-08-27 23:33:38.475516	11	delete	:app_user_role["id_app_user = ?" 17]
41	2019-08-27 23:33:38.475516	11	insert	:app_user_role{:id_role 7, :id_app_user 17}
42	2019-08-27 23:33:56.791217	11	delete	:app_user_role["id_app_user = ?" 16]
43	2019-08-27 23:33:56.791217	11	insert	:app_user_role{:id_role 2, :id_app_user 16}
44	2019-08-27 23:34:16.50955	11	delete	:app_user_role["id_app_user = ?" 15]
45	2019-08-27 23:34:16.50955	11	insert	:app_user_role{:id_role 5, :id_app_user 15}
46	2019-08-27 23:34:34.138189	11	delete	:app_user_role["id_app_user = ?" 14]
47	2019-08-27 23:34:34.138189	11	insert	:app_user_role{:id_role 6, :id_app_user 14}
48	2019-08-27 23:34:47.632289	11	delete	:app_user_role["id_app_user = ?" 13]
49	2019-08-27 23:34:47.632289	11	insert	:app_user_role{:id_role 3, :id_app_user 13}
50	2019-08-27 23:35:00.294242	11	delete	:app_user_role["id_app_user = ?" 12]
51	2019-08-27 23:35:00.294242	11	insert	:app_user_role{:id_role 4, :id_app_user 12}
\.


--
-- Name: app_activity_log_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tacocat
--

SELECT pg_catalog.setval('app_activity_log_id_seq', 51, true);


--
-- Data for Name: app_data; Type: TABLE DATA; Schema: public; Owner: tacocat
--

COPY app_data (key, val) FROM stdin;
business-name	Sonder Bar
profit-business-share	0.5
profit-services-share	0.1
profit-partners-share	0.4
partner-count	1
card-multiplicative	1.05
default-font	Lucida Blackletter Regular
business-address	Mariscala 101, Mineral de Pozos
business-state	Guanajuato
business-post-code	37910
business-telephone	55 3145 8216
business-website	facebook.com/barRestaurantSonder
\.


--
-- Data for Name: app_user; Type: TABLE DATA; Schema: public; Owner: tacocat
--

COPY app_user (id, user_name, name, salt, password_hash) FROM stdin;
1	dsm	David S. Moß	9c21331f-2fe7-4d49-bb03-735d01da9906	8d715d58d00d70e60a1d96e6dceff0b7521fd2e44e8957ccd59fe97b3c896816a8522d852f71e5bc38323dfed9dc51992955829cc4590f1162c72369c9fa8e92
9	tovar	Abraham Tovar Rivera	da242da5-b4f0-4c1c-b41e-4c692930077b	3f37d7ec4c7e2f51acbe7cae1af8e8fd7a4093d5a8bb4ce89aca24289a424ff40c0f76487a9d92a8a58d12a6581954b12dfe38459c8635088aa53158c334acff
10	daya	Dayana Leticia	14b62f82-79d0-4e1a-8267-5c74d93b1e62	59a9a786db05c8f412d9723cac1e83d17006d8131cab55ccb0578526f7979cfe867304cac05c7cbab52e6485cf6bc54410ebb735ee2e275dee9843c4ba333cbf
11	zdmin	Zdministrador	a7fb3080-d994-465b-821c-721cfd748210	853cf6503618d28f4ce5c31cd05be8a25a3349276d805cbaa81af4af3bce75e0dfdc6657944072b09dc07ff5fede96eea991607cdc162c76f7d3056d81ef4648
17	registro	Registro	b65e9e29-abb7-45b9-a4dc-b49d6a63279f	48fe887efa7051f2db1dc85cf6708cfcca1d1402f1d8b919471813d6d93b60767be31f4fcbd2f1c93d6349888a6446ab6396531b7ec400df7b7f9ecc6fbdd469
16	mesero	Mesero	f7e0b21e-03e0-407d-bca8-17c8a5e73fa7	84b35bc82fb14d288e9676988f4543297a7bbda824b049564a5bb2fb05b5e88cddcdd1b3649281f30091a0a91fcf8433047cfb1e4f7594501d5622c223f50ccc
15	limpieza	Limpieza	232e3b04-23c7-4d82-ae0c-8cedcde8277f	57f3d269710598b69faf7bbda6eed38596eb72570e648e2330153ae9a4330030e2ef213d1dccba6473f6aed4ce781ff615811ea53007ffcada94fdb91caa75a2
14	gerencia	Gerencia	492ef113-784d-4d03-b80b-9c6961026816	d5e5c65fe0740ba2e64427ae2b6b8b950529e3448093bc1f3176edba497789af69f45cb6d1a7483e6e03c73c737c63beeb4ec9c9e109cb31588bada943ca6830
13	cocina	Cocina	1f46db84-6eaf-4e14-a68a-69906a2a2575	493f628ac21ffd82e5d0200fecfb7235a5ac552206147f1bfbe20b4113eb7670760bb75b5e410421bb6b4f5e4475b5df9dacb1b70e1d72d006b35d998716d697
12	bar	Bar	af75e725-5fe4-47f3-9c39-bbc47da93c63	73d032de3272df4da9b5229bc83fc4d97ba9b223972c764d18c2c232210ba91bee9312cafcf2865d77e423f34633c3be5e5c01f06eb731e69e079bf4e3997302
\.


--
-- Name: app_user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tacocat
--

SELECT pg_catalog.setval('app_user_id_seq', 17, true);


--
-- Data for Name: app_user_ip_addr; Type: TABLE DATA; Schema: public; Owner: tacocat
--

COPY app_user_ip_addr (id_app_user, ip_addr, created, expires) FROM stdin;
1	192.168.100.174	2019-08-27 13:35:40.736167	2019-08-27 21:35:40.736167
1	192.168.100.131	2019-08-27 16:43:13.94523	2019-08-28 00:43:13.94523
1	192.168.100.142	2019-08-27 19:37:45.995496	2019-08-28 03:37:45.995496
\.


--
-- Data for Name: app_user_role; Type: TABLE DATA; Schema: public; Owner: tacocat
--

COPY app_user_role (id_app_user, id_role) FROM stdin;
9	4
9	3
9	2
9	7
10	4
10	3
10	5
10	2
10	7
11	1
1	4
1	3
1	6
1	5
1	2
1	7
17	7
16	2
15	5
14	6
13	3
12	4
\.


--
-- Data for Name: bill; Type: TABLE DATA; Schema: public; Owner: tacocat
--

COPY bill (id, date, location) FROM stdin;
1	2019-08-16 22:19:46.748855	B9
2	2019-08-16 22:20:38.343302	Sala
3	2019-08-16 22:23:29.468216	Sala
4	2019-08-16 22:24:08.51868	Sala
5	2019-08-16 22:25:44.675628	Sala
6	2019-08-17 20:58:06.717079	mesa
7	2019-08-17 21:45:15.890993	b6
8	2019-08-17 23:33:12.979434	b6
9	2019-08-17 23:47:34.596066	B8
10	2019-08-18 18:38:23.114726	Test
11	2019-08-18 21:10:25.681539	Sala
12	2019-08-19 14:45:09.661204	Test
13	2019-08-20 21:28:25.226449	Test
15	2019-08-22 10:32:21.324653	Test
14	2019-08-22 10:21:43.536313	Test 1
16	2019-08-22 20:05:05.187189	Sala
17	2019-08-22 21:54:18.201409	Mesa
18	2019-08-23 16:53:45.452494	Mesa
19	2019-08-24 15:35:39.192196	B10
20	2019-08-24 20:13:32.19957	llevar
21	2019-08-24 21:15:33.014366	Test 1
22	2019-08-24 22:46:59.243408	B4
23	2019-08-24 22:52:30.989869	llevar
24	2019-08-25 21:42:31.033989	B5
25	2019-08-25 22:51:12.493171	Fer
26	2019-08-27 19:57:07.100655	Misc
\.


--
-- Name: bill_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tacocat
--

SELECT pg_catalog.setval('bill_id_seq', 26, true);


--
-- Data for Name: bill_item; Type: TABLE DATA; Schema: public; Owner: tacocat
--

COPY bill_item (id, date, id_bill, person, id_item, charge_override) FROM stdin;
1	2019-08-16 22:19:55.728461	1	\N	1	\N
2	2019-08-16 22:20:57.76893	2	\N	13	\N
3	2019-08-16 22:21:12.512588	2	\N	13	\N
4	2019-08-16 22:21:31.039499	2	\N	22	\N
5	2019-08-16 22:21:37.450671	2	\N	22	\N
6	2019-08-16 22:21:43.27117	2	\N	22	\N
7	2019-08-16 22:21:47.851075	2	\N	22	\N
8	2019-08-16 22:21:52.423475	2	\N	22	\N
9	2019-08-16 22:22:04.123974	2	\N	5	\N
10	2019-08-16 22:22:20.015732	2	\N	22	\N
11	2019-08-16 22:22:25.636845	2	\N	22	\N
12	2019-08-16 22:22:30.342775	2	\N	22	\N
13	2019-08-16 22:22:35.130476	2	\N	22	\N
14	2019-08-16 22:22:47.489422	2	\N	3	\N
15	2019-08-16 22:22:55.409766	2	\N	22	\N
16	2019-08-16 22:23:00.188701	2	\N	22	\N
17	2019-08-16 22:23:04.84204	2	\N	22	\N
18	2019-08-16 22:23:40.420966	3	\N	22	\N
19	2019-08-16 22:23:44.66286	3	\N	22	\N
20	2019-08-16 22:23:48.547022	3	\N	22	\N
21	2019-08-16 22:23:51.776493	3	\N	1	\N
22	2019-08-16 22:24:15.947964	4	\N	13	\N
23	2019-08-16 22:24:30.835463	4	\N	13	\N
24	2019-08-16 22:24:47.722849	4	\N	1	\N
25	2019-08-16 22:24:49.810266	4	\N	1	\N
27	2019-08-16 22:25:48.987623	5	\N	1	\N
28	2019-08-16 22:25:51.240948	5	\N	1	\N
29	2019-08-16 22:25:53.724709	5	\N	1	\N
103	2019-08-22 21:25:35.601639	16	\N	15	\N
30	2019-08-17 20:58:59.009502	6	1	4	\N
31	2019-08-17 21:00:43.366613	6	1	8	\N
33	2019-08-17 21:01:12.575342	6	1	11	\N
34	2019-08-17 21:25:01.349683	6	1	11	\N
37	2019-08-17 21:50:29.080423	6	2	2	\N
38	2019-08-17 21:50:46.234321	6	2	2	\N
44	2019-08-17 21:55:13.460886	6	2	12	\N
39	2019-08-17 21:51:26.550299	6	3	11	75.00
40	2019-08-17 21:52:21.842437	6	3	22	\N
45	2019-08-17 21:56:51.698783	6	2	11	\N
41	2019-08-17 21:52:33.802566	6	2	4	\N
46	2019-08-17 22:51:35.577483	7	\N	2	\N
47	2019-08-17 22:52:00.994338	7	\N	2	\N
48	2019-08-17 22:56:06.00204	7	\N	13	\N
49	2019-08-17 22:56:27.944913	6	3	12	\N
104	2019-08-22 21:26:03.867254	16	\N	26	\N
50	2019-08-17 22:56:51.320906	6	3	25	\N
42	2019-08-17 21:52:57.181412	6	2	4	\N
43	2019-08-17 21:54:26.890254	6	2	4	\N
51	2019-08-17 23:33:24.707876	8	\N	1	\N
52	2019-08-17 23:33:37.350014	8	\N	1	\N
53	2019-08-17 23:34:46.087645	8	\N	3	\N
54	2019-08-17 23:47:56.28929	9	\N	1	\N
55	2019-08-17 23:48:05.568471	9	\N	1	\N
56	2019-08-17 23:48:09.535532	9	\N	1	\N
57	2019-08-18 00:04:52.602062	9	\N	1	\N
58	2019-08-18 00:08:06.063605	9	\N	1	\N
59	2019-08-18 00:17:34.702086	9	\N	1	\N
60	2019-08-18 00:18:34.683285	9	\N	20	\N
61	2019-08-18 00:36:39.186257	9	\N	1	\N
62	2019-08-18 00:36:42.598348	9	\N	1	\N
63	2019-08-18 01:23:44.609572	9	\N	1	\N
64	2019-08-18 01:23:47.999325	9	\N	1	\N
65	2019-08-18 01:23:51.05835	9	\N	1	\N
66	2019-08-18 01:24:31.304856	9	\N	13	\N
75	2019-08-18 21:14:48.251094	11	2	21	\N
76	2019-08-18 21:14:56.632079	11	1	1	\N
77	2019-08-18 21:50:22.074578	11	2	3	\N
105	2019-08-22 21:29:18.826279	16	\N	15	\N
106	2019-08-22 21:56:04.71713	17	\N	1	\N
107	2019-08-22 21:56:15.231889	17	\N	1	\N
108	2019-08-22 21:56:28.045294	17	\N	8	\N
109	2019-08-22 22:29:58.089663	17	\N	1	\N
110	2019-08-22 22:54:41.985121	17	\N	1	\N
111	2019-08-22 23:05:45.195496	17	\N	1	\N
112	2019-08-22 23:46:34.500078	17	\N	1	\N
113	2019-08-22 23:46:38.108038	17	\N	1	\N
114	2019-08-23 00:31:21.069109	17	\N	1	\N
115	2019-08-23 00:31:24.3646	17	\N	1	\N
116	2019-08-23 00:51:53.209886	17	\N	1	\N
99	2019-08-22 20:05:53.212284	16	\N	5	\N
100	2019-08-22 20:06:00.708857	16	\N	4	\N
101	2019-08-22 20:07:25.507621	16	\N	11	\N
102	2019-08-22 20:07:41.094289	16	\N	11	\N
157	2019-08-27 19:57:16.287389	26	\N	3	\N
118	2019-08-23 16:56:48.790294	18	\N	22	\N
120	2019-08-23 17:02:05.000673	18	\N	4	\N
119	2019-08-23 16:57:01.228815	18	\N	22	\N
121	2019-08-23 17:44:53.68459	18	\N	21	\N
122	2019-08-23 17:45:04.424126	18	\N	21	\N
123	2019-08-23 18:27:26.762838	18	\N	13	\N
124	2019-08-23 18:27:38.102769	18	\N	13	\N
125	2019-08-23 18:47:04.386994	18	\N	21	\N
126	2019-08-23 19:50:50.951243	18	\N	21	\N
127	2019-08-24 15:36:05.874572	19	\N	2	\N
128	2019-08-24 15:40:16.758402	19	\N	1	\N
129	2019-08-24 16:32:00.429585	19	\N	1	\N
130	2019-08-24 17:06:28.720523	19	\N	1	\N
131	2019-08-24 17:19:21.10492	19	\N	1	\N
132	2019-08-24 18:08:03.569924	19	\N	1	\N
133	2019-08-24 18:25:02.651456	19	\N	8	\N
134	2019-08-24 20:17:27.210434	20	\N	4	\N
135	2019-08-24 20:21:43.286454	20	\N	13	750.00
138	2019-08-24 22:47:06.82237	22	\N	1	\N
139	2019-08-24 22:47:15.187257	22	\N	1	\N
140	2019-08-24 22:48:26.826331	22	\N	5	\N
141	2019-08-24 22:53:03.883346	23	\N	1	\N
142	2019-08-24 23:16:30.946153	22	\N	1	\N
143	2019-08-24 23:16:35.325853	22	\N	1	\N
144	2019-08-24 23:42:47.987742	22	\N	1	\N
145	2019-08-24 23:42:51.710274	22	\N	1	\N
146	2019-08-25 00:01:36.740891	22	\N	1	\N
147	2019-08-25 00:01:40.426344	22	\N	1	\N
148	2019-08-25 21:42:43.283422	24	\N	2	\N
149	2019-08-25 21:44:08.811365	24	\N	1	\N
150	2019-08-25 22:06:26.731802	24	\N	2	\N
151	2019-08-25 22:51:28.783585	25	\N	10	\N
152	2019-08-25 23:03:42.051025	25	\N	3	\N
153	2019-08-26 18:47:08.073371	21	\N	3	\N
137	2019-08-24 21:25:36.688624	21	\N	1	\N
154	2019-08-26 21:29:12.07015	21	\N	33	\N
155	2019-08-26 23:01:31.262266	21	\N	10	\N
156	2019-08-27 01:18:18.737173	21	\N	13	\N
\.


--
-- Name: bill_item_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tacocat
--

SELECT pg_catalog.setval('bill_item_id_seq', 157, true);


--
-- Data for Name: bill_item_option; Type: TABLE DATA; Schema: public; Owner: tacocat
--

COPY bill_item_option (id_bill_item, id_option) FROM stdin;
1	19
2	3
3	3
9	50
22	3
23	3
30	24
30	30
30	33
30	38
30	41
125	76
33	58
34	56
126	76
127	11
128	19
129	19
130	19
131	19
132	19
134	42
37	11
38	11
39	58
40	80
41	42
42	39
42	41
42	43
42	45
43	24
43	28
43	34
43	45
44	59
45	57
45	58
134	20
135	2
46	11
46	14
46	15
46	16
47	11
47	14
47	15
47	16
48	8
49	60
50	85
51	19
52	19
56	19
55	19
54	19
57	19
58	19
59	19
62	19
61	19
63	19
64	19
65	19
66	7
138	19
139	19
76	18
75	75
140	51
141	19
143	19
142	19
145	19
144	19
147	19
146	19
148	11
149	19
150	11
151	54
154	39
155	53
155	114
156	126
100	39
100	40
100	42
100	47
99	50
101	56
102	56
103	64
104	89
105	64
106	18
107	18
109	18
110	18
111	18
113	18
112	18
115	18
114	18
116	18
120	42
120	22
120	25
120	31
118	80
119	79
121	76
122	75
124	1
123	1
\.


--
-- Data for Name: close; Type: TABLE DATA; Schema: public; Owner: tacocat
--

COPY close (id, date, expense_amount, intake_amount, earnings, business_share, partners_share, services_share, partner_take) FROM stdin;
1	2019-08-19 01:10:17.154288	-1704.00	3908.00	2204.00	1102.00	881.60	220.40	440.80
2	2019-08-26 12:11:40.587196	-2258.50	3475.00	1216.50	608.25	486.60	121.65	486.60
\.


--
-- Name: close_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tacocat
--

SELECT pg_catalog.setval('close_id_seq', 2, true);


--
-- Data for Name: expenses; Type: TABLE DATA; Schema: public; Owner: tacocat
--

COPY expenses (id, date, concept, amount, id_close) FROM stdin;
1	2019-08-16 22:29:26.52276	Caja	567.00	1
2	2019-08-16 22:29:49.36402	Transporte	-20.00	1
3	2019-08-16 22:30:04.3008	Aurrerá	-589.00	1
4	2019-08-16 22:30:17.130285	Compras	-202.00	1
5	2019-08-16 22:30:28.064646	Limpieza	-250.00	1
6	2019-08-17 14:57:01.074502	Compras	-448.00	1
7	2019-08-17 22:22:58.908765	agua	-37.00	1
8	2019-08-17 22:23:09.581447	limon	-25.00	1
9	2019-08-17 22:58:46.626167	Fanta	-13.00	1
10	2019-08-18 13:54:55.63305	Compras	-687.00	1
11	2019-08-19 01:10:17.154288	Caja	1102.00	2
12	2019-08-19 19:26:44.507666	Compras	-75.00	2
13	2019-08-20 18:56:38.741591	Compras	-82.00	2
14	2019-08-21 13:18:11.234193	Pago Mezcal Vergel de Guadalupe	-500.00	2
15	2019-08-21 19:27:42.3307	Compras	-87.00	2
16	2019-08-22 17:12:01.667982	Transporte	-80.00	2
17	2019-08-22 17:12:16.533434	Queso Brizuela	-360.00	2
18	2019-08-22 17:12:32.147169	Queso Parmesano	-57.00	2
19	2019-08-22 17:13:17.691306	Aurerá	-393.00	2
20	2019-08-22 17:38:32.763464	Compras	-230.00	2
21	2019-08-23 15:46:26.671104	Compras	-501.50	2
22	2019-08-23 21:24:49.996489	Limpieza	-300.00	2
23	2019-08-24 13:59:58.649	Compras	-429.00	2
24	2019-08-25 16:10:08.594596	Compras	-266.00	2
25	2019-08-26 12:11:40.587196	Caja	608.25	\N
26	2019-08-26 18:12:04.388038	Compras	-130.00	\N
27	2019-08-27 19:10:39.051335	Compras	-150.00	\N
28	2019-08-27 19:16:49.18304	Pago de Mezcal	-1000.00	\N
\.


--
-- Name: expenses_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tacocat
--

SELECT pg_catalog.setval('expenses_id_seq', 28, true);


--
-- Data for Name: intakes; Type: TABLE DATA; Schema: public; Owner: tacocat
--

COPY intakes (id_bill, date, amount, id_close) FROM stdin;
1	2019-08-16 22:20:13.681626	35.00	1
2	2019-08-16 22:23:13.6389	1166.00	1
3	2019-08-16 22:23:57.354688	260.00	1
4	2019-08-16 22:25:27.718272	230.00	1
5	2019-08-16 22:25:58.056227	105.00	1
6	2019-08-17 23:06:27.272072	1140.00	1
7	2019-08-17 23:30:53.572536	225.00	1
8	2019-08-17 23:47:49.028889	76.00	1
9	2019-08-18 01:28:33.869975	545.00	1
11	2019-08-18 22:14:17.15664	126.00	1
10	2019-08-19 14:42:50.612473	0.00	2
12	2019-08-20 20:12:23.60208	0.00	2
13	2019-08-22 10:21:14.197874	0.00	2
15	2019-08-22 11:08:34.819324	0.00	2
16	2019-08-22 21:44:10.103075	580.00	2
17	2019-08-23 00:54:00.1395	400.00	2
14	2019-08-23 16:49:20.947998	0.00	2
18	2019-08-23 20:38:45.183807	770.00	2
19	2019-08-24 19:00:02.04213	275.00	2
20	2019-08-24 20:44:40.238367	900.00	2
23	2019-08-24 22:54:10.041664	35.00	2
22	2019-08-25 00:03:38.055921	380.00	2
24	2019-08-25 22:37:20.73507	135.00	2
\.


--
-- Data for Name: item; Type: TABLE DATA; Schema: public; Owner: tacocat
--

COPY item (id, name, menu_group, charge, in_stock) FROM stdin;
1	Cerveza	Cervezas Nacionales	35.00	t
2	Michelada	Cervezas Nacionales	50.00	t
4	Pizza	Cocina	150.00	t
5	Alitas	Cocina	100.00	t
6	Pasta	Cocina	80.00	t
7	Guacamole	Cocina	50.00	t
8	Papas a la Francesa	Cocina	50.00	t
9	Quesadilla	Cocina	45.00	t
11	Bebida Fría	Bebidas sin Alcohol	25.00	t
12	Refresco	Bebidas sin Alcohol	30.00	t
13	El Dorado	Mezcales	65.00	t
15	Santa Rosa	Mezcales	80.00	t
16	Rancho la Quinta	Mezcales	100.00	t
17	Vergel de Guadalupe	Mezcales	110.00	t
18	Las Colonias	Mezcales	110.00	t
19	Pozo Hondo	Mezcales	120.00	t
20	Chilcuague de la Casa	Mezcales	50.00	t
23	Caliche	Cervezas Artesanales	100.00	t
24	Ruta .925	Cervezas Artesanales	80.00	t
25	6 Hileras	Cervezas Artesanales	105.00	t
26	Escafandra	Cervezas Artesanales	120.00	t
27	Alter Brewing	Cervezas Artesanales	130.00	t
28	Mamba Negra	Cervezas Artesanales	130.00	t
29	Paracaidista	Cervezas Artesanales	130.00	t
30	Dos Aves	Cervezas Artesanales	140.00	t
22	Vopper	Cervezas Artesanales	75.00	f
32	½ Quesadilla	Cocina	22.50	t
21	Tres Calderas	Cervezas Artesanales	85.00	t
31	½ Pizza	Cocina	75.00	t
33	Chilaquiles	Cocina	60.00	t
3	Cigarro	Misc	6.00	t
10	Bebida Caliente	Bebidas sin Alcohol	20.00	t
14	Lobolu	Mezcales	60.00	f
\.


--
-- Name: item_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tacocat
--

SELECT pg_catalog.setval('item_id_seq', 35, true);


--
-- Data for Name: item_option; Type: TABLE DATA; Schema: public; Owner: tacocat
--

COPY item_option (id_item, id_option) FROM stdin;
1	19
1	18
2	17
2	16
2	15
2	14
2	13
2	12
2	11
2	10
5	52
5	51
5	50
5	49
5	48
10	54
11	58
11	57
11	56
12	62
12	61
12	60
12	59
13	9
13	8
13	7
13	6
13	5
13	4
13	3
13	2
13	1
15	68
15	67
15	66
15	65
15	64
15	63
17	71
17	70
17	69
18	73
18	72
21	77
21	76
21	75
21	74
22	80
22	79
22	78
23	83
23	82
23	81
25	85
25	84
26	90
26	89
26	88
26	87
26	86
27	94
27	93
27	92
27	91
30	96
30	95
4	20
6	20
9	20
4	21
6	21
9	21
4	22
6	22
9	22
4	23
6	23
9	23
4	24
6	24
9	24
4	25
6	25
9	25
4	26
6	26
9	26
4	27
6	27
9	27
4	28
6	28
9	28
4	29
6	29
9	29
4	30
6	30
9	30
4	31
6	31
9	31
4	32
6	32
9	32
4	33
6	33
9	33
4	34
6	34
9	34
4	35
6	35
9	35
4	36
6	36
9	36
4	37
6	37
9	37
4	38
6	38
9	38
4	39
6	39
9	39
4	40
6	40
9	40
4	41
6	41
9	41
4	42
6	42
9	42
4	43
6	43
9	43
4	44
6	44
9	44
4	45
6	45
9	45
4	46
6	46
9	46
4	47
6	47
9	47
4	97
4	98
4	99
4	100
6	101
9	102
31	20
31	21
31	22
31	23
31	24
31	25
31	26
31	27
31	28
31	29
31	30
31	31
31	32
31	33
31	34
31	35
31	36
31	37
31	38
31	39
31	40
31	41
31	42
31	43
31	44
31	45
31	46
31	47
31	97
31	98
31	99
31	100
32	20
32	21
32	22
32	23
32	24
32	25
32	26
32	27
32	28
32	29
32	30
32	31
32	32
32	33
32	34
32	35
32	36
32	37
32	38
32	39
32	40
32	41
32	42
32	43
32	44
32	45
32	46
32	47
32	102
5	103
33	38
33	39
33	40
33	41
33	42
33	43
33	44
33	47
33	46
33	45
33	20
33	21
33	22
33	23
33	25
33	24
33	26
33	28
33	29
33	31
33	27
33	30
33	32
33	37
33	35
33	33
33	34
33	36
9	103
10	53
10	104
10	106
10	108
10	110
10	112
10	114
10	116
10	118
10	120
13	122
13	124
13	126
13	128
\.


--
-- Data for Name: menu_groups; Type: TABLE DATA; Schema: public; Owner: tacocat
--

COPY menu_groups (name) FROM stdin;
Cocina
Bebidas sin Alcohol
Mezcales
Cervezas Artesanales
Cervezas Nacionales
Misc
Botella
\.


--
-- Data for Name: option; Type: TABLE DATA; Schema: public; Owner: tacocat
--

COPY option (id, name, option_group, extra_charge, in_stock) FROM stdin;
1	Espadín	El Dorado	0.00	t
2	Arroqueño	El Dorado	10.00	t
3	Pacheco	El Dorado	15.00	t
4	Mexicano	El Dorado	15.00	t
5	Cuixe	El Dorado	45.00	t
6	Madre Cuixe	El Dorado	45.00	t
7	Tobaziche	El Dorado	45.00	t
8	Tobalá	El Dorado	60.00	t
9	Ramirez	El Dorado	75.00	t
12	Sin Sal	Michelada	0.00	t
13	Sin Limón	Michelada	0.00	t
14	Sin Clamato	Michelada	0.00	t
15	Sin Salsas	Michelada	0.00	t
16	Sin Picante	Michelada	0.00	t
17	Sin Escarchar	Michelada	0.00	t
18	Corona	Cerveza	0.00	t
19	Victoria	Cerveza	0.00	t
48	BBQ	Alitas	0.00	t
49	De la Casa	Alitas	0.00	t
50	Brava	Alitas	0.00	t
51	Buffalo	Alitas	0.00	t
52	Ajo y Parmesano	Alitas	0.00	t
53	Té	Bebida Caliente	0.00	t
54	Café	Bebida Caliente	0.00	t
55	Café Descafeinado	Bebida Caliente	0.00	t
56	Limonada	Bebida Fría	0.00	t
57	Al Tiempo	Bebida Fría	0.00	t
58	Naranjada	Bebida Fría	0.00	t
59	Coca Cola	Refresco	0.00	t
60	Fanta	Refresco	0.00	t
61	Squirt	Refresco	0.00	t
62	Manzanita	Refresco	0.00	t
63	Blanco	Santa Rosa	0.00	t
64	Reposado	Santa Rosa	0.00	t
65	Añejo	Santa Rosa	0.00	t
66	Chilcuague	Santa Rosa	0.00	t
67	Anís	Santa Rosa	0.00	t
68	Canela	Santa Rosa	0.00	t
69	Salmiana	Vergel de Guadalupe	0.00	t
70	Manso	Vergel de Guadalupe	0.00	t
71	Tequilero	Vergel de Guadalupe	0.00	t
72	B´ondri	Las Colonias	0.00	t
73	Chantamán	Las Colonias	0.00	t
74	Blonde Ale	Tres Calderas	0.00	t
75	Amber Ale	Tres Calderas	0.00	t
76	Red Ale	Tres Calderas	0.00	t
77	Stout	Tres Calderas	0.00	t
81	British Golden Ale	Caliche	0.00	t
82	German Weißbier	Caliche	0.00	t
83	Belgian Tripel	Caliche	0.00	t
85	Porter	6 Hileras	0.00	t
86	Abismo	Escafandra	0.00	t
87	Madness	Escafandra	0.00	t
88	Dulcinea	Escafandra	0.00	t
89	Nerea	Escafandra	0.00	t
90	Marisol	Escafandra	0.00	t
91	Clock Tower Stout	Alter Brewing	0.00	t
92	Clock Tower Porter	Alter Brewing	0.00	t
93	Abducted	Alter Brewing	0.00	t
94	Crop Circles	Alter Brewing	0.00	t
95	Stout	Dos Aves	0.00	t
96	Triple Belga	Dos Aves	0.00	t
20	Aceitunas Negras	Frutas y Verduras	0.00	t
21	Aceitunas Verdes	Frutas y Verduras	0.00	t
22	Alcaparras	Frutas y Verduras	0.00	t
23	Cebolla Morada	Frutas y Verduras	0.00	t
24	Cebolla Blanca	Frutas y Verduras	0.00	t
25	Champiñones	Frutas y Verduras	0.00	t
26	Chile Poblano	Frutas y Verduras	0.00	t
27	Rajas de Jalapeño	Frutas y Verduras	0.00	t
28	Chile Serrano	Frutas y Verduras	0.00	t
29	Espinacas	Frutas y Verduras	0.00	t
30	Jitomates	Frutas y Verduras	0.00	t
31	Pimiento Morrón	Frutas y Verduras	0.00	t
32	Piña	Frutas y Verduras	0.00	t
38	Chorizo	Carnes	0.00	t
39	Chuleta	Carnes	0.00	t
40	Jamón Serrano	Carnes	0.00	t
41	Jamón Virginia	Carnes	0.00	t
42	Pepperoni	Carnes	0.00	t
43	Pierna	Carnes	0.00	t
44	Salami	Carnes	0.00	t
45	Salchicha	Carnes	0.00	t
46	Salchichón	Carnes	0.00	t
47	Tocino	Carnes	0.00	t
97	Mexicana	Pizza	0.00	t
98	Carnes	Pizza	0.00	t
99	Hawaiiana	Pizza	0.00	t
100	Margherita	Pizza	0.00	t
101	Natural	Pasta	0.00	t
33	Queso Crema	Lácteos	0.00	t
34	Queso Oaxaca	Lácteos	0.00	t
35	Queso Panela	Lácteos	0.00	t
36	Queso Ranchero	Lácteos	0.00	t
37	Huevo	Lácteos	0.00	t
102	Natural	Quesadilla	0.00	t
103	½ y ½	Alitas	0.00	t
10	Corona	Cerveza	0.00	t
11	Victoria	Cerveza	0.00	t
117	Descafeinado	Café	0.00	t
118	Descafeinado	Café	0.00	t
119	Instantaneo	Café	0.00	t
121	Espadín	Botella	0.00	t
84	Lager	6 Hileras	0.00	t
79	Ámbar	Vopper	0.00	f
78	Clara	Vopper	0.00	f
80	Negra	Vopper	0.00	f
104	Hierbabuena	Té	0.00	t
105	Menta|	Té	0.00	t
106	Menta|	Té	0.00	t
107	Verde	Té	0.00	t
108	Verde	Té	0.00	t
109	Negro	Té	0.00	t
110	Negro	Té	0.00	t
111	De Limón	Té	0.00	t
112	De Limón	Té	0.00	t
113	Manzanilla	Té	0.00	t
114	Manzanilla	Té	0.00	t
115	Azahar y Tila	Té	0.00	t
116	Azahar y Tila	Té	0.00	t
120	Instantaneo	Café	-5.00	t
123	Arroqueño	Botella	0.00	t
125	Mexicano	Botella	0.00	t
127	Cuixe	Botella	0.00	t
124	Arroqueño	Botella	685.00	t
122	Espadín	Botella	585.00	t
128	Cuixe	Botella	1035.00	t
126	Mexicano	Botella	735.00	t
\.


--
-- Data for Name: option_groups; Type: TABLE DATA; Schema: public; Owner: tacocat
--

COPY option_groups (name) FROM stdin;
Cerveza
Frutas y Verduras
Lácteos
Carnes
Alitas
Bebida Caliente
Bebida Fría
Refresco
El Dorado
Santa Rosa
Vergel de Guadalupe
Las Colonias
Tres Calderas
Vopper
Caliche
6 Hileras
Escafandra
Alter Brewing
Dos Aves
Michelada
Pizza
Pasta
Quesadilla
Té
Café
Botella
\.


--
-- Name: option_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tacocat
--

SELECT pg_catalog.setval('option_id_seq', 128, true);


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
28	list-users
29	view-other-users
30	list-roles
31	list-items
32	change-stock-status
33	view-app-values
34	add-user
35	change-other-users-password
36	delete-user
37	change-other-users-name
38	view-role
39	delete-roles
40	add-new-item
41	add-new-menu-group
42	view-item
43	set-item-menu-group
44	set-item-charge
45	set-item-name
46	set-option-in-stock
47	add-option-to-item
48	remove-option-from-item
49	create-new-option-group
50	create-new-option
51	view-option
52	set-option-name
53	set-option-group
54	set-option-charge
55	delete-item
56	print-bill
57	view-log
\.


--
-- Name: permission_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tacocat
--

SELECT pg_catalog.setval('permission_id_seq', 57, true);


--
-- Data for Name: role; Type: TABLE DATA; Schema: public; Owner: tacocat
--

COPY role (id, name) FROM stdin;
1	Admin
3	Cocina
4	Bar
5	Limpieza
6	Gerencia
7	Registro
2	Mesero
\.


--
-- Name: role_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tacocat
--

SELECT pg_catalog.setval('role_id_seq', 9, true);


--
-- Data for Name: role_permission; Type: TABLE DATA; Schema: public; Owner: tacocat
--

COPY role_permission (id_role, id_permission) FROM stdin;
1	1
1	2
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
1	3
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
1	55
1	56
4	32
4	20
4	22
4	8
4	23
4	31
4	25
4	46
4	26
4	33
4	21
4	7
4	24
4	6
1	57
\.


--
-- Data for Name: services; Type: TABLE DATA; Schema: public; Owner: tacocat
--

COPY services (id, date, amount, running_total, id_close, concept) FROM stdin;
1	2019-08-16 22:28:01.170969	-2149.00	-2149.00	1	Cierre
2	2019-08-19 01:10:17.154288	220.40	-1928.60	2	Cierre 1
3	2019-08-26 12:11:40.587196	121.65	-1806.95	\N	Cierre 2
\.


--
-- Name: services_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tacocat
--

SELECT pg_catalog.setval('services_id_seq', 3, true);


--
-- Data for Name: t; Type: TABLE DATA; Schema: public; Owner: tacocat
--

COPY t (n) FROM stdin;
1 day
05:00:00
7 days
\.


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
-- PostgreSQL database dump complete
--

