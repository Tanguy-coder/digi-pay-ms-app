CREATE TABLE payments (
    id                  UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_reference   VARCHAR(40)   NOT NULL UNIQUE,
    sender_wallet_id    UUID          NOT NULL,
    receiver_wallet_id  UUID          NOT NULL,
    amount              NUMERIC(20,6) NOT NULL,
    fee_amount          NUMERIC(20,6) NOT NULL DEFAULT 0,
    currency            CHAR(3)       NOT NULL,
    exchange_rate       NUMERIC(15,6) NOT NULL DEFAULT 1,
    status              VARCHAR(30)   NOT NULL DEFAULT 'INITIATED',
    payment_type        VARCHAR(30)   NOT NULL,
    failure_reason      VARCHAR(500),
    description         VARCHAR(500),
    idempotency_key     VARCHAR(100)  NOT NULL UNIQUE,
    merchant_id         UUID,
    metadata            TEXT,
    version             BIGINT        NOT NULL DEFAULT 0,
    initiated_at        TIMESTAMPTZ   NOT NULL,
    completed_at        TIMESTAMPTZ
);

CREATE INDEX idx_payment_sender_wallet   ON payments (sender_wallet_id);
CREATE INDEX idx_payment_receiver_wallet ON payments (receiver_wallet_id);
CREATE UNIQUE INDEX idx_payment_idempotency_key ON payments (idempotency_key);
CREATE UNIQUE INDEX idx_payment_reference       ON payments (payment_reference);

CREATE TABLE saga_steps (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id          UUID        NOT NULL,
    step_name           VARCHAR(50) NOT NULL,
    step_status         VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    step_order          INT         NOT NULL,
    compensation_event  VARCHAR(100),
    retry_count         INT         NOT NULL DEFAULT 0,
    max_retries         INT         NOT NULL DEFAULT 3,
    error               TEXT,
    started_at          TIMESTAMPTZ,
    completed_at        TIMESTAMPTZ
);

CREATE INDEX idx_saga_step_payment_id ON saga_steps (payment_id);

CREATE TABLE idempotency_keys (
    idempotency_key VARCHAR(100) PRIMARY KEY,
    response_status INT          NOT NULL,
    response_body   TEXT,
    expires_at      TIMESTAMPTZ  NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL
);

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
