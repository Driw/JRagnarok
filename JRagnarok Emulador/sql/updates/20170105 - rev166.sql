
ALTER TABLE accounts ADD COLUMN char_slots TINYINT NOT NULL DEFAULT 0 AFTER last_ip;

ALTER TABLE characters_locations DROP FOREIGN KEY fk_char_loc;
ALTER TABLE characters_locations ADD CONSTRAINT fk_char_loc FOREIGN KEY (charid) REFERENCES characters(id) ON DELETE CASCADE ON UPDATE CASCADE;
