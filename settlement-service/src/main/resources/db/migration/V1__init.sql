CREATE TABLE settlement_batches (
    id            UUID          PRIMARY KEY,
    reference     VARCHAR(255)  NOT NULL UNIQUE,
    status        VARCHAR(20)   NOT NULL,
    cycle         VARCHAR(20)   NOT NULL,
    currency      CHAR(3)       NOT NULL,
    total_entries INT           NOT NULL,
    total_amount  NUMERIC(19,4) NOT NULL,
    opened_at     TIMESTAMPTZ   NOT NULL,
    closed_at     TIMESTAMPTZ,
    settled_at    TIMESTAMPTZ,
    created_at    TIMESTAMPTZ   NOT NULL
);

CREATE UNIQUE INDEX idx_batch_reference       ON settlement_batches (reference);
CREATE        INDEX idx_batch_status          ON settlement_batches (status);
CREATE        INDEX idx_batch_status_currency ON settlement_batches (status, currency);

CREATE TABLE settlement_entries (
    id                  UUID          PRIMARY KEY,
    batch_id            UUID          NOT NULL,
    payment_id          UUID          NOT NULL UNIQUE,
    payment_reference   VARCHAR(100)  NOT NULL,
    sender_wallet_id    UUID          NOT NULL,
    receiver_wallet_id  UUID          NOT NULL,
    amount              NUMERIC(19,4) NOT NULL,
    currency            CHAR(3)       NOT NULL,
    captured_at         TIMESTAMPTZ   NOT NULL
);

CREATE INDEX idx_entry_batch_id   ON settlement_entries (batch_id);
CREATE UNIQUE INDEX idx_entry_payment_id ON settlement_entries (payment_id);

CREATE TABLE net_positions (
    id            UUID          PRIMARY KEY,
    batch_id      UUID          NOT NULL,
    wallet_id     UUID          NOT NULL,
    gross_debit   NUMERIC(19,4) NOT NULL,
    gross_credit  NUMERIC(19,4) NOT NULL,
    net_amount    NUMERIC(19,4) NOT NULL,
    status        VARCHAR(20)   NOT NULL,
    CONSTRAINT uq_position_batch_wallet UNIQUE (batch_id, wallet_id)
);

CREATE        INDEX idx_position_batch_id     ON net_positions (batch_id);
CREATE UNIQUE INDEX idx_position_batch_wallet ON net_positions (batch_id, wallet_id);

CREATE TABLE batch_events (
    id                UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    batch_id          UUID          NOT NULL,
    event_type        VARCHAR(50)   NOT NULL,
    aggregate_version BIGINT        NOT NULL,
    occurred_at       TIMESTAMPTZ   NOT NULL,
    reference         VARCHAR(255),
    cycle             VARCHAR(20),
    currency          CHAR(3),
    status            VARCHAR(20),
    payment_id        UUID,
    payment_reference VARCHAR(255),
    sender_wallet_id  UUID,
    receiver_wallet_id UUID,
    amount            NUMERIC(19,4),
    wallet_id         UUID,
    gross_debit       NUMERIC(19,4),
    gross_credit      NUMERIC(19,4),
    net_amount        NUMERIC(19,4),
    position_status   VARCHAR(20),
    reason            VARCHAR(255)
);

CREATE        INDEX idx_batch_events_batch_id      ON batch_events (batch_id);
CREATE UNIQUE INDEX idx_batch_events_batch_version ON batch_events (batch_id, aggregate_version);

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
