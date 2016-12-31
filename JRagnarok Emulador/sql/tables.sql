
CREATE TABLE IF NOT EXISTS groups
(
	id INT AUTO_INCREMENT,
	access_level INT NOT NULL,
	name VARCHAR(24) NOT NULL,
	parent INT,
	log_commands TINYINT(1) NOT NULL DEFAULT 0,

	CONSTRAINT pk_groupid PRIMARY KEY (id),
	CONSTRAINT fk_group_parent FOREIGN KEY (parent) REFERENCES groups (id) ON DELETE NO ACTION ON UPDATE NO ACTION

) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS groups_commands
(
	groupid INT NOT NULL,
	command VARCHAR(24) NOT NULL,
	enabled TINYINT NOT NULL,

	CONSTRAINT pk_gid_command PRIMARY KEY (groupid, command),
	CONSTRAINT fk_gid_command FOREIGN KEY (groupid) REFERENCES groups (id) ON DELETE CASCADE ON UPDATE CASCADE

) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS groups_permissions
(
	groupid INT NOT NULL,
	permission VARCHAR(24) NOT NULL,
	enabled TINYINT NOT NULL,

	CONSTRAINT pk_gid_permission PRIMARY KEY (groupid, permission),
	CONSTRAINT fk_gid_permission FOREIGN KEY (groupid) REFERENCES groups (id) ON DELETE CASCADE ON UPDATE CASCADE

) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS vips
(
	id INT AUTO_INCREMENT,
	name VARCHAR(24) NOT NULL,
	char_slot_count TINYINT NOT NULL,
	max_storage SMALLINT NOT NULL,

	CONSTRAINT pk_vipid PRIMARY KEY (id)

) ENGINE=InnoDB;

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
	unban_time DATETIME NOT NULL DEFAULT '1970-01-01 00:00:00',
	expiration DATETIME NOT NULL DEFAULT '1970-01-01 00:00:00',
	account_state TINYINT NOT NULL DEFAULT 0,
	last_ip VARCHAR(15) NOT NULL DEFAULT '127.0.0.1',

	UNIQUE (email, username),
	CONSTRAINT pk_accountid PRIMARY KEY (id)

) ENGINE=InnoDB AUTO_INCREMENT=1000000;

CREATE TABLE IF NOT EXISTS accounts_groups
(
	accountid INT NOT NULL,
	current_group INT NOT NULL,
	old_group INT,
	vip INT,
	timeout DATETIME NOT NULL DEFAULT '1970-01-01 00:00:00',

	CONSTRAINT pk_aid_groups PRIMARY KEY (accountid),
	CONSTRAINT fk_acc_groups_aid FOREIGN KEY (accountid) REFERENCES accounts(id) ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT fk_acc_groups_cgid FOREIGN KEY (current_group) REFERENCES groups(id) ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT fk_acc_groups_ogid FOREIGN KEY (old_group) REFERENCES groups(id) ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT fk_acc_groups_vid FOREIGN KEY (vip) REFERENCES vips(id) ON DELETE CASCADE ON UPDATE CASCADE

) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS pincodes
(
	accountid INT NOT NULL,
	enabled TINYINT(1) DEFAULT 1,
	code_number CHAR(4) NOT NULL DEFAULT '0000',
	change_time DATETIME NOT NULL DEFAULT '1970-01-01 00:00:00',

	CONSTRAINT pk_pincode PRIMARY KEY (accountid),
	CONSTRAINT fk_pincode FOREIGN KEY (accountid) REFERENCES accounts(id) ON DELETE CASCADE ON UPDATE CASCADE

) ENGINE=InnoDB;

CREATE TABLE login_log
(
	id INT NOT NULL AUTO_INCREMENT,
	log_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
	ip INT NOT NULL DEFAULT 2130706433,
	accountid INT NOT NULL,
	rcode TINYINT NOT NULL,
	message VARCHAR(255),

	CONSTRAINT pk_loginlog PRIMARY KEY (id),
	CONSTRAINT fk_aid_loginlog FOREIGN KEY (accountid) REFERENCES accounts(id) ON DELETE CASCADE ON UPDATE CASCADE

) ENGINE=InnoDB;

CREATE TABLE ipban_list
(
	id INT NOT NULL AUTO_INCREMENT,
	address_list VARCHAR(15) NOT NULL,
	ban_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
	resume_time DATETIME NOT NULL,
	reason VARCHAR(255),

	UNIQUE (address_list),
	PRIMARY KEY (id)

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
	rename_count TINYINT(3) NOT NULL DEFAULT 0,
	unban DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
	delete_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
	moves TINYINT NOT NULL DEFAULT 0,
	font TINYINT NOT NULL DEFAULT 0,
	unique_item_counter INT NOT NULL DEFAULT 0,

	UNIQUE (name),
	CONSTRAINT pk_charid PRIMARY KEY (id)

) ENGINE=InnoDB AUTO_INCREMENT=1;

CREATE TABLE accounts_char_list
(
	accountid INT NOT NULL,
	charid INT NOT NULL,
	slot INT NOT NULL,

	CONSTRAINT pk_charlist PRIMARY KEY (accountid, slot),
	CONSTRAINT fk_charlist_aid FOREIGN KEY (accountid) REFERENCES accounts(id) ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT fk_charlist_cid FOREIGN KEY (charid) REFERENCES characters(id) ON DELETE CASCADE ON UPDATE CASCADE

) ENGINE=InnoDB;

CREATE TABLE characters_stats
(
	charid INT NOT NULL,
	strength INT NOT NULL DEFAULT 0,
	agility INT NOT NULL DEFAULT 0,
	vitality INT NOT NULL DEFAULT 0,
	intelligence INT NOT NULL DEFAULT 0,
	dexterity INT NOT NULL DEFAULT 0,
	luck INT NOT NULL DEFAULT 0,

	CONSTRAINT pk_char_stats PRIMARY KEY (charid),
	CONSTRAINT fk_char_stats FOREIGN KEY (charid) REFERENCES characters(id) ON DELETE CASCADE ON UPDATE CASCADE

) ENGINE=InnoDB;

CREATE TABLE characters_look
(
	charid INT NOT NULL,
	hair SMALLINT NOT NULL DEFAULT 0,
	hair_color SMALLINT NOT NULL DEFAULT 0,
	clothes_color SMALLINT NOT NULL DEFAULT 0,
	body SMALLINT NOT NULL DEFAULT 0,
	weapon SMALLINT NOT NULL DEFAULT 0,
	shield SMALLINT NOT NULL DEFAULT 0,
	head_top SMALLINT NOT NULL DEFAULT 0,
	head_mid SMALLINT NOT NULL DEFAULT 0,
	head_bottom SMALLINT NOT NULL DEFAULT 0,
	robe SMALLINT NOT NULL DEFAULT 0,

	CONSTRAINT pk_char_look PRIMARY KEY (charid),
	CONSTRAINT fk_char_look FOREIGN KEY (charid) REFERENCES characters(id) ON DELETE CASCADE ON UPDATE CASCADE

) ENGINE=InnoDB;

CREATE TABLE characters_family
(
	charid INT NOT NULL,
	partner INT NOT NULL,
	father INT,
	mother INT,
	child INT,

	CONSTRAINT pk_char_family PRIMARY KEY (charid),
	CONSTRAINT fk_char_family FOREIGN KEY (charid) REFERENCES characters(id) ON DELETE CASCADE ON UPDATE CASCADE

) ENGINE=InnoDB;

CREATE TABLE characters_experience
(
	charid INT NOT NULL,
	base INT NOT NULL,
	job INT NOT NULL,
	fame INT NOT NULL,

	CONSTRAINT pk_char_exp PRIMARY KEY (charid),
	CONSTRAINT fk_char_exp FOREIGN KEY (charid) REFERENCES characters(id) ON DELETE CASCADE ON UPDATE CASCADE

) ENGINE=InnoDB;

CREATE TABLE characters_mercenary_rank
(
	charid INT NOT NULL,
	archer_faith INT NOT NULL,
	archer_calls INT NOT NULL,
	spear_faith INT NOT NULL,
	spear_calls INT NOT NULL,
	sword_faith INT NOT NULL,
	sword_calls INT NOT NULL,

	CONSTRAINT pk_char_merc PRIMARY KEY (charid),
	CONSTRAINT fk_char_merc FOREIGN KEY (charid) REFERENCES characters(id) ON DELETE CASCADE ON UPDATE CASCADE

) ENGINE=InnoDB;

CREATE TABLE characters_locations
(
	charid INT NOT NULL,
	num INT NOT NULL,
	mapname VARCHAR(32) NOT NULL,
	coord_x INT NOT NULL,
	coord_y INT NOT NULL,

	CONSTRAINT pk_char_loc PRIMARY KEY (charid, num)

) ENGINE=InnoDB;

ALTER TABLE characters_locations ADD CONSTRAINT fk_char_loc FOREIGN KEY (charid) REFERENCES characters(id) ON DELETE RESTRICT ON UPDATE CASCADE;

CREATE TABLE register_account_int
(
	accountid INT NOT NULL,
	name VARCHAR(32) NOT NULL,
	int_value INT NOT NULL,

	CONSTRAINT pk_reg_acc_int PRIMARY KEY (accountid, name),
	CONSTRAINT fk_reg_acc_int_aid FOREIGN KEY (accountid) REFERENCES accounts(id)

) ENGINE=InnoDB;

CREATE TABLE register_account_str
(
	accountid INT NOT NULL,
	name VARCHAR(32) NOT NULL,
	str_value VARCHAR(254) NOT NULL,

	CONSTRAINT pk_reg_acc_str PRIMARY KEY (accountid, name),
	CONSTRAINT fk_reg_acc_str_aid FOREIGN KEY (accountid) REFERENCES accounts(id)

) ENGINE=InnoDB;

CREATE TABLE register_character_int
(
	charid INT NOT NULL,
	name VARCHAR(32) NOT NULL,
	int_value INT NOT NULL,

	CONSTRAINT pk_reg_char_int PRIMARY KEY (charid, name),
	CONSTRAINT fk_reg_char_int_cid FOREIGN KEY (charid) REFERENCES characters(id)

) ENGINE=InnoDB;

CREATE TABLE register_character_str
(
	charid INT NOT NULL,
	name VARCHAR(32) NOT NULL,
	str_value VARCHAR(254) NOT NULL,

	CONSTRAINT pk_reg_char_str PRIMARY KEY (charid, name),
	CONSTRAINT fk_reg_char_str_cid FOREIGN KEY (charid) REFERENCES characters(id)

) ENGINE=InnoDB;

CREATE TABLE register_global_int
(
	accountid INT NOT NULL,
	name VARCHAR(32) NOT NULL,
	int_value INT NOT NULL,

	CONSTRAINT pk_reg_global_int PRIMARY KEY (accountid, name),
	CONSTRAINT fk_reg_global_int_aid FOREIGN KEY (accountid) REFERENCES accounts(id)

) ENGINE=InnoDB;

CREATE TABLE register_global_str
(
	accountid INT NOT NULL,
	name VARCHAR(32) NOT NULL,
	str_value VARCHAR(254) NOT NULL,

	CONSTRAINT pk_reg_global_str PRIMARY KEY (accountid, name),
	CONSTRAINT fk_reg_global_str_aid FOREIGN KEY (accountid) REFERENCES accounts(id)

) ENGINE=InnoDB;
