#!/usr/bin/bash
set -u
BASE="http://localhost:8080"
RUN_ID=$(date +%s)
PASS="qaPass123"
LOG="/c/Users/jyr/Desktop/study/dev/portfolio/MoneyLog/QA/retest_output.log"
: > "$LOG"

TMP_BODY=$(mktemp)
TMP_RESP=$(mktemp)
HTTP_CODE=""
RESP_BODY=""

log() { printf '%s\n' "$1" >> "$LOG"; }
section() { log ""; log "===== $1 ====="; }

call() {
  local method="$1" path="$2" token="$3" json="${4:-}"
  : > "$TMP_BODY"
  local hdrs=(-H "Content-Type: application/json")
  if [ -n "$token" ]; then hdrs+=(-H "Authorization: Bearer $token"); fi
  if [ -n "$json" ]; then
    printf '%s' "$json" > "$TMP_BODY"
    HTTP_CODE=$(curl -s -o "$TMP_RESP" -w "%{http_code}" -X "$method" "${hdrs[@]}" --data-binary "@$TMP_BODY" "$BASE$path")
  else
    HTTP_CODE=$(curl -s -o "$TMP_RESP" -w "%{http_code}" -X "$method" "${hdrs[@]}" "$BASE$path")
  fi
  RESP_BODY=$(cat "$TMP_RESP")
  log "REQ: $method $path (token=${token:0:12}...) BODY=$json"
  log "RESP[$HTTP_CODE]: $RESP_BODY"
}

callraw() {
  # for raw non-json body
  local method="$1" path="$2" token="$3" raw="${4:-}"
  : > "$TMP_BODY"
  local hdrs=(-H "Content-Type: application/json")
  if [ -n "$token" ]; then hdrs+=(-H "Authorization: Bearer $token"); fi
  printf '%s' "$raw" > "$TMP_BODY"
  HTTP_CODE=$(curl -s -o "$TMP_RESP" -w "%{http_code}" -X "$method" "${hdrs[@]}" --data-binary "@$TMP_BODY" "$BASE$path")
  RESP_BODY=$(cat "$TMP_RESP")
  log "REQ: $method $path (token=${token:0:12}...) RAW=$raw"
  log "RESP[$HTTP_CODE]: $RESP_BODY"
}

########################################
section "DEF-003/DEF-002 재현용 계정 userD 생성(카테고리+지출 보유)"
EMAIL_D="qa_retestD_${RUN_ID}@test.local"
call POST /api/users/signup "" "{\"email\":\"$EMAIL_D\",\"userName\":\"재검증D\",\"password\":\"$PASS\"}"
call POST /api/users/login "" "{\"email\":\"$EMAIL_D\",\"password\":\"$PASS\"}"
TOKEN_D=$(echo "$RESP_BODY" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
log "TOKEN_D=$TOKEN_D"

section "카테고리 생성"
call POST /api/categories "$TOKEN_D" "{\"name\":\"재검증카테고리\"}"
CAT_ID=$(echo "$RESP_BODY" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
log "CAT_ID=$CAT_ID"

section "지출 생성"
call POST /api/expenses "$TOKEN_D" "{\"categoryId\":$CAT_ID,\"amount\":1000,\"content\":\"재검증지출\",\"memo\":\"\"}"

section "[DEF-003 재검증] 카테고리+지출 보유 계정 탈퇴 시도"
call DELETE /api/users/me "$TOKEN_D" "{\"password\":\"$PASS\"}"

section "[DEF-003 재검증] 탈퇴 후 재로그인 시도(탈퇴가 정상 처리됐다면 401/실패해야 함)"
call POST /api/users/login "" "{\"email\":\"$EMAIL_D\",\"password\":\"$PASS\"}"

section "[DEF-002 재검증] 탈퇴 전 발급된 토큰(TOKEN_D)으로 보호된 API 재호출"
call GET /api/categories "$TOKEN_D" ""

########################################
section "DEF-004 재현용 계정(userE) 준비 - 정상 유효 토큰 필요"
EMAIL_E="qa_retestE_${RUN_ID}@test.local"
call POST /api/users/signup "" "{\"email\":\"$EMAIL_E\",\"userName\":\"재검증E\",\"password\":\"$PASS\"}"
call POST /api/users/login "" "{\"email\":\"$EMAIL_E\",\"password\":\"$PASS\"}"
TOKEN_E=$(echo "$RESP_BODY" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
log "TOKEN_E=$TOKEN_E"

section "[DEF-004 재현1 재검증] 필수 쿼리 파라미터 누락 (endDate 누락) - 기대: 400"
call GET "/api/expenses/summary?startDate=2020-01-01" "$TOKEN_E" ""

section "[DEF-004 재현2 재검증] 잘못된 타입의 경로변수 - 기대: 400 (401이면 재현)"
call DELETE "/api/categories/abc" "$TOKEN_E" ""

section "[DEF-004 재현3 재검증] 잘못된 형식 JSON 바디 - 기대: 400 (401이면 재현)"
callraw POST "/api/categories" "$TOKEN_E" "{invalid json"

section "[DEF-004 재현4 재검증] 존재하지 않는 라우트 - 기대: 404 (401이면 재현)"
call GET "/api/nonexistent-route" "$TOKEN_E" ""

section "[비교] 실제 미인증 요청(정상적 401)"
call GET "/api/categories" "" ""

########################################
section "[DEF-005 재검증] 로그인 실패 5회 후 정상 로그인 가능 여부"
EMAIL_F="qa_retestF_${RUN_ID}@test.local"
call POST /api/users/signup "" "{\"email\":\"$EMAIL_F\",\"userName\":\"재검증F\",\"password\":\"$PASS\"}"
for i in 1 2 3 4 5; do
  call POST /api/users/login "" "{\"email\":\"$EMAIL_F\",\"password\":\"wrong$i\"}"
done
call POST /api/users/login "" "{\"email\":\"$EMAIL_F\",\"password\":\"$PASS\"}"

########################################
section "[DEF-006 재검증] Swagger UI/OpenAPI 노출 여부"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE/swagger-ui/index.html")
log "GET /swagger-ui/index.html -> $HTTP_CODE"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE/v3/api-docs")
log "GET /v3/api-docs -> $HTTP_CODE"

########################################
section "[DEF-007 재검증] /api/expenses/summary 응답에 증감률 필드 존재 여부(백엔드)"
call GET "/api/expenses/summary?startDate=2020-01-01&endDate=2030-12-31" "$TOKEN_E" ""

log ""
log "===== 종료 ====="
