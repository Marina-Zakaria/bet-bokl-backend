#!/usr/bin/env bash
# Contract-aligned E2E for marketplace APIs
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
    echo "    got: $(echo "$got" | head -c 500)"
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

section "Contract shapes — public"
R=$(curl -s "$BASE/health")
check "health JSON status UP" "$R" '"status"[[:space:]]*:[[:space:]]*"UP"'

R=$(curl -s "$BASE/locations/governorates")
check "governorates have nameAr" "$R" '"nameAr"'
check "governorates have nameEn" "$R" '"nameEn"'
check "search Cairo nameEn" "$(curl -s "$BASE/locations/governorates/search?q=Cairo")" '"nameEn"[[:space:]]*:[[:space:]]*"Cairo"'

R=$(curl -s "$BASE/locations/areas/search?q=Maadi")
check "areas have governorateId" "$R" '"governorateId"'
check "areas have nameEn Maadi" "$R" '"nameEn"[[:space:]]*:[[:space:]]*"Maadi"'

R=$(curl -s "$BASE/api/terms")
check "terms titleAr" "$R" '"titleAr"'
check "terms titleEn" "$R" '"titleEn"'
check "terms contentAr" "$R" '"contentAr"'
check "terms active" "$R" '"active"[[:space:]]*:[[:space:]]*true'

check "most-rented page envelope" "$(curl -s "$BASE/api/units/most-rented")" '"content"'
check "search public (no q)" "$(curl -s -o /dev/null -w '%{http_code}' "$BASE/api/units/search?category=ECONOMY")" "200"
CATEGORIES=$(curl -s "$BASE/api/units/categories")
check "categories expose Economy EN" "$CATEGORIES" '"code":"ECONOMY".*"nameEn":"Economy"'
check "categories expose Premium AR/EN" "$CATEGORIES" '"code":"PREMIUM".*"nameEn":"Premium".*"nameAr":"مميز"'
check "categories expose Hotel AR/EN" "$CATEGORIES" '"code":"HOTEL".*"nameEn":"Hotel".*"nameAr":"فندقي"'
check "economy configured max is 1000" "$(echo "$CATEGORIES" | jq -r '.[] | select(.code=="ECONOMY") | .maximumNightlyPrice')" "^1000"
check "premium configured max is 2000" "$(echo "$CATEGORIES" | jq -r '.[] | select(.code=="PREMIUM") | .maximumNightlyPrice')" "^2000"
check "POST units 401" "$(curl -s -o /dev/null -w '%{http_code}' -X POST "$BASE/api/units" -H 'Content-Type: application/json' -d '{}')" "401"

section "Auth + create unit"
OWNER_TOKEN=$(register_user "$OWNER_PHONE" "Owner User")
GUEST_TOKEN=$(register_user "$GUEST_PHONE" "Guest User")
check "owner token" "$OWNER_TOKEN" ".+"
check "guest token" "$GUEST_TOKEN" ".+"

LOGIN=$(curl -s -X POST "$BASE/auth/login" -H "Content-Type: application/json" \
  -d "{\"usernameOrPhone\":\"$OWNER_PHONE\",\"password\":\"$PASS\"}")
check "login has phone" "$LOGIN" '"phone"'
check "login has roles" "$LOGIN" '"roles"'

TERMS=$(curl -s "$BASE/api/terms")
TERMS_ID=$(echo "$TERMS" | jq -r 'first(.[] | select(.termsType=="OWNER_LISTING") | .id)')
INSTANT_TERMS_ID=$(echo "$TERMS" | jq -r '.[] | select(.termsType=="INSTANT_BOOKING_COMMITMENT") | .id')
check "instant booking terms seeded as id 1001" "$INSTANT_TERMS_ID" "^1001$"
GOV_ID=$(curl -s "$BASE/locations/governorates" | jq -r '.[] | select(.nameEn=="Cairo") | .id')
AREA_ID=$(curl -s "$BASE/locations/governorates/$GOV_ID/areas" | jq -r '.[0].id')

CREATE_BODY=$(cat <<EOF
{
  "title": "Cozy Maadi Flat",
  "description": "Bright 2-bedroom",
  "governorateId": $GOV_ID,
  "areaId": $AREA_ID,
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
  "photos": ["https://cdn.example.com/u1.jpg", "https://cdn.example.com/u2.jpg"],
  "idDocumentType": "NATIONAL_ID",
  "idFrontUrl": "https://cdn.example.com/id-front.jpg",
  "idBackUrl": "https://cdn.example.com/id-back.jpg",
  "termsDefinitionId": $TERMS_ID,
  "acceptTerms": true
}
EOF
)

UNIT_BODY=$(curl -s -X POST "$BASE/api/units" \
  -H "Authorization: Bearer $OWNER_TOKEN" -H "Content-Type: application/json" -d "$CREATE_BODY")
UNIT_ID=$(echo "$UNIT_BODY" | jq -r '.id')
check "unit nested governorate.nameEn" "$UNIT_BODY" '"governorate".*"nameEn"[[:space:]]*:[[:space:]]*"Cairo"|\"nameEn\"[[:space:]]*:[[:space:]]*\"Cairo\"'
check "unit nested owner.name" "$UNIT_BODY" '"owner"'
check "unit photos array" "$UNIT_BODY" 'cdn.example.com/u1'
check "unit status ACTIVE" "$(echo "$UNIT_BODY" | jq -r '.status')" "ACTIVE"
check "unit max adults" "$(echo "$UNIT_BODY" | jq -r '.maxAdults')" "^4$"
check "unit max children" "$(echo "$UNIT_BODY" | jq -r '.maxChildren')" "^2$"
check "unit requires owner approval" "$(echo "$UNIT_BODY" | jq -r '.requiresOwnerApproval')" "true"
check "unit derives ECONOMY from price" "$(echo "$UNIT_BODY" | jq -r '.category')" "ECONOMY"

INVALID_INSTANT=$(echo "$CREATE_BODY" | jq \
  --argjson termsId "$INSTANT_TERMS_ID" \
  '.title="Invalid instant unit" | .requiresOwnerApproval=false | .instantBookingTermsId=$termsId | .acceptInstantBookingTerms=false')
check "instant unit requires terms acceptance" "$(curl -s -o /dev/null -w '%{http_code}' -X POST "$BASE/api/units" \
  -H "Authorization: Bearer $OWNER_TOKEN" -H "Content-Type: application/json" -d "$INVALID_INSTANT")" "400"

INSTANT_CREATE=$(echo "$CREATE_BODY" | jq \
  --argjson termsId "$INSTANT_TERMS_ID" \
  '.title="Instant Booking Flat" | .requiresOwnerApproval=false | .instantBookingTermsId=$termsId | .acceptInstantBookingTerms=true')
INSTANT_UNIT=$(curl -s -X POST "$BASE/api/units" \
  -H "Authorization: Bearer $OWNER_TOKEN" -H "Content-Type: application/json" -d "$INSTANT_CREATE")
INSTANT_UNIT_ID=$(echo "$INSTANT_UNIT" | jq -r '.id')
check "instant unit stores commitment terms" "$(echo "$INSTANT_UNIT" | jq -r '.instantBookingTermsId')" "^1001$"
check "instant unit does not require approval" "$(echo "$INSTANT_UNIT" | jq -r '.requiresOwnerApproval')" "false"

ECONOMY_LIMIT_CREATE=$(echo "$CREATE_BODY" | jq '.title="Economy Limit Unit" | .rentPerDay=1000')
ECONOMY_LIMIT_UNIT=$(curl -s -X POST "$BASE/api/units" \
  -H "Authorization: Bearer $OWNER_TOKEN" -H "Content-Type: application/json" -d "$ECONOMY_LIMIT_CREATE")
check "unit derives ECONOMY at 1000 boundary" "$(echo "$ECONOMY_LIMIT_UNIT" | jq -r '.category')" "ECONOMY"

PREMIUM_CREATE=$(echo "$CREATE_BODY" | jq '.title="Premium Price Unit" | .rentPerDay=2000')
PREMIUM_UNIT=$(curl -s -X POST "$BASE/api/units" \
  -H "Authorization: Bearer $OWNER_TOKEN" -H "Content-Type: application/json" -d "$PREMIUM_CREATE")
check "unit derives PREMIUM at 2000 boundary" "$(echo "$PREMIUM_UNIT" | jq -r '.category')" "PREMIUM"

HOTEL_CREATE=$(echo "$CREATE_BODY" | jq '.title="Hotel Price Unit" | .rentPerDay=2000.01')
HOTEL_UNIT=$(curl -s -X POST "$BASE/api/units" \
  -H "Authorization: Bearer $OWNER_TOKEN" -H "Content-Type: application/json" -d "$HOTEL_CREATE")
check "unit derives HOTEL above 2000" "$(echo "$HOTEL_UNIT" | jq -r '.category')" "HOTEL"

DETAIL=$(curl -s "$BASE/api/units/$UNIT_ID")
check "detail nested area" "$DETAIL" '"area"'
check "detail owner.username" "$DETAIL" '"username"'

SEARCH=$(curl -s "$BASE/api/units/search?q=Maadi&sort=PRICE_ASC")
check "search page content" "$SEARCH" '"content"'
check "search finds flat" "$SEARCH" "Cozy Maadi Flat"

COMBINED=$(curl -s "$BASE/api/units/search?q=Maadi&category=ECONOMY&hasElevator=true&minRent=100&maxRent=1000&sort=RATING")
check "search+filter combined" "$COMBINED" "Cozy Maadi Flat"
check "search filters derived PREMIUM" "$(curl -s "$BASE/api/units/search?category=PREMIUM")" "Premium Price Unit"
check "search filters derived HOTEL" "$(curl -s "$BASE/api/units/search?category=HOTEL")" "Hotel Price Unit"

STREET=$(curl -s "$BASE/api/units/search?q=Road%209&hasWifi=true")
check "search street + amenity" "$STREET" "Road 9"

CAPACITY_OK=$(curl -s "$BASE/api/units/search?q=Maadi&adultsCount=4&childrenCount=2")
check "search capacity exact match" "$CAPACITY_OK" "Cozy Maadi Flat"

CAPACITY_ADULTS_TOO_HIGH=$(curl -s "$BASE/api/units/search?q=Maadi&adultsCount=5")
check "search excludes too many adults" "$(echo "$CAPACITY_ADULTS_TOO_HIGH" | jq -r --arg id "$UNIT_ID" '[.content[] | select(.id == ($id|tonumber))] | length')" "^0$"

CAPACITY_CHILDREN_TOO_HIGH=$(curl -s "$BASE/api/units/search?q=Maadi&childrenCount=3")
check "search excludes too many children" "$(echo "$CAPACITY_CHILDREN_TOO_HIGH" | jq -r --arg id "$UNIT_ID" '[.content[] | select(.id == ($id|tonumber))] | length')" "^0$"

check "search rejects adultsCount=0" "$(curl -s -o /dev/null -w '%{http_code}' "$BASE/api/units/search?adultsCount=0")" "400"
check "search rejects childrenCount=-1" "$(curl -s -o /dev/null -w '%{http_code}' "$BASE/api/units/search?childrenCount=-1")" "400"

MINE=$(curl -s -H "Authorization: Bearer $OWNER_TOKEN" "$BASE/api/units/mine?page=0&size=12")
check "mine is Spring page" "$MINE" '"totalElements"'
check "mine content has unit" "$MINE" "Cozy Maadi Flat"

section "Availability + booking lifecycle"
FROM=$(date -u +%Y-%m-%d -d "+30 days")
TO=$(date -u +%Y-%m-%d -d "+32 days")
UNA=$(curl -s -X POST "$BASE/api/units/$UNIT_ID/unavailability" \
  -H "Authorization: Bearer $OWNER_TOKEN" -H "Content-Type: application/json" \
  -d "{\"startDate\":\"$FROM\",\"endDate\":\"$TO\",\"reason\":\"Maintenance\"}")
check "unavailability source OWNER" "$UNA" '"source"[[:space:]]*:[[:space:]]*"OWNER"'

CAL=$(curl -s "$BASE/api/units/$UNIT_ID/availability?from=$FROM&to=$TO")
check "calendar requires from/to works" "$CAL" "OWNER"

DATE_FILTER=$(curl -s "$BASE/api/units/search?q=Maadi&category=ECONOMY&availableFrom=$FROM&availableTo=$(date -u +%Y-%m-%d -d '+33 days')")
check "search+dates excludes blocked" "$(echo "$DATE_FILTER" | jq -r --arg id "$UNIT_ID" '[.content[] | select(.id == ($id|tonumber))] | length')" "^0$"

OPEN_FROM=$(date -u +%Y-%m-%d -d "+10 days")
OPEN_TO=$(date -u +%Y-%m-%d -d "+13 days")
DATE_OK=$(curl -s "$BASE/api/units/search?q=Maadi&arrivalDate=$OPEN_FROM&leaveDate=$OPEN_TO&adultsCount=2&childrenCount=1")
check "search+arrival/leave+guests includes open unit" "$DATE_OK" "Cozy Maadi Flat"
check "search rejects arrival without leave" "$(curl -s -o /dev/null -w '%{http_code}' "$BASE/api/units/search?arrivalDate=$OPEN_FROM")" "400"
check "availability missing params → 400" "$(curl -s -o /dev/null -w '%{http_code}' "$BASE/api/units/$UNIT_ID/availability")" "400"

CHECK_IN=$(date -u +%Y-%m-%d -d "+10 days")
CHECK_OUT=$(date -u +%Y-%m-%d -d "+13 days")
BOOK=$(curl -s -X POST "$BASE/api/bookings" \
  -H "Authorization: Bearer $GUEST_TOKEN" -H "Content-Type: application/json" \
  -d "{\"unitId\":$UNIT_ID,\"checkInDate\":\"$CHECK_IN\",\"checkOutDate\":\"$CHECK_OUT\",\"adultsCount\":2,\"childrenCount\":1}")
BOOKING_ID=$(echo "$BOOK" | jq -r '.id')
GUEST_ID=$(echo "$BOOK" | jq -r '.guest.id')
check "booking nested unit.title" "$BOOK" '"unit"'
check "booking nested guest" "$BOOK" '"guest"'
check "booking pending owner approval" "$(echo "$BOOK" | jq -r '.status')" "PENDING_OWNER_APPROVAL"
check "booking has approval expiry" "$(echo "$BOOK" | jq -r '.approvalExpiresAt')" "[0-9]{4}-"
check "totalAmount 1350" "$(echo "$BOOK" | jq -r '.totalAmount')" "1350"
check "booking adults persisted" "$(echo "$BOOK" | jq -r '.adultsCount')" "^2$"
check "booking children persisted" "$(echo "$BOOK" | jq -r '.childrenCount')" "^1$"

INSTANT_IN=$(date -u +%Y-%m-%d -d "+40 days")
INSTANT_OUT=$(date -u +%Y-%m-%d -d "+42 days")
INSTANT_BOOK=$(curl -s -X POST "$BASE/api/bookings" \
  -H "Authorization: Bearer $GUEST_TOKEN" -H "Content-Type: application/json" \
  -d "{\"unitId\":$INSTANT_UNIT_ID,\"checkInDate\":\"$INSTANT_IN\",\"checkOutDate\":\"$INSTANT_OUT\",\"adultsCount\":2,\"childrenCount\":0}")
check "instant booking skips owner approval" "$(echo "$INSTANT_BOOK" | jq -r '.status')" "PENDING_PAYMENT"

OVER_CAPACITY_IN=$(date -u +%Y-%m-%d -d "+20 days")
OVER_CAPACITY_OUT=$(date -u +%Y-%m-%d -d "+22 days")
check "booking rejects too many adults" "$(curl -s -o /dev/null -w '%{http_code}' -X POST "$BASE/api/bookings" \
  -H "Authorization: Bearer $GUEST_TOKEN" -H "Content-Type: application/json" \
  -d "{\"unitId\":$UNIT_ID,\"checkInDate\":\"$OVER_CAPACITY_IN\",\"checkOutDate\":\"$OVER_CAPACITY_OUT\",\"adultsCount\":5,\"childrenCount\":0}")" "422"
check "booking rejects too many children" "$(curl -s -o /dev/null -w '%{http_code}' -X POST "$BASE/api/bookings" \
  -H "Authorization: Bearer $GUEST_TOKEN" -H "Content-Type: application/json" \
  -d "{\"unitId\":$UNIT_ID,\"checkInDate\":\"$OVER_CAPACITY_IN\",\"checkOutDate\":\"$OVER_CAPACITY_OUT\",\"adultsCount\":2,\"childrenCount\":3}")" "422"

REJECT_BOOK=$(curl -s -X POST "$BASE/api/bookings" \
  -H "Authorization: Bearer $GUEST_TOKEN" -H "Content-Type: application/json" \
  -d "{\"unitId\":$UNIT_ID,\"checkInDate\":\"$OVER_CAPACITY_IN\",\"checkOutDate\":\"$OVER_CAPACITY_OUT\",\"adultsCount\":2,\"childrenCount\":0}")
REJECT_BOOKING_ID=$(echo "$REJECT_BOOK" | jq -r '.id')
REJECTED=$(curl -s -X POST "$BASE/api/bookings/$REJECT_BOOKING_ID/reject" \
  -H "Authorization: Bearer $OWNER_TOKEN" -H "Content-Type: application/json" \
  -d '{"reason":"Owner unavailable"}')
check "owner rejection status" "$(echo "$REJECTED" | jq -r '.status')" "OWNER_REJECTED"
check "owner rejection reason returned" "$REJECTED" "Owner unavailable"

check "guest cannot approve" "$(curl -s -o /dev/null -w '%{http_code}' -X POST "$BASE/api/bookings/$BOOKING_ID/approve" \
  -H "Authorization: Bearer $GUEST_TOKEN")" "403"
APPROVED=$(curl -s -X POST "$BASE/api/bookings/$BOOKING_ID/approve" -H "Authorization: Bearer $OWNER_TOKEN")
check "owner approval moves to payment" "$(echo "$APPROVED" | jq -r '.status')" "PENDING_PAYMENT"
check "owner decision timestamp returned" "$(echo "$APPROVED" | jq -r '.ownerDecidedAt')" "[0-9]{4}-"

PAY=$(curl -s -X POST "$BASE/api/bookings/$BOOKING_ID/pay" -H "Authorization: Bearer $GUEST_TOKEN")
check "pay PAID" "$(echo "$PAY" | jq -r '.status')" "PAID"
CI=$(curl -s -X POST "$BASE/api/bookings/$BOOKING_ID/check-in" -H "Authorization: Bearer $GUEST_TOKEN")
check "check-in" "$(echo "$CI" | jq -r '.status')" "CHECKED_IN"
CO=$(curl -s -X POST "$BASE/api/bookings/$BOOKING_ID/check-out" -H "Authorization: Bearer $GUEST_TOKEN")
check "check-out" "$(echo "$CO" | jq -r '.status')" "CHECKED_OUT"

UR=$(curl -s -X POST "$BASE/api/bookings/$BOOKING_ID/reviews/unit" \
  -H "Authorization: Bearer $GUEST_TOKEN" -H "Content-Type: application/json" \
  -d '{"rating":5,"comment":"Great stay"}')
check "review unit returns Booking" "$UR" '"guestReviewed"[[:space:]]*:[[:space:]]*true'
check "review unit COMPLETED" "$(echo "$UR" | jq -r '.status')" "COMPLETED"

RR=$(curl -s -X POST "$BASE/api/bookings/$BOOKING_ID/reviews/renter" \
  -H "Authorization: Bearer $OWNER_TOKEN" -H "Content-Type: application/json" \
  -d '{"rating":4,"comment":"Good guest"}')
check "review renter returns Booking" "$RR" '"ownerReviewed"[[:space:]]*:[[:space:]]*true'

check "renter reviews endpoint requires login" "$(curl -s -o /dev/null -w '%{http_code}' \
  "$BASE/api/renters/$GUEST_ID/reviews")" "401"
RENTER_REVIEWS=$(curl -s -H "Authorization: Bearer $OWNER_TOKEN" \
  "$BASE/api/renters/$GUEST_ID/reviews")
check "renter profile returns name" "$(echo "$RENTER_REVIEWS" | jq -r '.renterName')" "Guest User"
check "renter profile returns review count" "$(echo "$RENTER_REVIEWS" | jq -r '.reviewCount')" "^1$"
check "renter profile returns average rating" "$(echo "$RENTER_REVIEWS" | jq -r '.averageRating')" "^4"
check "renter profile returns owner review" "$RENTER_REVIEWS" "Good guest"

REVIEWS=$(curl -s "$BASE/api/units/$UNIT_ID/reviews")
check "unit reviews nested reviewer.name" "$REVIEWS" '"reviewer"'
check "unit reviews comment" "$REVIEWS" "Great stay"

UNIT_AFTER=$(curl -s "$BASE/api/units/$UNIT_ID")
check "verified badge" "$(echo "$UNIT_AFTER" | jq -r '.verified')" "true"

GUEST_PAGE=$(curl -s -H "Authorization: Bearer $GUEST_TOKEN" "$BASE/api/bookings/mine?page=0&size=12")
check "bookings/mine is page" "$GUEST_PAGE" '"content"'
OWNER_PAGE=$(curl -s -H "Authorization: Bearer $OWNER_TOKEN" "$BASE/api/bookings/as-owner?page=0&size=12")
check "bookings/as-owner is page" "$OWNER_PAGE" '"totalElements"'

section "CORS preflight"
CORS=$(curl -s -o /dev/null -w "%{http_code}" -X OPTIONS "$BASE/api/units/most-rented" \
  -H "Origin: http://localhost:8081" \
  -H "Access-Control-Request-Method: GET")
check "CORS preflight OK" "$CORS" "200|204"

echo -e "\n${YELLOW}Result: ${PASS_N} passed, ${FAIL_N} failed${NC}"
[ "$FAIL_N" -eq 0 ]
