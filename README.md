# PathOps Control Plane – Design Notes & Decisions

## Overview

The **PathOps Control Plane** is the central backend service of the PathOps platform.

It is responsible for:

* Managing **users, tenants, and applications**
* Acting as the **source of truth** for platform state
* Orchestrating integrations with external systems:

  * Keycloak (Identity)
  * Jenkins (CI/CD)
  * Vault (secrets)
* Enforcing **PathOps guardrails**

---

## Core Principles

* **JWT = Identity, not Authorization**
* **Authorization lives in the Control Plane (DB)**
* External tools (Jenkins, etc.) are **projections of Control Plane state**
* All provisioning is **driven by Control Plane APIs**
* **Flyway = schema source of truth**
* **Multi-tenant by design**

---

## Identity & Authentication

### Keycloak Integration

The Control Plane uses Keycloak as the Identity Provider (IdP).

### Clients

#### 1. `control-plane-public`

* Used by:

  * CLI (`pathops login`)
  * UI (future)
* Flow:

  * Authorization Code + PKCE
* Token contains:

  * `iss` (issuer)
  * `sub` (subject)
  * `preferred_username`
  * `email`

#### 2. `control-plane-admin-api`

* Used for **administrative operations**
* Intended for:

  * internal services
  * automation
* Flow:

  * Client Credentials

#### 3. `control-plane-keycloak-admin` (technical client)

* Used internally by Control Plane
* Purpose:

  * call Keycloak Admin API
* Credentials:

  * stored in Vault

---

## Identity Model

Users are identified by:

```
(issuer, subject)
```

This maps directly to:

* `iss` + `sub` from JWT

### Why not email?

* Email is mutable
* `sub` is stable and guaranteed unique per issuer

---

## Core Domain Model

### PathOpsUser

Represents an external identity.

```
issuer + subject → unique identity
```

### Tenant

Represents a logical isolation unit.

* Each user gets a **personal tenant** on first login
* Has:

  * `name`
  * `slug` (used for URLs, DNS, etc.)

### Membership

Connects:

```
User ↔ Tenant
```

With role:

* OWNER
* ADMIN
* DEVELOPER
* VIEWER

### App

Represents an application inside a tenant.

* Scoped per tenant
* Identified by:

  * `name`
  * `slug`

---

## Login Flow (`POST /api/public/login`)

### Step-by-step

1. User authenticates via Keycloak
2. CLI calls:

```
POST /api/public/login
Authorization: Bearer <access_token>
```

3. Control Plane:

#### Step 1 – Extract identity

From JWT:

* `issuer`
* `subject`
* `preferred_username`
* `email`

#### Step 2 – Resolve user

* If user exists → update if needed
* If not → create `PathOpsUser`

#### Step 3 – Resolve memberships

* If user has no memberships:

  * Create tenant
  * Create membership with role `OWNER`

#### Step 4 – Return response

```json
{
  "userId": 1,
  "tenantId": 1,
  "tenantSlug": "gaston-abc123",
  "membershipRole": "OWNER",
  "identityChanged": true,
  "requiresTokenRefresh": true,
  "requiresToolRelogin": true
}
```

---

## Token Refresh Behavior

After first login:

* Tenant did not exist in token
* Control Plane created it

Therefore:

* CLI must **refresh token**
* Future tokens may include tenant context (later phase)

---

## Keycloak Sync

### Purpose

Ensure Keycloak reflects Control Plane state.

### What it does

After tenant creation:

* Create **group** (or equivalent):

  * e.g. `tenant:<slug>`
* Add user to group
* Optionally assign roles

### Future

* Tenant → Keycloak group
* Membership role → Keycloak role or attribute

---

## Jenkins Sync

### Purpose

Provision CI/CD isolation per tenant.

### What it does

When tenant is created:

* Create Jenkins **folder**:

  ```
  tenants/<tenant-slug>
  ```
* Configure permissions:

  * OWNER → full control
  * others → restricted

### Future

* Create pipelines per app
* Bind credentials per tenant

---

## Vault Integration

### Purpose

Secure storage of secrets used by Control Plane.

### Stored secrets

* Keycloak admin client credentials
* Jenkins API token
* Other service credentials

### Access pattern

Control Plane:

1. Authenticates to Vault
2. Reads secrets dynamically
3. Uses them to call external APIs

---

## Authorization Model

### Current (MVP)

* JWT → authentication only
* Authorization → database (`Membership`)

### Future

* Control Plane syncs roles into:

  * Keycloak (groups/claims)
  * Jenkins (roles/folders)
* Tools enforce authorization locally

---

## Security Design

| Layer           | Responsibility |
| --------------- | -------------- |
| Keycloak        | Authentication |
| Control Plane   | Authorization  |
| Jenkins / Tools | Enforcement    |

---

## Database Strategy

* Managed via **Flyway**
* Hibernate set to:

```
ddl-auto=validate
```

* No automatic schema changes

---

## Slug Usage

Slugs are:

* stable identifiers
* URL-safe
* used in:

  * DNS
  * paths
  * folder names
  * integrations

Examples:

```
tenant: acme
app: shop-frontend

→ shop-frontend.acme.demo.pathops.io
```

---

## Future Work

* Multi-tenant selection (user with multiple tenants)
* Token enrichment with tenant claims
* Sync engine (event-driven)
* Integration orchestration pipeline
* CLI enhancements

---

## Summary

The Control Plane is the **brain of PathOps**:

* Identity comes from Keycloak
* Authorization lives in the Control Plane
* External tools are projections
* Everything is driven by APIs and guardrails
