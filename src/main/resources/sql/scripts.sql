use vertx-web;

-- create table
create table user (username varchar(255), password varchar(255), password_salt varchar(255) );
create table user_roles (username varchar(255), role varchar(255));
create table roles_perms (role varchar(255), perm varchar(255));

insert into user values ('tim', 'EC0D6302E35B7E792DF9DA4A5FE0DB3B90FCAB65A6215215771BF96D498A01DA8234769E1CE8269A105E9112F374FDAB2158E7DA58CDC1348A732351C38E12A0', 'C59EB438D1E24CACA2B1A48BC129348589D49303858E493FBE906A9158B7D5DC');
insert into user_roles values ('tim', 'dev');
insert into user_roles values ('tim', 'admin');
insert into roles_perms values ('dev', 'commit_code');
insert into roles_perms values ('dev', 'eat_pizza');
insert into roles_perms values ('admin', 'merge_pr');