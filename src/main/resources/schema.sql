-- ============================================================
-- 독서실 출결관리 PostgreSQL 스키마
-- 사용: psql -U postgres -d dokseosil -f schema.sql
-- ============================================================

-- DB가 없으면 먼저 만들기 (psql 콘솔에서):
--   CREATE DATABASE dokseosil ENCODING 'UTF8';
--   \c dokseosil

CREATE TABLE IF NOT EXISTS member (
    member_id     SERIAL PRIMARY KEY,
    name          VARCHAR(50)  NOT NULL,
    phone         VARCHAR(20),
    pin           VARCHAR(4),                          -- 회원 키오스크 본인확인용 (선택)
    memo          TEXT,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_member_name  ON member(name);
CREATE INDEX IF NOT EXISTS idx_member_phone ON member(phone);

CREATE TABLE IF NOT EXISTS attend_session (
    session_id    SERIAL PRIMARY KEY,
    member_id     INTEGER     NOT NULL REFERENCES member(member_id) ON DELETE CASCADE,
    seat_no       INTEGER     NOT NULL,
    check_in      TIMESTAMP   NOT NULL DEFAULT NOW(),
    check_out     TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_session_member   ON attend_session(member_id);
CREATE INDEX IF NOT EXISTS idx_session_checkin  ON attend_session(check_in);

-- 한 회원이 동시에 두 좌석 입실 불가
CREATE UNIQUE INDEX IF NOT EXISTS uq_active_member
    ON attend_session(member_id) WHERE check_out IS NULL;

-- 같은 좌석에 두 명이 동시에 입실 불가
CREATE UNIQUE INDEX IF NOT EXISTS uq_active_seat
    ON attend_session(seat_no) WHERE check_out IS NULL;

CREATE TABLE IF NOT EXISTS app_setting (
    skey   VARCHAR(50)  PRIMARY KEY,
    svalue TEXT
);

INSERT INTO app_setting (skey, svalue) VALUES
    ('seat_count',  '30'),
    ('require_pin', 'false'),
    ('admin_pin',   '')
ON CONFLICT (skey) DO NOTHING;

-- 관리자 계정 (비밀번호: admin123 — SHA-256 해시. 운영 시 반드시 변경)
CREATE TABLE IF NOT EXISTS admin_user (
    admin_id    SERIAL PRIMARY KEY,
    username    VARCHAR(50) UNIQUE NOT NULL,
    pw_hash     VARCHAR(64) NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- SHA-256('admin123') = 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
INSERT INTO admin_user (username, pw_hash) VALUES
    ('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9')
ON CONFLICT (username) DO NOTHING;
