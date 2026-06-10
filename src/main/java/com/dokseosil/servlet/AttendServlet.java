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
import java.util.Map;

/** 입실/퇴실 처리 (관리자용). 폼 POST 후 redirect-with-flash 패턴. */
@WebServlet({"/admin/attend/checkin", "/admin/attend/checkout"})
public class AttendServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        // 입실 좌석 선택 화면
        if (req.getServletPath().equals("/admin/attend/checkin")) {
            String midStr = req.getParameter("memberId");
            if (midStr == null) { res.sendRedirect(req.getContextPath() + "/admin/dashboard"); return; }
            try {
                int mid = Integer.parseInt(midStr);
                Member m = new MemberDAO().findById(mid);
                if (m == null) { redirectFlash(req, res, "/admin/dashboard", "회원을 찾을 수 없습니다", "error"); return; }
                if (m.isActive()) { redirectFlash(req, res, "/admin/dashboard", "이미 입실중입니다", "error"); return; }
                int seatCount = new SettingDAO().getInt("seat_count", 30);
                Map<Integer, AttendSession> active = new AttendDAO().activeBySeat();
                req.setAttribute("member", m);
                req.setAttribute("seatCount", seatCount);
                req.setAttribute("activeSeats", active);
                req.setAttribute("activePage", "dashboard");
                req.getRequestDispatcher("/WEB-INF/views/seat-select.jsp").forward(req, res);
            } catch (NumberFormatException | SQLException e) {
                throw new ServletException(e);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        try {
            if (req.getServletPath().equals("/admin/attend/checkin")) {
                int memberId = Integer.parseInt(req.getParameter("memberId"));
                int seatNo = Integer.parseInt(req.getParameter("seatNo"));
                try {
                    new AttendDAO().checkIn(memberId, seatNo);
                    Member m = new MemberDAO().findById(memberId);
                    redirectFlash(req, res, "/admin/dashboard",
                        (m != null ? m.getName() : "회원") + "님 " + seatNo + "번 좌석 입실", "success");
                } catch (SQLException e) {
                    // 부분 UNIQUE 인덱스 위반 (좌석/회원 중복 입실)
                    redirectFlash(req, res, "/admin/dashboard",
                        "입실 실패: 이미 사용 중인 좌석이거나 입실 중인 회원입니다", "error");
                }
            } else { // /admin/attend/checkout
                int sessionId = Integer.parseInt(req.getParameter("sessionId"));
                boolean ok = new AttendDAO().checkOut(sessionId);
                String returnTo = req.getParameter("returnTo");
                if (returnTo == null || returnTo.isEmpty()) returnTo = "/admin/dashboard";
                redirectFlash(req, res, returnTo,
                    ok ? "퇴실 처리되었습니다" : "퇴실 실패 (이미 처리됨)",
                    ok ? "success" : "error");
            }
        } catch (NumberFormatException | SQLException e) {
            throw new ServletException(e);
        }
    }

    static void redirectFlash(HttpServletRequest req, HttpServletResponse res,
                              String path, String msg, String type) throws IOException {
        HttpSession s = req.getSession(true);
        s.setAttribute("flash.msg", msg);
        s.setAttribute("flash.type", type);
        res.sendRedirect(req.getContextPath() + path);
    }
}
