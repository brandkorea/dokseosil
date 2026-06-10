package com.dokseosil.servlet;

import com.dokseosil.dao.AttendDAO;
import com.dokseosil.dao.MemberDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

@WebServlet("/admin/stats")
public class StatsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        try {
            AttendDAO ad = new AttendDAO();
            MemberDAO md = new MemberDAO();

            LocalDate today = LocalDate.now();
            LocalDateTime startOfToday  = today.atStartOfDay();
            LocalDateTime startOfTomorrow = today.plusDays(1).atStartOfDay();
            YearMonth ym = YearMonth.from(today);
            LocalDateTime startOfMonth = ym.atDay(1).atStartOfDay();
            LocalDateTime startOfNextMonth = ym.plusMonths(1).atDay(1).atStartOfDay();

            int totalMembers = md.countAll();
            int todayVisits  = ad.countVisitsBetween(startOfToday, startOfTomorrow);
            int monthVisits  = ad.countVisitsBetween(startOfMonth, startOfNextMonth);
            double avgMin    = ad.avgUsageMinutes();
            List<int[]> weekly = ad.visitsLastDays(7);
            List<Object[]> top = ad.topAttendees(startOfMonth, startOfNextMonth, 10);

            req.setAttribute("totalMembers", totalMembers);
            req.setAttribute("todayVisits", todayVisits);
            req.setAttribute("monthVisits", monthVisits);
            req.setAttribute("avgHours", String.format("%.1f", avgMin / 60.0));
            req.setAttribute("weekly", weekly);
            req.setAttribute("topAttendees", top);
            req.setAttribute("activePage", "stats");
            req.getRequestDispatcher("/WEB-INF/views/stats.jsp").forward(req, res);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
