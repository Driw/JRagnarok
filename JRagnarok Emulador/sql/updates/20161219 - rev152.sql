
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
