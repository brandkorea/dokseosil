package com.dokseosil.dao;

import com.dokseosil.model.AttendSession;
import com.dokseosil.util.DB;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttendDAO {

    private static AttendSession map(ResultSet rs) throws SQLException {
        AttendSession s = new AttendSession();
        s.setSessionId(rs.getInt("session_id"));
        s.setMemberId(rs.getInt("member_id"));
        s.setSeatNo(rs.getInt("seat_no"));
        Timestamp tin = rs.getTimestamp("check_in");
        Timestamp tout = rs.getTimestamp("check_out");
        if (tin != null) s.setCheckIn(tin.toLocalDateTime());
        if (tout != null) s.setCheckOut(tout.toLocalDateTime());
        try {
            s.setMemberName(rs.getString("name"));
            s.setMemberPhone(rs.getString("phone"));
        } catch (SQLException ignore) {}
        return s;
    }

    /**
     * 입실 처리.
     * 동시성 보호: 회원/좌석 양쪽에 부분 UNIQUE 인덱스가 걸려있어 DB 레벨에서 중복 입실이 차단된다.
     * 예외를 던지므로 호출측에서 catch 후 사용자 메시지로 변환할 것.
     */
    public int checkIn(int memberId, int seatNo) throws SQLException {
        String sql = "INSERT INTO attend_session (member_id, seat_no) VALUES (?,?) RETURNING session_id";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            ps.setInt(2, seatNo);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /** 세션ID로 퇴실 처리 */
    public boolean checkOut(int sessionId) throws SQLException {
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "UPDATE attend_session SET check_out = NOW() " +
                 " WHERE session_id = ? AND check_out IS NULL")) {
            ps.setInt(1, sessionId);
            return ps.executeUpdate() > 0;
        }
    }

    /** 회원의 활성 세션 반환 (없으면 null) */
    public AttendSession findActiveByMember(int memberId) throws SQLException {
        String sql = "SELECT s.*, m.name, m.phone FROM attend_session s " +
                     " JOIN member m ON m.member_id = s.member_id " +
                     " WHERE s.member_id = ? AND s.check_out IS NULL";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /** seatNo -> memberName 매핑 (현재 입실 중인 좌석만) */
    public Map<Integer, AttendSession> activeBySeat() throws SQLException {
        String sql = "SELECT s.*, m.name, m.phone FROM attend_session s " +
                     " JOIN member m ON m.member_id = s.member_id " +
                     " WHERE s.check_out IS NULL";
        Map<Integer, AttendSession> map = new HashMap<>();
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                AttendSession s = map(rs);
                map.put(s.getSeatNo(), s);
            }
        }
        return map;
    }

    /** 기간 + 이름 필터 이용 기록 */
    public List<AttendSession> history(LocalDate from, LocalDate to, String nameQ) throws SQLException {
        StringBuilder sb = new StringBuilder(
            "SELECT s.*, m.name, m.phone FROM attend_session s " +
            " JOIN member m ON m.member_id = s.member_id " +
            " WHERE s.check_in >= ? AND s.check_in < ? ");
        if (nameQ != null && !nameQ.isEmpty()) sb.append(" AND m.name ILIKE ? ");
        sb.append(" ORDER BY s.check_in DESC");

        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sb.toString())) {
            ps.setTimestamp(1, Timestamp.valueOf(from.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(to.plusDays(1).atStartOfDay()));
            if (nameQ != null && !nameQ.isEmpty()) ps.setString(3, "%" + nameQ + "%");
            try (ResultSet rs = ps.executeQuery()) {
                List<AttendSession> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        }
    }

    public void deleteSession(int sessionId) throws SQLException {
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM attend_session WHERE session_id=?")) {
            ps.setInt(1, sessionId);
            ps.executeUpdate();
        }
    }

    public int countActive() throws SQLException {
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT COUNT(*) FROM attend_session WHERE check_out IS NULL");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    /* ===== 통계 ===== */

    public int countVisitsBetween(LocalDateTime from, LocalDateTime to) throws SQLException {
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT COUNT(*) FROM attend_session WHERE check_in >= ? AND check_in < ?")) {
            ps.setTimestamp(1, Timestamp.valueOf(from));
            ps.setTimestamp(2, Timestamp.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /** 완료된 세션들의 평균 이용 시간(분). 데이터 없으면 0. */
    public double avgUsageMinutes() throws SQLException {
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT COALESCE(AVG(EXTRACT(EPOCH FROM (check_out - check_in))/60), 0) " +
                 "  FROM attend_session WHERE check_out IS NOT NULL");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getDouble(1);
        }
    }

    /** 최근 N일 일별 방문자 수 (오래된 날짜 → 오늘 순). */
    public List<int[]> visitsLastDays(int days) throws SQLException {
        // [yyyymmdd숫자가 아니라 표시는 자바에서 만들고, 카운트만 받는다]
        String sql =
            "WITH d AS ( " +
            "  SELECT generate_series(CURRENT_DATE - (?::int - 1), CURRENT_DATE, INTERVAL '1 day')::date AS day " +
            ") " +
            "SELECT d.day, COUNT(s.session_id) " +
            "  FROM d LEFT JOIN attend_session s " +
            "    ON s.check_in::date = d.day " +
            " GROUP BY d.day ORDER BY d.day";
        List<int[]> out = new ArrayList<>();
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, days);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDate d = rs.getDate(1).toLocalDate();
                    int cnt = rs.getInt(2);
                    out.add(new int[]{ d.getMonthValue(), d.getDayOfMonth(), cnt });
                }
            }
        }
        return out;
    }

    /** 기간 내 회원별 방문 횟수 TOP N */
    public List<Object[]> topAttendees(LocalDateTime from, LocalDateTime to, int n) throws SQLException {
        String sql =
            "SELECT m.member_id, m.name, COUNT(*) AS cnt " +
            "  FROM attend_session s JOIN member m ON m.member_id = s.member_id " +
            " WHERE s.check_in >= ? AND s.check_in < ? " +
            " GROUP BY m.member_id, m.name " +
            " ORDER BY cnt DESC LIMIT ?";
        List<Object[]> out = new ArrayList<>();
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(from));
            ps.setTimestamp(2, Timestamp.valueOf(to));
            ps.setInt(3, n);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new Object[]{ rs.getInt(1), rs.getString(2), rs.getInt(3) });
                }
            }
        }
        return out;
    }
}
