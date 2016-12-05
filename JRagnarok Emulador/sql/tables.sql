
CREATE TABLE IF NOT EXISTS pincodes
(
	id INT AUTO_INCREMENT,
	enabled TINYINT(1) DEFAULT 1,
	code CHAR(4) NOT NULL DEFAULT '0000',
	changed DATETIME NOT NULL DEFAULT '1970-01-01 00:00:00',

	PRIMARY KEY (id)

) ENGINE=MyISAM;

CREATE TABLE IF NOT EXISTS groups
(
	id INT AUTO_INCREMENT,
	level INT NOT NULL,
	name VARCHAR(24) NOT NULL,
	parent INT,
	log_enabled TINYINT(1) NOT NULL DEFAULT 0,

	PRIMARY KEY (id),
	CONSTRAINT SelfKey FOREIGN KEY (parent) REFERENCES groups (id) ON DELETE NO ACTION ON UPDATE NO ACTION

) ENGINE=MyISAM;

CREATE TABLE IF NOT EXISTS group_commands
(
	id INT AUTO_INCREMENT,
	name VARCHAR(24) NOT NULL,

	PRIMARY KEY (id)

) ENGINE=MyISAM;

CREATE TABLE IF NOT EXISTS group_permissions
(
	id INT AUTO_INCREMENT,
	name VARCHAR(24) NOT NULL,

	PRIMARY KEY (id)

) ENGINE=MyISAM;

CREATE TABLE IF NOT EXISTS groups_commands_list
(
	groupid INT NOT NULL,
	command INT NOT NULL,

	PRIMARY KEY (groupid, command),
	FOREIGN KEY (groupid) REFERENCES groups (id),
	FOREIGN KEY (command) REFERENCES commands (id)

) ENGINE=MyISAM;

CREATE TABLE IF NOT EXISTS groups_permissions_list
(
	groupid INT NOT NULL,
	permission INT NOT NULL,

	PRIMARY KEY (groupid, permission),
	FOREIGN KEY (groupid) REFERENCES groups (id),
	FOREIGN KEY (permission) REFERENCES permissions (id)

) ENGINE=MyISAM;

CREATE TABLE IF NOT EXISTS vip
(
	id INT AUTO_INCREMENT,
	name VARCHAR(24) NOT NULL,
	groupid INT NOT NULL,
	char_slot_count TINYINT NOT NULL,
	max_storage SMALLINT NOT NULL,

	PRIMARY KEY (id),
	FOREIGN KEY (groupid) REFERENCES groups (id)

) ENGINE=MyISAM;

CREATE TABLE IF NOT EXISTS accounts
(
	id INT AUTO_INCREMENT,
	username VARCHAR(23) NOT NULL,
	password VARCHAR(32) NOT NULL,
	last_login DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
	registered DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
	email VARCHAR(40) NOT NULL DEFAULT 'a@a.com',
	birth_date CHAR(10) NOT NULL DEFAULT '0000-00-00',
	login_count INT NOT NULL DEFAULT 0,
	unban DATETIME NOT NULL DEFAULT '1970-01-01 00:00:00',
	expiration DATETIME NOT NULL DEFAULT '1970-01-01 00:00:00',
	pincode INT NOT NULL,
	groupid INT NOT NULL,
	state TINYINT NOT NULL DEFAULT 0,
	last_ip VARCHAR(15) NOT NULL DEFAULT '127.0.0.1',

	UNIQUE (email),
	PRIMARY KEY (id),
	FOREIGN KEY (pincode) REFERENCES pincodes (id),
	FOREIGN KEY (groupid) REFERENCES accounts_groups (id)

) ENGINE=MyISAM AUTO_INCREMENT=1000000;

CREATE TABLE IF NOT EXISTS accounts_groups
(
	id INT AUTO_INCREMENT,
	current_group INT NOT NULL,
	old_group INT,
	vip INT,
	timeout DATETIME NOT NULL DEFAULT '1970-01-01 00:00:00',

	PRIMARY KEY (id),

) ENGINE=MyISAM;

CREATE TABLE login_log
(
	time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
	ip INT NOT NULL DEFAULT 2130706433,
	account INT NOT NULL,
	rcode TINYINT NOT NULL,
	message VARCHAR(255),

	FOREIGN KEY (account) REFERENCES accounts (id)

) ENGINE=MyISAM;

CREATE TABLE ipban_list
(
	address_list VARCHAR(15) NOT NULL,
	ban_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
	resume_time DATETIME NOT NULL,
	reason VARCHAR(255),

	PRIMARY KEY (address_list)

) ENGINE=MyISAM;

CREATE TABLE characters
(
	id INT NOT NULL AUTO_INCREMENT,
	name VARCHAR(24) NOT NULL,
	sex ENUM('M', 'F') NOT NULL,
	zeny INT NOT NULL DEFAULT 0,
	status_point INT NOT NULL DEFAULT 0,
	skill_point INT NOT NULL DEFAULT 0,
	jobid SMALLINT NOT NULL DEFAULT 0,
	max_hp INT NOT NULL default 0,
	hp INT NOT NULL default 0,
	max_sp SMALLINT NOT NULL default 0,
	sp SMALLINT NOT NULL default 0,
	manner SMALLINT NOT NULL DEFAULT 0,
	effect_state INT NOT NULL DEFAULT 0,
	karma INT NOT NULL DEFAULT 0,
	base_level INT NOT NULL DEFAULT 0,
	job_level INT NOT NULL DEFAULT 0,
	online TINYINT(1) NOT NULL DEFAULT 0,
	rename TINYINT(3) NOT NULL DEFAULT 0,
	delete_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
	moves TINYINT NOT NULL DEFAULT 0,
	font TINYINT NOT NULL DEFAULT 0,
	unique_item_counter INT NOT NULL 0,

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
