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
- Detection de fraude en temps reel (regles configurables)
- Notifications multi-canal (Email, SMS, Push)

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

**Principes** : Loose Coupling (communication par evenements) · Database per Service (PostgreSQL par MS + Redis partage) · API Gateway unique · High Cohesion.

## Microservices & Contrats d'Evenements

| Microservice | Responsabilites | Publie (Events) | Consomme (Events) |
|---|---|---|---|
| **Customer MS** | Creation client · KYC · Infos personnelles | `customer.created` `customer.updated` `customer.verified` | — |
| **Wallet MS** | Portefeuille · Solde · Gel de fonds | `wallet.created` `wallet.credited` `wallet.debited` `wallet.amount_frozen` | `customer.created` · commandes Saga |
| **Payment MS** | Paiements P2P/marchands · Saga orchestration · Idempotency | `payment.initiated` `payment.completed` `payment.failed` | reponses Saga wallet |
| **Fraud Detection MS** | Regles anti-fraude · Score de risque · Alertes | `fraud.detected` `fraud.cleared` | `payment.initiated` |
| **Notification MS** | Email · SMS · Push | — | `payment.completed` `payment.failed` `wallet.credited` |
| **Settlement MS** | Compensation interbancaire · Position nette | `settlement.completed` `settlement.failed` | `payment.completed` |

## Configuration Kafka

| Topic | Partitions | Consumer Group(s) | Usage |
|---|---|---|---|
| `customer-events` | 3 | wallet-group | Cycle de vie client, KYC |
| `wallet-commands` | 3 | wallet-group | Commandes Saga vers wallet (DEBIT, CREDIT, COMPENSATE_DEBIT) |
| `wallet-saga-events` | 3 | payment-group | Reponses Saga du wallet (SUCCESS, FAILURE) |
| `payment-events` | 3 | fraud-group · notification-group · settlement-group | Flux de paiement central |
| `fraud-events` | 2 | payment-group · notification-group | Alertes fraude temps reel |
| `settlement-events` | 2 | reporting-group | Compensation & reglement |
| `notification-events` | 2 | notification-group | Declenchement notifications |

## Patterns & Concepts avances

| Pattern | Application dans ce projet |
|---|---|
| **Saga (Choreography)** | Payment MS orchestrate DEBIT→CREDIT→COMPLETE; compensation automatique si CREDIT echoue |
| **Idempotency** | Cle d'idempotency stockee dans Redis (TTL 24h) ; doublon → HTTP 409 sans re-traitement |
| **Event Sourcing** | Wallet : +1000 / +2000 / -500 → solde calcule a la volee depuis wallet-events |
| **CQRS** | Command: initier paiement · Query: historique transactions via read model dedie |
| **DDD** | Customer / Wallet / Payment / Fraud / Settlement = bounded contexts independants |
| **Clean Architecture** | Entites domaine pures sans dependance framework, use cases isoles |
| **Outbox Pattern** | `DomainOutboxEvent` garantit coherence entre sauvegarde DB et publication Kafka |
| **Circuit Breaker** | Resilience4j : open/closed/half-open par service |

### Saga Pattern — Flux de transfert P2P

```
Transfert 1000 XOF de Alice (wallet A) vers Bob (wallet B) :

Payment MS
  1. Verifie idempotency key (Redis) → 409 si doublon
  2. Sauvegarde paiement (INITIATED) → stocke cle idempotency
  3. Publie payment.initiated sur payment-events
  4. Envoie commande DEBIT_WALLET sur wallet-commands

Wallet MS
  5a. Debit Alice reussi → publie DEBIT_SUCCESS sur wallet-saga-events
  5b. Debit Alice echoue (solde insuffisant) → publie DEBIT_FAILED

Payment MS
  6a. (succes) Envoie commande CREDIT_WALLET sur wallet-commands
  6b. (echec) → paiement FAILED, fin

Wallet MS
  7a. Credit Bob reussi → publie CREDIT_SUCCESS sur wallet-saga-events
  7b. Credit Bob echoue → publie CREDIT_FAILED

Payment MS
  8a. (succes) → paiement COMPLETED, publie payment.completed
  8b. (echec) Envoie COMPENSATE_DEBIT → re-credit Alice → paiement REVERSED
```

## Regles de detection de fraude

| Regle | Condition | Action | Priorite |
|---|---|---|---|
| Velocite transactions | > 5 tx / minute / compte | Blocage temporaire | HAUTE |
| Montant eleve | Paiement > 10 000$ | Revue manuelle KYC | HAUTE |
| Pays a risque | Pays blacklistes FATF | Rejet automatique | CRITIQUE |
| Nouveau device | 1er device + > 5000$ | Blocage 24h + notification | MOYENNE |
| Heure inhabituelle | 2h-5h UTC + > 1000$ | Challenge OTP | MOYENNE |

## Stack technique complete

| Domaine | Technologie |
|---|---|
| Backend | Spring Boot 4.1.0 (Java 21) |
| Messaging | Apache Kafka 3.9+ |
| Service Discovery | Eureka (Spring Cloud) |
| API Gateway | Spring Cloud Gateway |
| Configuration | Spring Cloud Config |
| Base de donnees | PostgreSQL (par service) · H2 (dev/test) |
| Cache / Idempotency | Redis 7 |
| Observabilite | Prometheus + Grafana |
| Tracing distribue | Zipkin / Micrometer |
| Documentation API | SpringDoc OpenAPI / Swagger UI |
| Conteneurisation | Docker + Docker Compose |
| Mapping objets | MapStruct 1.5.5 |
| Resilience | Resilience4j |
| Tests | JUnit 5, Mockito, Testcontainers |

## Architecture logicielle (par service)

Chaque microservice suit une **architecture hexagonale** (Clean Architecture / Ports & Adapters) :

```
service/
├── Domain/                          # Coeur metier (zero dependance framework)
│   ├── Entities/                    # Entites metier pures
│   ├── Enums/                       # Enumerations du domaine
│   ├── Events/                      # Evenements domaine (Kafka DTOs)
│   ├── Ports/                       # Interfaces de services (ports de sortie)
│   ├── Gateways/                    # Abstractions de persistance
│   ├── Presenters/                  # Interfaces de presentation
│   ├── Responses/                   # DTOs de sortie
│   ├── UseCases/                    # Cas d'utilisation (logique metier)
│   └── Validations/                 # Validation metier custom
│
└── Infrastructure/                  # Adaptateurs techniques
    ├── Adapters/                    # Implementations des ports (Service, EventPublisher)
    ├── Config/                      # Configuration Spring (beans use cases)
    ├── Consumers/                   # Kafka consumers
    ├── Controllers/                 # API REST
    ├── Mappers/                     # Mapping Domain <-> JPA (MapStruct)
    ├── Models/                      # Entites JPA
    ├── Presenters/                  # Implementations des presenters
    ├── Repositories/                # Implementations de persistance
    └── Requests/                    # DTOs d'entree (validation Jakarta)
```

## Structure du repository

```
digi-pay-ms-app/
├── customer-service/         → Customer MS (port 8082)
├── wallet-service/           → Wallet MS (port 8083)
├── payment-service/          → Payment MS (port 8084)
├── docker-compose.yaml       → Kafka + Redis + services
└── README.md
```

## Demarrage rapide

### Prerequis

- Java 21+
- Maven 3.9+
- Docker

### 1. Demarrer l'infrastructure (Kafka + Redis)

```bash
docker compose up kafka redis -d
```

### 2. Lancer les services

```bash
# Terminal 1
cd customer-service && mvn spring-boot:run

# Terminal 2
cd wallet-service && mvn spring-boot:run

# Terminal 3
cd payment-service && mvn spring-boot:run
```

Ou tout demarrer via Docker Compose :

```bash
docker compose up --build -d
```

### 3. Tester le flux complet E2E

Tous les IDs (customer, wallet, payment) sont des **UUID**.

```bash
# 1. Creer Alice — capturer son UUID
ALICE_ID=$(curl -s -X POST http://localhost:8082/api/v1/customers \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Alice", "lastName": "Dupont",
    "email": "alice@example.com", "phoneNumber": "+22890000001",
    "nationality": "TGO", "addressLine1": "1 Rue de Lome",
    "city": "Lome", "country": "Togo", "preferredCurrency": "XOF"
  }' | jq -r '.id')
echo "Alice ID: $ALICE_ID"

# 2. Creer Bob — capturer son UUID
BOB_ID=$(curl -s -X POST http://localhost:8082/api/v1/customers \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Bob", "lastName": "Martin",
    "email": "bob@example.com", "phoneNumber": "+22890000002",
    "nationality": "TGO", "addressLine1": "2 Rue de Lome",
    "city": "Lome", "country": "Togo", "preferredCurrency": "XOF"
  }' | jq -r '.id')
echo "Bob ID: $BOB_ID"

# 3. Recuperer les UUIDs des wallets (attendre 2-3s propagation Kafka)
sleep 3
ALICE_WALLET=$(curl -s "http://localhost:8083/api/v1/wallets/customer/$ALICE_ID" | jq -r '.id')
BOB_WALLET=$(curl -s "http://localhost:8083/api/v1/wallets/customer/$BOB_ID" | jq -r '.id')
echo "Alice Wallet: $ALICE_WALLET"
echo "Bob Wallet:   $BOB_WALLET"

# 4. Crediter Alice
curl -s -X POST "http://localhost:8083/api/v1/wallets/$ALICE_WALLET/credit?amount=3000"

# 5. Transfert P2P Alice → Bob (Saga)
curl -s -X POST http://localhost:8084/api/v1/payments \
  -H "Content-Type: application/json" \
  -d "{
    \"senderWalletId\": \"$ALICE_WALLET\",
    \"receiverWalletId\": \"$BOB_WALLET\",
    \"amount\": 1000,
    \"currency\": \"XOF\",
    \"type\": \"P2P\",
    \"idempotencyKey\": \"pay-001\"
  }" | jq .

# 6. Verifier balances (Alice: 2000, Bob: 1000)
curl -s "http://localhost:8083/api/v1/wallets/$ALICE_WALLET" | jq '.balance'
curl -s "http://localhost:8083/api/v1/wallets/$BOB_WALLET" | jq '.balance'

# 7. Tester l'idempotency (meme cle → 409)
curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8084/api/v1/payments \
  -H "Content-Type: application/json" \
  -d "{
    \"senderWalletId\": \"$ALICE_WALLET\",
    \"receiverWalletId\": \"$BOB_WALLET\",
    \"amount\": 1000,
    \"currency\": \"XOF\",
    \"type\": \"P2P\",
    \"idempotencyKey\": \"pay-001\"
  }"
# Attendu : 409

# 8. Tester le solde insuffisant
curl -s -X POST http://localhost:8084/api/v1/payments \
  -H "Content-Type: application/json" \
  -d "{
    \"senderWalletId\": \"$ALICE_WALLET\",
    \"receiverWalletId\": \"$BOB_WALLET\",
    \"amount\": 9999,
    \"currency\": \"XOF\",
    \"type\": \"P2P\",
    \"idempotencyKey\": \"pay-002\"
  }" | jq '{status, failureReason}'
# Attendu : status FAILED, failureReason "Insufficient balance..."
```

### Scenarios de test valides

| Scenario | Attendu |
|---|---|
| Transfert normal | status `COMPLETED`, balances mises a jour |
| Meme `idempotencyKey` rejoue | HTTP `409 Conflict` |
| Solde insuffisant | status `FAILED`, `failureReason` detaille, balance inchangee |
| CREDIT echoue apres DEBIT | status `REVERSED`, Alice re-creditee via compensation |

### URLs utiles

| Service | URL |
|---|---|
| Customer API | http://localhost:8082/api/v1/customers |
| Wallet API | http://localhost:8083/api/v1/wallets |
| Payment API | http://localhost:8084/api/v1/payments |
| Customer H2 Console | http://localhost:8082/h2-console |
| Wallet H2 Console | http://localhost:8083/h2-console |
| Payment H2 Console | http://localhost:8084/h2-console |

## Endpoints API

### Customer Service (port 8082)

| Methode | URL | Description |
|---|---|---|
| POST | `/api/v1/customers` | Creer un client (publie `customer.created`) |
| GET | `/api/v1/customers` | Lister les clients |
| GET | `/api/v1/customers/{id}` | Trouver un client par ID (UUID) |
| PUT | `/api/v1/customers/{id}` | Mettre a jour un client |

### Wallet Service (port 8083)

Tous les `{id}` et `{customerId}` sont des **UUID**.

| Methode | URL | Description |
|---|---|---|
| POST | `/api/v1/wallets` | Creer un wallet (publie `wallet.created`) |
| GET | `/api/v1/wallets/{id}` | Trouver par UUID |
| GET | `/api/v1/wallets/customer/{customerId}` | Trouver par UUID client |
| POST | `/api/v1/wallets/{id}/credit?amount=X` | Crediter (publie `wallet.credited`) |
| POST | `/api/v1/wallets/{id}/debit?amount=X` | Debiter (publie `wallet.debited`) |
| POST | `/api/v1/wallets/{id}/freeze?amount=X` | Geler (publie `wallet.amount_frozen`) |

### Payment Service (port 8084)

Tous les `{id}` et `{walletId}` sont des **UUID**.

| Methode | URL | Description |
|---|---|---|
| POST | `/api/v1/payments` | Initier un paiement (Saga + idempotency) |
| GET | `/api/v1/payments/{id}` | Consulter un paiement par UUID |
| GET | `/api/v1/payments/wallet/{walletId}` | Historique des paiements d'un wallet (UUID) |

**Corps de la requete POST `/api/v1/payments`** :

```json
{
  "senderWalletId": "550e8400-e29b-41d4-a716-446655440000",
  "receiverWalletId": "550e8400-e29b-41d4-a716-446655440001",
  "amount": 1000,
  "currency": "XOF",
  "type": "P2P",
  "idempotencyKey": "pay-unique-ref-001"
}
```

Types de paiement disponibles : `P2P`, `MERCHANT`, `BILL`, `WITHDRAWAL`, `DEPOSIT`

## Communication Kafka entre services

```
 ┌──────────────────┐
 │  Customer Service│  POST /api/v1/customers
 │    (port 8082)   │  → IDs en UUID
 └────────┬─────────┘
          │ [customer-events]
          │  customer.created {customerId: UUID, ...}
          ▼
 ┌──────────────────┐                        ┌──────────────────┐
 │  Wallet Service  │◀──[wallet-commands]────│ Payment Service  │
 │    (port 8083)   │   DEBIT_WALLET         │   (port 8084)    │
 │                  │   CREDIT_WALLET        │                  │
 │  Auto-cree le    │   COMPENSATE_DEBIT     │  Saga Pattern    │
 │  wallet (UUID)   │                        │  Idempotency     │
 │  a la reception  │──[wallet-saga-events]─▶│  (Redis, TTL 24h)│
 │  de customer.    │   DEBIT_SUCCESS        │                  │
 │  created         │   DEBIT_FAILED         │                  │
 │                  │   CREDIT_SUCCESS       │                  │
 └──────────────────┘   CREDIT_FAILED        └────────┬─────────┘
                                                      │ [payment-events]
                                                      │  payment.initiated
                                                      │  payment.completed
                                                      ▼  payment.failed
                                             (Fraud MS · Notify MS · Settlement MS)
```

**Regles de deserialisation Kafka cross-service** :
- `spring.json.use.type.headers: false` — ignore les headers `__TypeId__` du producer
- `spring.json.value.default.type: java.util.HashMap` — deserialise en Map generique cote consumer

**Format des IDs dans les events Kafka** :
- `customerId` : UUID string (ex. `"a3bb189e-8bf9-3888-9912-ace4e6543002"`)
- `walletId` : UUID string

## Tests

```bash
# customer-service : 8 tests
cd customer-service
mvn test -Dtest="CreateCustomerUseCaseTest,FindCustomerByIdUseCaseTest,UpdateCustomerUseCaseTest,CustomerControllerIntegrationTest"

# wallet-service : 21 tests
cd wallet-service
mvn test

# payment-service : 21 tests
cd payment-service
mvn test
```

| Service | Tests | Couverture |
|---|---|---|
| customer-service | 8 | Use cases (create, find, update) + Integration controller |
| wallet-service | 21 | Use cases (4) + Controller (7) + Integration |
| payment-service | 21 | Use cases (3) + Saga (7) + Find (4) + Controller (6) + WebMvc (1) |

## Roadmap

| Phase | Livrables | Statut |
|---|---|---|
| Phase 1 | Infrastructure Docker + Kafka | Termine |
| Phase 2 | Customer MS + Wallet MS + Kafka events + Tests | Termine |
| Phase 3 | Payment MS + Saga Pattern + Redis Idempotency + Tests E2E | Termine |
| Phase 4 | Fraud Detection MS + Settlement MS | A venir |
| Phase 5 | Notification MS + Prometheus + Zipkin | A venir |
| Phase 6 | Tests de charge · CI/CD · Documentation | A venir |

## Approfondissements prevus (Senior+)

- Exactly-Once Semantics Kafka (`isolation.level = read_committed`)
- Outbox Pattern avec Debezium (CDC pour garantie transactionnelle DB → Kafka)
- GDPR Compliance (chiffrement PII, droit a l'oubli dans les topics)
- Multi-tenancy (isolation par institution bancaire)
- gRPC entre services (queries synchrones haute-performance)
- Kubernetes (Helm charts, HPA, PodDisruptionBudget)
- PCI-DSS basics (tokenisation donnees carte, audit logs immuables)
