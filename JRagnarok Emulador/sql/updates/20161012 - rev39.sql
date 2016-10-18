
DROP TABLE accounts, accounts_vip, commands, groups, groups_commands, groups_permissions, logins, login_log, permissions, pincodes, vip;

CREATE TABLE IF NOT EXISTS logins
(
	id INT AUTO_INCREMENT,
	username VARCHAR(23) NOT NULL,
	password VARCHAR(32) NOT NULL,
	last_login DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
	registered DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

	UNIQUE (username),
	PRIMARY KEY (id)

) ENGINE=MyISAM;

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

CREATE TABLE IF NOT EXISTS commands
(
	id INT AUTO_INCREMENT,
	name VARCHAR(24) NOT NULL,

	PRIMARY KEY (id)

) ENGINE=MyISAM;

CREATE TABLE IF NOT EXISTS permissions
(
	id INT AUTO_INCREMENT,
	name VARCHAR(24) NOT NULL,

	PRIMARY KEY (id)

) ENGINE=MyISAM;

CREATE TABLE IF NOT EXISTS groups_commands
(
	groupid INT NOT NULL,
	command INT NOT NULL,
	enabled TINYINT(1) NOT NULL DEFAULT 0,

	PRIMARY KEY (groupid, command),
	FOREIGN KEY (groupid) REFERENCES groups (id),
	FOREIGN KEY (command) REFERENCES commands (id)

) ENGINE=MyISAM;

CREATE TABLE IF NOT EXISTS groups_permissions
(
	groupid INT NOT NULL,
	permission INT NOT NULL,
	enabled TINYINT(1) NOT NULL DEFAULT 0,

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
	login INT NOT NULL,
	email VARCHAR(40) NOT NULL DEFAULT 'a@a.com',
	birthDate CHAR(10) NOT NULL DEFAULT '0000-00-00',
	char_slots TINYINT NOT NULL DEFAULT 9,
	login_count INT NOT NULL DEFAULT 0,
	unban DATETIME NOT NULL DEFAULT '1970-01-01 00:00:00',
	expiration DATETIME NOT NULL DEFAULT '1970-01-01 00:00:00',
	pincode INT NOT NULL,
	groupid INT NOT NULL,
	state TINYINT NOT NULL DEFAULT 0,
	last_ip VARCHAR(15) NOT NULL DEFAULT '127.0.0.1',

	UNIQUE (email),
	PRIMARY KEY (login),
	FOREIGN KEY (pincode) REFERENCES pincodes (id),
	FOREIGN KEY (groupid) REFERENCES groups (id)

) ENGINE=MyISAM;

CREATE TABLE IF NOT EXISTS accounts_vip
(
	id INT AUTO_INCREMENT,
	current_group INT NOT NULL,
	old_group INT,
	vip INT,
	timeout DATETIME NOT NULL DEFAULT '1970-01-01 00:00:00',

	PRIMARY KEY (id)

) ENGINE=MyISAM;

CREATE TABLE login_log
(
	time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
	ip INT NOT NULL DEFAULT 2130706433,
	login INT NOT NULL,
	rcode TINYINT NOT NULL,
	message VARCHAR(255),

	FOREIGN KEY (login) REFERENCES logins (id)

) ENGINE=MyISAM;
