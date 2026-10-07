-- SafeAccess / PAM Backend: additive migration for existing pam_database.
-- Run with mysql after making your usual database backup. No users or audit history are deleted.
ALTER TABLE mfa_codes MODIFY COLUMN code VARCHAR(255) NOT NULL;
SET @safeaccess_column_exists=(SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='mfa_codes' AND column_name='attempt_count');
SET @safeaccess_ddl=IF(@safeaccess_column_exists=0,'ALTER TABLE mfa_codes ADD COLUMN attempt_count INT NOT NULL DEFAULT 0','SELECT 1');
PREPARE safeaccess_stmt FROM @safeaccess_ddl;
EXECUTE safeaccess_stmt;
DEALLOCATE PREPARE safeaccess_stmt;
-- Existing plaintext codes are invalidated; new codes are stored as BCrypt hashes.
UPDATE mfa_codes SET used=1 WHERE used=0 AND code NOT LIKE '$2%';
