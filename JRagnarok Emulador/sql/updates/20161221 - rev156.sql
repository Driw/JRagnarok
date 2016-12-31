
ALTER TABLE characters_locations DROP FOREIGN KEY fk_char_loc;
ALTER TABLE characters_locations DROP PRIMARY KEY;
ALTER TABLE characters_locations ADD CONSTRAINT pk_char_loc PRIMARY KEY (charid, num);
ALTER TABLE characters_locations ADD CONSTRAINT fk_char_loc FOREIGN KEY (charid) REFERENCES characters(id) ON DELETE RESTRICT ON UPDATE CASCADE;
