drop database if exists phototype;
create database phototype;
use phototype;
drop table if exists authorization;
create table authorization (
	security_token varchar(32) primary key,
	design_file text,
	submit_time timestamp
);
drop table if exists report_run;
create table report_run (
	id int primary key,
	design_file text,
	submit_time timestamp
);
delete from authorization where security_token = 'test-token';
insert into authorization values('test-token', 'test.rptdesign', '2019-06-21 00:00:00')