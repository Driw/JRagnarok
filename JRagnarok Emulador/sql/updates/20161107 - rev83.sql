
ALTER TABLE login_log CHANGE login account INT NOT NULL;
ALTER TABLE login_log ADD FOREIGN KEY (account) REFERENCES accounts(id);
