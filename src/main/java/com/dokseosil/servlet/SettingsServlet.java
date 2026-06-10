package com.dokseosil.servlet;

import com.dokseosil.dao.AdminDAO;
import com.dokseosil.dao.AttendDAO;
import com.dokseosil.dao.SettingDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

@WebServlet({"/admin/settings", "/admin/settings/save", "/admin/settings/password"})
public class SettingsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        try {
            Map<String,String> s = new SettingDAO().getAll();
            req.setAttribute("settings", s);
            req.setAttribute("activePage", "settings");
            req.getRequestDispatcher("/WEB-INF/views/settings.jsp").forward(req, res);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        try {
            SettingDAO dao = new SettingDAO();
            if (req.getServletPath().endsWith("/save")) {
                int seatCount = Integer.parseInt(req.getParameter("seatCount"));
                // 사용중인 좌석 수보다 적게 줄이지 못하게
                int active = new AttendDAO().countActive();
                if (seatCount < active) {
                    AttendServlet.redirectFlash(req, res, "/admin/settings",
                        "현재 " + active + "명이 입실중입니다. 좌석 수를 줄일 수 없습니다", "error");
                    return;
                }
                boolean requirePin = req.getParameter("requirePin") != null;
                String adminPin = trim(req.getParameter("adminPin"));
                if (!adminPin.isEmpty() && !adminPin.matches("\\d{4,6}")) {
                    AttendServlet.redirectFlash(req, res, "/admin/settings", "관리자 PIN은 숫자 4~6자리", "error");
                    return;
                }
                dao.set("seat_count", String.valueOf(seatCount));
                dao.set("require_pin", String.valueOf(requirePin));
                dao.set("admin_pin", adminPin);
                AttendServlet.redirectFlash(req, res, "/admin/settings", "설정이 저장되었습니다", "success");
            } else if (req.getServletPath().endsWith("/password")) {
                String cur = req.getParameter("current");
                String next = req.getParameter("next");
                String confirm = req.getParameter("confirm");
                HttpSession ses = req.getSession(false);
                String user = ses == null ? "admin" : (String) ses.getAttribute("username");
                AdminDAO ad = new AdminDAO();
                if (next == null || next.length() < 4) {
                    AttendServlet.redirectFlash(req, res, "/admin/settings", "새 비밀번호는 4자 이상", "error"); return;
                }
                if (!next.equals(confirm)) {
                    AttendServlet.redirectFlash(req, res, "/admin/settings", "비밀번호 확인이 일치하지 않습니다", "error"); return;
                }
                if (!ad.authenticate(user, cur)) {
                    AttendServlet.redirectFlash(req, res, "/admin/settings", "현재 비밀번호가 일치하지 않습니다", "error"); return;
                }
                ad.changePassword(user, next);
                AttendServlet.redirectFlash(req, res, "/admin/settings", "비밀번호가 변경되었습니다", "success");
            }
        } catch (NumberFormatException | SQLException e) {
            throw new ServletException(e);
        }
    }

    private static String trim(String s) { return s == null ? "" : s.trim(); }
}
