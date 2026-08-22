# Bokl Home Rental Service — Frontend Integration Guide

Spring Boot API for short-term unit rentals. Base URL (local): `http://localhost:8080`.

**Auth header:** `Authorization: Bearer <accessToken>`
**Locale:** `Accept-Language: en` or `ar` (governorate/area/terms names)

Local OTP is fixed: `111111`. Seeded admin: phone `+201000000001`, password `Admin@123456`.

---

## Product flows

### Guest browse (no login)
1. Search locations: governorates/areas by text.
2. Search units by free text (governorate / area / street / title).
3. Filter by rent/day, category, amenities, availability dates; sort by price / rating / most rented.
4. Homepage: most rented units.
5. Open unit details, reviews, availability calendar.
6. **Login/register only when creating a booking.**

### Owner list a unit (login required)
1. Register/login.
2. Fetch active terms (`GET /api/terms`) and select the `OWNER_LISTING` terms.
3. Owner chooses `requiresOwnerApproval`. If false, also show and require acceptance of the seeded `INSTANT_BOOKING_COMMITMENT` terms (ID `1001`).
4. Submit unit in one call: details, terms, booking policy, photos, and ID front/back.
5. Listing is **ACTIVE immediately** — no admin/inspector approval.
6. Owner can mark date ranges unavailable.

### Booking lifecycle (login required)
```
approval unit: PENDING_OWNER_APPROVAL → owner approve → PENDING_PAYMENT
instant unit:                            PENDING_PAYMENT
                                                   ↓ pay
                                                  PAID → check-in → CHECKED_IN → check-out → CHECKED_OUT
                                                              ↓
                                         guest reviews unit → COMPLETED (+ unit may become verified)
```
- Owner rejects via `POST /api/bookings/{id}/reject`; status becomes `OWNER_REJECTED`.
- Pending approval expires to `APPROVAL_EXPIRED` after `booking_approval_expiration_hours` (default 24) from `app_config`. A daily job expires requests and releases their dates.
- Owner can review the renter after check-out.
- **Verified badge** on unit after first full cycle: paid + checked in + checked out + guest review/rating.

### Legacy flow (do not use in new UI)
Old inspection/approval APIs remain under `/api/legacy/**` for possible restoration. Tables kept; not part of the new product path.

---

## Public APIs (no token)

| Method | Path | Purpose |
|--------|------|---------|
| GET | `/health` | Liveness |
| GET | `/locations/governorates` | All governorates |
| GET | `/locations/governorates/{id}/areas` | Areas in governorate |
| GET | `/locations/governorates/search?q=` | Text search governorates |
| GET | `/locations/areas/search?q=` | Text search areas |
| GET | `/api/terms` | Active owner T&Cs |
| GET | `/api/terms/{id}` | Terms by id |
| GET | `/api/units/search?...` | Unified text search + filters + date availability |
| GET | `/api/units/most-rented?page&size` | Homepage list |
| GET | `/api/units/categories` | Localized category names and configured price ranges |
| GET | `/api/units/{id}` | Unit details |
| GET | `/api/units/{id}/reviews` | Unit reviews |
| GET | `/api/units/{id}/availability?from&to` | Unavailable ranges |

### Auth (public)

| Method | Path | Body notes |
|--------|------|------------|
| GET | `/auth/public-key` | RSA key if encrypting password/OTP |
| POST | `/auth/register` | `{ name, phone }` |
| POST | `/auth/verify-otp` | `{ identifier, otp }` → `registrationToken` |
| POST | `/auth/complete-registration` | `{ registrationToken, password }` → JWT |
| POST | `/auth/login` | `{ usernameOrPhone, password }` → JWT |
| POST | `/auth/refresh` | `{ refreshToken }` |
| POST | `/auth/forgot-password` / `/auth/reset-password` | OTP reset |

Plaintext password/OTP works locally (`decryptIfEncrypted`).

---

## Authenticated APIs

### Owner units

| Method | Path | Notes |
|--------|------|-------|
| POST | `/api/units` | Create + publish immediately |
| GET | `/api/units/mine` | Owner’s units |
| POST | `/api/units/{id}/unavailability` | `{ startDate, endDate, reason? }` |
| DELETE | `/api/units/{id}/unavailability/{blockId}` | Remove block |

### Bookings

| Method | Path | Notes |
|--------|------|-------|
| POST | `/api/bookings` | `{ unitId, checkInDate, checkOutDate }` |
| GET | `/api/bookings/mine` | As guest |
| GET | `/api/bookings/as-owner` | Bookings on my units |
| GET | `/api/bookings/{id}` | Guest or owner |
| POST | `/api/bookings/{id}/approve` | Owner: pending approval → pending payment |
| POST | `/api/bookings/{id}/reject` | Owner: optional `{ reason }` |
| POST | `/api/bookings/{id}/pay` | Stub payment → PAID |
| POST | `/api/bookings/{id}/check-in` | |
| POST | `/api/bookings/{id}/check-out` | |
| POST | `/api/bookings/{id}/cancel` | Pending approval, pending payment, or paid |
| POST | `/api/bookings/{id}/reviews/unit` | Guest: `{ rating 1-5, comment? }` |
| POST | `/api/bookings/{id}/reviews/renter` | Owner: `{ rating 1-5, comment? }` |

### Admin terms

| Method | Path | Role |
|--------|------|------|
| GET | `/api/terms/all` | ADMIN |
| POST | `/api/terms` | ADMIN create |
| PUT | `/api/terms/{id}` | ADMIN update (can activate; deactivates others) |

---

## Search / filter / sort (one API)

**`GET /api/units/search`** — all query params optional:

- `q` — free text: title, street, governorate/area names (ar/en)
- `minRent`, `maxRent` — rent per day
- `category` — `ECONOMY` | `PREMIUM` | `HOTEL`
- `furnishing` — `FURNISHED` | `SEMI_FURNISHED` | `UNFURNISHED`
- `roomsCount`, `governorateId`, `areaId`, `verified`
- Amenities (`true` = must have): `hasElevator`, `hasWashingMachine`, `hasWifi`, `hasAirConditioning`, `hasParking`, `hasPool`, `hasTv`, `hasKitchen`, `hasBalcony`, `hasWaterHeater`
- `arrivalDate`, `leaveDate` (ISO dates, optional but paired; leave date is exclusive) — excludes owner blocks and bookings
- `adultsCount` (optional, integer ≥ 1) — unit must allow at least this many adults
- `childrenCount` (optional, integer ≥ 0) — unit must allow at least this many children
- Backward-compatible aliases: `availableFrom`, `availableTo`
- `sort`: `PRICE_ASC` | `PRICE_DESC` | `RATING` | `MOST_RENTED` (default)
- `page`, `size`

Examples:
```
GET /api/units/search?q=Maadi&sort=RATING
GET /api/units/search?category=ECONOMY&hasElevator=true&minRent=100&maxRent=1000
GET /api/units/search?q=Road%209&arrivalDate=2026-09-01&leaveDate=2026-09-05&adultsCount=2&childrenCount=1&sort=PRICE_ASC
```

Responses are Spring `Page` objects: `{ content, totalElements, totalPages, number, size, ... }`.

**Availability calendar** returns list of:
```json
{ "id": 1, "unitId": 1, "startDate": "...", "endDate": "...", "reason": "...", "source": "OWNER|BOOKING" }
```
Booking nights are inclusive `[checkIn, checkOut - 1]`. Checkout day itself is free for the next guest.

---

## Create unit request body

```json
{
  "title": "Cozy Maadi Flat",
  "description": "Nice unit",
  "governorateId": 2,
  "areaId": 12,
  "streetName": "Road 9",
  "buildingNumber": "12",
  "apartmentNumber": "4B",
  "landmark": "Near metro",
  "latitude": 29.96,
  "longitude": 31.27,
  "roomsCount": 2,
  "bathroomsCount": 1,
  "areaSqm": 90,
  "furnishing": "FURNISHED",
  "rentPerDay": 450.00,
  "maxAdults": 4,
  "maxChildren": 2,
  "requiresOwnerApproval": true,
  "hasElevator": true,
  "hasWashingMachine": true,
  "hasWifi": true,
  "hasAirConditioning": true,
  "hasParking": false,
  "hasPool": false,
  "hasTv": true,
  "hasKitchen": true,
  "hasBalcony": true,
  "hasWaterHeater": true,
  "photos": ["https://cdn.example.com/1.jpg"],
  "idDocumentType": "NATIONAL_ID",
  "idFrontUrl": "https://cdn.example.com/id-front.jpg",
  "idBackUrl": "https://cdn.example.com/id-back.jpg",
  "termsDefinitionId": 1,
  "acceptTerms": true
}
```

Photos and ID images are **URLs** (upload to storage elsewhere; API stores references). No ownership evidence is required.

`maxAdults` is required and must be at least 1. `maxChildren` is required and must be at least 0.
Do not send `category` when creating a unit. The backend derives it from `rentPerDay` using the current DB-configured boundaries and returns it in `UnitResponse`.
For instant booking send `requiresOwnerApproval: false`, `instantBookingTermsId: 1001`, and `acceptInstantBookingTerms: true`.
Bookings must send `adultsCount` (at least 1); `childrenCount` defaults to 0. The API rejects a booking with HTTP 422 if either count exceeds the unit limit.

---

## Entities (relevant properties)

### `RentalUnit` (`rental_units`)
| Field | Type | Notes |
|-------|------|-------|
| id | Long | |
| owner | AuthUser | |
| title, description | String | |
| governorate, area | refs | |
| streetName, buildingNumber, apartmentNumber, landmark | String | |
| latitude, longitude | Decimal | optional |
| roomsCount, bathroomsCount, areaSqm | Int | |
| furnishing | enum | FURNISHED / SEMI_FURNISHED / UNFURNISHED |
| category | enum | ECONOMY / PREMIUM / HOTEL; derived from rentPerDay |
| rentPerDay | Decimal | |
| maxAdults | Int ≥ 1 | maximum allowed adults |
| maxChildren | Int ≥ 0 | maximum allowed children |
| hasElevator, hasWashingMachine, hasWifi, hasAirConditioning, hasParking, hasPool, hasTv, hasKitchen, hasBalcony, hasWaterHeater | boolean | |
| photos | JSON string[] | |
| idDocumentType | NATIONAL_ID / PASSPORT | |
| idFrontUrl, idBackUrl | String | |
| status | ACTIVE / PAUSED / ARCHIVED | |
| verified | boolean | after first completed guest cycle |
| averageRating, reviewCount, bookingCount | | denormalized for sort |
| termsDefinition, termsAcceptedAt | | |
| publishedAt, createdAt, updatedAt | Instant | |

### `UnitUnavailability`
`id`, `unit`, `startDate`, `endDate` (inclusive), `reason`, `createdAt`

### `Booking`
| Field | Notes |
|-------|-------|
| unit, guest | |
| checkInDate, checkOutDate | checkout exclusive |
| adultsCount, childrenCount | requested guests; enforced against unit capacity |
| totalAmount | rentPerDay × nights |
| status | PENDING_OWNER_APPROVAL, PENDING_PAYMENT, PAID, CHECKED_IN, CHECKED_OUT, COMPLETED, CANCELLED, OWNER_REJECTED, APPROVAL_EXPIRED |
| approvalExpiresAt, ownerDecidedAt, ownerRejectionReason | owner decision lifecycle |
| paidAt, checkedInAt, checkedOutAt | |
| guestReviewed, ownerReviewed | |

### `Review`
`booking`, `reviewer`, `reviewType` (`UNIT` \| `RENTER`), `unit` (for UNIT), `revieweeUser` (for RENTER), `rating` 1–5, `comment`, `createdAt`

### `UnitTermsConsent`
`unit`, `user`, `termsDefinition`, `acceptedAt`, `ipAddress`, `userAgent`

### `TermsDefinition`
`version`, `termsType` (`OWNER_LISTING` / `INSTANT_BOOKING_COMMITMENT`), `titleAr/En`, `contentAr/En`, `effectiveAt`, `active`

### Location
- `Governorate`: `id`, `nameAr`, `nameEn`
- `Area`: `id`, `governorate`, `nameAr`, `nameEn`

### Auth (existing)
`AuthUser`: `id`, `name`, `phone`, `username`, `verified`, `active`, roles (`USER`, `ADMIN`, `OWNER`, `RENTER`, `INSPECTOR`)

---

## Suggested UI screens → APIs

| Screen | APIs |
|--------|------|
| Home | `GET /api/units/most-rented` |
| Search / filters | `GET /api/units/search` (q + filters + dates) |
| Unit details | `GET /api/units/{id}`, `/reviews`, `/availability` |
| Book CTA | require login → `POST /api/bookings` |
| Owner list unit wizard | `GET /api/terms` → `POST /api/units` |
| Owner calendar | `POST/DELETE .../unavailability`, `GET .../availability` |
| Guest trips | `GET /api/bookings/mine` + pay/check-in/out/review |
| Owner bookings | `GET /api/bookings/as-owner` + review renter |

---

## Artifacts

- Postman: `postman/home-rental-service-new-flow.postman_collection.json`
- E2E script: `./e2e-new-flow.sh` (43 checks; all green against local Docker)
- Legacy Postman (inspection flow): `postman/home-rental-service.postman_collection.json` — paths now under `/api/legacy/...`

## Run locally

```bash
docker compose down -v   # fresh DB
docker compose up -d --build
./e2e-new-flow.sh
```
