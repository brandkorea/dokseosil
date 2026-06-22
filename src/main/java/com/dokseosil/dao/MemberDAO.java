package com.dokseosil.dao;

import com.dokseosil.model.Member;
import com.dokseosil.util.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MemberDAO {

    private static Member map(ResultSet rs) throws SQLException {
        Member m = new Member();
        m.setMemberId(rs.getInt("member_id"));
        m.setName(rs.getString("name"));
        m.setPhone(rs.getString("phone"));
        m.setPin(rs.getString("pin"));
        m.setMemo(rs.getString("memo"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) m.setCreatedAt(ts.toLocalDateTime());
        // 조인 결과에 seat_no가 있으면 현재 입실 좌석
        try {
            int seat = rs.getInt("seat_no");
            if (!rs.wasNull()) m.setCurrentSeatNo(seat);
        } catch (SQLException ignore) {}
        return m;
    }

    /** 회원 목록 + 현재 입실 좌석 정보 LEFT JOIN. q가 있으면 이름/전화로 필터링. */
    public List<Member> search(String q) throws SQLException {
        String base =
            "SELECT m.member_id, m.name, m.phone, m.pin, m.memo, m.created_at, s.seat_no " +
            "  FROM member m " +
            "  LEFT JOIN attend_session s " +
            "    ON s.member_id = m.member_id AND s.check_out IS NULL ";
        String tail = " ORDER BY m.member_id DESC";

        boolean hasQ      = q != null && !q.isEmpty();
        String  digitsOnly = hasQ ? q.replaceAll("[^0-9]", "") : "";
        boolean hasDigits = !digitsOnly.isEmpty();

        // 케이스별 WHERE 절 동적 구성. 빈 digits로 '%%' 가 만들어져 전체 반환되던 버그 수정.
        String where;
        if (!hasQ) {
            where = "";
        } else if (hasDigits) {
            where = " WHERE m.name ILIKE ? " +
                    "    OR regexp_replace(coalesce(m.phone,''),'[^0-9]','','g') LIKE ? ";
        } else {
            where = " WHERE m.name ILIKE ? ";
        }

        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(base + where + tail)) {
            if (hasQ) {
                ps.setString(1, "%" + q + "%");
                if (hasDigits) ps.setString(2, "%" + digitsOnly + "%");
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Member> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        }
    }

    public Member findById(int id) throws SQLException {
        String sql =
            "SELECT m.*, s.seat_no FROM member m " +
            "  LEFT JOIN attend_session s ON s.member_id = m.member_id AND s.check_out IS NULL " +
            " WHERE m.member_id = ?";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public int insert(Member m) throws SQLException {
        String sql = "INSERT INTO member (name, phone, pin, memo) VALUES (?,?,?,?) RETURNING member_id";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, m.getName());
            ps.setString(2, nullIfEmpty(m.getPhone()));
            ps.setString(3, nullIfEmpty(m.getPin()));
            ps.setString(4, nullIfEmpty(m.getMemo()));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void update(Member m) throws SQLException {
        String sql = "UPDATE member SET name=?, phone=?, pin=?, memo=? WHERE member_id=?";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, m.getName());
            ps.setString(2, nullIfEmpty(m.getPhone()));
            ps.setString(3, nullIfEmpty(m.getPin()));
            ps.setString(4, nullIfEmpty(m.getMemo()));
            ps.setInt(5, m.getMemberId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM member WHERE member_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public int countAll() throws SQLException {
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM member");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private static String nullIfEmpty(String s) {
        return (s == null || s.isEmpty()) ? null : s;
    }
}
