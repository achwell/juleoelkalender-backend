ALTER TABLE user_calendar_token
DROP PRIMARY KEY,
  ADD PRIMARY KEY (user_id, calendar_token_id);
