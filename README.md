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
- Paiements marchands et transferts peer-to-peer
- Reglement interbancaire avec calcul de position nette
- Detection de fraude en temps reel (regles configurables)
- Notifications multi-canal (Email, SMS, Push)

## Architecture generale

```
                        [ API Gateway ]
                              |
              [ Customer MS ]    [ Wallet MS ]
                    |                  |
        ════════════[ KAFKA CLUSTER ]════════════
              |              |              |
        [ Payment MS ] [ Fraud MS ] [ Notify MS ]
              |
        [ Settlement MS ]
```

**Principes** : Loose Coupling (communication par evenements) · Database per Service (PostgreSQL par MS + Redis partage) · API Gateway unique · High Cohesion.

## Microservices & Contrats d'Evenements

| Microservice | Responsabilites | Publie (Events) | Consomme (Events) |
|---|---|---|---|
| **Customer MS** | Creation client · KYC · Infos personnelles | `customer.created` `customer.updated` `customer.verified` | — |
| **Wallet MS** | Portefeuille · Solde · Gel de fonds | `wallet.created` `wallet.credited` `wallet.debited` `wallet.amount_frozen` | `customer.created` |
| **Payment MS** | Paiements marchands · Transferts P2P | `payment.initiated` `payment.completed` `payment.failed` | `wallet.debited` `wallet.credited` |
| **Fraud Detection MS** | Regles anti-fraude · Score de risque · Alertes | `fraud.detected` `fraud.cleared` | `payment.initiated` |
| **Notification MS** | Email · SMS · Push | — | `payment.completed` `payment.failed` `wallet.credited` |
| **Settlement MS** | Compensation interbancaire · Position nette | `settlement.completed` `settlement.failed` | `payment.completed` |

## Configuration Kafka

| Topic | Partitions | Consumer Group(s) | Usage |
|---|---|---|---|
| `customer-events` | 3 | wallet-group | Cycle de vie client, KYC |
| `wallet-events` | 3 | payment-group | Operations sur portefeuille |
| `payment-events` | 3 | fraud-group · notification-group · settlement-group | Flux de paiement central |
| `fraud-events` | 2 | payment-group · notification-group | Alertes fraude temps reel |
| `settlement-events` | 2 | reporting-group | Compensation & reglement |
| `notification-events` | 2 | notification-group | Declenchement notifications |

## Patterns & Concepts avances

| Pattern | Application dans ce projet |
|---|---|
| **Event Sourcing** | Wallet : +1000 / +2000 / -500 → solde calcule a la volee depuis wallet-events |
| **CQRS** | Command: initier paiement · Query: historique transactions via read model dedie |
| **Saga Pattern** | Transfert A→B : si `wallet.debited` OK mais `wallet.credited` KO → rollback via compensation event |
| **DDD** | Customer / Wallet / Payment / Fraud / Settlement = bounded contexts independants |
| **Clean Architecture** | Entites domaine pures sans dependance framework, use cases isoles |
| **Idempotency** | Idempotency keys Redis pour les paiements et transferts |
| **Circuit Breaker** | Resilience4j : open/closed/half-open par service |

### Saga Pattern — Flux de transfert

```
Transfert 500$ de A vers B :

1. Payment MS publie payment.initiated
2. Wallet MS consomme → publie wallet.debited (debit A) ou wallet.debit.failed
3. (succes) Wallet MS publie wallet.credited (credit B)
   (echec) Wallet MS publie wallet.credit.failed → wallet.compensated (remboursement A)
4. Payment MS publie payment.completed ou payment.failed
5. Notification MS informe l'utilisateur du resultat
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
| Base de donnees | PostgreSQL (par service) · H2 (dev) |
| Cache | Redis (sessions, idempotency, rate limiting) |
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
├── docker-compose.yaml       → Kafka + services
└── README.md
```

## Demarrage rapide

### Prerequis

- Java 21+
- Maven 3.9+
- Docker (pour Kafka)

### 1. Demarrer Kafka

```bash
docker compose up kafka -d
```

### 2. Lancer le Customer Service

```bash
cd customer-service
mvn spring-boot:run
```

L'application demarre sur le port **8082** avec une base H2 en memoire.

### 3. Lancer le Wallet Service

```bash
cd wallet-service
mvn spring-boot:run
```

L'application demarre sur le port **8083** avec une base H2 en memoire.
Le consumer Kafka ecoute automatiquement le topic `customer-events`.

### 4. Tester le flux complet

```bash
# Creer un client (publie customer.created sur Kafka → wallet auto-cree)
curl -X POST http://localhost:8082/api/v1/customers \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Tanguy",
    "lastName": "Mambafei",
    "email": "tanguy@example.com",
    "phoneNumber": "+22890000000",
    "nationality": "TGO",
    "addressLine1": "123 Rue de Lome",
    "city": "Lome",
    "country": "Togo",
    "preferredCurrency": "XOF"
  }'

# Verifier le wallet auto-cree (attendre 2-3s pour la propagation Kafka)
curl http://localhost:8083/api/v1/wallets/customer/1

# Crediter le wallet
curl -X POST "http://localhost:8083/api/v1/wallets/1/credit?amount=50000"

# Debiter le wallet
curl -X POST "http://localhost:8083/api/v1/wallets/1/debit?amount=15000"

# Geler un montant
curl -X POST "http://localhost:8083/api/v1/wallets/1/freeze?amount=10000"
```

### URLs utiles

| Service | URL |
|---|---|
| Customer API | http://localhost:8082/api/v1/customers |
| Wallet API | http://localhost:8083/api/v1/wallets |
| Customer H2 Console | http://localhost:8082/h2-console |
| Wallet H2 Console | http://localhost:8083/h2-console |

## Endpoints API

### Customer Service (port 8082)

| Methode | URL | Description |
|---|---|---|
| POST | `/api/v1/customers` | Creer un client (publie `customer.created`) |
| GET | `/api/v1/customers` | Lister les clients |
| GET | `/api/v1/customers/{id}` | Trouver un client par ID |
| PUT | `/api/v1/customers/{id}` | Mettre a jour un client |

### Wallet Service (port 8083)

| Methode | URL | Description |
|---|---|---|
| POST | `/api/v1/wallets` | Creer un wallet (publie `wallet.created`) |
| GET | `/api/v1/wallets/{id}` | Trouver par ID |
| GET | `/api/v1/wallets/customer/{customerId}` | Trouver par client |
| POST | `/api/v1/wallets/{id}/credit?amount=X` | Crediter (publie `wallet.credited`) |
| POST | `/api/v1/wallets/{id}/debit?amount=X` | Debiter (publie `wallet.debited`) |
| POST | `/api/v1/wallets/{id}/freeze?amount=X` | Geler (publie `wallet.amount_frozen`) |

## Communication Kafka entre services

```
┌──────────────────┐         customer-events         ┌──────────────────┐
│  Customer Service │ ─────────────────────────────▶ │  Wallet Service   │
│  (port 8082)      │    customer.created            │  (port 8083)      │
└──────────────────┘                                 └──────────────────┘
                                                              │
                                                              │ wallet-events
                                                              ▼
                                                     wallet.created
                                                     wallet.credited
                                                     wallet.debited
                                                     wallet.amount_frozen
```

**Flux** :
1. Un client est cree via POST `/api/v1/customers`
2. Le `CreateCustomerUseCase` publie `customer.created` sur Kafka
3. Le `CustomerEventConsumer` du wallet-service recoit l'event
4. Un wallet est automatiquement cree (idempotent) avec la devise preferee du client
5. L'event `wallet.created` est publie sur `wallet-events`

## Tests

```bash
# Tests unitaires + integration du wallet-service (21 tests)
cd wallet-service
mvn test

# Tests unitaires du customer-service (5 tests)
cd customer-service
mvn test -Dtest="CreateCustomerUseCaseTest"
```

## Roadmap

| Phase | Livrables | Statut |
|---|---|---|
| Phase 1 | Infrastructure Docker + Kafka | Termine |
| Phase 2 | Customer MS + Wallet MS + Kafka events + Tests | Termine |
| Phase 3 | Payment MS + Saga Pattern + Idempotency + Redis | A venir |
| Phase 4 | Fraud Detection MS + Settlement MS | A venir |
| Phase 5 | Notification MS + Prometheus + Zipkin | A venir |
| Phase 6 | Tests de charge · CI/CD · Documentation | A venir |

## Approfondissements prevus (Senior+)

- Exactly-Once Semantics Kafka (`isolation.level = read_committed`)
- Outbox Pattern (coherence DB write + Kafka publish sans 2PC)
- GDPR Compliance (chiffrement PII, droit a l'oubli dans les topics)
- Multi-tenancy (isolation par institution bancaire)
- gRPC entre services (queries synchrones haute-performance)
- Kubernetes (Helm charts, HPA, PodDisruptionBudget)
- PCI-DSS basics (tokenisation donnees carte, audit logs immuables)
