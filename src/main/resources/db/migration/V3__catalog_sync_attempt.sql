CREATE TABLE catalog_sync_attempt (
    id INT PRIMARY KEY,
    attempt_id CHAR(36) CHARACTER SET ascii COLLATE ascii_bin,
    started_at DATETIME(6),
    completed_at DATETIME(6),
    result VARCHAR(40) NOT NULL,
    error_code VARCHAR(60),
    CONSTRAINT ck_sync_attempt_single_row CHECK (id = 1)
);
INSERT INTO catalog_sync_attempt (id, result) VALUES (1, 'NEVER');
