
INSERT INTO groups (level, name, log_enabled) VALUES (0, 'Servidores', 1);
INSERT INTO groups (level, name, log_enabled) VALUES (0, 'Jogadores Comuns', 1);

INSERT INTO pincodes (enabled, code, changed) VALUES (0, '1001', '1970-01-01 00:00:00');

INSERT INTO accounts (id, username, password, groupid, pincode) VALUES (1, 'server', 'jragnarok', 1, 1);
