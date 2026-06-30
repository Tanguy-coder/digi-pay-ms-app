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
| **Wallet MS** | Portefeuille · Solde · Gel de fonds | `wallet.created` `wallet.credited` `wallet.debited` | `customer.created` |
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
| Messaging | Apache Kafka |
| Service Discovery | Eureka (Spring Cloud) |
| API Gateway | Spring Cloud Gateway |
| Configuration | Spring Cloud Config |
| Base de donnees | PostgreSQL (par service) |
| Cache | Redis (sessions, idempotency, rate limiting) |
| Observabilite | Prometheus + Grafana |
| Tracing distribue | Zipkin / Micrometer |
| Documentation API | SpringDoc OpenAPI / Swagger UI |
| Conteneurisation | Docker + Docker Compose |
| Mapping objets | MapStruct 1.5.5 |
| Resilience | Resilience4j |
| Tests | JUnit 5, Testcontainers |

## Architecture logicielle (par service)

Chaque microservice suit une **architecture hexagonale** (Clean Architecture / Ports & Adapters) :

```
service/
├── Domain/                          # Coeur metier (zero dependance framework)
│   ├── Entities/                    # Entites metier pures
│   ├── Enums/                       # Enumerations du domaine
│   ├── Ports/                       # Interfaces de services (ports de sortie)
│   ├── Gateways/                    # Abstractions de persistance
│   ├── Presenters/                  # Interfaces de presentation
│   ├── Responses/                   # DTOs de sortie
│   ├── UseCases/                    # Cas d'utilisation (logique metier)
│   └── Validations/                 # Validation metier custom
│
└── Infrastructure/                  # Adaptateurs techniques
    ├── Adapters/                    # Implementations des ports
    ├── Config/                      # Configuration Spring (beans)
    ├── Controllers/                 # API REST
    ├── Mappers/                     # Mapping Domain <-> JPA (MapStruct)
    ├── Models/                      # Entites JPA / Kafka events
    ├── Presenters/                  # Implementations des presenters
    ├── Repositories/                # Implementations de persistance
    └── Requests/                    # DTOs d'entree
```

## Structure du repository

```
digital-payment-platform/
├── api-gateway/              → Spring Cloud Gateway
├── config-server/            → Spring Cloud Config
├── eureka-server/            → Service Discovery
├── customer-service/         → Customer MS + KYC
├── wallet-service/           → Wallet MS + Event Sourcing
├── payment-service/          → Payment MS + Saga
├── fraud-detection/          → Fraud MS + regles
├── notification-service/     → Notification MS
├── settlement-service/       → Settlement MS + compensation
├── shared-events/            → DTOs + Event schemas partages
├── docker-compose.yml        → Kafka + DBs + Redis + Zipkin
└── k8s/                      → Manifests Kubernetes (bonus)
```

## Roadmap

| Phase | Semaines | Livrables | Statut |
|---|---|---|---|
| Phase 1 | S1-S2 | Infrastructure Docker + Kafka + Eureka + Config Server + Gateway | En cours |
| Phase 2 | S3-S4 | Customer MS (KYC) + Wallet MS (Event Sourcing) + topics Kafka | En cours |
| Phase 3 | S5-S6 | Payment MS + Saga Pattern + Idempotency + Redis | A venir |
| Phase 4 | S7-S8 | Fraud Detection MS + Settlement MS + compensation nette | A venir |
| Phase 5 | S9-S10 | Notification MS + Prometheus + Zipkin + Dashboards | A venir |
| Phase 6 | S11-S12 | Tests de charge · Chaos Engineering · Documentation · CI/CD | A venir |

---

## Avancement actuel — Phase 2

### Customer Service (en developpement)

Le premier microservice est fonctionnel avec :

**Implemente :**
- Entite domaine `DomainCustomer` (decouple de JPA)
- Enums metier : `AccountStatus`, `KycStatus`, `TierLevel`
- Use cases : `CreateCustomer`, `FindCustomerById`, `ListCustomers`
- Validation domaine custom (email, E.164, ISO 3 lettres, risk score)
- Entite JPA `Customer` avec contraintes et index
- Mapping MapStruct (Domain <-> JPA)
- Repository avec pattern adapter
- Controller REST `GET /api/v1/customers`
- Configuration Spring Cloud (Eureka + Config desactives en local)
- Dockerfile + Docker Compose
- Swagger UI (SpringDoc OpenAPI)

**Endpoints :**

| Methode | URL | Statut |
|---|---|---|
| GET | `/api/v1/customers` | Implemente |
| GET | `/api/v1/customers/{id}` | Use case pret, controller a cabler |
| POST | `/api/v1/customers` | Use case pret, controller a cabler |
| PUT | `/api/v1/customers/{id}` | Interface definie |

**Modele metier Customer :**
- Identite : nom, email, telephone (E.164)
- Localisation : adresse, ville, pays, nationalite (ISO 3 lettres)
- KYC : `NOT_SUBMITTED` → `PENDING` → `VERIFIED` / `REJECTED`
- Risk score : 0.00 - 100.00
- Tier : `BASIC` · `STANDARD` · `PREMIUM` · `VIP`
- Devise par defaut : XOF (franc CFA)

### Reste a faire (court terme)

- [ ] Cabler les endpoints POST et GET/{id} dans le controller
- [ ] Implementer `findByEmail` et `findByPhoneNumber` dans le repository
- [ ] Implementer le use case `UpdateCustomer`
- [ ] Ajouter la gestion d'erreurs globale (exception handler)
- [ ] Ecrire les tests unitaires et d'integration
- [ ] Publier les evenements Kafka (`customer.created`, `customer.updated`, `customer.verified`)
- [ ] Demarrer le Wallet Service (consommateur de `customer.created`)
- [ ] Mettre en place l'infrastructure (Kafka, Eureka, Config Server, Gateway)

## Demarrage rapide

### Prerequis

- Java 21+
- Maven 3.9+
- Docker (PostgreSQL, Kafka)

### Lancer le Customer Service en dev

```bash
cd customer-service
./mvnw spring-boot:run
```

L'application demarre sur le port **8082** avec une base H2 en memoire.

- API : http://localhost:8082/api/v1/customers
- Console H2 : http://localhost:8082/h2-console
- Swagger UI : http://localhost:8082/swagger-ui.html

### Lancer avec Docker Compose

```bash
docker compose up --build
```

## Approfondissements prevus (Senior+)

- Exactly-Once Semantics Kafka (`isolation.level = read_committed`)
- Outbox Pattern (coherence DB write + Kafka publish sans 2PC)
- GDPR Compliance (chiffrement PII, droit a l'oubli dans les topics)
- Multi-tenancy (isolation par institution bancaire)
- gRPC entre services (queries synchrones haute-performance)
- Kubernetes (Helm charts, HPA, PodDisruptionBudget)
- PCI-DSS basics (tokenisation donnees carte, audit logs immuables)
