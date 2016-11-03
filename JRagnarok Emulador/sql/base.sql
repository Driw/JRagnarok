
INSERT INTO groups (id, level, name, log_enabled) VALUES (0, 0, 'Servidores', 1), (1, 0, 'Jogadores Comuns', 0);

INSERT INTO pincodes (id, enabled, code, changed) VALUES (0, 0, '1001', '1970-01-01 00:00:00');
INSERT INTO accounts (id, username, password, groupid, pincode) VALUES (1, 'server', 'jragnarok', 0, 0);
