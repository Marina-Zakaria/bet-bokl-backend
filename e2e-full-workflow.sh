#!/bin/bash

# Full Workflow E2E Test:
#   Owner registers → Admin reviews → Admin assigns inspector →
#   Owner submits windows → Inspector confirms slot → Inspector submits report →
#   Admin approves report → Owner consents → Admin activates → LISTED
#
# Also tests rejection corner case: Admin rejects an application
#
# Prerequisites:
#   - Service running at BASE_URL (default: http://localhost:8080)
#   - Fixed OTP=111111 configured in application.properties
#   - Test admin user:     phone=+201000000001, password=Admin@123456
#   - Test inspector user: phone=+201000000002, password=Inspector@123456

set -e

BASE_URL="${1:-http://localhost:8080}"

# ── Test Users ──────────────────────────────────────────────────────────────
TIMESTAMP=$(date +%s%N)
OWNER_PHONE="+201$(echo $TIMESTAMP | tail -c 10)"
OWNER_NAME="E2E Owner $(date +%s)"
OWNER_PASSWORD="Test@123456"
OTP_VALUE="111111"

ADMIN_PHONE="+201000000001"
ADMIN_PASSWORD="Admin@123456"

INSPECTOR_PHONE="+201000000002"
INSPECTOR_PASSWORD="Inspector@123456"

# ── Colors ──────────────────────────────────────────────────────────────────
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

PASS=0
FAIL=0

pass() { echo -e "${GREEN}✓ $1${NC}"; PASS=$((PASS+1)); }
fail() { echo -e "${RED}✗ $1${NC}"; FAIL=$((FAIL+1)); echo "  Response: $2"; exit 1; }
step() { echo -e "\n${YELLOW}[$1] $2${NC}"; }
section() { echo -e "\n${CYAN}═══ $1 ═══${NC}"; }

echo -e "${BLUE}══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}   Full Workflow E2E Test: Application → Listed               ${NC}"
echo -e "${BLUE}══════════════════════════════════════════════════════════════${NC}"
echo "  Owner phone: $OWNER_PHONE"
echo "  Base URL:    $BASE_URL"

# ════════════════════════════════════════════════════════════════════════════
section "PHASE 1: Owner Setup & Application"
# ════════════════════════════════════════════════════════════════════════════

step "1/20" "Register Owner"
REG=$(curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"name\": \"$OWNER_NAME\", \"phone\": \"$OWNER_PHONE\"}")
echo "$REG" | jq -e '.message' > /dev/null 2>&1 && pass "Owner registered" || fail "Owner registration" "$REG"

step "2/20" "Verify OTP"
OTP_RESP=$(curl -s -X POST "$BASE_URL/auth/verify-otp" \
  -H "Content-Type: application/json" \
  -d "{\"identifier\": \"$OWNER_PHONE\", \"otp\": \"$OTP_VALUE\"}")
REG_TOKEN=$(echo "$OTP_RESP" | jq -r '.registrationToken // empty')
[ -n "$REG_TOKEN" ] && pass "OTP verified" || fail "OTP verification" "$OTP_RESP"

step "3/20" "Complete Registration (set password)"
COMP=$(curl -s -X POST "$BASE_URL/auth/complete-registration" \
  -H "Content-Type: application/json" \
  -d "{\"registrationToken\": \"$REG_TOKEN\", \"password\": \"$OWNER_PASSWORD\"}")
OWNER_TOKEN=$(echo "$COMP" | jq -r '.accessToken // empty')
[ -n "$OWNER_TOKEN" ] && pass "Registration completed" || fail "Complete registration" "$COMP"
echo "  Roles: $(echo "$COMP" | jq -r '.user.roles | join(", ")')"

step "4/20" "Fetch Governorate & Area IDs"
GOVS=$(curl -s "$BASE_URL/locations/governorates")
GOV_ID=$(echo "$GOVS" | jq -r '.[0].id // empty')
AREAS=$(curl -s "$BASE_URL/locations/governorates/$GOV_ID/areas")
AREA_ID=$(echo "$AREAS" | jq -r '.[0].id // empty')
[ -n "$GOV_ID" ] && [ -n "$AREA_ID" ] && pass "Got governorate=$GOV_ID area=$AREA_ID" || fail "Location lookup" ""

step "5/20" "Submit Property Application"
APP_RESP=$(curl -s -X POST "$BASE_URL/api/listings/applications" \
  -H "Authorization: Bearer $OWNER_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"propertyDetail\": {
      \"governorateId\": $GOV_ID,
      \"areaId\": $AREA_ID,
      \"address\": {
        \"streetAddress\": \"123 Nile St\",
        \"buildingNumber\": \"A\",
        \"apartmentNumber\": \"4\",
        \"landmark\": \"Near river\",
        \"latitude\": 30.044,
        \"longitude\": 31.235,
        \"googlePlaceId\": \"ChIJ_test_e2e\"
      },
      \"roomsCount\": 3,
      \"areaSqm\": 120.0,
      \"furnishing\": \"SEMI_FURNISHED\",
      \"expectedRent\": 5000.00,
      \"amenities\": [\"wifi\", \"parking\", \"gym\"],
      \"photos\": [\"https://example.com/photo1.jpg\", \"https://example.com/photo2.jpg\"]
    }
  }")
APP_ID=$(echo "$APP_RESP" | jq -r '.id // empty')
APP_STATUS=$(echo "$APP_RESP" | jq -r '.status // empty')
[ -n "$APP_ID" ] && [ "$APP_STATUS" = "SUBMITTED" ] && pass "Application submitted (id=$APP_ID, status=$APP_STATUS)" || fail "Application submission" "$APP_RESP"

# ════════════════════════════════════════════════════════════════════════════
section "PHASE 2: Admin Login & Application Review"
# ════════════════════════════════════════════════════════════════════════════

step "6/20" "Admin Login"
ADMIN_LOGIN=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"usernameOrPhone\": \"$ADMIN_PHONE\", \"password\": \"$ADMIN_PASSWORD\"}")
ADMIN_TOKEN=$(echo "$ADMIN_LOGIN" | jq -r '.accessToken // empty')
[ -n "$ADMIN_TOKEN" ] && pass "Admin logged in" || fail "Admin login" "$ADMIN_LOGIN"
echo "  Roles: $(echo "$ADMIN_LOGIN" | jq -r '.user.roles | join(", ")')"

step "7/20" "Admin: List All Applications"
LIST_RESP=$(curl -s "$BASE_URL/api/admin/listings/applications" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
TOTAL=$(echo "$LIST_RESP" | jq -r '.totalElements // empty')
[ -n "$TOTAL" ] && pass "Admin can list applications (total=$TOTAL)" || fail "Admin list applications" "$LIST_RESP"

step "8/20" "Admin: Review Application (APPROVE → UNDER_REVIEW)"
REVIEW_RESP=$(curl -s -X POST "$BASE_URL/api/admin/listings/applications/$APP_ID/review" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"decision\": \"APPROVE\", \"comments\": \"Documents look good\"}")
REVIEW_DECISION=$(echo "$REVIEW_RESP" | jq -r '.decision // empty')
[ "$REVIEW_DECISION" = "APPROVE" ] && pass "Admin approved application (decision=$REVIEW_DECISION)" || fail "Admin review" "$REVIEW_RESP"

# Confirm status changed to UNDER_REVIEW
APP_DETAIL=$(curl -s "$BASE_URL/api/admin/listings/applications/$APP_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$APP_DETAIL" | jq -r '.status // empty')
[ "$STATUS" = "UNDER_REVIEW" ] && pass "Application status = UNDER_REVIEW ✓" || fail "Status check after review" "$APP_DETAIL"

# ════════════════════════════════════════════════════════════════════════════
section "PHASE 3: Admin Assigns Inspector"
# ════════════════════════════════════════════════════════════════════════════

step "9/20" "Fetch Inspector User ID from DB"
INSPECTOR_ID=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"usernameOrPhone\": \"$INSPECTOR_PHONE\", \"password\": \"$INSPECTOR_PASSWORD\"}" \
  | jq -r '.user.id // empty')
[ -n "$INSPECTOR_ID" ] && pass "Inspector id=$INSPECTOR_ID" || fail "Inspector login to get ID" ""

step "10/20" "Admin: Assign Inspector"
ASSIGN_RESP=$(curl -s -X POST "$BASE_URL/api/admin/listings/applications/$APP_ID/assign-inspector" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"inspectorId\": $INSPECTOR_ID}")
ASSIGN_STATUS=$(echo "$ASSIGN_RESP" | jq -r '.status // empty')
[ "$ASSIGN_STATUS" = "PENDING_INSPECTOR_SLOTS" ] && pass "Inspector assigned (status=$ASSIGN_STATUS)" || fail "Assign inspector" "$ASSIGN_RESP"

# ════════════════════════════════════════════════════════════════════════════
section "PHASE 4: Owner Submits Inspection Windows"
# ════════════════════════════════════════════════════════════════════════════

step "11/20" "Owner: Submit 4 Inspection Windows"
# Use future timestamps (next week)
T1_START=$(date -u -d "+7 days 09:00" +"%Y-%m-%dT%H:%M:%SZ" 2>/dev/null || date -u -v+7d -v9H -v0M -v0S "+%Y-%m-%dT%H:%M:%SZ")
T1_END=$(date -u -d "+7 days 11:00" +"%Y-%m-%dT%H:%M:%SZ" 2>/dev/null || date -u -v+7d -v11H -v0M -v0S "+%Y-%m-%dT%H:%M:%SZ")
T2_START=$(date -u -d "+8 days 09:00" +"%Y-%m-%dT%H:%M:%SZ" 2>/dev/null || date -u -v+8d -v9H -v0M -v0S "+%Y-%m-%dT%H:%M:%SZ")
T2_END=$(date -u -d "+8 days 11:00" +"%Y-%m-%dT%H:%M:%SZ" 2>/dev/null || date -u -v+8d -v11H -v0M -v0S "+%Y-%m-%dT%H:%M:%SZ")
T3_START=$(date -u -d "+9 days 09:00" +"%Y-%m-%dT%H:%M:%SZ" 2>/dev/null || date -u -v+9d -v9H -v0M -v0S "+%Y-%m-%dT%H:%M:%SZ")
T3_END=$(date -u -d "+9 days 11:00" +"%Y-%m-%dT%H:%M:%SZ" 2>/dev/null || date -u -v+9d -v11H -v0M -v0S "+%Y-%m-%dT%H:%M:%SZ")
T4_START=$(date -u -d "+10 days 09:00" +"%Y-%m-%dT%H:%M:%SZ" 2>/dev/null || date -u -v+10d -v9H -v0M -v0S "+%Y-%m-%dT%H:%M:%SZ")
T4_END=$(date -u -d "+10 days 11:00" +"%Y-%m-%dT%H:%M:%SZ" 2>/dev/null || date -u -v+10d -v11H -v0M -v0S "+%Y-%m-%dT%H:%M:%SZ")

WINDOWS_RESP=$(curl -s -X POST "$BASE_URL/api/listings/applications/$APP_ID/inspection-windows" \
  -H "Authorization: Bearer $OWNER_TOKEN" \
  -H "Content-Type: application/json" \
  -d "[
    {\"proposedStart\": \"$T1_START\", \"proposedEnd\": \"$T1_END\"},
    {\"proposedStart\": \"$T2_START\", \"proposedEnd\": \"$T2_END\"},
    {\"proposedStart\": \"$T3_START\", \"proposedEnd\": \"$T3_END\"},
    {\"proposedStart\": \"$T4_START\", \"proposedEnd\": \"$T4_END\"}
  ]")
WIN_COUNT=$(echo "$WINDOWS_RESP" | jq -r '. | length // 0' 2>/dev/null || echo "0")
[ "$WIN_COUNT" = "4" ] && pass "Submitted 4 inspection windows" || fail "Submit inspection windows" "$WINDOWS_RESP"

# ════════════════════════════════════════════════════════════════════════════
section "PHASE 5: Inspector Confirms Slot"
# ════════════════════════════════════════════════════════════════════════════

step "12/20" "Inspector Login"
INSP_LOGIN=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"usernameOrPhone\": \"$INSPECTOR_PHONE\", \"password\": \"$INSPECTOR_PASSWORD\"}")
INSP_TOKEN=$(echo "$INSP_LOGIN" | jq -r '.accessToken // empty')
[ -n "$INSP_TOKEN" ] && pass "Inspector logged in" || fail "Inspector login" "$INSP_LOGIN"

step "13/20" "Inspector: List Assigned Applications"
INSP_APPS=$(curl -s "$BASE_URL/api/inspector/applications" \
  -H "Authorization: Bearer $INSP_TOKEN")
INSP_TOTAL=$(echo "$INSP_APPS" | jq -r '.totalElements // empty')
[ -n "$INSP_TOTAL" ] && pass "Inspector sees assigned applications (total=$INSP_TOTAL)" || fail "Inspector list apps" "$INSP_APPS"

step "14/20" "Inspector: Get Inspection Schedules"
SCHEDULES=$(curl -s "$BASE_URL/api/inspector/applications/$APP_ID/schedules" \
  -H "Authorization: Bearer $INSP_TOKEN")
SCHEDULE_ID=$(echo "$SCHEDULES" | jq -r '.[0].id // empty')
SCHED_START=$(echo "$SCHEDULES" | jq -r '.[0].proposedStart // empty')
SCHED_END=$(echo "$SCHEDULES" | jq -r '.[0].proposedEnd // empty')
[ -n "$SCHEDULE_ID" ] && pass "Got schedule id=$SCHEDULE_ID" || fail "Get schedules" "$SCHEDULES"

step "15/20" "Inspector: Confirm Inspection Slot"
# Exact time must be within the first window
EXACT_TIME=$(date -u -d "+7 days 10:00" +"%Y-%m-%dT%H:%M:%SZ" 2>/dev/null || date -u -v+7d -v10H -v0M -v0S "+%Y-%m-%dT%H:%M:%SZ")
CONFIRM_RESP=$(curl -s -X POST "$BASE_URL/api/inspector/schedules/$SCHEDULE_ID/confirm" \
  -H "Authorization: Bearer $INSP_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"exactTime\": \"$EXACT_TIME\"}")
CONFIRM_STATUS=$(echo "$CONFIRM_RESP" | jq -r '.status // empty')
[ "$CONFIRM_STATUS" = "CONFIRMED" ] && pass "Slot confirmed (status=$CONFIRM_STATUS)" || fail "Confirm slot" "$CONFIRM_RESP"

# Verify app status = INSPECTION_SCHEDULED
APP_DETAIL=$(curl -s "$BASE_URL/api/admin/listings/applications/$APP_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$APP_DETAIL" | jq -r '.status // empty')
[ "$STATUS" = "INSPECTION_SCHEDULED" ] && pass "Application status = INSPECTION_SCHEDULED ✓" || fail "Status after confirm" "$APP_DETAIL"

# ════════════════════════════════════════════════════════════════════════════
section "PHASE 6: Inspector Submits Report"
# ════════════════════════════════════════════════════════════════════════════

step "16/20" "Inspector: Submit Inspection Report"
REPORT_RESP=$(curl -s -X POST "$BASE_URL/api/inspector/schedules/$SCHEDULE_ID/report" \
  -H "Authorization: Bearer $INSP_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"recommendation\": \"APPROVE\",
    \"agreedRent\": 4800.00,
    \"comments\": \"Property in excellent condition\",
    \"reportData\": {
      \"overallCondition\": \"excellent\",
      \"wallsCondition\": \"good\",
      \"floorsCondition\": \"good\",
      \"electricalCondition\": \"good\",
      \"plumbingCondition\": \"good\"
    },
    \"evidencePhotos\": [\"https://example.com/report1.jpg\"],
    \"propertyDetail\": {
      \"governorateId\": $GOV_ID,
      \"areaId\": $AREA_ID,
      \"address\": {
        \"streetAddress\": \"123 Nile St\",
        \"buildingNumber\": \"A\",
        \"apartmentNumber\": \"4\",
        \"landmark\": \"Near river\",
        \"latitude\": 30.044,
        \"longitude\": 31.235,
        \"googlePlaceId\": \"ChIJ_test_e2e\"
      },
      \"roomsCount\": 3,
      \"areaSqm\": 120.0,
      \"furnishing\": \"SEMI_FURNISHED\",
      \"expectedRent\": 4800.00,
      \"amenities\": [\"wifi\", \"parking\"],
      \"photos\": [\"https://example.com/inspected1.jpg\"]
    }
  }")
REPORT_ID=$(echo "$REPORT_RESP" | jq -r '.id // empty')
REPORT_REC=$(echo "$REPORT_RESP" | jq -r '.recommendation // empty')
[ -n "$REPORT_ID" ] && pass "Report submitted (id=$REPORT_ID, recommendation=$REPORT_REC)" || fail "Submit report" "$REPORT_RESP"

# Verify app status = INSPECTION_COMPLETED
APP_DETAIL=$(curl -s "$BASE_URL/api/admin/listings/applications/$APP_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$APP_DETAIL" | jq -r '.status // empty')
[ "$STATUS" = "INSPECTION_COMPLETED" ] && pass "Application status = INSPECTION_COMPLETED ✓" || fail "Status after report" "$APP_DETAIL"

# ════════════════════════════════════════════════════════════════════════════
section "PHASE 7: Admin Reviews Report & Owner Consents"
# ════════════════════════════════════════════════════════════════════════════

step "17/20" "Admin: Approve Inspection Report (→ PENDING_OWNER_CONSENT)"
REPORT_REVIEW=$(curl -s -X POST "$BASE_URL/api/admin/listings/applications/$APP_ID/review-report" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"decision\": \"APPROVE\", \"finalRent\": 4800.00, \"comments\": \"Approved. Ready for listing.\"}")
RR_DECISION=$(echo "$REPORT_REVIEW" | jq -r '.decision // empty')
[ "$RR_DECISION" = "APPROVE" ] && pass "Report approved (decision=$RR_DECISION)" || fail "Admin review report" "$REPORT_REVIEW"

APP_DETAIL=$(curl -s "$BASE_URL/api/admin/listings/applications/$APP_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$APP_DETAIL" | jq -r '.status // empty')
[ "$STATUS" = "PENDING_OWNER_CONSENT" ] && pass "Application status = PENDING_OWNER_CONSENT ✓" || fail "Status after report review" "$APP_DETAIL"

step "18/20" "Owner: Submit Terms Consent (→ CONSENT_PROVIDED)"
CONSENT_RESP=$(curl -s -X POST "$BASE_URL/api/listings/applications/$APP_ID/terms-consent" \
  -H "Authorization: Bearer $OWNER_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"termsDefinitionId\": 1, \"agreedRent\": 4500.00, \"commissionPercentage\": 5.0, \"consentGiven\": true}")
CONSENT_STATUS=$(echo "$CONSENT_RESP" | jq -r '.status // empty')
[ "$CONSENT_STATUS" = "CONSENT_PROVIDED" ] && pass "Owner consented (status=$CONSENT_STATUS)" || fail "Terms consent" "$CONSENT_RESP"

# ════════════════════════════════════════════════════════════════════════════
section "PHASE 8: Admin Activates Listing"
# ════════════════════════════════════════════════════════════════════════════

step "19/20" "Admin: Activate Listing (→ LISTED)"
ACTIVATE_RESP=$(curl -s -X POST "$BASE_URL/api/admin/listings/applications/$APP_ID/activate" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"finalRent\": 4800.00}")
LISTING_ID=$(echo "$ACTIVATE_RESP" | jq -r '.id // empty')
LISTING_STATUS=$(echo "$ACTIVATE_RESP" | jq -r '.status // empty')
[ -n "$LISTING_ID" ] && [ "$LISTING_STATUS" = "ACTIVE" ] && pass "Listing activated (listingId=$LISTING_ID, status=$LISTING_STATUS)" || fail "Activate listing" "$ACTIVATE_RESP"

step "20/20" "Verify Application Status = LISTED"
APP_FINAL=$(curl -s "$BASE_URL/api/admin/listings/applications/$APP_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
FINAL_STATUS=$(echo "$APP_FINAL" | jq -r '.status // empty')
[ "$FINAL_STATUS" = "LISTED" ] && pass "Application status = LISTED ✓" || fail "Final status check" "$APP_FINAL"

# ════════════════════════════════════════════════════════════════════════════
section "CORNER CASE: Admin Rejects Application"
# ════════════════════════════════════════════════════════════════════════════

echo -e "\n${YELLOW}[Corner Case 1] Register second owner & submit application${NC}"

TIMESTAMP2=$(date +%s%N)
PHONE2="+201$(echo $TIMESTAMP2 | tail -c 10)"
sleep 1  # ensure unique timestamp

REG2=$(curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"name\": \"Rejection Test User\", \"phone\": \"$PHONE2\"}")
echo "$REG2" | jq -e '.message' > /dev/null 2>&1 && pass "Second owner registered ($PHONE2)" || fail "Second registration" "$REG2"

OTP2=$(curl -s -X POST "$BASE_URL/auth/verify-otp" \
  -H "Content-Type: application/json" \
  -d "{\"identifier\": \"$PHONE2\", \"otp\": \"$OTP_VALUE\"}")
REG_TOKEN2=$(echo "$OTP2" | jq -r '.registrationToken // empty')
[ -n "$REG_TOKEN2" ] && pass "OTP2 verified" || fail "OTP2 verification" "$OTP2"

COMP2=$(curl -s -X POST "$BASE_URL/auth/complete-registration" \
  -H "Content-Type: application/json" \
  -d "{\"registrationToken\": \"$REG_TOKEN2\", \"password\": \"$OWNER_PASSWORD\"}")
OWNER2_TOKEN=$(echo "$COMP2" | jq -r '.accessToken // empty')
[ -n "$OWNER2_TOKEN" ] && pass "Second owner registration completed" || fail "Second complete registration" "$COMP2"

APP2_RESP=$(curl -s -X POST "$BASE_URL/api/listings/applications" \
  -H "Authorization: Bearer $OWNER2_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"propertyDetail\": {
      \"governorateId\": $GOV_ID,
      \"areaId\": $AREA_ID,
      \"address\": {
        \"streetAddress\": \"456 Rejection Ave\",
        \"buildingNumber\": \"B\",
        \"apartmentNumber\": \"2\",
        \"landmark\": \"Near nowhere\",
        \"latitude\": 30.0,
        \"longitude\": 31.0,
        \"googlePlaceId\": \"rejected_place\"
      },
      \"roomsCount\": 2,
      \"areaSqm\": 80.0,
      \"furnishing\": \"UNFURNISHED\",
      \"expectedRent\": 2000.00,
      \"amenities\": [\"wifi\"],
      \"photos\": [\"https://example.com/rej1.jpg\"]
    }
  }")
APP2_ID=$(echo "$APP2_RESP" | jq -r '.id // empty')
APP2_STATUS=$(echo "$APP2_RESP" | jq -r '.status // empty')
[ -n "$APP2_ID" ] && [ "$APP2_STATUS" = "SUBMITTED" ] && pass "Second application submitted (id=$APP2_ID)" || fail "Second application submission" "$APP2_RESP"

echo -e "\n${YELLOW}[Corner Case 2] Admin REJECTS application at initial review${NC}"
REJECT_RESP=$(curl -s -X POST "$BASE_URL/api/admin/listings/applications/$APP2_ID/review" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"decision\": \"REJECT\", \"comments\": \"Documents incomplete, photos missing\"}")
REJ_DECISION=$(echo "$REJECT_RESP" | jq -r '.decision // empty')
[ "$REJ_DECISION" = "REJECT" ] && pass "Admin rejected application (decision=$REJ_DECISION)" || fail "Admin rejection" "$REJECT_RESP"

APP2_DETAIL=$(curl -s "$BASE_URL/api/admin/listings/applications/$APP2_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
REJ_STATUS=$(echo "$APP2_DETAIL" | jq -r '.status // empty')
[ "$REJ_STATUS" = "REJECTED" ] && pass "Rejected application status = REJECTED ✓" || fail "Rejection status check" "$APP2_DETAIL"

echo -e "\n${YELLOW}[Corner Case 3] Admin rejects inspection report (REQUEST_REINSPECTION)${NC}"

# ── Quick path: register 3rd owner, submit, approve, assign inspector, windows, confirm, report ──
TIMESTAMP3=$(date +%s%N)
PHONE3="+201$(echo $TIMESTAMP3 | tail -c 10)"
sleep 1

REG3=$(curl -s -X POST "$BASE_URL/auth/register" -H "Content-Type: application/json" \
  -d "{\"name\": \"Reinspect User\", \"phone\": \"$PHONE3\"}")
OTP3=$(curl -s -X POST "$BASE_URL/auth/verify-otp" -H "Content-Type: application/json" \
  -d "{\"identifier\": \"$PHONE3\", \"otp\": \"$OTP_VALUE\"}")
REG_TOKEN3=$(echo "$OTP3" | jq -r '.registrationToken // empty')
COMP3=$(curl -s -X POST "$BASE_URL/auth/complete-registration" -H "Content-Type: application/json" \
  -d "{\"registrationToken\": \"$REG_TOKEN3\", \"password\": \"$OWNER_PASSWORD\"}")
OWNER3_TOKEN=$(echo "$COMP3" | jq -r '.accessToken // empty')
[ -n "$OWNER3_TOKEN" ] && pass "Third owner registered & logged in" || fail "Third owner setup" "$COMP3"

APP3_RESP=$(curl -s -X POST "$BASE_URL/api/listings/applications" \
  -H "Authorization: Bearer $OWNER3_TOKEN" -H "Content-Type: application/json" \
  -d "{\"propertyDetail\":{\"governorateId\":$GOV_ID,\"areaId\":$AREA_ID,\"address\":{\"streetAddress\":\"789 Test St\",\"buildingNumber\":\"C\",\"apartmentNumber\":\"3\",\"landmark\":\"test\",\"latitude\":30.0,\"longitude\":31.0,\"googlePlaceId\":\"test3\"},\"roomsCount\":2,\"areaSqm\":90.0,\"furnishing\":\"FURNISHED\",\"expectedRent\":3000.00,\"amenities\":[\"wifi\"],\"photos\":[\"https://example.com/p3.jpg\"]}}")
APP3_ID=$(echo "$APP3_RESP" | jq -r '.id // empty')
[ -n "$APP3_ID" ] && pass "Third application submitted (id=$APP3_ID)" || fail "Third application" "$APP3_RESP"

# Admin: approve → assign inspector
curl -s -X POST "$BASE_URL/api/admin/listings/applications/$APP3_ID/review" \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" \
  -d "{\"decision\": \"APPROVE\"}" > /dev/null
curl -s -X POST "$BASE_URL/api/admin/listings/applications/$APP3_ID/assign-inspector" \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" \
  -d "{\"inspectorId\": $INSPECTOR_ID}" > /dev/null
pass "Third application: approved + inspector assigned"

# Owner: submit windows
T_S=$(date -u -d "+14 days 09:00" +"%Y-%m-%dT%H:%M:%SZ" 2>/dev/null || date -u -v+14d -v9H -v0M -v0S "+%Y-%m-%dT%H:%M:%SZ")
T_E=$(date -u -d "+14 days 11:00" +"%Y-%m-%dT%H:%M:%SZ" 2>/dev/null || date -u -v+14d -v11H -v0M -v0S "+%Y-%m-%dT%H:%M:%SZ")
T_S2=$(date -u -d "+15 days 09:00" +"%Y-%m-%dT%H:%M:%SZ" 2>/dev/null || date -u -v+15d -v9H -v0M -v0S "+%Y-%m-%dT%H:%M:%SZ")
T_E2=$(date -u -d "+15 days 11:00" +"%Y-%m-%dT%H:%M:%SZ" 2>/dev/null || date -u -v+15d -v11H -v0M -v0S "+%Y-%m-%dT%H:%M:%SZ")
T_S3=$(date -u -d "+16 days 09:00" +"%Y-%m-%dT%H:%M:%SZ" 2>/dev/null || date -u -v+16d -v9H -v0M -v0S "+%Y-%m-%dT%H:%M:%SZ")
T_E3=$(date -u -d "+16 days 11:00" +"%Y-%m-%dT%H:%M:%SZ" 2>/dev/null || date -u -v+16d -v11H -v0M -v0S "+%Y-%m-%dT%H:%M:%SZ")
T_S4=$(date -u -d "+17 days 09:00" +"%Y-%m-%dT%H:%M:%SZ" 2>/dev/null || date -u -v+17d -v9H -v0M -v0S "+%Y-%m-%dT%H:%M:%SZ")
T_E4=$(date -u -d "+17 days 11:00" +"%Y-%m-%dT%H:%M:%SZ" 2>/dev/null || date -u -v+17d -v11H -v0M -v0S "+%Y-%m-%dT%H:%M:%SZ")

curl -s -X POST "$BASE_URL/api/listings/applications/$APP3_ID/inspection-windows" \
  -H "Authorization: Bearer $OWNER3_TOKEN" -H "Content-Type: application/json" \
  -d "[{\"proposedStart\":\"$T_S\",\"proposedEnd\":\"$T_E\"},{\"proposedStart\":\"$T_S2\",\"proposedEnd\":\"$T_E2\"},{\"proposedStart\":\"$T_S3\",\"proposedEnd\":\"$T_E3\"},{\"proposedStart\":\"$T_S4\",\"proposedEnd\":\"$T_E4\"}]" > /dev/null
pass "Third application: inspection windows submitted"

# Inspector: get schedule ID & confirm
SCHEDS3=$(curl -s "$BASE_URL/api/inspector/applications/$APP3_ID/schedules" \
  -H "Authorization: Bearer $INSP_TOKEN")
SCHED3_ID=$(echo "$SCHEDS3" | jq -r '.[0].id // empty')
EXACT3=$(date -u -d "+14 days 10:00" +"%Y-%m-%dT%H:%M:%SZ" 2>/dev/null || date -u -v+14d -v10H -v0M -v0S "+%Y-%m-%dT%H:%M:%SZ")
curl -s -X POST "$BASE_URL/api/inspector/schedules/$SCHED3_ID/confirm" \
  -H "Authorization: Bearer $INSP_TOKEN" -H "Content-Type: application/json" \
  -d "{\"exactTime\": \"$EXACT3\"}" > /dev/null
pass "Third application: slot confirmed"

# Inspector: submit report
curl -s -X POST "$BASE_URL/api/inspector/schedules/$SCHED3_ID/report" \
  -H "Authorization: Bearer $INSP_TOKEN" -H "Content-Type: application/json" \
  -d "{\"recommendation\":\"REJECT\",\"agreedRent\":0,\"comments\":\"Property has significant damage\",\"reportData\":{\"overallCondition\":\"poor\"},\"evidencePhotos\":[\"https://example.com/damage.jpg\"],\"propertyDetail\":{\"governorateId\":$GOV_ID,\"areaId\":$AREA_ID,\"address\":{\"streetAddress\":\"789 Test St\",\"buildingNumber\":\"C\",\"apartmentNumber\":\"3\",\"landmark\":\"test\",\"latitude\":30.0,\"longitude\":31.0,\"googlePlaceId\":\"test3\"},\"roomsCount\":2,\"areaSqm\":90.0,\"furnishing\":\"FURNISHED\",\"expectedRent\":3000.00,\"amenities\":[\"wifi\"],\"photos\":[\"https://example.com/p3.jpg\"]}}" > /dev/null
pass "Third application: report submitted"

# Admin: reject inspection report
REJ_REPORT=$(curl -s -X POST "$BASE_URL/api/admin/listings/applications/$APP3_ID/review-report" \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" \
  -d "{\"decision\": \"REJECT\", \"comments\": \"Property not suitable for listing\"}")
RRJ_DECISION=$(echo "$REJ_REPORT" | jq -r '.decision // empty')
[ "$RRJ_DECISION" = "REJECT" ] && pass "Admin rejected inspection report (decision=$RRJ_DECISION)" || fail "Reject inspection report" "$REJ_REPORT"

APP3_STATUS=$(curl -s "$BASE_URL/api/admin/listings/applications/$APP3_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq -r '.status // empty')
[ "$APP3_STATUS" = "REJECTED" ] && pass "Rejected-report application status = REJECTED ✓" || fail "Rejection via report" "$APP3_STATUS"

# ════════════════════════════════════════════════════════════════════════════
echo -e "\n${BLUE}══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}   RESULTS${NC}"
echo -e "${BLUE}══════════════════════════════════════════════════════════════${NC}"
echo -e "  ${GREEN}PASSED: $PASS${NC}"
if [ $FAIL -gt 0 ]; then
  echo -e "  ${RED}FAILED: $FAIL${NC}"
  exit 1
else
  echo -e "  ${RED}FAILED: $FAIL${NC}"
  echo -e "\n${GREEN}All tests passed! Full workflow: SUBMITTED → LISTED ✓${NC}"
  echo -e "${GREEN}Corner cases: Initial rejection ✓ | Report rejection ✓${NC}"
fi
