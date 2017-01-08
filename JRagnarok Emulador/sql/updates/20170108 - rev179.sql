
ALTER TABLE accounts CHANGE unban_time unban_time DATETIME;
ALTER TABLE accounts CHANGE expiration expiration DATETIME;
ALTER TABLE accounts_groups CHANGE timeout timeout DATETIME;
ALTER TABLE pincodes CHANGE change_time change_time DATETIME;
ALTER TABLE characters CHANGE unban_time unban_time DATETIME;
ALTER TABLE characters CHANGE delete_date delete_date DATETIME;
