create user tacocat with encrypted password 'Tacocat2019';

create database tacocat;

grant all privileges on database tacocat to tacocat;

\c tacocat

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

