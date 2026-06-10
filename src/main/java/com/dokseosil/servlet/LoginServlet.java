package com.dokseosil.servlet;

import com.dokseosil.dao.AdminDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet({"/login", "/logout"})
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        if (req.getServletPath().equals("/logout")) {
            HttpSession s = req.getSession(false);
            if (s != null) s.invalidate();
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        String u = req.getParameter("username");
        String p = req.getParameter("password");
        try {
            if (new AdminDAO().authenticate(u, p)) {
                HttpSession s = req.getSession(true);
                s.setAttribute("admin", Boolean.TRUE);
                s.setAttribute("username", u);
                res.sendRedirect(req.getContextPath() + "/admin/dashboard");
            } else {
                req.setAttribute("error", "아이디 또는 비밀번호가 올바르지 않습니다");
                req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, res);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
