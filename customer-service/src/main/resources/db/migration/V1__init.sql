CREATE TABLE users (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name          VARCHAR(100)   NOT NULL,
    last_name           VARCHAR(100)   NOT NULL,
    email               VARCHAR(255)   NOT NULL UNIQUE,
    phone_number        VARCHAR(20)    UNIQUE,
    nationality         CHAR(3)        NOT NULL,
    address_line1       VARCHAR(255)   NOT NULL,
    city                VARCHAR(100)   NOT NULL,
    country             VARCHAR(100)   NOT NULL,
    status              VARCHAR(30)    NOT NULL DEFAULT 'PENDING',
    kyc_status          VARCHAR(30)    NOT NULL DEFAULT 'NOT_SUBMITTED',
    kyc_verified_at     TIMESTAMPTZ,
    risk_score          NUMERIC(5,2)   DEFAULT 0.00,
    tier_level          VARCHAR(20)    NOT NULL DEFAULT 'BASIC',
    daily_limit         NUMERIC(15,2),
    preferred_currency  CHAR(3)        NOT NULL DEFAULT 'XOF',
    is_email_verified   BOOLEAN        NOT NULL DEFAULT FALSE,
    is_phone_verified   BOOLEAN        NOT NULL DEFAULT FALSE,
    profile_picture_url TEXT,
    metadata            JSONB,
    created_at          TIMESTAMPTZ    NOT NULL,
    updated_at          TIMESTAMPTZ,
    deleted_at          TIMESTAMPTZ
);

CREATE INDEX idx_user_email ON users (email);

CREATE TABLE outbox_events (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type  VARCHAR(100) NOT NULL,
    aggregate_id    UUID         NOT NULL,
    event_type      VARCHAR(100) NOT NULL,
    kafka_topic     VARCHAR(200) NOT NULL,
    payload         TEXT         NOT NULL,
    published       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ  NOT NULL,
    published_at    TIMESTAMPTZ
);

CREATE INDEX idx_outbox_published ON outbox_events (published);
