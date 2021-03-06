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
insert into authorization values(
	'test-token', 
	'/disk1/home/innovent/projects/BIRT-Report-Runner-REST-Service/sample/test.rptdesign', 
	'2019-06-21 00:00:00');

delete from authorization where security_token = 'test-token2';
insert into authorization values(
	'test-token2', 
	null, 
	'2019-06-21 00:00:00');

update authorization set submit_time = now() where security_token = 'test-token';
update authorization set submit_time = now() where security_token = 'test-token2';