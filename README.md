# Pamestinukai - Event Ticket Platform

[Project document](https://docs.google.com/document/d/1BXPrlE2Rh4SfkYH3CAXSJpskK6-JIMyXsNiomqIKmC8/edit?usp=sharing)

## Tech Stack

| Layer    | Technology                          |
|----------|-------------------------------------|
| Backend  | Java 21, Spring Boot 4, Spring Data JPA |
| Database | PostgreSQL                              |
| Frontend | React, TypeScript, Vite                 |

## Prerequisites

1. **Java 21** — [Download](https://adoptium.net/temurin/releases?version=21&os=any&arch=any)
2. **Node.js + pnpm** — [Node.js](https://nodejs.org/) / `npm install -g pnpm`
3. **Podman Desktop** (to run the PostgreSQL container) — [Download](https://podman-desktop.io/)

## Getting Started

### 1. Install dependencies

- In root run `pnpm i`
- In root/frontend run `pnpm i`

### 2. Setup local app properties

- In `backend\src\main\resources create file` create file `application-local.properties` and copy values from `application-local.properties.example`
- Modify property values as needed

### 2.1 Local email testing with Mailpit/MailHog

The project includes a dev email endpoint at `POST /api/dev/email/test` that is enabled by `app.mail.dev-endpoint=true`.

Current local defaults (in `backend/src/main/resources/application-local.properties`) are already set for Mailpit/MailHog:

```properties
MAIL_HOST=localhost
MAIL_PORT=1025
MAIL_USERNAME=
MAIL_PASSWORD=
spring.mail.properties.mail.smtp.auth=false
spring.mail.properties.mail.smtp.starttls.enable=false
spring.mail.properties.mail.smtp.starttls.required=false
app.mail.dev-endpoint=true
```

Steps:

1. Install and run Mailpit (recommended) or MailHog.
2. Start the app (`pnpm start` or `pnpm start:data-init`).
3. Send a test email:

```bash
curl -X POST "http://localhost:8080/api/dev/email/test?to=anybody@example.com"
```

4. Open the inbox UI at `http://localhost:8025/` and verify the email content.

### 3. Start the Postgres database

Run a Postgres container via Podman

```bash
pnpm start:db
```

### 4. Start the frontend & backend

```bash
pnpm start
```

Or start with sample data:

```bash
pnpm start:data-init
```

The API will be available at `http://localhost:8080`.

The app will be available at `http://localhost:5173`.

## Ticket QR + Email Flow

- Confirming a reservation now generates a unique QR token for each ticket and sends a PDF ticket by email.
- Confirmation endpoint accepts buyer details (used for delivery):

```bash
curl -X POST "http://localhost:8080/api/tickets/reserve/{purchaseId}/confirm?buyerEmail=anybody@example.com&buyerName=John"
```

- Validate a ticket token:

```bash
curl "http://localhost:8080/api/tickets/validate/{qrToken}"
```

- Check in a ticket token:

```bash
curl -X POST "http://localhost:8080/api/tickets/check-in/{qrToken}"
```

- Email failures are persisted and retried by scheduler using these properties:
	- `app.scheduler.email-retry.rate-ms`
	- `app.mail.retry.max-attempts`
	- `app.mail.retry.delay-minutes`