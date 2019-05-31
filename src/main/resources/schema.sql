create database parser;


 create table entries
 (
	 ts timestamp not null,
	 ip varchar(20) not null,
	 method varchar(20) not null,
	 response int not null,
	 user_agent varchar(512)
 );
 
 create table reports
 (
 	query_run_at timestamp not null,
 	query_id varchar(50),
 	ip varchar(20) not null,
 	req_count int not null,
 	hourly boolean,
 	comments varchar(250)
 );
 
 CREATE INDEX reports_idx ON reports (query_id(10));