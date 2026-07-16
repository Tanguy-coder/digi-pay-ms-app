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
=======
# digi-pay-ms-app



## Getting started

To make it easy for you to get started with GitLab, here's a list of recommended next steps.

Already a pro? Just edit this README.md and make it your own. Want to make it easy? [Use the template at the bottom](#editing-this-readme)!

## Add your files

* [Create](https://docs.gitlab.com/user/project/repository/web_editor/#create-a-file) or [upload](https://docs.gitlab.com/user/project/repository/web_editor/#upload-a-file) files
* [Add files using the command line](https://docs.gitlab.com/topics/git/add_files/#add-files-to-a-git-repository) or push an existing Git repository with the following command:

```
cd existing_repo
git remote add origin https://gitlab.com/microservices6125502/digi-pay-ms-app.git
git branch -M main
git push -uf origin main
```

## Integrate with your tools

* [Set up project integrations](https://gitlab.com/microservices6125502/digi-pay-ms-app/-/settings/integrations)

## Collaborate with your team

* [Invite team members and collaborators](https://docs.gitlab.com/user/project/members/)
* [Create a new merge request](https://docs.gitlab.com/user/project/merge_requests/creating_merge_requests/)
* [Automatically close issues from merge requests](https://docs.gitlab.com/user/project/issues/managing_issues/#closing-issues-automatically)
* [Enable merge request approvals](https://docs.gitlab.com/user/project/merge_requests/approvals/)
* [Set auto-merge](https://docs.gitlab.com/user/project/merge_requests/auto_merge/)

## Test and Deploy

Use the built-in continuous integration in GitLab.

* [Get started with GitLab CI/CD](https://docs.gitlab.com/ci/quick_start/)
* [Analyze your code for known vulnerabilities with Static Application Security Testing (SAST)](https://docs.gitlab.com/user/application_security/sast/)
* [Deploy to Kubernetes, Amazon EC2, or Amazon ECS using Auto Deploy](https://docs.gitlab.com/topics/autodevops/requirements/)
* [Use pull-based deployments for improved Kubernetes management](https://docs.gitlab.com/user/clusters/agent/)
* [Set up protected environments](https://docs.gitlab.com/ci/environments/protected_environments/)

***

# Editing this README

When you're ready to make this README your own, just edit this file and use the handy template below (or feel free to structure it however you want - this is just a starting point!). Thanks to [makeareadme.com](https://www.makeareadme.com/) for this template.

## Suggestions for a good README

Every project is different, so consider which of these sections apply to yours. The sections used in the template are suggestions for most open source projects. Also keep in mind that while a README can be too long and detailed, too long is better than too short. If you think your README is too long, consider utilizing another form of documentation rather than cutting out information.

## Name
Choose a self-explaining name for your project.

## Description
Let people know what your project can do specifically. Provide context and add a link to any reference visitors might be unfamiliar with. A list of Features or a Background subsection can also be added here. If there are alternatives to your project, this is a good place to list differentiating factors.

## Badges
On some READMEs, you may see small images that convey metadata, such as whether or not all the tests are passing for the project. You can use Shields to add some to your README. Many services also have instructions for adding a badge.

## Visuals
Depending on what you are making, it can be a good idea to include screenshots or even a video (you'll frequently see GIFs rather than actual videos). Tools like ttygif can help, but check out Asciinema for a more sophisticated method.

## Installation
Within a particular ecosystem, there may be a common way of installing things, such as using Yarn, NuGet, or Homebrew. However, consider the possibility that whoever is reading your README is a novice and would like more guidance. Listing specific steps helps remove ambiguity and gets people to using your project as quickly as possible. If it only runs in a specific context like a particular programming language version or operating system or has dependencies that have to be installed manually, also add a Requirements subsection.

## Usage
Use examples liberally, and show the expected output if you can. It's helpful to have inline the smallest example of usage that you can demonstrate, while providing links to more sophisticated examples if they are too long to reasonably include in the README.

## Support
Tell people where they can go to for help. It can be any combination of an issue tracker, a chat room, an email address, etc.

## Roadmap
If you have ideas for releases in the future, it is a good idea to list them in the README.

## Contributing
State if you are open to contributions and what your requirements are for accepting them.

For people who want to make changes to your project, it's helpful to have some documentation on how to get started. Perhaps there is a script that they should run or some environment variables that they need to set. Make these steps explicit. These instructions could also be useful to your future self.

You can also document commands to lint the code or run tests. These steps help to ensure high code quality and reduce the likelihood that the changes inadvertently break something. Having instructions for running tests is especially helpful if it requires external setup, such as starting a Selenium server for testing in a browser.

## Authors and acknowledgment
Show your appreciation to those who have contributed to the project.

## License
For open source projects, say how it is licensed.

## Project status
If you have run out of energy or time for your project, put a note at the top of the README saying that development has slowed down or stopped completely. Someone may choose to fork your project or volunteer to step in as a maintainer or owner, allowing your project to keep going. You can also make an explicit request for maintainers.

