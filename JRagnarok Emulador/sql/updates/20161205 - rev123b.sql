
ALTER TABLE characters CHANGE status_point status_point SMALLINT NOT NULL DEFAULT 0;
ALTER TABLE characters CHANGE skill_point skill_point SMALLINT NOT NULL DEFAULT 0;

ALTER TABLE characters DROP COLUMN strength;
ALTER TABLE characters DROP COLUMN agility;
ALTER TABLE characters DROP COLUMN vitality;
ALTER TABLE characters DROP COLUMN inteligence;
ALTER TABLE characters DROP COLUMN dexterity;
ALTER TABLE characters DROP COLUMN luck;

ALTER TABLE characters ADD effect_state INT NOT NULL DEFAULT 0 AFTER manner;
ALTER TABLE characters ADD karma INT NOT NULL DEFAULT 0 AFTER effect_state;
ALTER TABLE characters ADD delete_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE characters ADD moves TINYINT NOT NULL DEFAULT 0;
ALTER TABLE characters ADD font TINYINT NOT NULL DEFAULT 0;
ALTER TABLE characters ADD unique_item_counter INT NOT NULL DEFAULT 0;

CREATE TABLE characters_stats
(
	charid INT NOT NULL,
	strength INT NOT NULL DEFAULT 0,
	agility INT NOT NULL DEFAULT 0,
	vitality INT NOT NULL DEFAULT 0,
	intelligence INT NOT NULL DEFAULT 0,
	dexterity INT NOT NULL DEFAULT 0,
	luck INT NOT NULL DEFAULT 0,

	PRIMARY KEY (charid)
);
