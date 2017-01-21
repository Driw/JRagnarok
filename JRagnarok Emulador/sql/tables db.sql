
CREATE TABLE map_index
(
	id INT AUTO_INCREMENT,
	map_name VARCHAR(16) NOT NULL,

	UNIQUE (map_name),
	CONSTRAINT pk_map_index_id PRIMARY KEY (id)

) ENGINE=InnoDB;
