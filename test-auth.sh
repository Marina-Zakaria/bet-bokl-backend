#!/usr/bin/env bash
# Comprehensive API + Security Test Suite for home-rental-service
# Uses timestamp-based unique test data to avoid conflicts on re-runs

BASE=http://localhost:8080
PASS=0; FAIL=0
TS=$(date +%s)
PHONE="+1999${TS: -7}"
PHONE2="+1888${TS: -7}"
PHONE3="+1777${TS: -7}"
# Derived usernames — auto-assigned at complete-registration (strip leading +)
USERNAME="${PHONE:1}"
USERNAME3="${PHONE3:1}"

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; NC='\033[0m'; BOLD='\033[1m'

check() {
  local label="$1" got="$2" expect="$3"
  if echo "$got" | grep -qE "$expect"; then
    echo -e "  ${GREEN}✓${NC} $label"
    PASS=$((PASS + 1))
  else
    echo -e "  ${RED}✗${NC} $label  [expected: $expect]"
    echo "    got: $(echo "$got" | head -c 250)"
    FAIL=$((FAIL + 1))
  fi
}

section() { echo -e "\n${BOLD}══ $1 ══${NC}"; }

get_otp() {
  local phone="$1"
  local partial="${phone: -10}"
  docker logs home-rental-service-app 2>&1 \
    | grep "TODO: IMPLEMENT_OTP_SENDER" \
    | grep "$partial" \
    | tail -1 \
    | grep -oE 'otp=[0-9]{6}' \
    | cut -d= -f2
}

ACCESS_TOKEN=""
REFRESH_TOKEN=""
REGISTRATION_TOKEN=""

# ─────────────────────────────────────────────────────────────────────────────
section "[1] HEALTH CHECK"
R=$(curl -s -o /dev/null -w "%{http_code}" $BASE/health)
check "GET /health → 200"                                "$R" "200"
HDR=$(curl -sI $BASE/health)
check "X-Content-Type-Options: nosniff"                  "$HDR" "X-Content-Type-Options: nosniff"
check "X-Frame-Options: DENY (anti-clickjacking)"        "$HDR" "X-Frame-Options: DENY"
check "Cache-Control: no-store"                          "$HDR" "no-store"

# ─────────────────────────────────────────────────────────────────────────────
section "[2] REGISTER — happy path (name + phone only)"
R=$(curl -s -w "\n%{http_code}" -X POST $BASE/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Alice\",\"phone\":\"$PHONE\"}")
CODE=$(echo "$R" | tail -1); BODY=$(echo "$R" | sed '$d')
check "POST /auth/register → 200"                        "$CODE" "200"
check "response: OTP sent message"                       "$BODY" "OTP sent"

# ─────────────────────────────────────────────────────────────────────────────
section "[3] REGISTER — validation and conflict"
R=$(curl -s -w "\n%{http_code}" -X POST $BASE/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Dup\",\"phone\":\"$PHONE\"}")
check "duplicate phone → 409"                            "$(echo "$R" | tail -1)" "409"

R=$(curl -s -w "\n%{http_code}" -X POST $BASE/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":""}')
check "missing phone → 400"                              "$(echo "$R" | tail -1)" "400"

R=$(curl -s -w "\n%{http_code}" -X POST $BASE/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"X\",\"phone\":\"badphone\"}")
check "invalid phone format → 400"                       "$(echo "$R" | tail -1)" "400"

# ─────────────────────────────────────────────────────────────────────────────
section "[4] LOGIN BEFORE VERIFICATION"
R=$(curl -s -w "\n%{http_code}" -X POST $BASE/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"usernameOrPhone\":\"$PHONE\",\"password\":\"S3cur3P@ss!\"}")
CODE=$(echo "$R" | tail -1); BODY=$(echo "$R" | sed '$d')
check "login before OTP verify → 403"                    "$CODE" "403"
check "reason mentions 'verified'"                       "$BODY" "verified"

R=$(curl -s -w "\n%{http_code}" -X POST $BASE/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrPhone":"nobody99999","password":"x"}')
CODE=$(echo "$R" | tail -1); BODY=$(echo "$R" | sed '$d')
check "unknown user → 401 (not 404)"                     "$CODE" "401"
check "same generic message (anti-enumeration)"          "$BODY" "Invalid credentials"

# ─────────────────────────────────────────────────────────────────────────────
section "[5] OTP VERIFY FLOW"
OTP=$(get_otp "$PHONE")
if [ -n "$OTP" ]; then
  echo -e "  ${YELLOW}ℹ  OTP extracted from logs: $OTP${NC}"
  check "OTP is exactly 6 digits"                       "$OTP" "^[0-9]{6}$"

  R=$(curl -s -w "\n%{http_code}" -X POST $BASE/auth/verify-otp \
    -H "Content-Type: application/json" \
    -d "{\"identifier\":\"$PHONE\",\"otp\":\"000000\"}")
  CODE=$(echo "$R" | tail -1); BODY=$(echo "$R" | sed '$d')
  check "wrong OTP → 400"                               "$CODE" "400"
  check "wrong OTP error message"                       "$BODY" "Invalid OTP"

  R=$(curl -s -w "\n%{http_code}" -X POST $BASE/auth/verify-otp \
    -H "Content-Type: application/json" \
    -d "{\"identifier\":\"$PHONE\",\"otp\":\"$OTP\"}")
  CODE=$(echo "$R" | tail -1); BODY=$(echo "$R" | sed '$d')
  check "correct OTP → 200"                             "$CODE" "200"
  check "response has registrationToken"                "$BODY" "registrationToken"
  check "response has set-password message"             "$BODY" "set your password"

  REGISTRATION_TOKEN=$(echo "$BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d['registrationToken'])" 2>/dev/null || echo "")

  # OTP cannot be reused
  R=$(curl -s -w "\n%{http_code}" -X POST $BASE/auth/verify-otp \
    -H "Content-Type: application/json" \
    -d "{\"identifier\":\"$PHONE\",\"otp\":\"$OTP\"}")
  check "OTP reuse → 400 (one-time use)"                "$(echo "$R" | tail -1)" "400"
else
  echo -e "  ${RED}✗ Could not extract OTP — skipping OTP subtests${NC}"
  FAIL=$((FAIL + 6))
fi

# ─────────────────────────────────────────────────────────────────────────────
section "[6] COMPLETE REGISTRATION"
if [ -n "$REGISTRATION_TOKEN" ]; then
  R=$(curl -s -w "\n%{http_code}" -X POST $BASE/auth/complete-registration \
    -H "Content-Type: application/json" \
    -d "{\"registrationToken\":\"invalid-token-xxx\",\"password\":\"S3cur3P@ss!\"}")
  check "wrong registration token → 400"                "$(echo "$R" | tail -1)" "400"

  R=$(curl -s -w "\n%{http_code}" -X POST $BASE/auth/complete-registration \
    -H "Content-Type: application/json" \
    -d "{\"registrationToken\":\"$REGISTRATION_TOKEN\",\"password\":\"short\"}")
  check "password < 8 chars → 400"                      "$(echo "$R" | tail -1)" "400"

  R=$(curl -s -w "\n%{http_code}" -X POST $BASE/auth/complete-registration \
    -H "Content-Type: application/json" \
    -d "{\"registrationToken\":\"$REGISTRATION_TOKEN\",\"password\":\"S3cur3P@ss!\"}")
  CODE=$(echo "$R" | tail -1); BODY=$(echo "$R" | sed '$d')
  check "complete-registration → 200"                   "$CODE" "200"
  check "response has accessToken"                      "$BODY" "accessToken"
  check "response has refreshToken"                     "$BODY" "refreshToken"
  check "response has tokenType Bearer"                 "$BODY" "Bearer"
  check "auto-generated username matches phone digits"  "$BODY" "$USERNAME"
  check "response has roles array"                      "$BODY" "roles"
  check "default role USER assigned"                    "$BODY" "USER"

  ACCESS_TOKEN=$(echo "$BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d['accessToken'])" 2>/dev/null || echo "")
  REFRESH_TOKEN=$(echo "$BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d['refreshToken'])" 2>/dev/null || echo "")

  # Registration token cannot be reused
  R=$(curl -s -w "\n%{http_code}" -X POST $BASE/auth/complete-registration \
    -H "Content-Type: application/json" \
    -d "{\"registrationToken\":\"$REGISTRATION_TOKEN\",\"password\":\"AnotherP@ss1\"}")
  check "registration token reuse → 400"                "$(echo "$R" | tail -1)" "400"
else
  echo -e "  ${RED}✗ No registration token — skipping complete-registration subtests${NC}"
  FAIL=$((FAIL + 9))
fi

# ─────────────────────────────────────────────────────────────────────────────
if [ -n "$ACCESS_TOKEN" ] && [ -n "$REFRESH_TOKEN" ]; then
  section "[7] LOGIN AFTER REGISTRATION"
  R=$(curl -s -w "\n%{http_code}" -X POST $BASE/auth/login \
    -H "Content-Type: application/json" \
    -d "{\"usernameOrPhone\":\"$USERNAME\",\"password\":\"S3cur3P@ss!\"}")
  CODE=$(echo "$R" | tail -1); BODY=$(echo "$R" | sed '$d')
  check "login by auto-generated username → 200"        "$CODE" "200"
  check "login returns accessToken"                     "$BODY" "accessToken"

  R=$(curl -s -w "\n%{http_code}" -X POST $BASE/auth/login \
    -H "Content-Type: application/json" \
    -d "{\"usernameOrPhone\":\"$PHONE\",\"password\":\"S3cur3P@ss!\"}")
  check "login by phone number → 200"                   "$(echo "$R" | tail -1)" "200"

  R=$(curl -s -w "\n%{http_code}" -X POST $BASE/auth/login \
    -H "Content-Type: application/json" \
    -d "{\"usernameOrPhone\":\"$USERNAME\",\"password\":\"wrongpw\"}")
  CODE=$(echo "$R" | tail -1); BODY=$(echo "$R" | sed '$d')
  check "wrong password → 401"                          "$CODE" "401"
  check "generic error (anti-enumeration)"              "$BODY" "Invalid credentials"

  section "[8] PROTECTED ENDPOINTS — JWT enforcement"
  # no token
  R=$(curl -s -w "\n%{http_code}" -X POST $BASE/auth/logout \
    -H "Content-Type: application/json" \
    -d '{"refreshToken":"anything"}')
  check "logout without token → 401"                    "$(echo "$R" | tail -1)" "401"

  # tampered token
  FAKE="eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJoYWNrZXIiLCJyb2xlcyI6WyJBRE1JTiJdfQ.invalidsig"
  R=$(curl -s -w "\n%{http_code}" -X POST $BASE/auth/logout \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $FAKE" \
    -d '{"refreshToken":"anything"}')
  check "tampered JWT → 401"                            "$(echo "$R" | tail -1)" "401"

  # none algorithm
  NONE_JWT="eyJhbGciOiJub25lIn0.eyJzdWIiOiJhbGljZSIsInJvbGVzIjpbIkFETUlOIl19."
  R=$(curl -s -w "\n%{http_code}" -X POST $BASE/auth/logout \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $NONE_JWT" \
    -d '{"refreshToken":"anything"}')
  check "JWT 'none' algorithm → 401"                   "$(echo "$R" | tail -1)" "401"

  # valid token
  R=$(curl -s -w "\n%{http_code}" -X POST $BASE/auth/logout \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $ACCESS_TOKEN" \
    -d "{\"refreshToken\":\"$REFRESH_TOKEN\"}")
  CODE=$(echo "$R" | tail -1); BODY=$(echo "$R" | sed '$d')
  check "logout with valid token → 200"                 "$CODE" "200"
  check "logout response message"                       "$BODY" "Logged out"

  # revoked refresh token
  R=$(curl -s -w "\n%{http_code}" -X POST $BASE/auth/refresh \
    -H "Content-Type: application/json" \
    -d "{\"refreshToken\":\"$REFRESH_TOKEN\"}")
  check "refresh with revoked token → 401"              "$(echo "$R" | tail -1)" "401"

  section "[9] REFRESH TOKEN FLOW"
  R=$(curl -s -w "\n%{http_code}" -X POST $BASE/auth/login \
    -H "Content-Type: application/json" \
    -d "{\"usernameOrPhone\":\"$USERNAME\",\"password\":\"S3cur3P@ss!\"}")
  NEW_REFRESH=$(echo "$R" | sed '$d' | python3 -c "import sys,json; d=json.load(sys.stdin); print(d['refreshToken'])" 2>/dev/null || echo "")

  if [ -n "$NEW_REFRESH" ]; then
    R=$(curl -s -w "\n%{http_code}" -X POST $BASE/auth/refresh \
      -H "Content-Type: application/json" \
      -d "{\"refreshToken\":\"$NEW_REFRESH\"}")
    CODE=$(echo "$R" | tail -1); BODY=$(echo "$R" | sed '$d')
    check "refresh with valid token → 200"              "$CODE" "200"
    check "refresh returns accessToken"                 "$BODY" "accessToken"
    check "refresh tokenType is Bearer"                 "$BODY" "Bearer"
  fi

  R=$(curl -s -w "\n%{http_code}" -X POST $BASE/auth/refresh \
    -H "Content-Type: application/json" \
    -d '{"refreshToken":"unknowntoken123456"}')
  check "refresh with unknown token → 401"              "$(echo "$R" | tail -1)" "401"
fi

# ─────────────────────────────────────────────────────────────────────────────
section "[10] RESEND OTP FLOW"
curl -s -X POST $BASE/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Bob\",\"phone\":\"$PHONE2\"}" > /dev/null

R=$(curl -s -w "\n%{http_code}" -X POST $BASE/auth/resend-otp \
  -H "Content-Type: application/json" \
  -d "{\"identifier\":\"$PHONE2\"}")
CODE=$(echo "$R" | tail -1); BODY=$(echo "$R" | sed '$d')
check "resend OTP → 200"                                 "$CODE" "200"
check "resend response message"                          "$BODY" "resent"

R=$(curl -s -w "\n%{http_code}" -X POST $BASE/auth/resend-otp \
  -H "Content-Type: application/json" \
  -d '{"identifier":"+19999999999"}')
check "resend for unknown phone → 404"                   "$(echo "$R" | tail -1)" "404"

R=$(curl -s -w "\n%{http_code}" -X POST $BASE/auth/resend-otp \
  -H "Content-Type: application/json" \
  -d '{}')
check "resend missing identifier → 400"                  "$(echo "$R" | tail -1)" "400"

# ─────────────────────────────────────────────────────────────────────────────
section "[11] FORGOT / RESET PASSWORD"
R=$(curl -s -w "\n%{http_code}" -X POST $BASE/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d "{\"identifier\":\"$PHONE\"}")
CODE=$(echo "$R" | tail -1); BODY=$(echo "$R" | sed '$d')
check "forgot-password (known) → 200"                    "$CODE" "200"
check "forgot-password same message always"              "$BODY" "If an account"

R=$(curl -s -w "\n%{http_code}" -X POST $BASE/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"identifier":"+19999999999"}')
CODE=$(echo "$R" | tail -1); BODY=$(echo "$R" | sed '$d')
check "forgot-password (unknown) → 200 (anti-enumeration)" "$CODE" "200"
check "forgot-password unknown: same message"            "$BODY" "If an account"

R=$(curl -s -w "\n%{http_code}" -X POST $BASE/auth/reset-password \
  -H "Content-Type: application/json" \
  -d "{\"identifier\":\"$PHONE\",\"otp\":\"000000\",\"newPassword\":\"NewS3cur3P@ss!\"}")
check "reset with wrong OTP → 400"                       "$(echo "$R" | tail -1)" "400"

RESET_OTP=$(get_otp "$PHONE")
if [ -n "$RESET_OTP" ]; then
  R=$(curl -s -w "\n%{http_code}" -X POST $BASE/auth/reset-password \
    -H "Content-Type: application/json" \
    -d "{\"identifier\":\"$PHONE\",\"otp\":\"$RESET_OTP\",\"newPassword\":\"NewS3cur3P@ss!\"}")
  CODE=$(echo "$R" | tail -1); BODY=$(echo "$R" | sed '$d')
  check "reset with correct OTP → 200"                   "$CODE" "200"
  check "reset response message"                         "$BODY" "reset"

  R=$(curl -s -w "\n%{http_code}" -X POST $BASE/auth/login \
    -H "Content-Type: application/json" \
    -d "{\"usernameOrPhone\":\"$USERNAME\",\"password\":\"S3cur3P@ss!\"}")
  check "old password rejected after reset → 401"        "$(echo "$R" | tail -1)" "401"

  R=$(curl -s -w "\n%{http_code}" -X POST $BASE/auth/login \
    -H "Content-Type: application/json" \
    -d "{\"usernameOrPhone\":\"$USERNAME\",\"password\":\"NewS3cur3P@ss!\"}")
  check "new password accepted after reset → 200"        "$(echo "$R" | tail -1)" "200"
fi

# ─────────────────────────────────────────────────────────────────────────────
section "[12] SECURITY — ACCOUNT LOCKOUT (OWASP A07)"
curl -s -X POST $BASE/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Locker\",\"phone\":\"$PHONE3\"}" > /dev/null

LOCK_OTP=$(get_otp "$PHONE3")
if [ -n "$LOCK_OTP" ]; then
  LOCK_REG_RESP=$(curl -s -X POST $BASE/auth/verify-otp \
    -H "Content-Type: application/json" \
    -d "{\"identifier\":\"$PHONE3\",\"otp\":\"$LOCK_OTP\"}")
  LOCK_REG_TOKEN=$(echo "$LOCK_REG_RESP" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d['registrationToken'])" 2>/dev/null || echo "")

  if [ -n "$LOCK_REG_TOKEN" ]; then
    curl -s -X POST $BASE/auth/complete-registration \
      -H "Content-Type: application/json" \
      -d "{\"registrationToken\":\"$LOCK_REG_TOKEN\",\"password\":\"S3cur3P@ss!\"}" > /dev/null

    for i in 1 2 3 4 5; do
      curl -s -X POST $BASE/auth/login -H "Content-Type: application/json" \
        -d "{\"usernameOrPhone\":\"$USERNAME3\",\"password\":\"WRONGPASSWORD\"}" > /dev/null
    done

    R=$(curl -s -w "\n%{http_code}" -X POST $BASE/auth/login \
      -H "Content-Type: application/json" \
      -d "{\"usernameOrPhone\":\"$USERNAME3\",\"password\":\"WRONGPASSWORD\"}")
    CODE=$(echo "$R" | tail -1); BODY=$(echo "$R" | sed '$d')
    check "locked after 5 failures → 423"                "$CODE" "423"
    check "locked message returned"                      "$BODY" "locked"

    R=$(curl -s -w "\n%{http_code}" -X POST $BASE/auth/login \
      -H "Content-Type: application/json" \
      -d "{\"usernameOrPhone\":\"$USERNAME3\",\"password\":\"S3cur3P@ss!\"}")
    check "correct password also blocked when locked → 423" "$(echo "$R" | tail -1)" "423"
  else
    echo -e "  ${YELLOW}ℹ Skipping (could not extract lockout registration token)${NC}"
  fi
else
  echo -e "  ${YELLOW}ℹ Skipping (could not extract lockout OTP)${NC}"
fi

# ─────────────────────────────────────────────────────────────────────────────
section "[13] SECURITY — SQL INJECTION"
R=$(curl -s -w "\n%{http_code}" -X POST $BASE/auth/login \
  -H "Content-Type: application/json" \
  --data-raw '{"usernameOrPhone":"admin\u0027 OR \u00271\u0027=\u00271","password":"x"}')
check "SQL injection OR pattern → 401"                   "$(echo "$R" | tail -1)" "401"

R=$(curl -s -w "\n%{http_code}" -X POST $BASE/auth/login \
  -H "Content-Type: application/json" \
  --data-raw '{"usernameOrPhone":"1; DROP TABLE auth_users; --","password":"x"}')
check "SQL DROP TABLE injection → 401"                   "$(echo "$R" | tail -1)" "401"

# service must still work after injection
check "service alive after SQL injection"                "$(curl -s -o /dev/null -w '%{http_code}' $BASE/health)" "200"

# ─────────────────────────────────────────────────────────────────────────────
section "[14] SECURITY — INPUT VALIDATION"
R=$(curl -s -w "\n%{http_code}" -X POST $BASE/auth/login \
  -H "Content-Type: application/json" \
  -d 'not-json')
check "malformed JSON → 400"                             "$(echo "$R" | tail -1)" "400"

R=$(curl -s -w "\n%{http_code}" -X POST $BASE/auth/login \
  -H "Content-Type: application/json" \
  -d '{}')
check "empty body → 400"                                 "$(echo "$R" | tail -1)" "400"

R=$(curl -s -w "\n%{http_code}" -X POST $BASE/auth/login \
  -H "Content-Type: text/plain" \
  -d '{"usernameOrPhone":"x","password":"y"}')
check "wrong Content-Type → 4xx"                         "$(echo "$R" | tail -1)" "^4"

# ─────────────────────────────────────────────────────────────────────────────
section "[15] SECURITY — RESPONSE QUALITY"
HDR=$(curl -sI $BASE/health)
check "X-Trace-ID present (traceability)"                "$HDR" "X-Trace-ID"

if ! echo "$HDR" | grep -qi "^Server:"; then
  echo -e "  ${GREEN}✓${NC} Server header absent (no tech stack leak)"
  PASS=$((PASS + 1))
else
  echo -e "  ${RED}✗${NC} Server header exposed"
  FAIL=$((FAIL + 1))
fi

ERR=$(curl -s -X POST $BASE/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrPhone":"x","password":"y"}')
check "error body is JSON (has timestamp+status)"        "$ERR" "\"timestamp\""

if ! echo "$ERR" | grep -q "at com\."; then
  echo -e "  ${GREEN}✓${NC} No Java stack trace in error response"
  PASS=$((PASS + 1))
else
  echo -e "  ${RED}✗${NC} Java stack trace exposed in error response"
  FAIL=$((FAIL + 1))
fi

# ─────────────────────────────────────────────────────────────────────────────
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "${BOLD}  RESULTS:  ${GREEN}PASS: $PASS${NC}  ${RED}FAIL: $FAIL${NC}  TOTAL: $((PASS+FAIL))${NC}"
if [ "$FAIL" -eq 0 ]; then
  echo -e "  ${GREEN}${BOLD}ALL TESTS PASSED${NC}"
else
  echo -e "  ${RED}${BOLD}$FAIL test(s) failed${NC}"
fi
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

