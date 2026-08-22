# Frontend integration: guest capacity, booking approval, and automatic categories

Base URL: `http://localhost:8080`

## 1. Guest capacity and date-aware search

Unit creation requires:

```json
{
  "maxAdults": 4,
  "maxChildren": 2
}
```

Booking creation requires `adultsCount`; `childrenCount` is optional and defaults to `0`.
The backend returns HTTP `422` when either count exceeds the selected unit's limits.

Public unified search:

```http
GET /api/units/search?arrivalDate=2026-09-10&leaveDate=2026-09-13&adultsCount=2&childrenCount=1
```

`leaveDate` is exclusive. All search parameters remain optional. `availableFrom` and
`availableTo` remain backward-compatible aliases.

## 2. Owner approval versus instant booking

Fetch active terms before submitting a unit:

```http
GET /api/terms
```

Each item has `termsType`. Use the active `OWNER_LISTING` item for every unit.
The seeded `INSTANT_BOOKING_COMMITMENT` item has ID `1001`.

For a unit that requires approval:

```json
{
  "requiresOwnerApproval": true,
  "termsDefinitionId": 1,
  "acceptTerms": true
}
```

For instant booking:

```json
{
  "requiresOwnerApproval": false,
  "instantBookingTermsId": 1001,
  "acceptInstantBookingTerms": true,
  "termsDefinitionId": 1,
  "acceptTerms": true
}
```

When approval is required, `POST /api/bookings` returns
`PENDING_OWNER_APPROVAL` and an `approvalExpiresAt` timestamp.

The owner acts through:

```http
POST /api/bookings/{bookingId}/approve
POST /api/bookings/{bookingId}/reject
Content-Type: application/json

{"reason":"Optional rejection reason"}
```

Approval changes the status to `PENDING_PAYMENT`. Rejection changes it to
`OWNER_REJECTED`. Overdue requests become `APPROVAL_EXPIRED`; they are visible
through the existing guest/owner booking APIs and no longer block unit dates.

The expiration period is controlled by the DB config key
`booking_approval_expiration_hours` (default `24`).

## 3. Automatic nightly-price categories

Do not send `category` to `POST /api/units`. Send only `rentPerDay`; the backend
assigns and returns the category.

Default ranges:

- `ECONOMY` / Economy / اقتصادي: price ≤ 1000
- `PREMIUM` / Premium / مميز: price > 1000 and ≤ 2000
- `HOTEL` / Hotel / فندقي: price > 2000

Retrieve localized names and the current configured ranges:

```http
GET /api/units/categories
```

Example response:

```json
[
  {
    "code": "ECONOMY",
    "nameEn": "Economy",
    "nameAr": "اقتصادي",
    "minimumNightlyPrice": 0,
    "minimumInclusive": true,
    "maximumNightlyPrice": 1000,
    "maximumInclusive": true
  },
  {
    "code": "PREMIUM",
    "nameEn": "Premium",
    "nameAr": "مميز",
    "minimumNightlyPrice": 1000,
    "minimumInclusive": false,
    "maximumNightlyPrice": 2000,
    "maximumInclusive": true
  },
  {
    "code": "HOTEL",
    "nameEn": "Hotel",
    "nameAr": "فندقي",
    "minimumNightlyPrice": 2000,
    "minimumInclusive": false,
    "maximumNightlyPrice": null,
    "maximumInclusive": false
  }
]
```

The DB configuration keys are:

- `unit_category_economy_max_nightly_price`
- `unit_category_premium_max_nightly_price`
- `unit_category_hotel_min_nightly_price`

The premium maximum and hotel minimum must have the same value. The premium
maximum must be greater than the economy maximum.

Filtering is unchanged except for the new values:

```http
GET /api/units/search?category=PREMIUM
GET /api/units/search?category=HOTEL&sort=PRICE_ASC
```

Unit detail, search, owner-unit, booking summary, and most-rented responses still
contain a `category` field. Its possible values are now `ECONOMY`, `PREMIUM`,
and `HOTEL`.

## Updated Postman collection

Import:

`postman/home-rental-service-new-flow.postman_collection.json`

It includes the category metadata endpoint, category-free unit submission,
instant-booking submission, and owner approve/reject requests.

## 4. Renter name and reviews

Authenticated users can retrieve a renter's public booking reputation:

```http
GET /api/renters/{renterId}/reviews
Authorization: Bearer <token>
```

The response contains `renterId`, `renterName`, `averageRating`, `reviewCount`,
and the newest-first `reviews` array. Each review contains its rating, optional
comment, reviewer name, and creation timestamp. Unknown renter IDs return `404`;
requests without authentication return `401`.
