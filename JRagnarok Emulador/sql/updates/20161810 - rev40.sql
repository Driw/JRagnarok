
ALTER TABLE accounts CHANGE birthDate birth_date CHAR(10) NOT NULL DEFAULT '0000-00-00';
ALTER TABLE accounts DROP COLUMN char_slots;

ALTER TABLE groups_commands DROP COLUMN enabled;
ALTER TABLE groups_permissions DROP COLUMN enabled;

RENAME TABLE accounts_vip TO accounts_groups;
RENAME TABLE group_commands TO group_commands_list;
RENAME TABLE group_permissions TO group_permissions_list;
RENAME TABLE commands TO group_commands;
RENAME TABLE permissions TO group_permissions;
