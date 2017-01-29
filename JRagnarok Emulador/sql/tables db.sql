
CREATE TABLE map_index
(
	id INT AUTO_INCREMENT,
	mapid SMALLINT NOT NULL,
	map_name VARCHAR(16) NOT NULL,

	CONSTRAINT un_mapid UNIQUE(mapid),
	CONSTRAINT un_map_name UNIQUE(map_name),
	CONSTRAINT pk_map_index_id PRIMARY KEY (id)

) ENGINE=InnoDB;
