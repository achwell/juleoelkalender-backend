CREATE TABLE authority (
  id uuid NOT NULL PRIMARY KEY,
  name varchar(255) NOT NULL
);

INSERT INTO authority VALUES ('ca6cd190-5a0c-11ef-aeab-caf0d1bef569', 'user:create');
INSERT INTO authority VALUES ('ca6cdfbe-5a0c-11ef-aeab-caf0d1bef569', 'user:read');
INSERT INTO authority VALUES ('ca6ceb3a-5a0c-11ef-aeab-caf0d1bef569', 'user:update');
INSERT INTO authority VALUES ('ca6d9af8-5a0c-11ef-aeab-caf0d1bef569', 'user:delete');
INSERT INTO authority VALUES ('ca6daeb2-5a0c-11ef-aeab-caf0d1bef569', 'user:seelogintime');
INSERT INTO authority VALUES ('ca6db9a2-5a0c-11ef-aeab-caf0d1bef569', 'user:update_other');
INSERT INTO authority VALUES ('ca6dc0be-5a0c-11ef-aeab-caf0d1bef569', 'beer:create');
INSERT INTO authority VALUES ('ca6dc5fa-5a0c-11ef-aeab-caf0d1bef569', 'beer:read');
INSERT INTO authority VALUES ('ca6dcbb8-5a0c-11ef-aeab-caf0d1bef569', 'beer:update');
INSERT INTO authority VALUES ('ca6dd23e-5a0c-11ef-aeab-caf0d1bef569', 'beer:delete');
INSERT INTO authority VALUES ('ca6dd81a-5a0c-11ef-aeab-caf0d1bef569', 'beer:delete_other');
INSERT INTO authority VALUES ('ca6ddd4c-5a0c-11ef-aeab-caf0d1bef569', 'beer:update_other');
INSERT INTO authority VALUES ('ca6de422-5a0c-11ef-aeab-caf0d1bef569', 'calendar:create');
INSERT INTO authority VALUES ('ca6df232-5a0c-11ef-aeab-caf0d1bef569', 'calendar:read');
INSERT INTO authority VALUES ('ca6df7b4-5a0c-11ef-aeab-caf0d1bef569', 'calendar:update');
INSERT INTO authority VALUES ('ca6dfe3a-5a0c-11ef-aeab-caf0d1bef569', 'calendar:delete');
INSERT INTO authority VALUES ('ca6e04b6-5a0c-11ef-aeab-caf0d1bef569', 'review:create');
INSERT INTO authority VALUES ('ca6e0f60-5a0c-11ef-aeab-caf0d1bef569', 'review:read');
INSERT INTO authority VALUES ('ca6e6244-5a0c-11ef-aeab-caf0d1bef569', 'review:update');
INSERT INTO authority VALUES ('ca6e6a8c-5a0c-11ef-aeab-caf0d1bef569', 'review:delete');
INSERT INTO authority VALUES ('ca6e7176-5a0c-11ef-aeab-caf0d1bef569', 'review:delete_other');
INSERT INTO authority VALUES ('ca6e78a6-5a0c-11ef-aeab-caf0d1bef569', 'review:update_other');
INSERT INTO authority VALUES ('ca6e7f5e-5a0c-11ef-aeab-caf0d1bef569', 'calendartoken:create');
INSERT INTO authority VALUES ('ca6e8a26-5a0c-11ef-aeab-caf0d1bef569', 'calendartoken:read');
INSERT INTO authority VALUES ('ca6e9610-5a0c-11ef-aeab-caf0d1bef569', 'calendartoken:update');
INSERT INTO authority VALUES ('ca6e9d7c-5a0c-11ef-aeab-caf0d1bef569', 'calendartoken:delete');
INSERT INTO authority VALUES ('ca7365e6-5a0c-11ef-aeab-caf0d1bef569', 'beercalendar:create');
INSERT INTO authority VALUES ('ca737004-5a0c-11ef-aeab-caf0d1bef569', 'beercalendar:read');
INSERT INTO authority VALUES ('ca73766c-5a0c-11ef-aeab-caf0d1bef569', 'beercalendar:update');
INSERT INTO authority VALUES ('ca737dc4-5a0c-11ef-aeab-caf0d1bef569', 'beercalendar:delete');
INSERT INTO authority VALUES ('cac0ef28-5a0c-11ef-aeab-caf0d1bef569', 'dashboard');
INSERT INTO authority VALUES ('cac33e86-5a0c-11ef-aeab-caf0d1bef569', 'dashboard');
INSERT INTO authority VALUES ('cb2920d4-5a0c-11ef-aeab-caf0d1bef569', 'review:total');
INSERT INTO authority VALUES ('cb2b46f2-5a0c-11ef-aeab-caf0d1bef569', 'beerstyle:create');
INSERT INTO authority VALUES ('cb2b56c4-5a0c-11ef-aeab-caf0d1bef569', 'beerstyle:update');
INSERT INTO authority VALUES ('cb2b5e3a-5a0c-11ef-aeab-caf0d1bef569', 'beerstyle:delete');

CREATE TABLE calendar_token (
  id uuid NOT NULL PRIMARY KEY,
  active boolean NOT NULL,
  name varchar(255) NOT NULL,
  token varchar(255) NOT NULL,
  created_at timestamp(6) NOT NULL DEFAULT current_timestamp(6),
  updated_at timestamp(6) NOT NULL DEFAULT current_timestamp(6)
);

CREATE TABLE  role (
  id uuid         NOT NULL PRIMARY KEY,
  name varchar(255) NOT NULL
);

INSERT INTO role VALUES ('d05b35e4-b265-4342-b6be-b0edb0cb6f73', 'ROLE_USER');
INSERT INTO role VALUES ('60d7075c-72a9-4015-9ebe-a881daa63ac4', 'ROLE_ADMIN');
INSERT INTO role VALUES ('8c7ac2e6-489c-4d00-b2bb-d37592de3746', 'ROLE_MASTER');

CREATE TABLE roles_authorities (
  role_id uuid NOT NULL,
  authority_id uuid NOT NULL,
  PRIMARY KEY (role_id,authority_id),
  KEY roles_authorities_role_fkey (role_id),
  KEY roles_authorities_authority_fkey (authority_id),
  CONSTRAINT roles_authorities_authority_fkey FOREIGN KEY (authority_id) REFERENCES authority (id),
  CONSTRAINT roles_authorities_role_fkey FOREIGN KEY (role_id) REFERENCES role (id)
);

INSERT INTO roles_authorities VALUES ('d05b35e4-b265-4342-b6be-b0edb0cb6f73', 'ca6cdfbe-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('d05b35e4-b265-4342-b6be-b0edb0cb6f73', 'ca6ceb3a-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('d05b35e4-b265-4342-b6be-b0edb0cb6f73', 'ca6dc0be-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('d05b35e4-b265-4342-b6be-b0edb0cb6f73', 'ca6dc5fa-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('d05b35e4-b265-4342-b6be-b0edb0cb6f73', 'ca6dcbb8-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('d05b35e4-b265-4342-b6be-b0edb0cb6f73', 'ca6dd23e-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('d05b35e4-b265-4342-b6be-b0edb0cb6f73', 'ca6df232-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('d05b35e4-b265-4342-b6be-b0edb0cb6f73', 'ca6e04b6-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('d05b35e4-b265-4342-b6be-b0edb0cb6f73', 'ca6e0f60-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('d05b35e4-b265-4342-b6be-b0edb0cb6f73', 'ca6e6244-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('d05b35e4-b265-4342-b6be-b0edb0cb6f73', 'ca6e6a8c-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('d05b35e4-b265-4342-b6be-b0edb0cb6f73', 'ca6e8a26-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('d05b35e4-b265-4342-b6be-b0edb0cb6f73', 'ca7365e6-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('60d7075c-72a9-4015-9ebe-a881daa63ac4', 'ca6cd190-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('60d7075c-72a9-4015-9ebe-a881daa63ac4', 'ca6cdfbe-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('60d7075c-72a9-4015-9ebe-a881daa63ac4', 'ca6ceb3a-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('60d7075c-72a9-4015-9ebe-a881daa63ac4', 'ca6d9af8-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('60d7075c-72a9-4015-9ebe-a881daa63ac4', 'ca6db9a2-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('60d7075c-72a9-4015-9ebe-a881daa63ac4', 'ca6dc0be-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('60d7075c-72a9-4015-9ebe-a881daa63ac4', 'ca6dc5fa-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('60d7075c-72a9-4015-9ebe-a881daa63ac4', 'ca6dcbb8-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('60d7075c-72a9-4015-9ebe-a881daa63ac4', 'ca6dd23e-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('60d7075c-72a9-4015-9ebe-a881daa63ac4', 'ca6dd81a-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('60d7075c-72a9-4015-9ebe-a881daa63ac4', 'ca6ddd4c-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('60d7075c-72a9-4015-9ebe-a881daa63ac4', 'ca6de422-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('60d7075c-72a9-4015-9ebe-a881daa63ac4', 'ca6df232-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('60d7075c-72a9-4015-9ebe-a881daa63ac4', 'ca6df7b4-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('60d7075c-72a9-4015-9ebe-a881daa63ac4', 'ca6dfe3a-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('60d7075c-72a9-4015-9ebe-a881daa63ac4', 'ca6e04b6-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('60d7075c-72a9-4015-9ebe-a881daa63ac4', 'ca6e0f60-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('60d7075c-72a9-4015-9ebe-a881daa63ac4', 'ca6e6244-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('60d7075c-72a9-4015-9ebe-a881daa63ac4', 'ca6e6a8c-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('60d7075c-72a9-4015-9ebe-a881daa63ac4', 'ca6e7176-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('60d7075c-72a9-4015-9ebe-a881daa63ac4', 'ca6e78a6-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('60d7075c-72a9-4015-9ebe-a881daa63ac4', 'ca6e8a26-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('60d7075c-72a9-4015-9ebe-a881daa63ac4', 'ca7365e6-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('60d7075c-72a9-4015-9ebe-a881daa63ac4', 'ca737004-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('60d7075c-72a9-4015-9ebe-a881daa63ac4', 'ca73766c-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('60d7075c-72a9-4015-9ebe-a881daa63ac4', 'ca737dc4-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('60d7075c-72a9-4015-9ebe-a881daa63ac4', 'cac0ef28-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('60d7075c-72a9-4015-9ebe-a881daa63ac4', 'cac33e86-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('60d7075c-72a9-4015-9ebe-a881daa63ac4', 'cb2920d4-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('60d7075c-72a9-4015-9ebe-a881daa63ac4', 'cb2b46f2-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('60d7075c-72a9-4015-9ebe-a881daa63ac4', 'cb2b56c4-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('60d7075c-72a9-4015-9ebe-a881daa63ac4', 'cb2b5e3a-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('8c7ac2e6-489c-4d00-b2bb-d37592de3746', 'ca6cd190-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('8c7ac2e6-489c-4d00-b2bb-d37592de3746', 'ca6cdfbe-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('8c7ac2e6-489c-4d00-b2bb-d37592de3746', 'ca6ceb3a-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('8c7ac2e6-489c-4d00-b2bb-d37592de3746', 'ca6d9af8-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('8c7ac2e6-489c-4d00-b2bb-d37592de3746', 'ca6daeb2-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('8c7ac2e6-489c-4d00-b2bb-d37592de3746', 'ca6db9a2-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('8c7ac2e6-489c-4d00-b2bb-d37592de3746', 'ca6dc0be-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('8c7ac2e6-489c-4d00-b2bb-d37592de3746', 'ca6dc5fa-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('8c7ac2e6-489c-4d00-b2bb-d37592de3746', 'ca6dcbb8-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('8c7ac2e6-489c-4d00-b2bb-d37592de3746', 'ca6dd23e-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('8c7ac2e6-489c-4d00-b2bb-d37592de3746', 'ca6dd81a-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('8c7ac2e6-489c-4d00-b2bb-d37592de3746', 'ca6ddd4c-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('8c7ac2e6-489c-4d00-b2bb-d37592de3746', 'ca6de422-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('8c7ac2e6-489c-4d00-b2bb-d37592de3746', 'ca6df232-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('8c7ac2e6-489c-4d00-b2bb-d37592de3746', 'ca6df7b4-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('8c7ac2e6-489c-4d00-b2bb-d37592de3746', 'ca6dfe3a-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('8c7ac2e6-489c-4d00-b2bb-d37592de3746', 'ca6e04b6-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('8c7ac2e6-489c-4d00-b2bb-d37592de3746', 'ca6e0f60-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('8c7ac2e6-489c-4d00-b2bb-d37592de3746', 'ca6e6244-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('8c7ac2e6-489c-4d00-b2bb-d37592de3746', 'ca6e6a8c-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('8c7ac2e6-489c-4d00-b2bb-d37592de3746', 'ca6e7176-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('8c7ac2e6-489c-4d00-b2bb-d37592de3746', 'ca6e78a6-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('8c7ac2e6-489c-4d00-b2bb-d37592de3746', 'ca6e7f5e-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('8c7ac2e6-489c-4d00-b2bb-d37592de3746', 'ca6e8a26-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('8c7ac2e6-489c-4d00-b2bb-d37592de3746', 'ca6e9610-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('8c7ac2e6-489c-4d00-b2bb-d37592de3746', 'ca6e9d7c-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('8c7ac2e6-489c-4d00-b2bb-d37592de3746', 'ca7365e6-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('8c7ac2e6-489c-4d00-b2bb-d37592de3746', 'ca737004-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('8c7ac2e6-489c-4d00-b2bb-d37592de3746', 'ca73766c-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('8c7ac2e6-489c-4d00-b2bb-d37592de3746', 'ca737dc4-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('8c7ac2e6-489c-4d00-b2bb-d37592de3746', 'cac0ef28-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('8c7ac2e6-489c-4d00-b2bb-d37592de3746', 'cb2920d4-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('8c7ac2e6-489c-4d00-b2bb-d37592de3746', 'cb2b46f2-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('8c7ac2e6-489c-4d00-b2bb-d37592de3746', 'cb2b56c4-5a0c-11ef-aeab-caf0d1bef569');
INSERT INTO roles_authorities VALUES ('8c7ac2e6-489c-4d00-b2bb-d37592de3746', 'cb2b5e3a-5a0c-11ef-aeab-caf0d1bef569');

CREATE TABLE user (
  id uuid NOT NULL PRIMARY KEY,
  area varchar(255) DEFAULT NULL,
  email varchar(255) NOT NULL,
  locked boolean NOT NULL,
  last_name varchar(255) NOT NULL,
  password varchar(255) NOT NULL,
  role_id uuid NOT NULL,
  last_login_date timestamp NULL DEFAULT NULL,
  created_at timestamp(6) NOT NULL DEFAULT current_timestamp(6),
  updated_at timestamp(6) NOT NULL DEFAULT current_timestamp(6),
  first_name varchar(255) NOT NULL DEFAULT ' ',
  middle_name varchar(255) DEFAULT NULL,
  facebook_user_id varchar(255) DEFAULT NULL,
  image_url varchar(255) DEFAULT NULL,
  image_height int(11) DEFAULT NULL,
  image_width int(11) DEFAULT NULL,
  image_silhouette boolean DEFAULT NULL,
  UNIQUE KEY w882cca5xovd4tx3i2d5qus8fsb (email),
  KEY fk_user_role (role_id),
  CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES role (id)
);

CREATE TABLE beer (
  id uuid NOT NULL PRIMARY KEY,
  abv double DEFAULT NULL,
  archived boolean NOT NULL,
  bottle_date timestamp(6) NULL DEFAULT NULL,
  brewed_date timestamp(6) NULL DEFAULT NULL,
  description varchar(255) DEFAULT NULL,
  ebc double DEFAULT NULL,
  ibu double DEFAULT NULL,
  name varchar(255) NOT NULL,
  recipe varchar(255) DEFAULT NULL,
  style varchar(255) NOT NULL,
  untapped varchar(255) DEFAULT NULL,
  user_id uuid NOT NULL,
  created_at timestamp(6) NOT NULL DEFAULT current_timestamp(6),
  updated_at timestamp(6) NOT NULL DEFAULT current_timestamp(6),
  desired_date int(11) DEFAULT NULL,
  KEY fkcsif2ht5qw3vy56f8jc8v49c6 (user_id),
  CONSTRAINT fkcsif2ht5qw3vy56f8jc8v49c6 FOREIGN KEY (user_id) REFERENCES user (id)
);

CREATE TABLE beer_style (
  id uuid NOT NULL PRIMARY KEY,
  name varchar(255) NOT NULL
);


CREATE TABLE user_calendar_token (
  user_id uuid NOT NULL PRIMARY KEY,
  calendar_token_id uuid NOT NULL,
  KEY user_calendar_token_user_fkey (user_id),
  KEY user_calendar_token_calendar_token_fkey (calendar_token_id),
  CONSTRAINT user_calendar_token_calendar_token_fkey FOREIGN KEY (calendar_token_id) REFERENCES calendar_token (id),
  CONSTRAINT user_calendar_token_user_fkey FOREIGN KEY (user_id) REFERENCES user (id)
);

CREATE TABLE calendar (
  id uuid NOT NULL PRIMARY KEY,
  archived boolean NOT NULL,
  published boolean NOT NULL,
  name varchar(255) NOT NULL,
  calendar_year int(11) NOT NULL,
  calendar_token_id uuid NOT NULL,
  created_at timestamp(6) NOT NULL DEFAULT current_timestamp(6),
  updated_at timestamp(6) NOT NULL DEFAULT current_timestamp(6),
  UNIQUE KEY ukdbr14kfycqt5egoikmwiwk267 (calendar_year,name),
  KEY fk6dtaax4vvinpg3vn36femw613 (calendar_token_id),
  CONSTRAINT fk6dtaax4vvinpg3vn36femw613 FOREIGN KEY (calendar_token_id) REFERENCES calendar_token (id)
);

CREATE TABLE beer_calendar (
  id uuid NOT NULL PRIMARY KEY,
  calendar_day int(11) NOT NULL,
  beer_id uuid NOT NULL,
  calendar_id uuid NOT NULL,
  KEY fkc332me2huhr2hdsnhcio7gx2t (calendar_id),
  CONSTRAINT fkc332me2huhr2hdsnhcio7gx2t FOREIGN KEY (calendar_id) REFERENCES calendar (id)
);

CREATE TABLE device (
  id uuid NOT NULL PRIMARY KEY,
  mobile_vendor varchar(255) DEFAULT NULL,
  mobile_model varchar(255) DEFAULT NULL,
  os_name varchar(255) DEFAULT NULL,
  os_version varchar(255) DEFAULT NULL,
  browser_name varchar(255) DEFAULT NULL,
  browser_version varchar(255) DEFAULT NULL,
  user_id uuid NOT NULL,
  created_at timestamp(6) NOT NULL DEFAULT current_timestamp(6),
  updated_at timestamp(6) NOT NULL DEFAULT current_timestamp(6),
  is_mobile boolean NOT NULL DEFAULT 0,
  KEY fk_device_user (user_id),
  CONSTRAINT fk_device_user FOREIGN KEY (user_id) REFERENCES user (id)
);

CREATE TABLE password_change_request (
  id uuid NOT NULL PRIMARY KEY,
  created timestamp(6) NOT NULL,
  email varchar(255) NOT NULL,
  token varchar(255) NOT NULL,
  updated timestamp(6) NOT NULL DEFAULT current_timestamp(6)
);

CREATE TABLE review (
  id uuid NOT NULL PRIMARY KEY,
  comment varchar(255) DEFAULT NULL,
  created_at timestamp(6) NOT NULL,
  rating_feel double NOT NULL,
  rating_label double NOT NULL,
  rating_looks double NOT NULL,
  rating_overall double NOT NULL,
  rating_smell double NOT NULL,
  rating_taste double NOT NULL,
  beer_id uuid NOT NULL,
  calendar_id uuid NOT NULL,
  reviewer_id uuid NOT NULL,
  updated_at timestamp(6) NOT NULL DEFAULT current_timestamp(6),
  UNIQUE KEY ukiwh5gdebyk111fw7ckp3nxo5x (beer_id,calendar_id,reviewer_id),
  KEY fkbwjo2uqvc7kcwiktemroulw61 (beer_id),
  KEY fkdksd7fbjmi2jpt8dsg27etnh2 (reviewer_id),
  KEY fkom0u914uxnfvt5xt1plhc6ta5 (calendar_id),
  CONSTRAINT fkbwjo2uqvc7kcwiktemroulw61 FOREIGN KEY (beer_id) REFERENCES beer (id),
  CONSTRAINT fkdksd7fbjmi2jpt8dsg27etnh2 FOREIGN KEY (reviewer_id) REFERENCES user (id),
  CONSTRAINT fkom0u914uxnfvt5xt1plhc6ta5 FOREIGN KEY (calendar_id) REFERENCES calendar (id)
);
