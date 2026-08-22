#!/usr/bin/env bash
# E2E test for the simplified unit/booking flow
set -euo pipefail

BASE="${1:-http://localhost:8080}"
OTP="111111"
PASS="Test@123456"
ADMIN_PHONE="+201000000001"
ADMIN_PASS="Admin@123456"
TS=$(date +%s)
OWNER_PHONE="+2019${TS: -8}"
GUEST_PHONE="+2018${TS: -8}"

GREEN='\033[0;32m'; RED='\033[0;31m'; YELLOW='\033[1;33m'; NC='\033[0m'
PASS_N=0; FAIL_N=0

check() {
  local label="$1" got="$2" expect="$3"
  if echo "$got" | grep -qE "$expect"; then
    echo -e "  ${GREEN}✓${NC} $label"
    PASS_N=$((PASS_N + 1))
  else
    echo -e "  ${RED}✗${NC} $label  [expected: $expect]"
    echo "    got: $(echo "$got" | head -c 400)"
    FAIL_N=$((FAIL_N + 1))
  fi
}

section() { echo -e "\n${YELLOW}══ $1 ══${NC}"; }

register_user() {
  local phone="$1" name="$2"
  curl -s -X POST "$BASE/auth/register" -H "Content-Type: application/json" \
    -d "{\"name\":\"$name\",\"phone\":\"$phone\"}" > /dev/null
  local reg
  reg=$(curl -s -X POST "$BASE/auth/verify-otp" -H "Content-Type: application/json" \
    -d "{\"identifier\":\"$phone\",\"otp\":\"$OTP\"}" | jq -r '.registrationToken')
  curl -s -X POST "$BASE/auth/complete-registration" -H "Content-Type: application/json" \
    -d "{\"registrationToken\":\"$reg\",\"password\":\"$PASS\"}" | jq -r '.accessToken'
}

echo "Owner phone: $OWNER_PHONE"
echo "Guest phone: $GUEST_PHONE"

section "Public browse (no auth)"
R=$(curl -s -o /dev/null -w "%{http_code}" "$BASE/health")
check "GET /health → 200" "$R" "200"

R=$(curl -s -o /dev/null -w "%{http_code}" "$BASE/locations/governorates")
check "GET /locations/governorates → 200" "$R" "200"

R=$(curl -s "$BASE/locations/governorates/search?q=Cairo")
check "search governorates Cairo" "$R" "Cairo"

R=$(curl -s "$BASE/locations/areas/search?q=Maadi")
check "search areas Maadi" "$R" "Maadi"

R=$(curl -s -o /dev/null -w "%{http_code}" "$BASE/api/terms")
check "GET /api/terms public → 200" "$R" "200"

R=$(curl -s -o /dev/null -w "%{http_code}" "$BASE/api/units/most-rented")
check "GET /api/units/most-rented public → 200" "$R" "200"

R=$(curl -s -o /dev/null -w "%{http_code}" "$BASE/api/units/filter?category=ECONOMY")
check "GET /api/units/filter public → 200" "$R" "200"

R=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE/api/units" -H "Content-Type: application/json" -d '{}')
check "POST /api/units without auth → 401" "$R" "401"

R=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE/api/bookings" -H "Content-Type: application/json" -d '{}')
check "POST /api/bookings without auth → 401" "$R" "401"

section "Register owner + guest"
OWNER_TOKEN=$(register_user "$OWNER_PHONE" "Owner User")
GUEST_TOKEN=$(register_user "$GUEST_PHONE" "Guest User")
check "owner token issued" "$OWNER_TOKEN" ".+"
check "guest token issued" "$GUEST_TOKEN" ".+"

TERMS_ID=$(curl -s "$BASE/api/terms" | jq -r '.[0].id')
GOV_ID=$(curl -s "$BASE/locations/governorates" | jq -r '.[] | select(.name=="Cairo") | .id')
AREA_ID=$(curl -s "$BASE/locations/governorates/$GOV_ID/areas" | jq -r '.[0].id')
check "terms id present" "$TERMS_ID" "[0-9]+"
check "cairo gov id present" "$GOV_ID" "[0-9]+"

section "Owner creates unit (live immediately)"
CREATE_BODY=$(cat <<EOF
{
  "title": "Cozy Maadi Flat",
  "description": "Nice unit near street",
  "governorateId": $GOV_ID,
  "areaId": $AREA_ID,
  "streetName": "Road 9",
  "buildingNumber": "12",
  "apartmentNumber": "4B",
  "roomsCount": 2,
  "bathroomsCount": 1,
  "areaSqm": 90,
  "furnishing": "FURNISHED",
  "category": "ECONOMY",
  "rentPerDay": 450.00,
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
  "photos": ["https://cdn.example.com/u1.jpg", "https://cdn.example.com/u2.jpg"],
  "idDocumentType": "NATIONAL_ID",
  "idFrontUrl": "https://cdn.example.com/id-front.jpg",
  "idBackUrl": "https://cdn.example.com/id-back.jpg",
  "termsDefinitionId": $TERMS_ID,
  "acceptTerms": true
}
EOF
)

UNIT_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/api/units" \
  -H "Authorization: Bearer $OWNER_TOKEN" -H "Content-Type: application/json" -d "$CREATE_BODY")
UNIT_CODE=$(echo "$UNIT_RESP" | tail -1)
UNIT_BODY=$(echo "$UNIT_RESP" | sed '$d')
check "POST /api/units → 201" "$UNIT_CODE" "201"
UNIT_ID=$(echo "$UNIT_BODY" | jq -r '.id')
check "unit active" "$(echo "$UNIT_BODY" | jq -r '.status')" "ACTIVE"
check "unit not verified yet" "$(echo "$UNIT_BODY" | jq -r '.verified')" "false"

section "Public search / filter / details"
R=$(curl -s "$BASE/api/units/search?q=Maadi&sort=PRICE_ASC")
check "search finds unit" "$R" "Cozy Maadi Flat"

R=$(curl -s "$BASE/api/units/search?q=Road%209")
check "search by street" "$R" "Road 9"

R=$(curl -s "$BASE/api/units/filter?category=ECONOMY&hasElevator=true&minRent=100&maxRent=1000&sort=RATING")
check "filter economy+elevator" "$R" "Cozy Maadi Flat"

R=$(curl -s "$BASE/api/units/$UNIT_ID")
check "public unit details" "$R" "Cozy Maadi Flat"

section "Owner unavailability + calendar"
FROM=$(date -u +%Y-%m-%d -d "+30 days")
TO=$(date -u +%Y-%m-%d -d "+32 days")
UNA=$(curl -s -w "\n%{http_code}" -X POST "$BASE/api/units/$UNIT_ID/unavailability" \
  -H "Authorization: Bearer $OWNER_TOKEN" -H "Content-Type: application/json" \
  -d "{\"startDate\":\"$FROM\",\"endDate\":\"$TO\",\"reason\":\"Maintenance\"}")
check "add unavailability → 201" "$(echo "$UNA" | tail -1)" "201"
BLOCK_ID=$(echo "$UNA" | sed '$d' | jq -r '.id')

CAL=$(curl -s "$BASE/api/units/$UNIT_ID/availability?from=$FROM&to=$TO")
check "calendar shows OWNER block" "$CAL" "OWNER"

FILTER_BLOCKED=$(curl -s "$BASE/api/units/filter?availableFrom=$FROM&availableTo=$(date -u +%Y-%m-%d -d '+33 days')&category=ECONOMY")
check "filter excludes unavailable unit" "$(echo "$FILTER_BLOCKED" | jq -r --arg id "$UNIT_ID" '[.content[] | select(.id == ($id|tonumber))] | length')" "^0$"

section "Guest booking lifecycle"
CHECK_IN=$(date -u +%Y-%m-%d -d "+10 days")
CHECK_OUT=$(date -u +%Y-%m-%d -d "+13 days")
BOOK=$(curl -s -w "\n%{http_code}" -X POST "$BASE/api/bookings" \
  -H "Authorization: Bearer $GUEST_TOKEN" -H "Content-Type: application/json" \
  -d "{\"unitId\":$UNIT_ID,\"checkInDate\":\"$CHECK_IN\",\"checkOutDate\":\"$CHECK_OUT\"}")
check "create booking → 201" "$(echo "$BOOK" | tail -1)" "201"
BOOKING_ID=$(echo "$BOOK" | sed '$d' | jq -r '.id')
check "booking pending payment" "$(echo "$BOOK" | sed '$d' | jq -r '.status')" "PENDING_PAYMENT"
check "total 3 nights * 450" "$(echo "$BOOK" | sed '$d' | jq -r '.totalAmount')" "1350"

# Overlap should fail
OVER=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE/api/bookings" \
  -H "Authorization: Bearer $GUEST_TOKEN" -H "Content-Type: application/json" \
  -d "{\"unitId\":$UNIT_ID,\"checkInDate\":\"$CHECK_IN\",\"checkOutDate\":\"$CHECK_OUT\"}")
check "overlapping booking → 409" "$OVER" "409"

PAY=$(curl -s -X POST "$BASE/api/bookings/$BOOKING_ID/pay" -H "Authorization: Bearer $GUEST_TOKEN")
check "pay → PAID" "$(echo "$PAY" | jq -r '.status')" "PAID"

CI=$(curl -s -X POST "$BASE/api/bookings/$BOOKING_ID/check-in" -H "Authorization: Bearer $GUEST_TOKEN")
check "check-in → CHECKED_IN" "$(echo "$CI" | jq -r '.status')" "CHECKED_IN"

CO=$(curl -s -X POST "$BASE/api/bookings/$BOOKING_ID/check-out" -H "Authorization: Bearer $GUEST_TOKEN")
check "check-out → CHECKED_OUT" "$(echo "$CO" | jq -r '.status')" "CHECKED_OUT"

section "Reviews + verified badge"
UR=$(curl -s -w "\n%{http_code}" -X POST "$BASE/api/bookings/$BOOKING_ID/reviews/unit" \
  -H "Authorization: Bearer $GUEST_TOKEN" -H "Content-Type: application/json" \
  -d '{"rating":5,"comment":"Great stay"}')
check "guest reviews unit → 201" "$(echo "$UR" | tail -1)" "201"

RR=$(curl -s -w "\n%{http_code}" -X POST "$BASE/api/bookings/$BOOKING_ID/reviews/renter" \
  -H "Authorization: Bearer $OWNER_TOKEN" -H "Content-Type: application/json" \
  -d '{"rating":4,"comment":"Good guest"}')
check "owner reviews renter → 201" "$(echo "$RR" | tail -1)" "201"

UNIT_AFTER=$(curl -s "$BASE/api/units/$UNIT_ID")
check "unit verified after full cycle" "$(echo "$UNIT_AFTER" | jq -r '.verified')" "true"
check "unit rating updated" "$(echo "$UNIT_AFTER" | jq -r '.averageRating')" "5"
check "booking completed" "$(curl -s -H "Authorization: Bearer $GUEST_TOKEN" "$BASE/api/bookings/$BOOKING_ID" | jq -r '.status')" "COMPLETED"

REVIEWS=$(curl -s "$BASE/api/units/$UNIT_ID/reviews")
check "public unit reviews" "$REVIEWS" "Great stay"

MOST=$(curl -s "$BASE/api/units/most-rented")
check "most rented includes unit" "$MOST" "Cozy Maadi Flat"

MINE=$(curl -s -H "Authorization: Bearer $OWNER_TOKEN" "$BASE/api/units/mine")
check "owner mine list" "$MINE" "Cozy Maadi Flat"

section "Admin terms update"
ADMIN_TOKEN=$(curl -s -X POST "$BASE/auth/login" -H "Content-Type: application/json" \
  -d "{\"usernameOrPhone\":\"$ADMIN_PHONE\",\"password\":\"$ADMIN_PASS\"}" | jq -r '.accessToken')
check "admin login" "$ADMIN_TOKEN" ".+"

UPD=$(curl -s -w "\n%{http_code}" -X PUT "$BASE/api/terms/$TERMS_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" \
  -d "{\"version\":\"1.0\",\"titleEn\":\"Updated Terms\",\"titleAr\":\"شروط محدثة\",\"contentEn\":\"Updated content\",\"contentAr\":\"محتوى محدث\",\"active\":true}")
check "admin update terms → 200" "$(echo "$UPD" | tail -1)" "200"
check "terms title updated" "$(curl -s "$BASE/api/terms" | jq -r '.[0].title')" "Updated Terms"

section "Legacy paths isolated"
R=$(curl -s -o /dev/null -w "%{http_code}" -H "Authorization: Bearer $OWNER_TOKEN" "$BASE/api/listings/applications")
check "old /api/listings path gone → 404" "$R" "404"
R=$(curl -s -o /dev/null -w "%{http_code}" -H "Authorization: Bearer $OWNER_TOKEN" "$BASE/api/legacy/listings/applications")
check "legacy listings reachable → 200" "$R" "200"

echo -e "\n${YELLOW}Result: ${PASS_N} passed, ${FAIL_N} failed${NC}"
if [ "$FAIL_N" -gt 0 ]; then exit 1; fi
