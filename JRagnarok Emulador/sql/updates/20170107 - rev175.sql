
ALTER TABLE accounts ADD COLUMN  sex ENUM('M', 'F', 'S') NOT NULL AFTER password;
ALTER TABLE accounts AUTO_INCREMENT = 2000000;
ALTER TABLE characters AUTO_INCREMENT = 150000;
