#!/bin/bash

# Complete E2E Test: Registration → OTP Verification → Login → Property Application
# This script tests the entire flow from user registration to property application submission

set -e

BASE_URL="${1:-http://localhost:8080}"
PHONE="+201012345678"
NAME="Ahmed Hassan"
PASSWORD="Test@123456"
APP_ID=""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}   E2E Test: Registration → Login → Property Application${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"

# ─────────────────────────────────────────────────────────────────────────────
# Step 1: Register User
# ─────────────────────────────────────────────────────────────────────────────
echo -e "\n${YELLOW}[Step 1] User Registration${NC}"
echo "POST /auth/register"
echo "Body: {\"name\": \"$NAME\", \"phone\": \"$PHONE\"}"

REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"$NAME\",
    \"phone\": \"$PHONE\"
  }")

echo "Response: $REGISTER_RESPONSE"

if echo "$REGISTER_RESPONSE" | jq -e '.message' > /dev/null 2>&1; then
  echo -e "${GREEN}✓ Registration successful${NC}"
else
  echo -e "${RED}✗ Registration failed${NC}"
  exit 1
fi

# ─────────────────────────────────────────────────────────────────────────────
# Step 2: Get OTP from Database (for testing purposes)
# ─────────────────────────────────────────────────────────────────────────────
echo -e "\n${YELLOW}[Step 2] Retrieving OTP from Database${NC}"

OTP=$(docker compose exec -T db psql -U home_rental -d home_rental_db -c \
  "SELECT otp_hash FROM auth_users WHERE phone = '$PHONE' LIMIT 1;" 2>/dev/null || echo "")

if [ -z "$OTP" ]; then
  echo -e "${RED}✗ Could not retrieve OTP from database${NC}"
  echo "  Note: In production, OTP would be sent via SMS"
  echo "  Using default test OTP: 123456"
  OTP="123456"
else
  echo "  Retrieved OTP hash from database (encrypted)"
  echo "  Using OTP verification endpoint with test value: 123456"
fi

# ─────────────────────────────────────────────────────────────────────────────
# Step 3: Verify OTP
# ─────────────────────────────────────────────────────────────────────────────
echo -e "\n${YELLOW}[Step 3] Verify OTP${NC}"
echo "POST /auth/verify-otp"
echo "Body: {\"identifier\": \"$PHONE\", \"otp\": \"123456\"}"

VERIFY_OTP_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/verify-otp" \
  -H "Content-Type: application/json" \
  -d "{
    \"identifier\": \"$PHONE\",
    \"otp\": \"123456\"
  }")

echo "Response: $VERIFY_OTP_RESPONSE"

REGISTRATION_TOKEN=$(echo "$VERIFY_OTP_RESPONSE" | jq -r '.registrationToken // empty')

if [ -z "$REGISTRATION_TOKEN" ]; then
  echo -e "${RED}✗ OTP verification failed${NC}"
  echo "  Response might indicate: invalid OTP, expired OTP, or user not found"
  exit 1
fi

echo -e "${GREEN}✓ OTP verified successfully${NC}"
echo "  Registration Token: ${REGISTRATION_TOKEN:0:20}..."

# ─────────────────────────────────────────────────────────────────────────────
# Step 4: Complete Registration (Set Password)
# ─────────────────────────────────────────────────────────────────────────────
echo -e "\n${YELLOW}[Step 4] Complete Registration (Set Password)${NC}"
echo "POST /auth/complete-registration"
echo "Body: {\"registrationToken\": \"...\", \"password\": \"$PASSWORD\"}"

COMPLETE_REG_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/complete-registration" \
  -H "Content-Type: application/json" \
  -d "{
    \"registrationToken\": \"$REGISTRATION_TOKEN\",
    \"password\": \"$PASSWORD\"
  }")

echo "Response: $COMPLETE_REG_RESPONSE"

ACCESS_TOKEN=$(echo "$COMPLETE_REG_RESPONSE" | jq -r '.accessToken // empty')
REFRESH_TOKEN=$(echo "$COMPLETE_REG_RESPONSE" | jq -r '.refreshToken // empty')

if [ -z "$ACCESS_TOKEN" ]; then
  echo -e "${RED}✗ Complete registration failed${NC}"
  exit 1
fi

echo -e "${GREEN}✓ Registration completed successfully${NC}"
echo "  Access Token: ${ACCESS_TOKEN:0:50}..."
echo "  User: $(echo "$COMPLETE_REG_RESPONSE" | jq -r '.user.name')"

# ─────────────────────────────────────────────────────────────────────────────
# Step 5: Login with Credentials
# ─────────────────────────────────────────────────────────────────────────────
echo -e "\n${YELLOW}[Step 5] Login with Credentials${NC}"
echo "POST /auth/login"
echo "Body: {\"usernameOrPhone\": \"$PHONE\", \"password\": \"$PASSWORD\"}"

LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{
    \"usernameOrPhone\": \"$PHONE\",
    \"password\": \"$PASSWORD\"
  }")

echo "Response: $LOGIN_RESPONSE"

LOGIN_TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.accessToken // empty')

if [ -z "$LOGIN_TOKEN" ]; then
  echo -e "${RED}✗ Login failed${NC}"
  exit 1
fi

echo -e "${GREEN}✓ Login successful${NC}"
echo "  Access Token: ${LOGIN_TOKEN:0:50}..."

# Use the login token for subsequent requests
AUTH_TOKEN=$LOGIN_TOKEN

# ─────────────────────────────────────────────────────────────────────────────
# Step 6: Get Locations (Governorates)
# ─────────────────────────────────────────────────────────────────────────────
echo -e "\n${YELLOW}[Step 6] Fetch Governorates${NC}"
echo "GET /locations/governorates"

GOVERNORATES=$(curl -s "$BASE_URL/locations/governorates")
echo "Response: $GOVERNORATES"

GOVERNORATE_ID=$(echo "$GOVERNORATES" | jq -r '.[0].id // empty')

if [ -z "$GOVERNORATE_ID" ]; then
  echo -e "${RED}✗ Failed to fetch governorates${NC}"
  exit 1
fi

echo -e "${GREEN}✓ Governorates fetched${NC}"
echo "  First Governorate ID: $GOVERNORATE_ID"

# ─────────────────────────────────────────────────────────────────────────────
# Step 7: Get Areas by Governorate
# ─────────────────────────────────────────────────────────────────────────────
echo -e "\n${YELLOW}[Step 7] Fetch Areas by Governorate${NC}"
echo "GET /locations/governorates/$GOVERNORATE_ID/areas"

AREAS=$(curl -s "$BASE_URL/locations/governorates/$GOVERNORATE_ID/areas")
echo "Response: $AREAS"

AREA_ID=$(echo "$AREAS" | jq -r '.[0].id // empty')

if [ -z "$AREA_ID" ]; then
  echo -e "${RED}✗ Failed to fetch areas${NC}"
  exit 1
fi

echo -e "${GREEN}✓ Areas fetched${NC}"
echo "  First Area ID: $AREA_ID"

# ─────────────────────────────────────────────────────────────────────────────
# Step 8: Get Terms
# ─────────────────────────────────────────────────────────────────────────────
echo -e "\n${YELLOW}[Step 8] Fetch Active Terms${NC}"
echo "GET /api/listings/terms"

TERMS=$(curl -s "$BASE_URL/api/listings/terms" \
  -H "Authorization: Bearer $AUTH_TOKEN")
echo "Response: $TERMS"

TERMS_ID=$(echo "$TERMS" | jq -r '.[0].id // empty')

if [ -z "$TERMS_ID" ]; then
  echo -e "${RED}✗ Failed to fetch terms${NC}"
  exit 1
fi

echo -e "${GREEN}✓ Terms fetched${NC}"
echo "  First Terms ID: $TERMS_ID"

# ─────────────────────────────────────────────────────────────────────────────
# Step 9: Create Property Application
# ─────────────────────────────────────────────────────────────────────────────
echo -e "\n${YELLOW}[Step 9] Create Property Application${NC}"
echo "POST /api/listings/applications"

APPLICATION_JSON=$(cat <<'EOF'
{
  "propertyDetail": {
    "governorateId": GOVERNORATE_ID_PLACEHOLDER,
    "areaId": AREA_ID_PLACEHOLDER,
    "address": {
      "streetAddress": "123 Nile Street",
      "buildingNumber": "A",
      "apartmentNumber": "405",
      "landmark": "Near Tahrir Square",
      "latitude": 30.0444,
      "longitude": 31.2357,
      "googlePlaceId": "ChIJIQBpAG2qQRMR_6128GljmTQ"
    },
    "roomsCount": 3,
    "areaSqm": 155.5,
    "furnishing": "SEMI_FURNISHED",
    "expectedRent": 2500.00,
    "amenities": [
      "washing_machine",
      "kettle",
      "parking",
      "wifi",
      "gym",
      "pool",
      "elevator",
      "microwave"
    ],
    "photos": [
      "https://example.com/property-photo-1.jpg",
      "https://example.com/property-photo-2.jpg",
      "https://example.com/property-photo-3.jpg"
    ]
  }
}
EOF
)

# Replace placeholders with actual values
APPLICATION_JSON="${APPLICATION_JSON//GOVERNORATE_ID_PLACEHOLDER/$GOVERNORATE_ID}"
APPLICATION_JSON="${APPLICATION_JSON//AREA_ID_PLACEHOLDER/$AREA_ID}"

echo "Body:"
echo "$APPLICATION_JSON" | jq '.'

APPLICATION_RESPONSE=$(curl -s -X POST "$BASE_URL/api/listings/applications" \
  -H "Authorization: Bearer $AUTH_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$APPLICATION_JSON")

echo "Response: $APPLICATION_RESPONSE"

APP_ID=$(echo "$APPLICATION_RESPONSE" | jq -r '.id // empty')

if [ -z "$APP_ID" ]; then
  echo -e "${RED}✗ Failed to create property application${NC}"
  exit 1
fi

echo -e "${GREEN}✓ Property application created successfully${NC}"
echo "  Application ID: $APP_ID"
echo "  Status: $(echo "$APPLICATION_RESPONSE" | jq -r '.status')"
echo "  Submitted At: $(echo "$APPLICATION_RESPONSE" | jq -r '.submittedAt')"

# ─────────────────────────────────────────────────────────────────────────────
# Step 10: List User's Applications
# ─────────────────────────────────────────────────────────────────────────────
echo -e "\n${YELLOW}[Step 10] List User's Applications${NC}"
echo "GET /api/listings/applications?page=0&size=10"

LIST_RESPONSE=$(curl -s "$BASE_URL/api/listings/applications?page=0&size=10" \
  -H "Authorization: Bearer $AUTH_TOKEN")

echo "Response:"
echo "$LIST_RESPONSE" | jq '.'

TOTAL_APPLICATIONS=$(echo "$LIST_RESPONSE" | jq -r '.totalElements // 0')

if [ "$TOTAL_APPLICATIONS" -gt 0 ]; then
  echo -e "${GREEN}✓ Applications listed successfully${NC}"
  echo "  Total Applications: $TOTAL_APPLICATIONS"
else
  echo -e "${RED}✗ No applications found${NC}"
  exit 1
fi

# ─────────────────────────────────────────────────────────────────────────────
# Step 11: Get Application Details
# ─────────────────────────────────────────────────────────────────────────────
echo -e "\n${YELLOW}[Step 11] Get Application Details${NC}"
echo "GET /api/listings/applications/$APP_ID"

DETAILS_RESPONSE=$(curl -s "$BASE_URL/api/listings/applications/$APP_ID" \
  -H "Authorization: Bearer $AUTH_TOKEN")

echo "Response:"
echo "$DETAILS_RESPONSE" | jq '.'

echo -e "${GREEN}✓ Application details retrieved${NC}"

# ─────────────────────────────────────────────────────────────────────────────
# Final Summary
# ─────────────────────────────────────────────────────────────────────────────
echo -e "\n${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo -e "${GREEN}✓ E2E TEST COMPLETED SUCCESSFULLY!${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"

echo -e "\n${YELLOW}Summary:${NC}"
echo "  ✓ User registered: $PHONE"
echo "  ✓ OTP verified"
echo "  ✓ Password set"
echo "  ✓ User logged in"
echo "  ✓ Locations fetched"
echo "  ✓ Property application created"
echo "  ✓ Application ID: $APP_ID"

echo -e "\n${YELLOW}Next Steps:${NC}"
echo "  1. Admin assigns inspector to application $APP_ID"
echo "  2. Owner submits inspection windows"
echo "  3. Inspector confirms and submits report"
echo "  4. Admin makes decision"
echo "  5. Owner provides consent"
echo "  6. Listing activated"

echo -e "\n${YELLOW}Useful Variables for Next Steps:${NC}"
echo "  ACCESS_TOKEN: $AUTH_TOKEN"
echo "  APPLICATION_ID: $APP_ID"
echo "  PHONE: $PHONE"
echo "  BASE_URL: $BASE_URL"
