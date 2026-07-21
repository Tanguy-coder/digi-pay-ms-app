CREATE TABLE wallets (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id     UUID         NOT NULL,
    wallet_number   VARCHAR(30)  NOT NULL UNIQUE,
    wallet_type     VARCHAR(20)  NOT NULL DEFAULT 'PERSONAL',
    currency        CHAR(3)      NOT NULL DEFAULT 'XOF',
    balance         NUMERIC(19,4) NOT NULL DEFAULT 0.0000,
    frozen_amount   NUMERIC(19,4) NOT NULL DEFAULT 0.0000,
    daily_limit     NUMERIC(19,4),
    monthly_limit   NUMERIC(19,4),
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    version         BIGINT       NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ  NOT NULL,
    updated_at      TIMESTAMPTZ,
    CONSTRAINT uq_wallet_customer_type UNIQUE (customer_id, wallet_type)
);

CREATE INDEX idx_wallet_customer_id ON wallets (customer_id);
CREATE UNIQUE INDEX idx_wallet_number ON wallets (wallet_number);

CREATE TABLE wallet_events (
    id                UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    wallet_id         UUID        NOT NULL,
    event_type        VARCHAR(50) NOT NULL,
    customer_id       UUID,
    currency          CHAR(3),
    wallet_type       VARCHAR(20),
    wallet_number     VARCHAR(30),
    amount            NUMERIC(19,4),
    daily_limit       NUMERIC(19,4),
    monthly_limit     NUMERIC(19,4),
    status            VARCHAR(20),
    aggregate_version BIGINT      NOT NULL,
    occurred_at       TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_wallet_events_wallet_id ON wallet_events (wallet_id);
CREATE UNIQUE INDEX idx_wallet_events_wallet_version ON wallet_events (wallet_id, aggregate_version);

CREATE TABLE outbox_events (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
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
