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

@WebServlet("/admin/dashboard")
public class DashboardServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        try {
            int seatCount = new SettingDAO().getInt("seat_count", 30);
            Map<Integer, AttendSession> activeSeats = new AttendDAO().activeBySeat();
            String q = req.getParameter("q");
            List<Member> searchResults = (q != null && !q.trim().isEmpty())
                    ? new MemberDAO().search(q.trim())
                    : null;

            req.setAttribute("seatCount", seatCount);
            req.setAttribute("activeSeats", activeSeats);
            req.setAttribute("activeCount", activeSeats.size());
            req.setAttribute("searchResults", searchResults);
            req.setAttribute("q", q);
            req.setAttribute("activePage", "dashboard");
            req.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(req, res);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
