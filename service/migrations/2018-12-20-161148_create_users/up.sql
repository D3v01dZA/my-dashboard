CREATE TABLE users
(
  id       SERIAL PRIMARY KEY,
  name     VARCHAR NOT NULL,
  password VARCHAR NOT NULL,
  salt_one VARCHAR NOT NULL,
  salt_two VARCHAR NOT NULL
);

ALTER TABLE accounts
  ADD COLUMN user_id INTEGER NOT NULL;

ALTER TABLE accounts
  ADD CONSTRAINT accounts_fk
    FOREIGN KEY (user_id)
      REFERENCES users (id);