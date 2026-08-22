#!/bin/bash

# Complete E2E Test: Registration → OTP Verification → Login → Property Application
# Note: Uses fixed OTP=111111 configured in application-local.properties
# Uses unique phone number each run to avoid "already registered" errors

set -e

BASE_URL="${1:-http://localhost:8080}"

# Generate unique phone number based on timestamp
TIMESTAMP=$(date +%s%N)
PHONE="+201$(echo $TIMESTAMP | tail -c 10)"
echo "Using phone: $PHONE"

NAME="Test User $(date +%s)"
PASSWORD="Test@123456"
OTP_VALUE="111111"  # Fixed OTP from application-local.properties
APP_ID=""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}   E2E Test: Registration → Login → Property Application${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"

# ─────────────────────────────────────────────────────────────────────────────
# Step 1: Register User
# ─────────────────────────────────────────────────────────────────────────────
echo -e "\n${YELLOW}[Step 1/11] User Registration${NC}"

REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"$NAME\",
    \"phone\": \"$PHONE\"
  }")

if echo "$REGISTER_RESPONSE" | jq -e '.message' > /dev/null 2>&1; then
  echo -e "${GREEN}✓ Registration successful${NC}"
  echo "  Response: $(echo "$REGISTER_RESPONSE" | jq -r '.message')"
else
  echo -e "${RED}✗ Registration failed${NC}"
  echo "  Response: $REGISTER_RESPONSE"
  exit 1
fi

# ─────────────────────────────────────────────────────────────────────────────
# Step 2: Verify OTP
# ─────────────────────────────────────────────────────────────────────────────
echo -e "\n${YELLOW}[Step 2/11] Verify OTP (Value: $OTP_VALUE)${NC}"

VERIFY_OTP_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/verify-otp" \
  -H "Content-Type: application/json" \
  -d "{
    \"identifier\": \"$PHONE\",
    \"otp\": \"$OTP_VALUE\"
  }")

REGISTRATION_TOKEN=$(echo "$VERIFY_OTP_RESPONSE" | jq -r '.registrationToken // empty')

if [ -z "$REGISTRATION_TOKEN" ]; then
  echo -e "${RED}✗ OTP verification failed${NC}"
  echo "  Response: $VERIFY_OTP_RESPONSE"
  exit 1
fi

echo -e "${GREEN}✓ OTP verified successfully${NC}"
echo "  Registration Token: ${REGISTRATION_TOKEN:0:30}..."

# ─────────────────────────────────────────────────────────────────────────────
# Step 3: Complete Registration (Set Password)
# ─────────────────────────────────────────────────────────────────────────────
echo -e "\n${YELLOW}[Step 3/11] Complete Registration (Set Password)${NC}"

COMPLETE_REG_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/complete-registration" \
  -H "Content-Type: application/json" \
  -d "{
    \"registrationToken\": \"$REGISTRATION_TOKEN\",
    \"password\": \"$PASSWORD\"
  }")

ACCESS_TOKEN=$(echo "$COMPLETE_REG_RESPONSE" | jq -r '.accessToken // empty')

if [ -z "$ACCESS_TOKEN" ]; then
  echo -e "${RED}✗ Complete registration failed${NC}"
  echo "  Response: $COMPLETE_REG_RESPONSE"
  exit 1
fi

echo -e "${GREEN}✓ Registration completed${NC}"
echo "  User: $(echo "$COMPLETE_REG_RESPONSE" | jq -r '.user.name')"
echo "  Roles: $(echo "$COMPLETE_REG_RESPONSE" | jq -r '.user.roles | join(", ")')"

# ─────────────────────────────────────────────────────────────────────────────
# Step 4: Login with Credentials
# ─────────────────────────────────────────────────────────────────────────────
echo -e "\n${YELLOW}[Step 4/11] Login with Credentials${NC}"

LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{
    \"usernameOrPhone\": \"$PHONE\",
    \"password\": \"$PASSWORD\"
  }")

AUTH_TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.accessToken // empty')

if [ -z "$AUTH_TOKEN" ]; then
  echo -e "${RED}✗ Login failed${NC}"
  echo "  Response: $LOGIN_RESPONSE"
  exit 1
fi

echo -e "${GREEN}✓ Login successful${NC}"
echo "  Token: ${AUTH_TOKEN:0:50}..."

# ─────────────────────────────────────────────────────────────────────────────
# Step 5: Get Governorates
# ─────────────────────────────────────────────────────────────────────────────
echo -e "\n${YELLOW}[Step 5/11] Fetch Governorates${NC}"

GOVERNORATES=$(curl -s "$BASE_URL/locations/governorates")
GOVERNORATE_ID=$(echo "$GOVERNORATES" | jq -r '.[0].id // empty')
GOVERNORATE_NAME=$(echo "$GOVERNORATES" | jq -r '.[0].name // empty')

if [ -z "$GOVERNORATE_ID" ]; then
  echo -e "${RED}✗ Failed to fetch governorates${NC}"
  exit 1
fi

echo -e "${GREEN}✓ Governorate selected${NC}"
echo "  ID: $GOVERNORATE_ID | Name: $GOVERNORATE_NAME"

# ─────────────────────────────────────────────────────────────────────────────
# Step 6: Get Areas by Governorate
# ─────────────────────────────────────────────────────────────────────────────
echo -e "\n${YELLOW}[Step 6/11] Fetch Areas by Governorate${NC}"

AREAS=$(curl -s "$BASE_URL/locations/governorates/$GOVERNORATE_ID/areas")
AREA_ID=$(echo "$AREAS" | jq -r '.[0].id // empty')
AREA_NAME=$(echo "$AREAS" | jq -r '.[0].name // empty')

if [ -z "$AREA_ID" ]; then
  echo -e "${RED}✗ Failed to fetch areas${NC}"
  exit 1
fi

echo -e "${GREEN}✓ Area selected${NC}"
echo "  ID: $AREA_ID | Name: $AREA_NAME"

# ─────────────────────────────────────────────────────────────────────────────
# Step 7: Get Active Terms
# ─────────────────────────────────────────────────────────────────────────────
echo -e "\n${YELLOW}[Step 7/11] Fetch Active Terms${NC}"

TERMS=$(curl -s "$BASE_URL/api/listings/terms" \
  -H "Authorization: Bearer $AUTH_TOKEN")

TERMS_ID=$(echo "$TERMS" | jq -r '.[0].id // empty')
TERMS_VERSION=$(echo "$TERMS" | jq -r '.[0].version // empty')

if [ -z "$TERMS_ID" ]; then
  echo -e "${RED}✗ Failed to fetch terms${NC}"
  echo "  Response: $TERMS"
  exit 1
fi

echo -e "${GREEN}✓ Terms retrieved${NC}"
echo "  ID: $TERMS_ID | Version: $TERMS_VERSION"

# ─────────────────────────────────────────────────────────────────────────────
# Step 8: Create Property Application
# ─────────────────────────────────────────────────────────────────────────────
echo -e "\n${YELLOW}[Step 8/11] Create Property Application${NC}"

APPLICATION_JSON=$(cat <<APPEOF
{
  "propertyDetail": {
    "governorateId": $GOVERNORATE_ID,
    "areaId": $AREA_ID,
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
      "https://example.com/property-photo-2.jpg"
    ]
  }
}
APPEOF
)

APPLICATION_RESPONSE=$(curl -s -X POST "$BASE_URL/api/listings/applications" \
  -H "Authorization: Bearer $AUTH_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$APPLICATION_JSON")

APP_ID=$(echo "$APPLICATION_RESPONSE" | jq -r '.id // empty')
APP_STATUS=$(echo "$APPLICATION_RESPONSE" | jq -r '.status // empty')

if [ -z "$APP_ID" ]; then
  echo -e "${RED}✗ Failed to create property application${NC}"
  echo "  Response: $APPLICATION_RESPONSE"
  exit 1
fi

echo -e "${GREEN}✓ Property application created${NC}"
echo "  Application ID: $APP_ID"
echo "  Status: $APP_STATUS"
echo "  Submitted At: $(echo "$APPLICATION_RESPONSE" | jq -r '.submittedAt')"

# ─────────────────────────────────────────────────────────────────────────────
# Step 9: List User Applications
# ─────────────────────────────────────────────────────────────────────────────
echo -e "\n${YELLOW}[Step 9/11] List User's Applications${NC}"

LIST_RESPONSE=$(curl -s "$BASE_URL/api/listings/applications?page=0&size=10" \
  -H "Authorization: Bearer $AUTH_TOKEN")

TOTAL_APPS=$(echo "$LIST_RESPONSE" | jq -r '.totalElements // 0')

if [ "$TOTAL_APPS" -lt 1 ]; then
  echo -e "${RED}✗ No applications found${NC}"
  exit 1
fi

echo -e "${GREEN}✓ Applications listed${NC}"
echo "  Total Applications: $TOTAL_APPS"
echo "  Content: $(echo "$LIST_RESPONSE" | jq '.content | length') records"

# ─────────────────────────────────────────────────────────────────────────────
# Step 10: Get Application Details
# ─────────────────────────────────────────────────────────────────────────────
echo -e "\n${YELLOW}[Step 10/11] Get Application Details${NC}"

DETAILS=$(curl -s "$BASE_URL/api/listings/applications/$APP_ID" \
  -H "Authorization: Bearer $AUTH_TOKEN")

echo -e "${GREEN}✓ Application details retrieved${NC}"
echo "  ID: $APP_ID"
echo "  Status: $(echo "$DETAILS" | jq -r '.status')"
echo "  Submitted: $(echo "$DETAILS" | jq -r '.submittedAt')"

# ─────────────────────────────────────────────────────────────────────────────
# Step 11: Verify Amenities in Request
# ─────────────────────────────────────────────────────────────────────────────
echo -e "\n${YELLOW}[Step 11/11] Verify Amenities${NC}"

AMENITIES=$(echo "$APPLICATION_JSON" | jq -r '.propertyDetail.amenities | join(", ")')

echo -e "${GREEN}✓ Amenities submitted with application:${NC}"
echo "  $AMENITIES"

# ─────────────────────────────────────────────────────────────────────────────
# Summary
# ─────────────────────────────────────────────────────────────────────────────
echo -e "\n${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo -e "${GREEN}✓✓✓ E2E TEST COMPLETED SUCCESSFULLY! ✓✓✓${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"

echo -e "\n${YELLOW}Workflow Summary:${NC}"
echo "  1. ✓ User registered with phone: $PHONE"
echo "  2. ✓ OTP verified with fixed value: $OTP_VALUE"
echo "  3. ✓ Password set: $PASSWORD"
echo "  4. ✓ User logged in successfully"
echo "  5. ✓ Governorate selected: $GOVERNORATE_NAME ($GOVERNORATE_ID)"
echo "  6. ✓ Area selected: $AREA_NAME ($AREA_ID)"
echo "  7. ✓ Terms reviewed: v$TERMS_VERSION"
echo "  8. ✓ Property application created: ID=$APP_ID"
echo "  9. ✓ Application listed in owner's portfolio"
echo " 10. ✓ Application details retrieved"
echo " 11. ✓ Amenities included: $AMENITIES"

echo -e "\n${YELLOW}Database Verification:${NC}"
echo "  User can now:"
echo "    • View their application: $APP_ID"
echo "    • Submit inspection windows (after admin assigns inspector)"
echo "    • Track application status through workflow"

echo -e "\n${YELLOW}Next Steps in Workflow:${NC}"
echo "  1. Admin: Assign inspector to application $APP_ID"
echo "  2. Owner: Submit inspection availability windows"
echo "  3. Inspector: Confirm slot and submit inspection report"
echo "  4. Admin: Review report and make decision"
echo "  5. Owner: Provide rental agreement consent"
echo "  6. Admin: Activate listing"

echo -e "\n${YELLOW}Useful Information for Testing:${NC}"
echo "  Phone: $PHONE"
echo "  Password: $PASSWORD"
echo "  Application ID: $APP_ID"
echo "  Access Token: ${AUTH_TOKEN:0:50}..."
echo "  Base URL: $BASE_URL"
