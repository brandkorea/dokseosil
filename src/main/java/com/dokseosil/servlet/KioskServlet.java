package com.dokseosil.servlet;

import com.dokseosil.dao.AttendDAO;
import com.dokseosil.dao.MemberDAO;
import com.dokseosil.dao.SettingDAO;
import com.dokseosil.model.AttendSession;
import com.dokseosil.model.Member;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 회원 키오스크 (로그인 불필요).
 *
 * /kiosk                    - 검색 화면
 * /kiosk?q=...              - 검색 결과
 * /kiosk/member?id=...      - 회원 선택 (PIN 필요 시 PIN 화면, 아니면 액션 화면)
 * /kiosk/verify  (POST)     - PIN 검증
 * /kiosk/seat?memberId=...  - 좌석 선택 화면 (PIN 통과 후)
 * /kiosk/checkin (POST)     - 입실
 * /kiosk/checkout (POST)    - 퇴실
 */
@WebServlet({
    "/kiosk", "/kiosk/", "/kiosk/member", "/kiosk/verify",
    "/kiosk/seat", "/kiosk/checkin", "/kiosk/checkout", "/kiosk/done"
})
public class KioskServlet extends HttpServlet {

    private static final String AUTH_ATTR = "kiosk.authed.memberId";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        try {
            String path = req.getServletPath();
            switch (path) {
                case "/kiosk":
                case "/kiosk/":
                    showSearch(req, res); break;
                case "/kiosk/member":
                    showMember(req, res); break;
                case "/kiosk/seat":
                    showSeat(req, res); break;
                case "/kiosk/done":
                    showDone(req, res); break;
                default:
                    res.sendError(404);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        try {
            String path = req.getServletPath();
            switch (path) {
                case "/kiosk/verify":
                    verifyPin(req, res); break;
                case "/kiosk/checkin":
                    doCheckin(req, res); break;
                case "/kiosk/checkout":
                    doCheckout(req, res); break;
                default:
                    res.sendError(405);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    /* ---------- 단계 1: 검색 ---------- */
    private void showSearch(HttpServletRequest req, HttpServletResponse res)
            throws SQLException, ServletException, IOException {
        // 새 세션 시작 (이전 인증 초기화)
        HttpSession s = req.getSession(false);
        if (s != null) s.removeAttribute(AUTH_ATTR);

        String q = req.getParameter("q");
        List<Member> results = null;
        if (q != null && !q.trim().isEmpty()) {
            results = new MemberDAO().search(q.trim());
            if (results.size() > 6) results = results.subList(0, 6);
        }
        req.setAttribute("q", q);
        req.setAttribute("results", results);
        req.setAttribute("activeCount", new AttendDAO().countActive());
        req.getRequestDispatcher("/WEB-INF/views/kiosk-search.jsp").forward(req, res);
    }

    /* ---------- 단계 2: 회원 선택 (PIN 또는 액션) ---------- */
    private void showMember(HttpServletRequest req, HttpServletResponse res)
            throws SQLException, ServletException, IOException {
        int mid = parseInt(req.getParameter("id"));
        Member m = new MemberDAO().findById(mid);
        if (m == null) { res.sendRedirect(req.getContextPath() + "/kiosk"); return; }

        boolean requirePin = new SettingDAO().getBool("require_pin", false);
        boolean needPin = requirePin && m.getPin() != null && !m.getPin().isEmpty();

        if (needPin) {
            req.setAttribute("member", m);
            req.getRequestDispatcher("/WEB-INF/views/kiosk-pin.jsp").forward(req, res);
        } else {
            // 통과
            markAuthed(req, mid);
            showAction(req, res, m);
        }
    }

    /* ---------- 단계 2-1: PIN 검증 ---------- */
    private void verifyPin(HttpServletRequest req, HttpServletResponse res)
            throws SQLException, ServletException, IOException {
        int mid = parseInt(req.getParameter("memberId"));
        String pin = req.getParameter("pin");
        Member m = new MemberDAO().findById(mid);
        if (m == null) { res.sendRedirect(req.getContextPath() + "/kiosk"); return; }
        if (m.getPin() == null || !m.getPin().equals(pin)) {
            req.setAttribute("member", m);
            req.setAttribute("error", "PIN이 일치하지 않습니다");
            req.getRequestDispatcher("/WEB-INF/views/kiosk-pin.jsp").forward(req, res);
            return;
        }
        markAuthed(req, mid);
        showAction(req, res, m);
    }

    /* ---------- 단계 3: 액션 (입실/퇴실 선택) ---------- */
    private void showAction(HttpServletRequest req, HttpServletResponse res, Member m)
            throws SQLException, ServletException, IOException {
        AttendSession active = new AttendDAO().findActiveByMember(m.getMemberId());
        req.setAttribute("member", m);
        req.setAttribute("active", active);
        req.getRequestDispatcher("/WEB-INF/views/kiosk-action.jsp").forward(req, res);
    }

    /* ---------- 단계 4: 좌석 선택 ---------- */
    private void showSeat(HttpServletRequest req, HttpServletResponse res)
            throws SQLException, ServletException, IOException {
        int mid = parseInt(req.getParameter("memberId"));
        if (!isAuthed(req, mid)) { res.sendRedirect(req.getContextPath() + "/kiosk"); return; }

        Member m = new MemberDAO().findById(mid);
        int seatCount = new SettingDAO().getInt("seat_count", 30);
        Map<Integer, AttendSession> active = new AttendDAO().activeBySeat();
        req.setAttribute("member", m);
        req.setAttribute("seatCount", seatCount);
        req.setAttribute("activeSeats", active);
        req.getRequestDispatcher("/WEB-INF/views/kiosk-seat.jsp").forward(req, res);
    }

    /* ---------- 처리: 입실 ---------- */
    private void doCheckin(HttpServletRequest req, HttpServletResponse res)
            throws SQLException, IOException {
        int mid = parseInt(req.getParameter("memberId"));
        int seat = parseInt(req.getParameter("seatNo"));
        if (!isAuthed(req, mid)) { res.sendRedirect(req.getContextPath() + "/kiosk"); return; }
        Member m = new MemberDAO().findById(mid);
        if (m == null) { res.sendRedirect(req.getContextPath() + "/kiosk"); return; }
        try {
            new AttendDAO().checkIn(mid, seat);
            res.sendRedirect(req.getContextPath() + "/kiosk/done?msg=" +
                java.net.URLEncoder.encode(m.getName() + "님 " + seat + "번 좌석 입실 완료", "UTF-8"));
        } catch (SQLException e) {
            res.sendRedirect(req.getContextPath() + "/kiosk/done?error=" +
                java.net.URLEncoder.encode("입실 실패: 이미 사용 중인 좌석입니다", "UTF-8"));
        }
    }

    /* ---------- 처리: 퇴실 ---------- */
    private void doCheckout(HttpServletRequest req, HttpServletResponse res)
            throws SQLException, IOException {
        int mid = parseInt(req.getParameter("memberId"));
        if (!isAuthed(req, mid)) { res.sendRedirect(req.getContextPath() + "/kiosk"); return; }
        AttendSession active = new AttendDAO().findActiveByMember(mid);
        if (active == null) { res.sendRedirect(req.getContextPath() + "/kiosk"); return; }
        new AttendDAO().checkOut(active.getSessionId());
        String dur = active.getDurationStr();
        Member m = new MemberDAO().findById(mid);
        String name = m != null ? m.getName() : "회원";
        res.sendRedirect(req.getContextPath() + "/kiosk/done?msg=" +
            java.net.URLEncoder.encode(name + "님 퇴실 완료\n이용시간: " + dur, "UTF-8"));
    }

    /* ---------- 완료 화면 ---------- */
    private void showDone(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        HttpSession s = req.getSession(false);
        if (s != null) s.removeAttribute(AUTH_ATTR);
        req.setAttribute("msg", req.getParameter("msg"));
        req.setAttribute("error", req.getParameter("error"));
        req.getRequestDispatcher("/WEB-INF/views/kiosk-done.jsp").forward(req, res);
    }

    /* ---------- helper ---------- */
    private void markAuthed(HttpServletRequest req, int memberId) {
        req.getSession(true).setAttribute(AUTH_ATTR, memberId);
    }
    private boolean isAuthed(HttpServletRequest req, int memberId) {
        HttpSession s = req.getSession(false);
        if (s == null) return false;
        Object v = s.getAttribute(AUTH_ATTR);
        return v instanceof Integer && (Integer) v == memberId;
    }
    private int parseInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }
}
