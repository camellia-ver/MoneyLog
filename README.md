# 📊 MoneyLog

> 내 소비를 기록하고, 숫자로 이해하는 가계부 웹앱 
---
## 📌 프로젝트 소개
지출 데이터를 기록·관리하고, 집계 쿼리 기반의 월별/카테고리별 통계 화면을 제공하는 웹앱입니다.

단순 CRUD 구현을 넘어, 실제 서비스처럼 데이터를 쌓고 통계를 뽑아내는 과정에서 쿼리 최적화와 인프라 배포까지 직접 경험해보고 싶어 시작했습니다.

이 프로젝트를 통해 API 설계부터 DB 성능 개선, 배포까지 백엔드 개발의 전체 흐름을 이해하는 것을 목표로 했습니다.

- **개발 기간**: 2026.00.00 ~ 2026.00.00
- **배포 URL**: `http://xxx.xx.xxx.xxx`
- **API 문서 (Swagger)**: `http://xxx.xx.xxx.xxx/api-docs`

---
## 🛠 기술 스택
| 구분 | 스택 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.x |
| Database | MySQL 8.x |
| ORM | Spring Data JPA (Hibernate) |
| Auth | Spring Security + JWT (jjwt 라이브러리) |
| API 문서 | Springdoc OpenAPI (Swagger UI)|
| 빌드 도구 | Gradle |
| 형상관리 | Git / GitHub | 

---

## ✨ 주요 기능
 
### 1. 인증/인가
- 회원가입 / 로그인 (JWT 발급)
- 인증이 필요한 라우트 보호, 본인 데이터 접근 제어
### 2. 지출(Expense) CRUD
- 지출 내역 생성 / 조회(목록·상세) / 수정 / 삭제
### 3. 카테고리(Category) 관리
- 카테고리 생성 / 조회
### 4. 통계 대시보드
- 월별 총 지출 조회
- 카테고리별 지출 비중
- 전월 대비 증감률
