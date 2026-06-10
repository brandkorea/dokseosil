# 독서실 출결관리 (Tomcat + JSP + PostgreSQL)

Servlet/JSP + JDBC(HikariCP) 기반 풀스택 웹앱. Tomcat 9.x에 WAR로 배포한다.

## 스택

| 계층    | 기술                                |
|--------|------------------------------------|
| Web    | JSP + JSTL 1.2 + Tailwind CSS (CDN) |
| 컨트롤러 | Servlet 4.0 (`@WebServlet`)         |
| DB     | PostgreSQL 13+ (JDBC 42.7.x)        |
| 풀     | HikariCP 4                          |
| 빌드    | Maven 3.6+, Java 11+                |
| 실행    | Apache Tomcat 9.x                   |

> **Tomcat 10/11**(Jakarta EE)을 사용하려면 `javax.servlet.*` → `jakarta.servlet.*`로 패키지 변경 + `pom.xml` 의존성 교체 필요.

## 기능 요약

### 관리자 (`/admin/*` - 로그인 필요)
- **대시보드** — 좌석 현황(빨강/초록), 빠른 입/퇴실 검색
- **회원관리** — 등록/수정/삭제, 이름·연락처 검색, PIN 등록
- **이용기록** — 기간/이름 필터, CSV 내보내기(UTF-8 BOM), 인쇄
- **통계** — 회원수/방문수/평균이용시간, 최근 7일 차트, 월간 TOP 10
- **설정** — 좌석 수, 회원 PIN 필수 토글, 관리자 PIN, 비밀번호 변경

### 회원 키오스크 (`/kiosk` - 로그인 불필요)
- 이름 또는 연락처 뒷자리로 검색
- (옵션) PIN 4자리 본인 확인 — 화면 키패드 제공
- 입실: 큰 좌석 그리드에서 빈자리 선택
- 퇴실: 큰 버튼 한 번에 처리
- 완료 화면 3초 후 자동 복귀

## 설치 — 0부터

### 1. PostgreSQL 준비

```sql
-- psql 접속 후
CREATE DATABASE dokseosil ENCODING 'UTF8';
\c dokseosil
\i 'C:/Users/brand/dokseosil/src/main/resources/schema.sql'
```

기본 관리자: **admin / admin123** (로그인 후 즉시 변경 권장)

### 2. DB 접속 정보

기본값은 `src/main/resources/db.properties`:

```
db.url=jdbc:postgresql://localhost:5432/dokseosil
db.user=postgres
db.password=postgres
db.poolSize=10
```

운영 환경에서는 환경변수로 덮어쓰기 가능: `DB_URL`, `DB_USER`, `DB_PASSWORD`.

### 3. 빌드

```bash
cd C:/Users/brand/dokseosil
mvn clean package
```

산출물: `target/dokseosil.war`

### 4. Tomcat 배포

```
target/dokseosil.war  →  $CATALINA_HOME/webapps/
```

Tomcat 기동 후 접속:

- 관리자: http://localhost:8080/dokseosil/login
- 회원 키오스크: http://localhost:8080/dokseosil/kiosk

> Tomcat에 배포된 컨텍스트 경로를 ROOT로 만들려면 WAR 이름을 `ROOT.war`로 변경해 배포.

## 디렉토리 구조

```
dokseosil/
├── pom.xml
├── README.md
└── src/main/
    ├── java/com/dokseosil/
    │   ├── filter/AuthFilter.java         # /admin/* 인증
    │   ├── util/DB.java                   # HikariCP 풀
    │   ├── util/PasswordUtil.java         # SHA-256
    │   ├── model/Member.java
    │   ├── model/AttendSession.java
    │   ├── dao/MemberDAO.java
    │   ├── dao/AttendDAO.java
    │   ├── dao/SettingDAO.java
    │   ├── dao/AdminDAO.java
    │   └── servlet/
    │       ├── HomeServlet.java           # /
    │       ├── LoginServlet.java          # /login, /logout
    │       ├── DashboardServlet.java      # /admin/dashboard
    │       ├── MemberServlet.java         # /admin/members/*
    │       ├── AttendServlet.java         # /admin/attend/*
    │       ├── HistoryServlet.java        # /admin/history/*
    │       ├── StatsServlet.java          # /admin/stats
    │       ├── SettingsServlet.java       # /admin/settings/*
    │       └── KioskServlet.java          # /kiosk/*
    ├── resources/
    │   ├── schema.sql                     # PostgreSQL DDL + 초기 데이터
    │   └── db.properties
    └── webapp/
        ├── WEB-INF/
        │   ├── web.xml
        │   └── views/
        │       ├── layout/header.jsp
        │       ├── layout/footer.jsp
        │       ├── login.jsp
        │       ├── dashboard.jsp
        │       ├── seat-select.jsp
        │       ├── members.jsp
        │       ├── history.jsp
        │       ├── stats.jsp
        │       ├── settings.jsp
        │       ├── kiosk-search.jsp
        │       ├── kiosk-pin.jsp
        │       ├── kiosk-action.jsp
        │       ├── kiosk-seat.jsp
        │       ├── kiosk-done.jsp
        │       └── error.jsp
        └── static/
            ├── css/app.css
            └── js/app.js
```

## 동시성 안전 장치

DB 레벨 부분 UNIQUE 인덱스로 중복 입실을 차단한다:

```sql
CREATE UNIQUE INDEX uq_active_member
  ON attend_session(member_id) WHERE check_out IS NULL;

CREATE UNIQUE INDEX uq_active_seat
  ON attend_session(seat_no) WHERE check_out IS NULL;
```

- 동일 회원이 두 좌석에 동시 입실 불가
- 동일 좌석에 두 명이 동시 입실 불가
- 두 키오스크가 같은 좌석을 동시에 누른 경우, 한쪽은 SQLException → 사용자에게 "이미 사용 중" 안내

## 보안 메모

- 관리자 비밀번호는 **SHA-256 + 단순 hash** (실서비스라면 BCrypt 권장)
- 세션 쿠키는 `HttpOnly`. HTTPS 환경이라면 `secure` 플래그를 톰캣 측에서 활성화하세요.
- 회원 PIN은 평문 4자리. 키오스크 PC를 신뢰할 수 있는 망에 둘 것.
- 운영 배포 시 `db.properties`에 평문 비밀번호 두지 말고 환경변수 사용 권장.

## 라이선스

내부 사용 / 학습용 샘플.
