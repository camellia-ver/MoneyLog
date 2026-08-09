#!/usr/bin/bash
set -u
BASE="http://localhost:8080"
RUN_ID=$(date +%s)
PASS="qaPass123"
LOG="/c/Users/jyr/Desktop/study/dev/portfolio/MoneyLog/QA/test_run_output3.log"
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

# ===================== 회원가입 검증 TC-001~008 =====================
EMAIL_A="qa_userA_${RUN_ID}@test.local"
section "TC-001 정상 회원가입 성공"
call POST /api/users/signup "" "{\"email\":\"$EMAIL_A\",\"userName\":\"QA사용자A\",\"password\":\"$PASS\"}"

section "TC-002 이메일 중복 가입 시도"
call POST /api/users/signup "" "{\"email\":\"$EMAIL_A\",\"userName\":\"중복테스트\",\"password\":\"$PASS\"}"

section "TC-003 이메일 형식 오류"
call POST /api/users/signup "" "{\"email\":\"invalid-email-${RUN_ID}\",\"userName\":\"형식테스트\",\"password\":\"$PASS\"}"

section "TC-004 이메일 공백/누락"
call POST /api/users/signup "" "{\"email\":\"\",\"userName\":\"공백테스트\",\"password\":\"$PASS\"}"

section "TC-005 비밀번호 8자 미만"
call POST /api/users/signup "" "{\"email\":\"qa_pwshort_${RUN_ID}@test.local\",\"userName\":\"짧은비번\",\"password\":\"abc123\"}"

section "TC-006 비밀번호 20자 초과"
call POST /api/users/signup "" "{\"email\":\"qa_pwlong_${RUN_ID}@test.local\",\"userName\":\"긴비번\",\"password\":\"abcdefghijklmnopqrstuvwxyz123456\"}"

section "TC-007 사용자명 공백"
call POST /api/users/signup "" "{\"email\":\"qa_unameblank_${RUN_ID}@test.local\",\"userName\":\"\",\"password\":\"$PASS\"}"

section "TC-008 이메일 대소문자 중복 처리 확인"
EMAIL_A_UPPER=$(echo "$EMAIL_A" | tr '[:lower:]' '[:upper:]')
call POST /api/users/signup "" "{\"email\":\"$EMAIL_A_UPPER\",\"userName\":\"대문자테스트\",\"password\":\"$PASS\"}"

# ===================== 로그인 TC-010~013,015,018 =====================
section "TC-010 존재하지 않는 이메일로 로그인"
call POST /api/users/login "" "{\"email\":\"qa_nouser_${RUN_ID}@test.local\",\"password\":\"$PASS\"}"

section "TC-011 비밀번호 불일치 로그인"
call POST /api/users/login "" "{\"email\":\"$EMAIL_A\",\"password\":\"wrongPassword\"}"

section "TC-012 이메일 공백으로 로그인 시도"
call POST /api/users/login "" "{\"email\":\"\",\"password\":\"$PASS\"}"

section "TC-013 비밀번호 공백으로 로그인 시도"
call POST /api/users/login "" "{\"email\":\"$EMAIL_A\",\"password\":\"\"}"

section "userA 로그인 (이후 TC용 토큰 확보)"
call POST /api/users/login "" "{\"email\":\"$EMAIL_A\",\"password\":\"$PASS\"}"
TOKEN_A=$(echo "$RESP_BODY" | jq -r '.token')
log "TOKEN_A=$TOKEN_A"

section "TC-015 잘못된 형식의 Authorization 헤더 (Bearer 접두어 없음)"
CODE=$(curl -s -o "$TMP_RESP" -w "%{http_code}" -H "Authorization: $TOKEN_A" "$BASE/api/categories")
log "REQ: GET /api/categories (Authorization: $TOKEN_A - no Bearer prefix)"
log "RESP[$CODE]: $(cat "$TMP_RESP")"

section "TC-018 로그인 다회 실패 후 정상 로그인 가능 여부(계정잠금 없음 확인)"
for i in 1 2 3 4 5; do
  call POST /api/users/login "" "{\"email\":\"$EMAIL_A\",\"password\":\"wrong$i\"}"
done
call POST /api/users/login "" "{\"email\":\"$EMAIL_A\",\"password\":\"$PASS\"}"
TOKEN_A=$(echo "$RESP_BODY" | jq -r '.token')

section "TC-019 로그아웃 API 호출"
call POST /api/users/logout "$TOKEN_A" ""

# ===================== 회원 프로필 TC-025~029,031 =====================
section "TC-025 사용자명 변경 성공"
call PUT /api/users/me/username "$TOKEN_A" "{\"userName\":\"QA수정이름\"}"

section "TC-026 사용자명 공백으로 변경 시도"
call PUT /api/users/me/username "$TOKEN_A" "{\"userName\":\"\"}"

section "TC-027 비밀번호 변경 성공"
call PUT /api/users/me/password "$TOKEN_A" "{\"currentPassword\":\"$PASS\",\"newPassword\":\"qaNewPass456\"}"
call POST /api/users/login "" "{\"email\":\"$EMAIL_A\",\"password\":\"qaNewPass456\"}"
log "재로그인 결과(새 비밀번호): HTTP $HTTP_CODE"
TOKEN_A=$(echo "$RESP_BODY" | jq -r '.token')
PASS="qaNewPass456"

section "TC-028 현재 비밀번호 불일치로 변경 실패"
call PUT /api/users/me/password "$TOKEN_A" "{\"currentPassword\":\"wrongCurrent\",\"newPassword\":\"anotherPass789\"}"

section "TC-029 새 비밀번호 정책 위반(8자 미만)"
call PUT /api/users/me/password "$TOKEN_A" "{\"currentPassword\":\"$PASS\",\"newPassword\":\"short1\"}"

section "[준비] userE 생성 (TC-031 탈퇴 비밀번호 불일치 전용)"
EMAIL_E="qa_userE_${RUN_ID}@test.local"
call POST /api/users/signup "" "{\"email\":\"$EMAIL_E\",\"userName\":\"QA사용자E\",\"password\":\"$PASS\"}"
call POST /api/users/login "" "{\"email\":\"$EMAIL_E\",\"password\":\"$PASS\"}"
TOKEN_E=$(echo "$RESP_BODY" | jq -r '.token')

section "TC-031 회원 탈퇴 시 비밀번호 불일치"
call DELETE /api/users/me "$TOKEN_E" "{\"password\":\"wrongDeletePw\"}"

# ===================== 카테고리 TC-033~039,041,044 =====================
section "TC-033 카테고리 생성 성공"
call POST /api/categories "$TOKEN_A" "{\"name\":\"QA식비\"}"
CATID_A1=$(echo "$RESP_BODY" | jq -r '.id')
log "CATID_A1=$CATID_A1"

section "TC-034 카테고리 생성 시 이름 중복"
call POST /api/categories "$TOKEN_A" "{\"name\":\"QA식비\"}"

section "TC-035 카테고리 생성 시 이름 공백"
call POST /api/categories "$TOKEN_A" "{\"name\":\"\"}"

section "[준비] 카테고리 하나 더 생성(교통비)"
call POST /api/categories "$TOKEN_A" "{\"name\":\"QA교통비\"}"
CATID_A2=$(echo "$RESP_BODY" | jq -r '.id')
log "CATID_A2=$CATID_A2"

section "TC-036 카테고리 목록 조회 (본인 소유만) - userB 준비"
EMAIL_B="qa_userB_${RUN_ID}@test.local"
call POST /api/users/signup "" "{\"email\":\"$EMAIL_B\",\"userName\":\"QA사용자B\",\"password\":\"$PASS\"}"
call POST /api/users/login "" "{\"email\":\"$EMAIL_B\",\"password\":\"$PASS\"}"
TOKEN_B=$(echo "$RESP_BODY" | jq -r '.token')
call POST /api/categories "$TOKEN_B" "{\"name\":\"QA용돈\"}"
CATID_B1=$(echo "$RESP_BODY" | jq -r '.id')
call GET /api/categories "$TOKEN_A" ""
log "userA 카테고리 목록에 userB의 'QA용돈'이 포함되면 결함"

section "TC-037 카테고리 수정 성공"
call PUT "/api/categories/$CATID_A2" "$TOKEN_A" "{\"name\":\"QA교통비수정\"}"

section "TC-038 자기 자신과 동일한 이름으로 수정"
call PUT "/api/categories/$CATID_A2" "$TOKEN_A" "{\"name\":\"QA교통비수정\"}"

section "TC-039 타 카테고리와 이름 중복 시 수정 실패"
call PUT "/api/categories/$CATID_A2" "$TOKEN_A" "{\"name\":\"QA식비\"}"

section "TC-044 존재하지 않는 카테고리 접근"
call GET "/api/categories" "$TOKEN_A" ""
call PUT "/api/categories/9999999" "$TOKEN_A" "{\"name\":\"없는카테고리\"}"

section "TC-041 카테고리 삭제 성공(소속 지출 없음)"
call POST /api/categories "$TOKEN_A" "{\"name\":\"QA삭제될카테고리\"}"
CATID_DEL=$(echo "$RESP_BODY" | jq -r '.id')
call DELETE "/api/categories/$CATID_DEL" "$TOKEN_A" ""

section "TC-042 카테고리 삭제 시 소속 지출 존재하면 삭제 거부"
call POST /api/expenses "$TOKEN_A" "{\"categoryId\":$CATID_A1,\"amount\":15000,\"content\":\"QA점심\",\"memo\":\"김밥\"}"
EXPID_A1=$(echo "$RESP_BODY" | jq -r '.id')
log "EXPID_A1=$EXPID_A1"
call DELETE "/api/categories/$CATID_A1" "$TOKEN_A" ""

# ===================== 지출 TC-046~049,051~054,056,058~060 =====================
section "TC-046 지출 생성 시 금액 0/음수 입력"
call POST /api/expenses "$TOKEN_A" "{\"categoryId\":$CATID_A1,\"amount\":0,\"content\":\"제로금액\",\"memo\":\"\"}"
call POST /api/expenses "$TOKEN_A" "{\"categoryId\":$CATID_A1,\"amount\":-500,\"content\":\"음수금액\",\"memo\":\"\"}"

section "TC-047 지출 생성 시 금액 null"
call POST /api/expenses "$TOKEN_A" "{\"categoryId\":$CATID_A1,\"content\":\"금액없음\",\"memo\":\"\"}"

section "TC-048 지출 생성 시 content 공백"
call POST /api/expenses "$TOKEN_A" "{\"categoryId\":$CATID_A1,\"amount\":1000,\"content\":\"\",\"memo\":\"\"}"

section "TC-049 지출 생성 시 categoryId null"
call POST /api/expenses "$TOKEN_A" "{\"amount\":1000,\"content\":\"카테고리없음\",\"memo\":\"\"}"

section "TC-051 존재하지 않는 categoryId로 지출 생성"
call POST /api/expenses "$TOKEN_A" "{\"categoryId\":9999999,\"amount\":1000,\"content\":\"없는카테고리지출\",\"memo\":\"\"}"

section "TC-052 지출 목록 조회(본인 소유만)"
call POST /api/expenses "$TOKEN_B" "{\"categoryId\":$CATID_B1,\"amount\":8000,\"content\":\"QB커피\",\"memo\":\"\"}"
EXPID_B1=$(echo "$RESP_BODY" | jq -r '.id')
log "EXPID_B1=$EXPID_B1"
call GET /api/expenses "$TOKEN_A" ""
log "userA 지출 목록에 userB의 EXPID_B1($EXPID_B1)이 포함되면 결함"

section "TC-053 지출 수정 성공"
call PUT "/api/expenses/$EXPID_A1" "$TOKEN_A" "{\"categoryId\":$CATID_A1,\"amount\":20000,\"content\":\"QA점심수정\",\"memo\":\"수정메모\"}"

section "TC-054 지출 수정 시 카테고리 변경"
call POST /api/categories "$TOKEN_A" "{\"name\":\"QA간식비\"}"
CATID_A3=$(echo "$RESP_BODY" | jq -r '.id')
call PUT "/api/expenses/$EXPID_A1" "$TOKEN_A" "{\"categoryId\":$CATID_A3,\"amount\":20000,\"content\":\"QA점심수정\",\"memo\":\"카테고리변경\"}"

section "TC-056 지출 삭제 성공"
call POST /api/expenses "$TOKEN_A" "{\"categoryId\":$CATID_A3,\"amount\":3000,\"content\":\"삭제될지출\",\"memo\":\"\"}"
EXPID_TODELETE=$(echo "$RESP_BODY" | jq -r '.id')
call DELETE "/api/expenses/$EXPID_TODELETE" "$TOKEN_A" ""

section "TC-058 존재하지 않는 지출 접근"
call GET "/api/expenses" "$TOKEN_A" ""
call PUT "/api/expenses/9999999" "$TOKEN_A" "{\"categoryId\":$CATID_A3,\"amount\":1000,\"content\":\"없음\",\"memo\":\"\"}"
call DELETE "/api/expenses/9999999" "$TOKEN_A" ""

section "TC-059 지출 목록 categoryId 필터 동작"
call GET "/api/expenses?categoryId=$CATID_A3" "$TOKEN_A" ""

section "TC-060 지출 목록 날짜 필터 동작"
call GET "/api/expenses?startDate=2020-01-01&endDate=2030-12-31" "$TOKEN_A" ""
call GET "/api/expenses?startDate=2099-01-01&endDate=2099-12-31" "$TOKEN_A" ""

# ===================== 통계 TC-061~065,070 =====================
section "TC-061 지출 요약 조회 성공"
call GET "/api/expenses/summary?startDate=2020-01-01&endDate=2030-12-31" "$TOKEN_A" ""

section "TC-062 지출 없는 기간 조회 시 총액 0"
call GET "/api/expenses/summary?startDate=2099-01-01&endDate=2099-12-31" "$TOKEN_A" ""

section "TC-063 startDate/endDate 파라미터 누락"
call GET "/api/expenses/summary?startDate=2020-01-01" "$TOKEN_A" ""
call GET "/api/expenses/summary" "$TOKEN_A" ""

section "TC-064 startDate > endDate 역전 케이스"
call GET "/api/expenses/summary?startDate=2030-01-01&endDate=2020-01-01" "$TOKEN_A" ""

section "TC-065 전월 대비 증감률 응답필드 존재 여부"
log "TC-061 응답 스키마 확인: totalAmount, categorySummaryList 만 존재 -> 증감률 필드 코드상 부재(README와 실구현 불일치 여부는 위 TC-061/062 응답 JSON 키로 판정)"

section "TC-070 지출 생성 시 categoryId 문자열 타입 전송 (프론트 select.value 시뮬레이션)"
call POST /api/expenses "$TOKEN_A" "{\"categoryId\":\"$CATID_A3\",\"amount\":5000,\"content\":\"문자열카테고리ID\",\"memo\":\"\"}"

echo "DONE_PHASE3"
