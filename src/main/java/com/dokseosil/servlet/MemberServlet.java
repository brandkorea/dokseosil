package com.dokseosil.servlet;

import com.dokseosil.dao.MemberDAO;
import com.dokseosil.model.Member;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet({"/admin/members", "/admin/members/save", "/admin/members/delete"})
public class MemberServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        try {
            String q = req.getParameter("q");
            List<Member> list = new MemberDAO().search(q == null ? "" : q.trim());
            req.setAttribute("members", list);
            req.setAttribute("q", q);
            req.setAttribute("activePage", "members");

            // 수정 모드: ?edit=id
            String editId = req.getParameter("edit");
            if (editId != null) {
                Member edit = new MemberDAO().findById(Integer.parseInt(editId));
                req.setAttribute("edit", edit);
            }
            req.getRequestDispatcher("/WEB-INF/views/members.jsp").forward(req, res);
        } catch (SQLException | NumberFormatException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        try {
            if (req.getServletPath().endsWith("/save")) {
                String idStr = req.getParameter("memberId");
                String name = trim(req.getParameter("name"));
                String phone = trim(req.getParameter("phone"));
                String pin = trim(req.getParameter("pin"));
                String memo = trim(req.getParameter("memo"));
                if (name == null || name.isEmpty()) {
                    AttendServlet.redirectFlash(req, res, "/admin/members", "이름을 입력하세요", "error");
                    return;
                }
                if (!pin.isEmpty() && !pin.matches("\\d{4}")) {
                    AttendServlet.redirectFlash(req, res, "/admin/members", "PIN은 숫자 4자리여야 합니다", "error");
                    return;
                }
                Member m = new Member();
                m.setName(name); m.setPhone(phone); m.setPin(pin); m.setMemo(memo);
                MemberDAO dao = new MemberDAO();
                if (idStr == null || idStr.isEmpty()) {
                    dao.insert(m);
                    AttendServlet.redirectFlash(req, res, "/admin/members", "등록되었습니다", "success");
                } else {
                    m.setMemberId(Integer.parseInt(idStr));
                    dao.update(m);
                    AttendServlet.redirectFlash(req, res, "/admin/members", "수정되었습니다", "success");
                }
            } else if (req.getServletPath().endsWith("/delete")) {
                int id = Integer.parseInt(req.getParameter("memberId"));
                new MemberDAO().delete(id);  // CASCADE 로 세션도 삭제
                AttendServlet.redirectFlash(req, res, "/admin/members", "삭제되었습니다", "success");
            }
        } catch (SQLException | NumberFormatException e) {
            throw new ServletException(e);
        }
    }

    private static String trim(String s) { return s == null ? "" : s.trim(); }
}
