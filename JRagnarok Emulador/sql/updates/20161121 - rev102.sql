
CREATE TABLE ipban_list
(
	address_list VARCHAR(15) NOT NULL,
	ban_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
	resume_time DATETIME NOT NULL,
	reason VARCHAR(255)

) ENGINE=MyISAM;