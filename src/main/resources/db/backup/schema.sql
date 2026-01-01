create table role
(
  id   uuid         not null primary key,
  name varchar(255) not null
);
create table authority
(
  id   uuid         not null primary key,
  name varchar(255) not null
);
create table roles_authorities
(
  role_id      uuid not null
    constraint roles_authorities_role_fkey references role,
  authority_id uuid not null
    constraint roles_authorities_authority_fkey references authority,
  primary key (role_id, authority_id)
);

create table calendar_token
(
  id         uuid                    not null primary key,
  active     boolean                 not null,
  name       varchar(255)            not null,
  token      varchar(255)            not null,
  created_at timestamp default now() not null,
  updated_at timestamp default now() not null
);
create table user
(
  id              uuid                    not null primary key,
  area            varchar(255),
  email           varchar(255)            not null
    constraint w882cca5xovd4tx3i2d5qus8fsb unique,
  locked          boolean                 not null,
  name            varchar(255)            not null,
  password        varchar(255)            not null,
  role_id         uuid                    not null
    constraint fk_user_role references role,
  last_login_date timestamp,
  created_at      timestamp default now() not null,
  updated_at      timestamp default now() not null
);
create table user_calendar_token
(
  user_id           uuid not null
    constraint user_calendar_token_user_fkey references user,
  calendar_token_id uuid not null
    constraint user_calendar_token_calendar_token_fkey references calendar_token,
  primary key (user_id, calendar_token_id)
);
create table beer
(
  id          uuid                    not null primary key,
  abv         double precision,
  archived    boolean                 not null,
  bottle_date timestamp,
  brewed_date timestamp,
  description varchar(255),
  ebc         double precision,
  ibu         double precision,
  name        varchar(255)            not null,
  recipe      varchar(255),
  style       varchar(255)            not null,
  untapped    varchar(255),
  user_id     uuid                    not null
    constraint fkcsif2ht5qw3vy56f8jc8v49c6 references user,
  created_at  timestamp default now() not null,
  updated_at  timestamp default now() not null
);
create table calendar
(
  id                uuid                    not null primary key,
  archived          boolean                 not null,
  published         boolean                 not null,
  name              varchar(255)            not null,
  calendar_year     integer                 not null,
  calendar_token_id uuid                    not null
    constraint fk6dtaax4vvinpg3vn36femw613 references calendar_token,
  created_at        timestamp default now() not null,
  updated_at        timestamp default now() not null,
  constraint ukdbr14kfycqt5egoikmwiwk267 unique (calendar_year, name)
);
create table beer_calendar
(
  id           uuid    not null primary key,
  calendar_day integer not null,
  beer_id      uuid    not null
    constraint fk8a6b1koq3ft962d0jnua2n594 references beer,
  calendar_id  uuid    not null
    constraint fkc332me2huhr2hdsnhcio7gx2t references calendar
);
create table review
(
  id             uuid                    not null primary key,
  comment        varchar(255),
  created_at     timestamp               not null,
  rating_feel    double precision        not null,
  rating_label   double precision        not null,
  rating_looks   double precision        not null,
  rating_overall double precision        not null,
  rating_smell   double precision        not null,
  rating_taste   double precision        not null,
  beer_id        uuid                    not null
    constraint fkbwjo2uqvc7kcwiktemroulw61 references beer,
  calendar_id    uuid                    not null
    constraint fkom0u914uxnfvt5xt1plhc6ta5 references calendar,
  reviewer_id    uuid                    not null
    constraint fkdksd7fbjmi2jpt8dsg27etnh2 references user,
  updated_at     timestamp default now() not null,
  constraint ukiwh5gdebyk111fw7ckp3nxo5x unique (beer_id, calendar_id, reviewer_id)
);

create table beer_style
(
  id         uuid         not null primary key,
  beer_style varchar(255) not null
);
create table device
(
  id              uuid                    not null primary key,
  mobile_vendor   varchar(255),
  mobile_model    varchar(255),
  os_name         varchar(255),
  os_version      varchar(255),
  browser_name    varchar(255),
  browser_version varchar(255),
  user_id         uuid                    not null
    constraint fk_device_user references user,
  created_at      timestamp default now() not null,
  updated_at      timestamp default now() not null
);
create table password_change_request
(
  id      uuid                    not null primary key,
  created timestamp               not null,
  email   varchar(255)            not null,
  token   varchar(255)            not null,
  updated timestamp default now() not null
);
create table flyway_schema_history
(
  installed_rank integer                 not null
    constraint flyway_schema_history_pk primary key,
  version        varchar(50),
  description    varchar(200)            not null,
  type           varchar(20)             not null,
  script         varchar(1000)           not null,
  checksum       integer,
  installed_by   varchar(100)            not null,
  installed_on   timestamp default now() not null,
  execution_time integer                 not null,
  success        boolean                 not null
);

alter table role
  owner to juleoel;
alter table authority
  owner to juleoel;
alter table roles_authorities
  owner to juleoel;
alter table calendar_token
  owner to juleoel;
alter table user
  owner to juleoel;
alter table user_calendar_token
  owner to juleoel;
alter table beer
  owner to juleoel;
alter table calendar
  owner to juleoel;
alter table beer_calendar
  owner to juleoel;
alter table review
  owner to juleoel;
alter table beer_style
  owner to juleoel;
alter table device
  owner to juleoel;
alter table password_change_request
  owner to juleoel;
alter table flyway_schema_history
  owner to juleoel;

create index flyway_schema_history_s_idx on flyway_schema_history (success);
