
CREATE TABLE characters
(
	id INT NOT NULL AUTO_INCREMENT,
	name VARCHAR(24) NOT NULL,
	sex ENUM('M', 'F') NOT NULL,
	zeny INT NOT NULL DEFAULT 0,
	status_point INT NOT NULL DEFAULT 0,
	skill_point INT NOT NULL DEFAULT 0,
	jobid SMALLINT NOT NULL DEFAULT 0,
	manner SMALLINT NOT NULL DEFAULT 0,
	base_level INT NOT NULL DEFAULT 0,
	job_level INT NOT NULL DEFAULT 0,
	strength INT NOT NULL DEFAULT 0,
	agility INT NOT NULL DEFAULT 0,
	vitality INT NOT NULL DEFAULT 0,
	inteligence INT NOT NULL DEFAULT 0,
	dexterity INT NOT NULL DEFAULT 0,
	luck INT NOT NULL DEFAULT 0,
	online TINYINT(1) NOT NULL DEFAULT 0,

	UNIQUE (name),
	PRIMARY KEY (id)

) ENGINE=MyISAM AUTO_INCREMENT=1;

CREATE TABLE accounts_char_list
(
	account INT NOT NULL,
	slot INT NOT NULL,
	charid INT NOT NULL,

	PRIMARY KEY (account, slot),
	FOREIGN KEY (account) REFERENCES accounts(id),
	FOREIGN KEY (charid) REFERENCES characters(id)

) ENGINE=MyISAM;

CREATE TABLE characters_look
(
	charid INT NOT NULL,
	hair SMALLINT NOT NULL DEFAULT 0,
	hairColor SMALLINT NOT NULL DEFAULT 0,
	clothesColor SMALLINT NOT NULL DEFAULT 0,
	body SMALLINT NOT NULL DEFAULT 0,
	weapon SMALLINT NOT NULL DEFAULT 0,
	shield SMALLINT NOT NULL DEFAULT 0,
	headTop SMALLINT NOT NULL DEFAULT 0,
	headMid SMALLINT NOT NULL DEFAULT 0,
	headBottom SMALLINT NOT NULL DEFAULT 0,
	robe SMALLINT NOT NULL DEFAULT 0,

	PRIMARY KEY (charid)
);

CREATE TABLE characters_family
(
	charid INT NOT NULL,
	partner INT NOT NULL,
	father INT,
	mother INT,
	child INT,

	PRIMARY KEY (charid)
);

CREATE TABLE characters_experience
(
	charid INT NOT NULL,
	base INT NOT NULL,
	job INT NOT NULL,
	fame INT NOT NULL,

	PRIMARY KEY (charid)
);

CREATE TABLE characters_mercenary_rank
(
	charid INT NOT NULL,
	archer_faith INT NOT NULL,
	archer_calls INT NOT NULL,
	spear_faith INT NOT NULL,
	spear_calls INT NOT NULL,
	sword_faith INT NOT NULL,
	sword_calls INT NOT NULL,

	PRIMARY KEY (charid)
);

CREATE TABLE characters_locations
(
	charid INT NOT NULL,
	num INT NOT NULL,
	mapname VARCHAR(32) NOT NULL,
	coord_x INT NOT NULL,
	coord_y INT NOT NULL,

	PRIMARY KEY (charid, num)
);
