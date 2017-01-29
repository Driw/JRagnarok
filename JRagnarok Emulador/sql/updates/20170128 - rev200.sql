
-- tables.sql

ALTER TABLE characters_locations CHANGE mapname mapid SMALLINT NOT NULL;

-- tables_db.sql

ALTER TABLE map_index DROP INDEX map_name;
ALTER TABLE map_index ADD mapid SMALLINT NOT NULL AFTER map_name;
ALTER TABLE map_index ADD CONSTRAINT un_mapid UNIQUE(mapid);
ALTER TABLE map_index ADD CONSTRAINT un_map_name UNIQUE(map_name);
