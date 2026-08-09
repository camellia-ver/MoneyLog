#!/usr/bin/bash
BASE="http://localhost:8080"
LOG="/c/Users/jyr/Desktop/study/dev/portfolio/MoneyLog/QA/smoke_regress_output.log"
: > "$LOG"
TMP=$(mktemp)

TOKEN_G=$(grep -o 'TOKEN_G=.*' token_g.txt | cut -d= -f2)

# 409 duplicate category name
printf '{"name":"ChangeRateTestCat"}' > "$TMP"
CODE=$(curl -s -o /tmp_resp.json -w "%{http_code}" -X POST -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN_G" --data-binary "@$TMP" "$BASE/api/categories")
echo "POST duplicate category name -> $CODE : $(cat /tmp_resp.json)" >> "$LOG"

# 403 ownership: userE token trying to access userG's category (id 41)
RUN_ID=$(date +%s)
printf '{"email":"qa_ownercheck_%s@test.local","userName":"OwnerCheck","password":"qaPass123"}' "$RUN_ID" > "$TMP"
curl -s -X POST -H "Content-Type: application/json" --data-binary "@$TMP" "$BASE/api/users/signup" >> "$LOG"
echo >> "$LOG"
printf '{"email":"qa_ownercheck_%s@test.local","password":"qaPass123"}' "$RUN_ID" > "$TMP"
LOGIN=$(curl -s -X POST -H "Content-Type: application/json" --data-binary "@$TMP" "$BASE/api/users/login")
TOKEN_H=$(echo "$LOGIN" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
CODE=$(curl -s -o /tmp_resp2.json -w "%{http_code}" -X DELETE -H "Authorization: Bearer $TOKEN_H" "$BASE/api/categories/41")
echo "DELETE 타 사용자 카테고리(41) -> $CODE : $(cat /tmp_resp2.json)" >> "$LOG"

cat "$LOG"
