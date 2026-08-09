#!/usr/bin/bash
BASE="http://localhost:8080"
RUN_ID=$(date +%s)
EMAIL_G="qa_retestG_${RUN_ID}@test.local"
TMP=$(mktemp)
printf '{"email":"%s","userName":"QAretestG","password":"qaPass123"}' "$EMAIL_G" > "$TMP"
curl -s -X POST -H "Content-Type: application/json" --data-binary "@$TMP" "$BASE/api/users/signup"; echo
LOGIN_TMP=$(mktemp)
printf '{"email":"%s","password":"qaPass123"}' "$EMAIL_G" > "$LOGIN_TMP"
LOGIN=$(curl -s -X POST -H "Content-Type: application/json" --data-binary "@$LOGIN_TMP" "$BASE/api/users/login")
echo "$LOGIN"
TOKEN_G=$(echo "$LOGIN" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
echo "TOKEN_G=$TOKEN_G" > token_g.txt
CAT_TMP=$(mktemp)
printf '{"name":"ChangeRateTestCat"}' > "$CAT_TMP"
CAT=$(curl -s -X POST -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN_G" --data-binary "@$CAT_TMP" "$BASE/api/categories")
echo "$CAT"
CAT_ID=$(echo "$CAT" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
EXP_TMP=$(mktemp)
printf '{"categoryId":%s,"amount":50000,"content":"ChangeRateTestExpense","memo":""}' "$CAT_ID" > "$EXP_TMP"
curl -s -X POST -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN_G" --data-binary "@$EXP_TMP" "$BASE/api/expenses"; echo
cat token_g.txt
