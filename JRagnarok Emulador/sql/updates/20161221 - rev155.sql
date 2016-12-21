
ALTER TABLE accounts_groups DROP FOREIGN KEY fk_aid_groups;

ALTER TABLE accounts_groups ADD CONSTRAINT fk_acc_groups_aid FOREIGN KEY (accountid) REFERENCES accounts(id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE accounts_groups ADD CONSTRAINT fk_acc_groups_cgid FOREIGN KEY (current_group) REFERENCES groups(id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE accounts_groups ADD CONSTRAINT fk_acc_groups_ogid FOREIGN KEY (old_group) REFERENCES groups(id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE accounts_groups ADD CONSTRAINT fk_acc_groups_vid FOREIGN KEY (vip) REFERENCES vips(id) ON DELETE CASCADE ON UPDATE CASCADE;
