CREATE TABLE professional_category (
    id BIGINT PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    enabled BOOLEAN NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL
);

CREATE TABLE professional (
    id BIGINT PRIMARY KEY,
    category_id BIGINT NOT NULL,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    enabled BOOLEAN NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_professional_category FOREIGN KEY (category_id) REFERENCES professional_category(id)
);

CREATE TABLE weekly_schedule (
    id BIGINT PRIMARY KEY,
    professional_id BIGINT NOT NULL,
    day_of_week VARCHAR(9) NOT NULL,
    start_time TIME(6) NOT NULL,
    end_time TIME(6) NOT NULL,
    slot_duration_minutes INT NOT NULL,
    enabled BOOLEAN NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_schedule_professional FOREIGN KEY (professional_id) REFERENCES professional(id),
    CONSTRAINT ck_schedule_day CHECK (day_of_week IN ('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY')),
    CONSTRAINT ck_schedule_duration CHECK (slot_duration_minutes > 0),
    CONSTRAINT ck_schedule_interval CHECK (end_time > start_time),
    INDEX ix_schedule_professional_day (professional_id, day_of_week)
);

CREATE TABLE catalog_sync_state (
    id INT PRIMARY KEY,
    applied_version BIGINT NULL,
    applied_at DATETIME(6) NULL,
    CONSTRAINT ck_sync_single_row CHECK (id = 1),
    CONSTRAINT ck_sync_version CHECK (applied_version IS NULL OR applied_version >= 0),
    CONSTRAINT ck_sync_initial_state CHECK (
        (applied_version IS NULL AND applied_at IS NULL)
        OR (applied_version IS NOT NULL AND applied_at IS NOT NULL)
    )
);
INSERT INTO catalog_sync_state(id, applied_version, applied_at) VALUES (1, NULL, NULL);
