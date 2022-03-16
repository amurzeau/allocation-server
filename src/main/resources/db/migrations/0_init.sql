create sequence hibernate_sequence start 1 increment 1;

create table ActivityType (
    id varchar(255) not null,
    isDisabled boolean,
    name varchar(255),
    primary key (id)
);

create table AllocationReply (
    id int8 not null,
    duration numeric(19, 2),
    activityType_id varchar(255),
    project_id int8,
    primary key (id)
);

create table ApplicationType (
    id varchar(255) not null,
    isDisabled boolean,
    name varchar(255),
    primary key (id)
);

create table Eotp (
    id varchar(255) not null,
    isDisabled boolean,
    name varchar(255),
    primary key (id)
);

create table Project_Eotp_eotpClosed (
    ProjectReply_id int8 not null,
    eotpClosed_id varchar(255) not null,
    primary key (ProjectReply_id, eotpClosed_id)
);

create table Project_Eotp_eotpOpen (
    ProjectReply_id int8 not null,
    eotpOpen_id varchar(255) not null,
    primary key (ProjectReply_id, eotpOpen_id)
);

create table ProjectReply (
    id int8 not null,
    arch varchar(255),
    board varchar(255),
    component varchar(255),
    name varchar(255),
    type_id varchar(255),
    primary key (id)
);

create table SchemaVersion (
    id int8 not null,
    description varchar(255),
    installed_on date,
    script varchar(255),
    success boolean not null,
    version int8 not null,
    primary key (id)
);

create table "user" (
    id int8 not null,
    isDeleted boolean not null,
    name varchar(255),
    password varchar(255),
    roles varchar(255),
    primary key (id)
);

alter table if exists AllocationReply 
    add constraint FKm276evaa4gy2ao0ixka9cmajx 
    foreign key (activityType_id) 
    references ActivityType;

alter table if exists AllocationReply 
    add constraint FKj4y26vikgmtuix5hfqvau51e4 
    foreign key (project_id) 
    references ProjectReply;

alter table if exists Project_Eotp_eotpClosed 
    add constraint FKopv8vxi6o4i1xookw9sjujgne 
    foreign key (eotpClosed_id) 
    references Eotp;

alter table if exists Project_Eotp_eotpClosed 
    add constraint FKnj0ewflx5rqbdnwxodlmdwjqj 
    foreign key (ProjectReply_id) 
    references ProjectReply;

alter table if exists Project_Eotp_eotpOpen 
    add constraint FK58okg0m3f1rgynj1k5kwlnxqx 
    foreign key (eotpOpen_id) 
    references Eotp;

alter table if exists Project_Eotp_eotpOpen 
    add constraint FKiilc1h1auku5i6i1o8wlqrfkk 
    foreign key (ProjectReply_id) 
    references ProjectReply;

alter table if exists ProjectReply 
    add constraint FK5395ricxsmqlummn10qqp0mbp 
    foreign key (type_id) 
    references ApplicationType;
