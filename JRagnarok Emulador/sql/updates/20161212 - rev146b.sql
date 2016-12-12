
ALTER TABLE vip RENAME vips;

ALTER TABLE groups CHANGE log_enabled log_commands TINYINT(1) NOT NULL DEFAULT 0;

DROP TABLE group_commands;
DROP TABLE group_commands_list;
DROP TABLE group_permissions;
DROP TABLE group_permissions_list;

CREATE TABLE IF NOT EXISTS groups_commands
(
	groupid INT NOT NULL,
	command VARCHAR(24) NOT NULL,
	enabled TINYINT(1)

	PRIMARY KEY (groupid, command),
	FOREIGN KEY (groupid) REFERENCES groups (id)

) ENGINE=MyISAM;

CREATE TABLE IF NOT EXISTS groups_permissions
(
	groupid INT NOT NULL,
	permission VARCHAR(24) NOT NULL,

	PRIMARY KEY (groupid, permission),
	FOREIGN KEY (groupid) REFERENCES groups (id)

) ENGINE=MyISAM;
