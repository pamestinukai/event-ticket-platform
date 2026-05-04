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

### 1. Start the Postgres database

Run a Postgres container via Podman

```bash
pnpm start:db
```

### 2. Start the frontend & backend

```bash
pnpm start
```

The API will be available at `http://localhost:8080`.

The app will be available at `http://localhost:5173`.