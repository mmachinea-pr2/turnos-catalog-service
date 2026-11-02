CREATE TABLE processed_catalog_event (
    event_id VARBINARY(255) PRIMARY KEY,
    new_version BIGINT NOT NULL,
    processed_at DATETIME(6) NOT NULL,
    CONSTRAINT ck_processed_event_version CHECK (new_version >= 0)
);
