drop schema if exists brrs;
create schema brrs;
use brrs;
drop table if exists authorization;
create table authorization (
	id int primary key,
	security_token varchar(32),
	design_file text,
	submit_time timestamp
);
drop table if exists report_run;
create table report_run (
	id int primary key,
	design_file text,
	submit_time timestamp
);
