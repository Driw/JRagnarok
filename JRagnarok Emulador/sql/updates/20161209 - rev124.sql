
ALTER TABLE characters ADD rename_count TINYINT(3) NOT NULL DEFAULT 0 AFTER online;
ALTER TABLE characters ADD unban DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER rename_count;

ALTER TABLE characters_look CHANGE hairColor hair_color SMALLINT NOT NULL DEFAULT 0;
ALTER TABLE characters_look CHANGE clothesColor clothes_color SMALLINT NOT NULL DEFAULT 0;
ALTER TABLE characters_look CHANGE headTop head_top SMALLINT NOT NULL DEFAULT 0;
ALTER TABLE characters_look CHANGE headMid head_mid SMALLINT NOT NULL DEFAULT 0;
ALTER TABLE characters_look CHANGE headBottom head_bottom SMALLINT NOT NULL DEFAULT 0;
