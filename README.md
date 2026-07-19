# Digital Payment & Settlement Platform

> Architecture Microservices | Event-Driven | Kafka | Fintech

Plateforme de paiement electronique simulant le cycle de vie complet d'une transaction financiere : de la creation du compte client jusqu'au reglement interbancaire, en passant par la detection de fraude et les notifications en temps reel.

## Contexte

| | |
|---|---|
| **Domaine** | Fintech / Banking / Paiement electronique |
| **Type** | Projet personnel — Portfolio technique senior |
| **Niveau** | Senior / Expert |
| **Stack principale** | Spring Boot 4 · Kafka · PostgreSQL · Redis · Docker |
| **Patterns cles** | Event-Driven · CQRS · Saga · Event Sourcing · DDD |

**References metier** : Visa/Mastercard (clearing), Flutterwave/Paystack (paiements Afrique), Stripe (APIs), CinetPay/Wave (mobile money).

## Fonctionnalites metier

- Creation et gestion de comptes clients avec processus KYC
- Portefeuilles electroniques avec gestion des soldes et gel de fonds
- Paiements marchands et transferts peer-to-peer avec Saga distribue
- Idempotency garantie sur les paiements via Redis
- Reglement interbancaire avec calcul de position nette
- Detection de fraude en temps reel (7 regles configurables, score de risque 0-100)
- Notifications temps reel : paiement initie, complete, echoue, fraude detectee

## Architecture generale

```
                        [ API Gateway ]
                              |
       [ Customer MS ] [ Wallet MS ] [ Payment MS ]
             |               |              |
     ════════════════[ KAFKA CLUSTER ]═══════════════
             |               |              |
       [ Fraud MS ]   [ Notify MS ]  [ Settlement MS ]
```

**Principes** : Loose Coupling (communication par evenements) · Database per Service (H2 dev / PostgreSQL prod) · API Gateway unique · High Cohesion.

## Microservices & Contrats d'Evenements

| Microservice | Port | Responsabilites | Publie | Consomme |
|---|---|---|---|---|
| **Customer MS** | 8082 | Creation client · KYC · Infos personnelles | `customer.created` | — |
| **Wallet MS** | 8083 | Portefeuille · Event Sourcing · Solde · Gel de fonds | `wallet.created` `wallet.credited` `wallet.debited` | `customer.created` · commandes Saga |
| **Payment MS** | 8084 | Paiements P2P/marchands · Saga orchestration · Idempotency Redis | `payment.initiated` `payment.completed` `payment.failed` `payment.reversed` `payment.compensation_failed` | reponses Saga wallet · `fraud-check-events` |
| **Fraud MS** | 8085 | Regles anti-fraude · Score de risque · Alertes | `fraud.cleared` `fraud.blocked` `fraud.review` | `payment.initiated` |
| **Notification MS** | 8086 | Notifications en temps reel (email/SMS/push simules) | — | `payment.initiated` `payment.completed` `payment.failed` `fraud.blocked` `fraud.review` |
| **Settlement MS** | 8087 | Compensation interbancaire · Position nette · Idempotency par paymentId | `settlement.completed` `settlement.failed` | `payment.completed` |

## Configuration Kafka

| Topic | Consumer Group(s) | Usage |
|---|---|---|
| `customer-events` | wallet-group | Cycle de vie client |
| `wallet-commands` | wallet-saga-group | Commandes Saga (DEBIT, CREDIT, COMPENSATE_DEBIT) |
| `wallet-events` | — | Evenements wallet (created, credited, debited) |
| `wallet-saga-events` | payment-saga-group | Reponses Saga wallet (SUCCESS, FAILURE) |
| `payment-events` | fraud-group · notification-payment-group · settlement-group | Flux de paiement central |
| `fraud-check-events` | payment-fraud-group · notification-fraud-group | Verdict fraude (cleared / blocked / review) |
| `settlement-events` | — | Resultat du reglement (completed / failed) |

## Patterns & Concepts avances

| Pattern | Application dans ce projet |
|---|---|
| **Event Sourcing** | Wallet MS : l'etat du portefeuille est reconstruit depuis les evenements (table append-only `wallet_events`). Le solde n'est jamais modifie directement — il est calcule en rejouant WALLET_CREATED → WALLET_CREDITED → WALLET_DEBITED → AMOUNT_FROZEN. Endpoint `/history` pour consulter toutes les operations. |
| **Saga (Orchestration)** | Payment MS orchestrate DEBIT → FRAUD_CHECK → CREDIT → COMPLETE; compensation automatique si une etape echoue |
| **Idempotency** | Cle d'idempotency stockee dans Redis (TTL 24h) ; doublon → HTTP 409 sans re-traitement |
| **Clean Architecture** | Hexagonal (Ports & Adapters) : domaine pur sans dependance framework, use cases isoles |
| **Presenter Pattern** | Interface domaine + implementation infrastructure ; le controller ne connait que le domaine |
| **DDD** | Customer / Wallet / Payment / Fraud / Notification / Settlement = bounded contexts independants |
| **Event-Driven** | Tous les services communiquent exclusivement via Kafka ; zero appel synchrone inter-service |

### Saga Pattern — Flux de transfert P2P avec detection fraude

```
Payment MS
  1. Verifie idempotency key (Redis) → 409 si doublon
  2. Sauvegarde paiement (INITIATED)
  3. Publie payment.initiated sur payment-events
  4. Demarre etape FRAUD_CHECK → paiement passe en statut FRAUD_CHECK

Fraud MS
  5. Recoit payment.initiated, evalue les 7 regles actives
  5a. Score 0-30 → publie fraud.cleared sur fraud-check-events
  5b. Score 31-80 → publie fraud.review
  5c. Score > 80 ou regle BLOCK → publie fraud.blocked

Payment MS
  6a. (fraud.cleared) → envoie DEBIT_WALLET sur wallet-commands
  6b. (fraud.blocked) → paiement FAILED, fin

Wallet MS
  7a. Debit Alice reussi → publie DEBIT_SUCCESS
  7b. Debit Alice echoue → publie DEBIT_FAILED

Payment MS
  8a. (succes) → CREDIT_WALLET vers Bob
  8b. (echec) → paiement FAILED

Wallet MS
  9a. Credit Bob reussi → publie CREDIT_SUCCESS → paiement COMPLETED
  9b. Credit Bob echoue → publie CREDIT_FAILED → COMPENSATE_DEBIT → paiement REVERSED
  9c. Si compensation echoue → paiement COMPENSATION_FAILED

Notification MS
  (en parallele) Recoit payment.initiated / completed / failed / fraud.blocked
  → sauvegarde la notification en base avec statut SENT

Settlement MS
  10. Recoit payment.completed sur payment-events (group: settlement-group)
  11. Verifie idempotency (paymentId deja traite → skip)
  12. Cree un settlement (status PROCESSING) + 2 entries (DEBIT sender, CREDIT receiver)
  13. Calcule la position nette, passe en COMPLETED
  14. Publie settlement.completed sur settlement-events
```

## Regles de detection de fraude

| Code | Condition | Score | Action | Priorite |
|---|---|---|---|---|
| `HIGH_AMOUNT` | Montant > 10 000 | 85 | BLOCK | CRITICAL |
| `VELOCITY_1MIN` | >= 3 tx / minute / compte | 40 | REVIEW | HIGH |
| `VELOCITY_1H` | >= 10 tx / heure / compte | 25 | FLAG | MEDIUM |
| `RISKY_COUNTRY_KP` | Pays = KP (Coree du Nord) | 90 | BLOCK | CRITICAL |
| `RISKY_COUNTRY_IR` | Pays = IR (Iran) | 90 | BLOCK | CRITICAL |
| `NEW_DEVICE` | Nouveau device (non reconnu) | 20 | CHALLENGE_OTP | MEDIUM |
| `SUSPICIOUS_HOUR` | Heure entre 0h-5h UTC | 15 | FLAG | LOW |

**Score → Verdict** : 0-30 = CLEARED · 31-60 = REVIEW · 61-80 = FLAGGED · 81-100 = BLOCKED

## Stack technique

| Domaine | Technologie |
|---|---|
| Backend | Spring Boot 4.1.0 (Java 21) |
| Messaging | Apache Kafka 3.9+ (KRaft mode) |
| Base de donnees | H2 in-memory (dev/test) |
| Cache / Idempotency | Redis 7 |
| Mapping objets | MapStruct 1.5.5 |
| Tests | JUnit 5, Mockito, @WebMvcTest |
| Conteneurisation | Docker + Docker Compose |
| Service Discovery | Spring Cloud Netflix Eureka |
| API Gateway | Spring Cloud Gateway (reactive, route dynamique via Eureka) |
| Observabilite | Prometheus + Zipkin (prevu Phase 7) |

## Architecture logicielle (par service)

Chaque microservice suit une **architecture hexagonale** (Clean Architecture / Ports & Adapters) avec SRP strict :

```
service/
├── Domain/                          # Coeur metier (zero dependance framework)
│   ├── Aggregates/                  # Event-sourced aggregates (ex: WalletAggregate)
│   ├── Entities/                    # Entites metier pures
│   ├── Enums/                       # Enumerations du domaine
│   ├── Events/                      # Evenements domaine (WalletEventEntry, WalletEvent)
│   ├── Ports/                       # Interfaces de persistance / publication / event store
│   ├── Presenters/                  # Interface de presentation (1 classe = 1 responsabilite)
│   ├── Responses/                   # DTOs de sortie (1 fichier = 1 DTO)
│   └── UseCases/                    # Command + Interface + Implementation
│
└── Infrastructure/                  # Adaptateurs techniques
    ├── Config/                      # DomainConfig (beans use cases) + PresentationConfig (presenter)
    ├── Consumers/                   # Kafka consumers
    ├── Controllers/                 # API REST
    ├── EventStore/                  # Event Store PostgreSQL (append-only, table wallet_events)
    ├── Mappers/                     # MapStruct : Domain <-> JPA <-> Response
    ├── Models/                      # Entites JPA (projection / read model)
    ├── Presenters/                  # Implementation des interfaces presenter
    └── Repositories/                # JpaRepository + adaptateur hexagonal
```

## Structure du repository

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
├── docker-compose.yaml       → Kafka + Redis + 7 services applicatifs
├── e2e-fraud.sh              → Scenarios E2E fraud detection
├── e2e-settlement.sh         → Scenarios E2E settlement
├── e2e-gateway.sh            → Scenarios E2E gateway routing
└── README.md
```

## Demarrage rapide

### Prerequis

- Java 21+
- Maven 3.9+
- Docker + jq

### Tout demarrer via Docker Compose

```bash
# Build et demarrage de tous les services
docker compose up --build -d
```

### Demarrage local (developpement)

```bash
# 1. Infrastructure
docker compose up kafka redis -d

# 2. Discovery + Gateway
cd discovery-service && ./mvnw spring-boot:run
cd gateway-service   && ./mvnw spring-boot:run

# 3. Services metier (un terminal par service)
cd customer-service      && ./mvnw spring-boot:run
cd wallet-service        && ./mvnw spring-boot:run
cd payment-service       && ./mvnw spring-boot:run
cd fraud-service         && ./mvnw spring-boot:run
cd notification-service  && ./mvnw spring-boot:run
cd settlement-service    && ./mvnw spring-boot:run
```

## Scenarios E2E

### Flux de base P2P

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

### Script E2E complet (fraud detection)

```bash
chmod +x e2e-fraud.sh && ./e2e-fraud.sh
```

Scenarios couverts :
- **Scenario 1** : paiement 100 EUR → verdict `CLEARED`
- **Scenario 2** : paiement 15 000 EUR → verdict `BLOCKED` (regle `HIGH_AMOUNT`)
- **Scenario 3** : 3+ paiements rapides → verdict `REVIEW` (regle `VELOCITY_1MIN`)
- **Scenario 4** : historique fraud analyses par wallet

### Script E2E complet (settlement)

```bash
chmod +x e2e-settlement.sh && ./e2e-settlement.sh
```

Scenarios couverts :
- **Scenario 1** : paiement completed → settlement cree avec status `COMPLETED`
- **Scenario 2** : consultation settlement par ID
- **Scenario 3** : second paiement → second settlement
- **Scenario 4** : idempotency — meme paiement ne cree pas de doublon settlement

### Scenarios de test valides

| Scenario | Attendu |
|---|---|
| Transfert normal | fraud verdict `CLEARED`, paiement `COMPLETED` |
| Montant > 10 000 | fraud verdict `BLOCKED`, paiement `FAILED` |
| > 5 tx / minute | fraud verdict `REVIEW`, risque eleve |
| Meme `idempotencyKey` rejoue | HTTP `409 Conflict` |
| Solde insuffisant | paiement `FAILED`, balance inchangee |

## Endpoints API

### Customer Service (port 8082)

| Methode | URL | Description |
|---|---|---|
| POST | `/api/v1/customers` | Creer un client |
| GET | `/api/v1/customers` | Lister les clients |
| GET | `/api/v1/customers/{id}` | Trouver par UUID |
| PUT | `/api/v1/customers/{id}` | Mettre a jour |

### Wallet Service (port 8083)

| Methode | URL | Description |
|---|---|---|
| GET | `/api/v1/wallets/{id}` | Trouver par UUID |
| GET | `/api/v1/wallets/customer/{customerId}` | Trouver par UUID client |
| GET | `/api/v1/wallets/{id}/history` | Historique complet des operations (Event Sourcing) |
| POST | `/api/v1/wallets/{id}/credit?amount=X` | Crediter |
| POST | `/api/v1/wallets/{id}/debit?amount=X` | Debiter |
| POST | `/api/v1/wallets/{id}/freeze?amount=X` | Geler |

### Payment Service (port 8084)

| Methode | URL | Description |
|---|---|---|
| POST | `/api/v1/payments` | Initier un paiement (Saga + idempotency) |
| GET | `/api/v1/payments/{id}` | Consulter par UUID |
| GET | `/api/v1/payments/wallet/{walletId}` | Historique par wallet |

Corps de la requete POST :

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

Types disponibles : `P2P`, `MERCHANT`, `BILL`, `WITHDRAWAL`, `DEPOSIT`

### Fraud Service (port 8085)

| Methode | URL | Description |
|---|---|---|
| GET | `/api/v1/fraud-analyses/{paymentId}` | Analyse fraude par paiement |
| GET | `/api/v1/fraud-analyses/customer/{customerId}` | Historique fraude par client |

### Notification Service (port 8086)

| Methode | URL | Description |
|---|---|---|
| GET | `/api/v1/notifications/wallet/{walletId}` | Notifications par wallet |
| GET | `/api/v1/notifications/payment/{paymentId}` | Notifications par paiement |

### Settlement Service (port 8087)

| Methode | URL | Description |
|---|---|---|
| GET | `/api/settlements` | Lister tous les settlements |
| GET | `/api/settlements/{id}` | Consulter un settlement par UUID |

### Discovery & Gateway

| Service | Port | Description |
|---|---|---|
| Eureka Server | 8761 | Service registry (dashboard: http://localhost:8761) |
| API Gateway | 8888 | Point d'entree unique, routage dynamique via Eureka |

### URLs utiles

| Service | URL |
|---|---|
| Customer API | http://localhost:8082/api/v1/customers |
| Wallet API | http://localhost:8083/api/v1/wallets |
| Payment API | http://localhost:8084/api/v1/payments |
| Fraud API | http://localhost:8085/api/v1/fraud-analyses |
| Notification API | http://localhost:8086/api/v1/notifications |
| Customer H2 Console | http://localhost:8082/h2-console |
| Wallet H2 Console | http://localhost:8083/h2-console |
| Payment H2 Console | http://localhost:8084/h2-console |
| Fraud H2 Console | http://localhost:8085/h2-console |
| Notification H2 Console | http://localhost:8086/h2-console |
| Settlement API | http://localhost:8087/api/settlements |
| Settlement H2 Console | http://localhost:8087/h2-console |
| Eureka Dashboard | http://localhost:8761 |
| Gateway | http://localhost:8888 |

## Communication Kafka entre services

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
                                             │  Consomme:               │
                                             │  - payment.completed     │
                                             │                          │
                                             │  Cree settlement +       │
                                             │  2 entries (DEBIT/CREDIT)│
                                             │  Idempotency paymentId   │
                                             │                          │
                                             │  Publie:                 │
                                             │  - settlement.completed  │
                                             │  - settlement.failed     │
                                             │  → [settlement-events]   │
                                             └──────────────────────────┘
```

## Tests

```bash
# Lancer tous les tests d'un service
cd customer-service      && ./mvnw test
cd wallet-service        && ./mvnw test
cd payment-service       && ./mvnw test
cd fraud-service         && ./mvnw test
cd notification-service  && ./mvnw test
cd settlement-service    && ./mvnw test
```

| Service | Tests | Couverture |
|---|---|---|
| customer-service | 19 | Use cases (create, find, update) + Integration controller |
| wallet-service | 32 | WalletAggregate (8) + Use cases event-sourced (14) + Controller (7) + History (2) + Integration (1) |
| payment-service | 21 | Use cases (3) + Saga (7) + Find (4) + Controller (6) + WebMvc (1) |
| fraud-service | 24 | FraudRulesEngine (13) + AnalyzePaymentUseCase (5) + FraudController (5) + ApplicationContext (1) |
| notification-service | 12 | SendNotificationUseCase (6) + NotificationController (5) + ApplicationContext (1) |
| settlement-service | 14 | ProcessPaymentSettlementUseCase (5) + SettlementController (4) + PaymentEventConsumer (4) + ApplicationContext (1) |
| **Total** | **124** | |

## Roadmap

| Phase | Livrables | Statut |
|---|---|---|
| Phase 1 | Infrastructure Docker + Kafka | Termine |
| Phase 2 | Customer MS + Wallet MS + Kafka events + Tests | Termine |
| Phase 3 | Payment MS + Saga Pattern + Redis Idempotency + Tests E2E | Termine |
| Phase 4 | Fraud Detection MS + Notification MS | Termine |
| Phase 5 | Discovery Service (Eureka) + API Gateway (Spring Cloud Gateway) | Termine |
| Phase 6 | Settlement MS (compensation interbancaire, position nette, 14 tests) | Termine |
| Phase 7 | Event Sourcing (Wallet MS) + CI/CD GitLab | Termine |
| Phase 8 | CQRS + Observabilite (Prometheus + Zipkin) | A venir |

## Approfondissements prevus (Senior+)

- Exactly-Once Semantics Kafka (`isolation.level = read_committed`)
- Outbox Pattern avec Debezium (CDC pour garantie transactionnelle DB → Kafka)
- GDPR Compliance (chiffrement PII, droit a l'oubli dans les topics)
- gRPC entre services (queries synchrones haute-performance)
- Kubernetes (Helm charts, HPA, PodDisruptionBudget)
- PCI-DSS basics (tokenisation donnees carte, audit logs immuables)
