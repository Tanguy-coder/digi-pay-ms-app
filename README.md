# Digital Payment & Settlement Platform

> Microservices Architecture | Event-Driven | Kafka | Fintech

Electronic payment platform simulating the complete lifecycle of a financial transaction: from customer account creation to interbank settlement, including fraud detection and real-time notifications.

## Context

| | |
|---|---|
| **Domain** | Fintech / Banking / Electronic Payment |
| **Type** | Personal project — Senior technical portfolio |
| **Level** | Senior / Expert |
| **Main stack** | Spring Boot 4 · Kafka · PostgreSQL · Redis · Keycloak · Docker |
| **Key patterns** | Event-Driven · CQRS · Saga · Event Sourcing · DDD · OAuth2/JWT · Circuit Breaker · Rate Limiting |

**Business references**: Visa/Mastercard (clearing), Flutterwave/Paystack (African payments), Stripe (APIs), CinetPay/Wave (mobile money).

## Business Features

- Customer account creation and management with KYC process
- Electronic wallets with balance management and fund freezing
- Merchant payments and peer-to-peer transfers with distributed Saga
- Guaranteed idempotency on payments via Redis
- Distributed rate limiting (Token Bucket) on the gateway: 10 req/s per JWT user (or IP), burst 20
- Interbank settlement with net position calculation
- Real-time fraud detection (7 configurable rules, risk score 0-100)
- Real-time notifications: payment initiated, completed, failed, fraud detected

## General Architecture

```
                       [ Keycloak ]
                            │ JWT (RS256)
                        [ API Gateway ]  ← OAuth2 Resource Server · Rate Limiter (Token Bucket)
                              |
       [ Customer MS ] [ Wallet MS ] [ Payment MS ]
             |               |              |
     ════════════════[ KAFKA CLUSTER ]═══════════════
             |               |              |
       [ Fraud MS ]   [ Notify MS ]  [ Settlement MS ]
```

**Principles**: Loose Coupling (event-driven communication) · Database per Service (H2 dev / PostgreSQL prod) · Single API Gateway · High Cohesion.

## Microservices & Event Contracts

| Microservice | Port | Responsibilities | Publishes | Consumes |
|---|---|---|---|---|
| **Customer MS** | 8082 | Customer creation · KYC · Personal info | `customer.created` | — |
| **Wallet MS** | 8083 | Wallet · Event Sourcing · Balance · Fund freeze | `wallet.created` `wallet.credited` `wallet.debited` | `customer.created` · Saga commands |
| **Payment MS** | 8084 | P2P/merchant payments · Saga orchestration · Redis idempotency | `payment.initiated` `payment.completed` `payment.failed` `payment.reversed` `payment.compensation_failed` | Saga wallet responses · `fraud-check-events` |
| **Fraud MS** | 8085 | Anti-fraud rules · Risk score · Alerts | `fraud.cleared` `fraud.blocked` `fraud.review` | `payment.initiated` |
| **Notification MS** | 8086 | Real-time notifications (simulated email/SMS/push) | — | `payment.initiated` `payment.completed` `payment.failed` `fraud.blocked` `fraud.review` |
| **Settlement MS** | 8087 | Multilateral compensation · Hourly batches · Net positions · Event Sourcing | `settlement.completed` `settlement.failed` | `payment.completed` |

## Kafka Configuration

| Topic | Consumer Group(s) | Usage |
|---|---|---|
| `customer-events` | wallet-group | Customer lifecycle |
| `wallet-commands` | wallet-saga-group | Saga commands (DEBIT, CREDIT, COMPENSATE_DEBIT) |
| `wallet-events` | — | Wallet events (created, credited, debited) |
| `wallet-saga-events` | payment-saga-group | Wallet Saga responses (SUCCESS, FAILURE) |
| `payment-events` | fraud-group · notification-payment-group · settlement-group | Central payment stream |
| `fraud-check-events` | payment-fraud-group · notification-fraud-group | Fraud verdict (cleared / blocked / review) |
| `settlement-events` | — | Settlement result (completed / failed) |

## Advanced Patterns & Concepts

| Pattern | Application in this project |
|---|---|
| **Event Sourcing** | Wallet MS: wallet state is rebuilt from events (append-only `wallet_events` table). Settlement MS: batch state is rebuilt from events (`batch_events`). Balance/status is never directly modified — it is computed by replaying events. |
| **Saga (Orchestration)** | Payment MS orchestrates DEBIT → FRAUD_CHECK → CREDIT → COMPLETE; automatic compensation if a step fails |
| **Idempotency** | Idempotency key stored in Redis (TTL 24h); duplicate → HTTP 409 without reprocessing |
| **Clean Architecture** | Hexagonal (Ports & Adapters): pure domain with no framework dependency, isolated use cases |
| **Presenter Pattern** | Domain interface + infrastructure implementation; the controller only knows the domain |
| **DDD** | Customer / Wallet / Payment / Fraud / Notification / Settlement = independent bounded contexts |
| **CQRS** | Command Query Responsibility Segregation: controllers split into `CommandController` (POST/PUT, @Transactional) and `QueryController` (GET, read-only). Applied across all 6 business services. Strict read/write separation at the API level. |
| **OAuth2 / JWT** | Centralized security via Keycloak (Identity Provider) and Spring Security OAuth2 Resource Server at the gateway level. JWT validation (RS256) at the entry point, Keycloak role extraction (`realm_access.roles`) via custom converter. Downstream microservices have no security — they trust the gateway (perimeter zero-trust). |
| **Outbox Pattern** | Kafka publishing via transactional `outbox_events` table (same transaction as business data). Polling relay (1s) ensures at-least-once delivery without event loss. Applied to 5 services (customer, wallet, payment, fraud, settlement). |
| **Circuit Breaker / Retry** | Resilience4j on the payment-service `OutboxRelay`: Retry (3 attempts, 500ms) + Circuit Breaker (CLOSED/OPEN/HALF-OPEN). If Kafka is unavailable, the circuit opens after 50% failures over 10 calls, and the event stays in the outbox to be replayed. Protects against failure cascades. |
| **Rate Limiting (Token Bucket)** | Spring Cloud Gateway + Redis: global `RequestRateLimiter` on all routes. 10 tokens/s replenished, burst max 20. Key = JWT `sub` if authenticated, IP otherwise. HTTP 429 if bucket is empty. Counters stored in Redis → shared limit across all gateway instances. |
| **Event-Driven** | All services communicate exclusively via Kafka; zero synchronous inter-service calls |

### Saga Pattern — P2P transfer flow with fraud detection

```
Payment MS
  1. Checks idempotency key (Redis) → 409 if duplicate
  2. Saves payment (INITIATED)
  3. Publishes payment.initiated on payment-events
  4. Starts FRAUD_CHECK step → payment moves to FRAUD_CHECK status

Fraud MS
  5. Receives payment.initiated, evaluates 7 active rules
  5a. Score 0-30 → publishes fraud.cleared on fraud-check-events
  5b. Score 31-80 → publishes fraud.review
  5c. Score > 80 or BLOCK rule → publishes fraud.blocked

Payment MS
  6a. (fraud.cleared) → sends DEBIT_WALLET on wallet-commands
  6b. (fraud.blocked) → payment FAILED, end

Wallet MS
  7a. Alice debit succeeded → publishes DEBIT_SUCCESS
  7b. Alice debit failed → publishes DEBIT_FAILED

Payment MS
  8a. (success) → CREDIT_WALLET to Bob
  8b. (failure) → payment FAILED

Wallet MS
  9a. Bob credit succeeded → publishes CREDIT_SUCCESS → payment COMPLETED
  9b. Bob credit failed → publishes CREDIT_FAILED → COMPENSATE_DEBIT → payment REVERSED
  9c. If compensation fails → payment COMPENSATION_FAILED

Notification MS
  (in parallel) Receives payment.initiated / completed / failed / fraud.blocked
  → saves notification to database with status SENT

Settlement MS
  10. Receives payment.completed on payment-events (group: settlement-group)
  11. Checks idempotency (paymentId already processed → skip)
  12. Captures the entry in the open batch (Event Sourcing)
  13. Hourly scheduler: close batch → calculate net positions → apply settlement → complete
  14. Publishes settlement.completed on settlement-events
```

## Fraud Detection Rules

| Code | Condition | Score | Action | Priority |
|---|---|---|---|---|
| `HIGH_AMOUNT` | Amount > 10,000 | 85 | BLOCK | CRITICAL |
| `VELOCITY_1MIN` | >= 3 tx / minute / account | 40 | REVIEW | HIGH |
| `VELOCITY_1H` | >= 10 tx / hour / account | 25 | FLAG | MEDIUM |
| `RISKY_COUNTRY_KP` | Country = KP (North Korea) | 90 | BLOCK | CRITICAL |
| `RISKY_COUNTRY_IR` | Country = IR (Iran) | 90 | BLOCK | CRITICAL |
| `NEW_DEVICE` | New device (unrecognized) | 20 | CHALLENGE_OTP | MEDIUM |
| `SUSPICIOUS_HOUR` | Hour between 0am-5am UTC | 15 | FLAG | LOW |

**Score → Verdict**: 0-30 = CLEARED · 31-60 = REVIEW · 61-80 = FLAGGED · 81-100 = BLOCKED

## Tech Stack

| Domain | Technology |
|---|---|
| Backend | Spring Boot 4.1.0 (Java 21) |
| Messaging | Apache Kafka 3.9+ (KRaft mode) |
| Database | PostgreSQL 16 (prod) · H2 in-memory (dev/test) |
| Cache / Idempotency | Redis 7 |
| Object mapping | MapStruct 1.5.5 |
| Tests | JUnit 5, Mockito, @WebMvcTest, Testcontainers |
| Containerization | Docker + Docker Compose |
| Service Discovery | Spring Cloud Netflix Eureka |
| API Gateway | Spring Cloud Gateway (reactive, dynamic routing via Eureka) |
| Security | Keycloak 26 (OIDC / OAuth2) + Spring Security Resource Server (JWT RS256) |
| Observability | Prometheus · Grafana · Micrometer (real-time metrics) · Jaeger + OpenTelemetry (distributed tracing) |
| Resilience | Resilience4j (Circuit Breaker + Retry on Kafka publishing) |
| Rate Limiting | Redis 7 + Spring Cloud Gateway `RequestRateLimiter` (Token Bucket, 10 req/s per user) |

## Software Architecture (per service)

Each microservice follows a **hexagonal architecture** (Clean Architecture / Ports & Adapters) with strict SRP:

```
service/
├── Domain/                          # Business core (zero framework dependency)
│   ├── Aggregates/                  # Event-sourced aggregates (e.g. WalletAggregate)
│   ├── Entities/                    # Pure business entities
│   ├── Enums/                       # Domain enumerations
│   ├── Events/                      # Domain events (WalletEventEntry, WalletEvent)
│   ├── Ports/                       # Persistence / publishing / event store interfaces
│   ├── Presenters/                  # Presentation interface (1 class = 1 responsibility)
│   ├── Responses/                   # Output DTOs (1 file = 1 DTO)
│   └── UseCases/                    # Command + Interface + Implementation
│
└── Infrastructure/                  # Technical adapters
    ├── Config/                      # DomainConfig (use case beans) + PresentationConfig (presenter)
    ├── Consumers/                   # Kafka consumers
    ├── Controllers/                 # REST API (CQRS: CommandController + QueryController)
    ├── EventStore/                  # PostgreSQL Event Store (append-only, wallet_events table)
    ├── Mappers/                     # MapStruct: Domain <-> JPA <-> Response
    ├── Models/                      # JPA entities (projection / read model / OutboxEvent)
    ├── Presenters/                  # Presenter interface implementations
    ├── Repositories/                # JpaRepository + hexagonal adapter
    └── Schedulers/                  # OutboxRelay (polling 1s) + BatchScheduler (settlement)
```

## Repository Structure

```
digi-pay-ms-app/
├── customer-service/         → Customer MS (port 8082)
├── wallet-service/           → Wallet MS   (port 8083)
├── payment-service/          → Payment MS  (port 8084)
├── fraud-service/            → Fraud MS    (port 8085)
├── notification-service/     → Notify MS       (port 8086)
├── settlement-service/       → Settlement MS   (port 8087)
├── discovery-service/        → Eureka Server   (port 8761)
├── gateway-service/          → API Gateway     (port 8888)
├── keycloak/                 → Realm export (auto-import on boot)
├── docker-compose.yaml       → Keycloak + Kafka + Redis + 8 application services
├── e2e-fraud.sh              → E2E fraud detection scenarios
├── e2e-settlement.sh         → E2E settlement scenarios
├── e2e-gateway.sh            → E2E gateway routing scenarios
└── README.md
```

## Quick Start

### Prerequisites

- Java 21+
- Maven 3.9+
- Docker + jq

### Start everything with Docker Compose

```bash
# Build and start all services
docker compose up --build -d
```

### Get a JWT token (Keycloak)

```bash
# Get a token for user1 (USER role)
TOKEN=$(curl -s -X POST http://localhost:8080/realms/digipay/protocol/openid-connect/token \
  -d "grant_type=password" \
  -d "client_id=digipay-gateway" \
  -d "client_secret=digipay-gateway-secret" \
  -d "username=user1" \
  -d "password=password" | jq -r '.access_token')

# Call a service via the gateway with the token
curl -H "Authorization: Bearer $TOKEN" http://localhost:8888/customer-service/api/v1/customers
```

Pre-configured users:
| Username | Password | Roles |
|---|---|---|
| `user1` | `password` | USER |
| `admin1` | `password` | USER, ADMIN |

### Local start (development)

```bash
# 1. Infrastructure
docker compose up kafka redis keycloak keycloak-db -d

# 2. Discovery + Gateway
cd discovery-service && ./mvnw spring-boot:run
cd gateway-service   && ./mvnw spring-boot:run

# 3. Business services (one terminal per service)
cd customer-service      && ./mvnw spring-boot:run
cd wallet-service        && ./mvnw spring-boot:run
cd payment-service       && ./mvnw spring-boot:run
cd fraud-service         && ./mvnw spring-boot:run
cd notification-service  && ./mvnw spring-boot:run
cd settlement-service    && ./mvnw spring-boot:run
```

## E2E Scenarios

### Basic P2P flow

```bash
SUFFIX=$(date +%s)

# 1. Creer Alice
ALICE_ID=$(curl -s -X POST http://localhost:8082/api/v1/customers \
  -H "Content-Type: application/json" \
  -d "{
    \"firstName\": \"Alice\", \"lastName\": \"Dupont\",
    \"email\": \"alice.${SUFFIX}@example.com\", \"phoneNumber\": \"+336${SUFFIX}\",
    \"nationality\": \"FRA\", \"addressLine1\": \"1 Rue de la Paix\",
    \"city\": \"Paris\", \"country\": \"France\", \"preferredCurrency\": \"EUR\"
  }" | jq -r '.id')

# 2. Creer Bob
BOB_ID=$(curl -s -X POST http://localhost:8082/api/v1/customers \
  -H "Content-Type: application/json" \
  -d "{
    \"firstName\": \"Bob\", \"lastName\": \"Martin\",
    \"email\": \"bob.${SUFFIX}@example.com\", \"phoneNumber\": \"+337${SUFFIX}\",
    \"nationality\": \"FRA\", \"addressLine1\": \"2 Rue de la Paix\",
    \"city\": \"Paris\", \"country\": \"France\", \"preferredCurrency\": \"EUR\"
  }" | jq -r '.id')

# 3. Recuperer les wallets (attendre propagation Kafka)
sleep 3
ALICE_WALLET=$(curl -s "http://localhost:8083/api/v1/wallets/customer/$ALICE_ID" | jq -r '.id')
BOB_WALLET=$(curl -s "http://localhost:8083/api/v1/wallets/customer/$BOB_ID" | jq -r '.id')

# 4. Crediter Alice
curl -s -X POST "http://localhost:8083/api/v1/wallets/$ALICE_WALLET/credit?amount=50000"

# 5. Paiement normal (100 EUR → CLEARED)
curl -s -X POST http://localhost:8084/api/v1/payments \
  -H "Content-Type: application/json" \
  -d "{
    \"senderWalletId\": \"$ALICE_WALLET\",
    \"receiverWalletId\": \"$BOB_WALLET\",
    \"amount\": 100, \"currency\": \"EUR\", \"type\": \"P2P\",
    \"idempotencyKey\": \"pay-${SUFFIX}-1\"
  }" | jq '{id, status}'

# 6. Paiement bloque (15000 EUR → HIGH_AMOUNT BLOCKED)
curl -s -X POST http://localhost:8084/api/v1/payments \
  -H "Content-Type: application/json" \
  -d "{
    \"senderWalletId\": \"$ALICE_WALLET\",
    \"receiverWalletId\": \"$BOB_WALLET\",
    \"amount\": 15000, \"currency\": \"EUR\", \"type\": \"P2P\",
    \"idempotencyKey\": \"pay-${SUFFIX}-2\"
  }" | jq '{id, status}'
```

### Full E2E script (fraud detection)

```bash
chmod +x e2e-fraud.sh && ./e2e-fraud.sh
```

Covered scenarios:
- **Scenario 1**: 100 EUR payment → verdict `CLEARED`
- **Scenario 2**: 15,000 EUR payment → verdict `BLOCKED` (`HIGH_AMOUNT` rule)
- **Scenario 3**: 3+ rapid payments → verdict `REVIEW` (`VELOCITY_1MIN` rule)
- **Scenario 4**: fraud analysis history by wallet

### Full E2E script (settlement)

```bash
chmod +x e2e-settlement.sh && ./e2e-settlement.sh
```

Covered scenarios:
- **Scenario 1**: completed payment → entry captured in open batch
- **Scenario 2**: view current batch + entries + net positions
- **Scenario 3**: manual batch close → position calculation
- **Scenario 4**: idempotency — same payment does not create duplicate entry

### Validated test scenarios

| Scenario | Expected |
|---|---|
| Normal transfer | fraud verdict `CLEARED`, payment `COMPLETED` |
| Amount > 10,000 | fraud verdict `BLOCKED`, payment `FAILED` |
| > 5 tx / minute | fraud verdict `REVIEW`, high risk |
| Same `idempotencyKey` replayed | HTTP `409 Conflict` |
| Insufficient balance | payment `FAILED`, balance unchanged |

## API Endpoints

### Customer Service (port 8082)

| Method | URL | Description |
|---|---|---|
| POST | `/api/v1/customers` | Create a customer |
| GET | `/api/v1/customers` | List customers |
| GET | `/api/v1/customers/{id}` | Find by UUID |
| PUT | `/api/v1/customers/{id}` | Update |

### Wallet Service (port 8083)

| Method | URL | Description |
|---|---|---|
| GET | `/api/v1/wallets/{id}` | Find by UUID |
| GET | `/api/v1/wallets/customer/{customerId}` | Find by customer UUID |
| GET | `/api/v1/wallets/{id}/history` | Full operation history (Event Sourcing) |
| POST | `/api/v1/wallets/{id}/credit?amount=X` | Credit |
| POST | `/api/v1/wallets/{id}/debit?amount=X` | Debit |
| POST | `/api/v1/wallets/{id}/freeze?amount=X` | Freeze |

### Payment Service (port 8084)

| Method | URL | Description |
|---|---|---|
| POST | `/api/v1/payments` | Initiate a payment (Saga + idempotency) |
| GET | `/api/v1/payments/{id}` | Get by UUID |
| GET | `/api/v1/payments/wallet/{walletId}` | History by wallet |

POST request body:

```json
{
  "senderWalletId": "uuid",
  "receiverWalletId": "uuid",
  "amount": 1000,
  "currency": "EUR",
  "type": "P2P",
  "idempotencyKey": "pay-unique-ref-001"
}
```

Available types: `P2P`, `MERCHANT`, `BILL`, `WITHDRAWAL`, `DEPOSIT`

### Fraud Service (port 8085)

| Method | URL | Description |
|---|---|---|
| GET | `/api/v1/fraud-analyses/{paymentId}` | Fraud analysis by payment |
| GET | `/api/v1/fraud-analyses/customer/{customerId}` | Fraud history by customer |

### Notification Service (port 8086)

| Method | URL | Description |
|---|---|---|
| GET | `/api/v1/notifications/wallet/{walletId}` | Notifications by wallet |
| GET | `/api/v1/notifications/payment/{paymentId}` | Notifications by payment |

### Settlement Service (port 8087)

| Method | URL | Description |
|---|---|---|
| GET | `/api/settlements/batches` | List all batches |
| GET | `/api/settlements/batches/current?currency=XAF` | Current open batch |
| GET | `/api/settlements/batches/{id}` | Get batch by UUID |
| GET | `/api/settlements/batches/{id}/entries` | Batch entries (captured payments) |
| GET | `/api/settlements/batches/{id}/positions` | Batch net positions |
| POST | `/api/settlements/batches/open` | Manually open a batch |
| POST | `/api/settlements/batches/{id}/close` | Manually close a batch |

### Discovery, Gateway & Security

| Service | Port | Description |
|---|---|---|
| Eureka Server | 8761 | Service registry (dashboard: http://localhost:8761) |
| API Gateway | 8888 | Single entry point, dynamic routing via Eureka, JWT validation, Redis rate limiting |
| Keycloak | 8080 | Identity Provider (OIDC), realm `digipay`, roles USER/ADMIN |
| Jaeger | 16686 | Distributed tracing — OpenTelemetry traces from all services |

### Useful URLs

| Service | URL |
|---|---|
| Customer API | http://localhost:8082/api/v1/customers |
| Wallet API | http://localhost:8083/api/v1/wallets |
| Payment API | http://localhost:8084/api/v1/payments |
| Fraud API | http://localhost:8085/api/v1/fraud-analyses |
| Notification API | http://localhost:8086/api/v1/notifications |
| Settlement API | http://localhost:8087/api/settlements/batches |
| Eureka Dashboard | http://localhost:8761 |
| Gateway | http://localhost:8888 |
| Keycloak Admin Console | http://localhost:8080 (admin / admin) |
| Keycloak Token Endpoint | http://localhost:8080/realms/digipay/protocol/openid-connect/token |
| Jaeger UI (tracing) | http://localhost:16686 |
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3000 (admin / admin) |

## Kafka Communication Between Services

```
 ┌──────────────────┐
 │  Customer Service│  POST /api/v1/customers
 │    (port 8082)   │
 └────────┬─────────┘
          │ [customer-events] customer.created
          ▼
 ┌──────────────────┐                        ┌──────────────────────────────┐
 │  Wallet Service  │◀──[wallet-commands]────│      Payment Service         │
 │    (port 8083)   │   DEBIT_WALLET         │        (port 8084)           │
 │                  │   CREDIT_WALLET        │                              │
 │  EVENT SOURCING  │   COMPENSATE_DEBIT     │  Saga Orchestration          │
 │                  │                        │  Idempotency Redis (TTL 24h) │
 │  wallet_events   │──[wallet-saga-events]─▶│  Statuts: INITIATED →        │
 │  (append-only)   │   DEBIT_SUCCESS        │  FRAUD_CHECK → PROCESSING →  │
 │  ↓ projection    │   CREDIT_SUCCESS       │  COMPLETED / FAILED          │
 │  wallets (read)  │   CREDIT_FAILED        └──────────────┬───────────────┘
 │                  │                                        │
 │  Auto-cree le    │                                        │
 │  wallet a la     │                                        │
 │  reception de    │                                        │
 │  customer.created│                                        │
 │                  │                                        │
 │  GET /history →  │                                        │
 │  replay events   │                                        │
 └──────────────────┘                                        │ [payment-events]
                                             ┌──────────────┴───────────────┐
                                             │                              │
                              ┌──────────────▼──────────┐   ┌──────────────▼──────────┐
                              │     Fraud Service        │   │  Notification Service   │
                              │      (port 8085)         │   │      (port 8086)        │
                              │                          │   │                         │
                              │  7 regles actives        │   │  Consomme:              │
                              │  Score de risque 0-100   │   │  - payment-events       │
                              │  Verdict: CLEARED /      │   │    (initiated/completed │
                              │  REVIEW / FLAGGED /      │   │     /failed)            │
                              │  BLOCKED                 │   │  - fraud-check-events   │
                              └──────────────┬───────────┘   │    (blocked/review)     │
                                             │ [fraud-check-events]                    │
                                             │ fraud.cleared / blocked / review        │
                                             ▼                                         │
                              ┌──────────────────────────┐   │                         │
                              │     Payment Service       │◀──┘                         │
                              │  onFraudCleared →        │   [notification sauvegardee │
                              │  continue la Saga        │    en base, statut SENT]    │
                              │  onFraudBlocked →        │                             │
                              │  paiement FAILED         │   └─────────────────────────┘
                              └──────────────────────────┘

                                         [payment-events] payment.completed
                                                            │
                                             ┌──────────────▼──────────┐
                                             │   Settlement Service     │
                                             │      (port 8087)         │
                                             │                          │
                                             │  EVENT SOURCING          │
                                             │  batch_events            │
                                             │  (append-only)           │
                                             │                          │
                                             │  Consomme:               │
                                             │  - payment.completed     │
                                             │    → capture entry       │
                                             │                          │
                                             │  Scheduler horaire:      │
                                             │  close → calculate net   │
                                             │  positions → apply →     │
                                             │  complete batch          │
                                             │                          │
                                             │  Publie:                 │
                                             │  - settlement.completed  │
                                             │  - settlement.failed     │
                                             │  → [settlement-events]   │
                                             └──────────────────────────┘
```

## Tests

```bash
# Run all tests for a service
cd customer-service      && ./mvnw test
cd wallet-service        && ./mvnw test
cd payment-service       && ./mvnw test
cd fraud-service         && ./mvnw test
cd notification-service  && ./mvnw test
cd settlement-service    && ./mvnw test
```

| Service | Tests | Coverage |
|---|---|---|
| customer-service | 18 | Use cases (create, find, update) + CommandController (2) + QueryController (2) + Integration (1) |
| wallet-service | 32 | WalletAggregate (8) + Event-sourced use cases (14) + CommandController (5) + QueryController (2) + History (2) + Integration (1) |
| payment-service | 24 | Use cases (3) + Saga (7) + Find (4) + CommandController (3) + QueryController (3) + CircuitBreaker (3) + Integration (1) |
| fraud-service | 24 | FraudRulesEngine (13) + AnalyzePaymentUseCase (5) + QueryController (5) + ApplicationContext (1) |
| notification-service | 12 | SendNotificationUseCase (6) + QueryController (5) + ApplicationContext (1) |
| settlement-service | 31 | SettlementBatchAggregate (12) + Use cases (9) + CommandController (1) + QueryController (4) + Consumer (4) + ApplicationContext (1) |
| gateway-service | 7 | SecurityConfig (actuator public, 401 without token, mock JWT authorized, register public) + ApplicationContext (1) + RateLimiterConfig (bean present, anonymous → IP) |
| **Total** | **148** | |

## Roadmap

| Phase | Deliverables | Status |
|---|---|---|
| Phase 1 | Docker + Kafka infrastructure | Done |
| Phase 2 | Customer MS + Wallet MS + Kafka events + Tests | Done |
| Phase 3 | Payment MS + Saga Pattern + Redis Idempotency + E2E Tests | Done |
| Phase 4 | Fraud Detection MS + Notification MS | Done |
| Phase 5 | Discovery Service (Eureka) + API Gateway (Spring Cloud Gateway) | Done |
| Phase 6 | Settlement MS v1 (simple compensation, net position) | Done |
| Phase 7 | Event Sourcing (Wallet MS + Settlement MS rewrite) + CI/CD GitLab | Done |
| Phase 8 | Settlement MS v2: multilateral compensation, hourly batches, Event Sourcing, scheduler, 31 tests | Done |
| Phase 9 | Outbox Pattern (transactional DB → Kafka guarantee, at-least-once, 5 services) | Done |
| Phase 10 | CQRS (separate Command/Query controllers, 6 services) | Done |
| Phase 11 | JWT/Keycloak security via API Gateway (OAuth2 Resource Server, auto-import realm, 5 tests) | Done |
| Phase 12a | Observability: Prometheus + Grafana (real-time metrics, 8 services scraped, dashboards) | Done |
| Phase 12b | Resilience: Circuit Breaker + Retry Resilience4j on OutboxRelay (3 tests) | Done |
| Phase 12c | Distributed tracing: OpenTelemetry + Jaeger (trace IDs propagated across 8 services, OTLP) | Done |
| Phase 12d | RFC 7807 Problem Details: standardized error format across 6 business services | Done |
| Phase 12e | PostgreSQL migration: 5 business services (Flyway, dev/prod profiles, versioned schema) | Done |
| Phase 12f | Distributed rate limiting: Token Bucket Redis on gateway (10 req/s, JWT sub or IP, HTTP 429) | Done |
| Phase 13 | OpenAPI / Swagger: automatic API documentation | Upcoming |

## Planned Deep-Dives

- Exactly-Once Semantics Kafka (`isolation.level = read_committed`)
- Outbox relay migration → Debezium CDC (PostgreSQL WAL capture, zero polling)
- GDPR Compliance (PII encryption, right to be forgotten in topics)
- gRPC between services (high-performance synchronous queries)
- Kubernetes (Helm charts, HPA, PodDisruptionBudget)
- PCI-DSS basics (card data tokenization, immutable audit logs)
