-- create table
create table user (username varchar(255), password varchar(255), password_salt varchar(255), first_name varchar(50), last_name varchar(50), address varchar(255) );
create table user_roles (username varchar(255), role varchar(255));
create table roles_perms (role varchar(255), perm varchar(255));

-- admin : password
insert into user values ('admin', '5B844DDCB549E9DF29A7116C38B585FE452A5E7A0352102E984E1B793F44419B6BAC8246EA19F2F3618AE29AF6F015889A18D41BD038707C1017AFCCF70CE263', 'C7AD44CBAD762A5DA0A452F9E854FDC1E0E7A52A38015F23F3EAB1D80B931DD472634DFAC71CD34EBC35D16AB7FB8A90C81F975113D6C7538DC69DD8DE9077EC', 'Super', 'Admin', 'At main office');
-- jhon : password
insert into user values ('jhon', '42F2E57DB605380D4ED651E390B08FCAC9CF29F42E523A07FC3FB0B73DCF3D813C164F6F75B5508DCF121895692DC7BE438CA5860C354073C95EC55B93CFB35D', '74591AF8230F8D40BCC8143DC743B5AB0A76FADD63D2E7BB21D570A265C49DDD6E3FCFF0E7BEC5BF0C676F3C38A76283D437656CA25F29173721D0219CF7A5A8', 'Jhon', 'Doe', '21st Street');
insert into user_roles values ('admin', 'admin');
insert into user_roles values ('jhon', 'user');
insert into roles_perms values ('user', 'manage_profile');
insert into roles_perms values ('admin', 'manage_user');

create table item (id int(10) unsigned not null auto_increment, title varchar(100) default null, description text, primary key (id));