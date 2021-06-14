DROP TABLE IF EXISTS accounts;

CREATE TABLE accounts (
    id VARCHAR(200) PRIMARY KEY,
	name VARCHAR(200) NOT NULL,
	email VARCHAR(200) NOT NULL
);

INSERT INTO accounts (id, name, email) VALUES
('0004ddc3-e99a-419e-b14b-e91a2138416e', 'Aliese', 'Aliese48905@yahoo.com'),
('0006c188-a161-47f1-a51e-7b890901b5ec', 'Shaguana', 'Shaguana41615@yahoo.com'),
('00083929-9dcb-4b20-98d5-9e2ff0c3c17d', 'Darek', 'Darek38985@gmail.com'),
('000846f0-1043-414e-8ad2-7ad4955c6833', 'Tangee', 'Tangee67363@aim.chat'),
('00087ff5-c8c4-41ce-a35e-bd463285dc3d', 'Naji', 'Naji86174@aim.chat'),
('00010e9e-d948-4726-bcaa-dca94b139552', 'Dennis', 'Dennis33585@aim.chat'),
('00030892-617a-4e7d-8e21-cb1aa3330850', 'Noella', 'Noella102413@gmail.com'),
('00030da3-4521-472d-8849-9a0fa22c42dc', 'Shaundrea', 'Shaundrea96739@yahoo.com'),
('0003b6ba-bce3-4305-8748-932610afbbde', 'Kalysta', 'Kalysta88511@hotmail.com'),
('0004b816-9d35-4900-a8d1-5c6fd9ecc9c6', 'Cintia', 'Cintia42880@yahoo.com');