DROP TABLE IF EXISTS accounts;

CREATE TABLE accounts (
    id uuid DEFAULT random_uuid() PRIMARY KEY,
	name VARCHAR(200) NOT NULL,
	email VARCHAR(200) NOT NULL
);

INSERT INTO accounts (name, email) VALUES
  ('Tom', 'tom123@email.com'),
  ('Bill', 'bill839@yahoo.com'),
  ('Folrunsho', 'Folrunsho889@gmail.com');