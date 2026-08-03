# stockflow-api

[![CI](https://github.com/Ambdulghaffar/stockflow-api/actions/workflows/ci.yml/badge.svg)](https://github.com/Ambdulghaffar/stockflow-api/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-21-blue)

REST API backend for a retail inventory management platform. Tracks products, customer orders (with stock decrements on sale and reversals on cancellation), supplier restocking, and role-separated access for admins, managers, and clients.

## Key Features

- **Authentication** — email/password (register + login returning JWT access + refresh tokens), Google OAuth (frontend-initiated: client sends a Google ID token to `POST /api/auth/google`, backend creates or retrieves the account), and email-based password reset.
- **RBAC (3 roles)** — `ADMIN`, `MANAGER`, `CLIENT`. Enforced per-method via `@PreAuthorize`, keeping mixed-access controllers intact (e.g., `GET /api/orders/me` is open to any authenticated user while `GET /api/orders` requires ADMIN or MANAGER).
- **Product catalogue** — paginated listing with combinable filters: `status` (ACTIVE / INACTIVE / OUT_OF_STOCK), `categoryId`, keyword `search`, `minPrice` / `maxPrice`, and an `excludeInactive` boolean flag.
- **Customer orders** — any authenticated user places orders and reads their own history. Status lifecycle: `PENDING → CONFIRMED → SHIPPED → DELIVERED / CANCELLED`. Order confirmation triggers a `SALE` stock movement; cancellation triggers a `CANCELLATION` reversal.
- **Stock movements** — four tracked types: `SALE` (order fulfillment), `CANCELLATION` (order reversal), `RESTOCK` (manual top-up, ADMIN/MANAGER), `DAMAGE` (loss or breakage, ADMIN only). Filterable by product ID and movement type.
- **Supplier management** — full CRUD for suppliers and supplier orders, with a four-state lifecycle: `PENDING → ORDERED → RECEIVED / CANCELLED`.
- **Reports** — date-range sales report aggregating revenue only from `CONFIRMED`, `SHIPPED`, and `DELIVERED` orders (`PENDING` and `CANCELLED` are explicitly excluded via a `REVENUE_STATUSES` constant); includes daily breakdown and top-5 products by quantity. Separate stock report with low-stock and out-of-stock counts and a movement-type summary.
- **Image uploads** — server-signed Cloudinary upload: backend generates a signed request payload, client uploads the file directly to Cloudinary.

## Tech Stack

| Concern | Library / Version |
|---|---|
| Framework | Spring Boot **3.5.6**, Java 21 |
| Security | Spring Security + JJWT **0.11.5** |
| Persistence | Spring Data JPA + MySQL Connector |
| Mapping | MapStruct **1.6.3** + Lombok |
| Email | Spring Boot Starter Mail (SMTP/Gmail) |
| File storage | Cloudinary SDK **1.39.0** |
| API docs | springdoc-openapi **2.8.15** (Swagger UI) |
| Testing | JUnit 5 + Mockito (via `spring-boot-starter-test`) |
| Config | spring-dotenv **4.0.0** (`.env` file support) |

## Architecture

- **DTO/Entity separation via MapStruct** — validated request DTOs are mapped to entities before persistence and mapped back to response DTOs before serialization; entities never cross the service boundary.
- **Per-method authorization** — `@PreAuthorize` on individual handler methods rather than at the class level, which keeps controllers with mixed access rules (like `UserController`) in a single place without splitting them by role.
- **Revenue calculation is a deliberate constant** — `ReportServiceImpl` defines `REVENUE_STATUSES = List.of(CONFIRMED, SHIPPED, DELIVERED)` and passes it to every revenue query, making the exclusion of `PENDING` and `CANCELLED` explicit and auditable rather than buried in SQL.
- **Stock movement as the audit trail** — every change to product stock (sale, cancellation, manual restock, damage) is recorded as a typed `StockMovement` row linked to both the product and the user who triggered it.

## Getting Started

### Prerequisites

- Java 21
- MySQL 8+ (database `stockflow-db` is created automatically if it does not exist)
- Maven wrapper included (`./mvnw`)

### Environment variables

Create a `.env` file at the project root (loaded automatically via spring-dotenv at startup):

```env
JWT_SECRET=<base64-encoded-256-bit-secret>
JWT_ACCESS_EXPIRATION=900000
JWT_REFRESH_EXPIRATION=604800000

MAIL_USERNAME=your-gmail@gmail.com
MAIL_PASSWORD=your-gmail-app-password

FRONTEND_URL=http://localhost:5173

CLOUDINARY_CLOUD_NAME=your-cloud-name
CLOUDINARY_API_KEY=your-api-key
CLOUDINARY_API_SECRET=your-api-secret
```

The MySQL connection defaults to `localhost:3306` with username `root` and no password. Override via `spring.datasource.url`, `spring.datasource.username`, and `spring.datasource.password` in `application.properties` or as environment variables.

### Run locally

```bash
./mvnw spring-boot:run
```

Swagger UI: `http://localhost:8080/swagger-ui.html`

## Demo Data Seeding

Populates the database with realistic demo data — 9 electronics-themed categories (Smartphones, Ordinateurs, Tablettes, Montres Connectées, Écouteurs & Casques, Gaming & Consoles, Téléviseurs & Image, Chargeurs & Batteries, Gadgets Lifestyle) and 29 products — for development and demo purposes. No users are created.

The seeder is a `CommandLineRunner` (`config/seed/DataSeeder.java`) backed by [DataFaker 2.4.2](https://www.datafaker.net/). It is gated by `@Profile("seed")` and is **never active during a normal startup**.

**Idempotent** — at startup the seeder calls `categoryRepository.count()` before touching any data. If the result is greater than zero it logs a message and returns immediately, so it is safe to run multiple times against the same database.

### Run locally

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=seed
```

For a Docker Compose run, see the **stockflow-infra** repository README for the exact command.

> Category and product images used by this seeder are sourced from [Unsplash](https://unsplash.com) (free to use), for demonstration purposes only.

## Testing

```bash
./mvnw test
```

6 unit tests cover `AuthService` (successful registration, duplicate email/phone rejection, successful login, wrong password, unknown email). All dependencies are mocked with Mockito — no real database or SMTP connection required.

## Docker

```bash
# Build
docker build -t stockflow-api .

# Run
docker run -p 8080:8080 \
  -e JWT_SECRET=... \
  -e JWT_ACCESS_EXPIRATION=900000 \
  -e JWT_REFRESH_EXPIRATION=604800000 \
  -e MAIL_USERNAME=... \
  -e MAIL_PASSWORD=... \
  -e FRONTEND_URL=... \
  -e CLOUDINARY_CLOUD_NAME=... \
  -e CLOUDINARY_API_KEY=... \
  -e CLOUDINARY_API_SECRET=... \
  stockflow-api
```

Multi-stage build: `eclipse-temurin:21-jdk` compiles the fat JAR, `eclipse-temurin:21-jre` runs it as a non-root `spring` user (UID 1001).

## Project Structure

```
src/main/java/com/elhaffar/exoformbackend/
├── config/          # Security filter chain, JWT filter, JWT utilities, OpenAPI config
├── controllers/     # 10 REST controllers, one per domain
├── services/
│   ├── *.java       # Service interfaces
│   └── impl/        # Service implementations
├── repository/      # Spring Data JPA repositories
├── entities/        # JPA entities: User, Product, Category, Order, OrderItem,
│                    #   StockMovement, Supplier, SupplierOrder, SupplierOrderItem,
│                    #   PasswordResetToken
├── dto/             # Request/response DTOs organized by domain
│   ├── auth/
│   ├── user/
│   ├── product/
│   ├── order/
│   ├── stock/
│   ├── report/
│   ├── supplier/
│   ├── category/
│   ├── upload/
│   └── common/      # PageResponseDTO
├── mapper/          # MapStruct mappers (Entity ↔ DTO)
├── common/
│   ├── enums/       # UserRole, OrderStatus, ProductStatus,
│   │                #   StockMovementType, SupplierOrderStatus, AuthProvider
│   └── utils/       # SortUtils
└── exceptions/      # BusinessException, ResourceNotFoundException,
                     #   GlobalExceptionHandler
```

## Related Repositories

- **Frontend**: [stockflow-web](https://github.com/Ambdulghaffar/stockflow-web)
- **Infrastructure**: [stockflow-infra](https://github.com/Ambdulghaffar/stockflow-infra)
