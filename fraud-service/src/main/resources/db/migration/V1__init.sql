CREATE TABLE fraud_rules (
    id               UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    rule_code        VARCHAR(50)   NOT NULL UNIQUE,
    description      TEXT,
    rule_type        VARCHAR(30)   NOT NULL,
    threshold_value  NUMERIC(20,6),
    score_weight     NUMERIC(5,2)  NOT NULL,
    action           VARCHAR(30)   NOT NULL,
    priority         VARCHAR(20)   NOT NULL,
    is_active        BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ   NOT NULL,
    updated_at       TIMESTAMPTZ
);

CREATE TABLE fraud_analyses (
    id                   UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id           UUID         NOT NULL UNIQUE,
    customer_id          UUID         NOT NULL,
    risk_score           NUMERIC(5,2) NOT NULL,
    verdict              VARCHAR(20)  NOT NULL,
    analysis_duration_ms INT,
    rules_triggered      TEXT,
    ip_address           VARCHAR(45),
    country_code         CHAR(2),
    device_fingerprint   VARCHAR(255),
    is_new_device        BOOLEAN,
    transaction_hour     INT,
    velocity_1min        INT,
    velocity_1h          INT,
    reviewed_by          UUID,
    reviewed_at          TIMESTAMPTZ,
    created_at           TIMESTAMPTZ  NOT NULL
);

CREATE UNIQUE INDEX idx_fraud_analysis_payment_id  ON fraud_analyses (payment_id);
CREATE        INDEX idx_fraud_analysis_customer_id ON fraud_analyses (customer_id);

CREATE TABLE fraud_alerts (
    id                UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    fraud_analysis_id UUID         NOT NULL,
    fraud_rule_id     UUID         NOT NULL,
    alert_status      VARCHAR(20)  NOT NULL DEFAULT 'OPEN',
    score_at_trigger  NUMERIC(5,2),
    resolution_note   TEXT,
    triggered_at      TIMESTAMPTZ  NOT NULL,
    resolved_at       TIMESTAMPTZ
);

CREATE INDEX idx_fraud_alert_analysis_id ON fraud_alerts (fraud_analysis_id);

CREATE TABLE customer_risk_profiles (
    customer_id              UUID          PRIMARY KEY,
    lifetime_risk_score      NUMERIC(5,2)  NOT NULL DEFAULT 0.00,
    total_alerts             INT           NOT NULL DEFAULT 0,
    false_positives          INT           NOT NULL DEFAULT 0,
    is_blacklisted           BOOLEAN       NOT NULL DEFAULT FALSE,
    avg_transaction_amount   NUMERIC(20,6) DEFAULT 0,
    last_analysis_at         TIMESTAMPTZ,
    updated_at               TIMESTAMPTZ
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

INSERT INTO fraud_rules (id, rule_code, description, rule_type, threshold_value, score_weight, action, priority, is_active, created_at, updated_at) VALUES
    (gen_random_uuid(), 'HIGH_AMOUNT',      'Block payments above 10 000 units',                   'AMOUNT',      10000.00, 85.00, 'BLOCK',         'CRITICAL', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'VELOCITY_1MIN',    'Flag when more than 3 transactions in 1 minute',       'VELOCITY',    3,        40.00, 'REVIEW',        'HIGH',     TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'VELOCITY_1H',      'Flag when more than 10 transactions in 1 hour',        'VELOCITY',    10,       25.00, 'FLAG',          'MEDIUM',   TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'RISKY_COUNTRY_KP', 'Block transactions from North Korea',                  'GEOLOCATION', 0,        90.00, 'BLOCK',         'CRITICAL', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'RISKY_COUNTRY_IR', 'Block transactions from Iran',                         'GEOLOCATION', 0,        90.00, 'BLOCK',         'CRITICAL', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'NEW_DEVICE',       'Review payments from an unrecognised device',           'DEVICE',      0,        20.00, 'CHALLENGE_OTP', 'MEDIUM',   TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'SUSPICIOUS_HOUR',  'Flag transactions between 00:00 and 05:00 UTC',        'BEHAVIORAL',  0,        15.00, 'FLAG',          'LOW',      TRUE, NOW(), NOW());
