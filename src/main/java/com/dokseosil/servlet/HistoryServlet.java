package com.dokseosil.servlet;

import com.dokseosil.dao.AttendDAO;
import com.dokseosil.model.AttendSession;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@WebServlet({"/admin/history", "/admin/history/export", "/admin/history/delete"})
public class HistoryServlet extends HttpServlet {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DT_FMT   = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        try {
            LocalDate to = parseOrToday(req.getParameter("to"));
            LocalDate from = parseOrDefault(req.getParameter("from"), to.minusDays(30));
            String nameQ = req.getParameter("name");
            List<AttendSession> list = new AttendDAO().history(from, to, nameQ == null ? "" : nameQ.trim());

            if (req.getServletPath().endsWith("/export")) {
                exportCsv(res, list);
                return;
            }
            req.setAttribute("from", from.format(DATE_FMT));
            req.setAttribute("to", to.format(DATE_FMT));
            req.setAttribute("name", nameQ);
            req.setAttribute("sessions", list);
            req.setAttribute("activePage", "history");
            req.getRequestDispatcher("/WEB-INF/views/history.jsp").forward(req, res);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        if (!req.getServletPath().endsWith("/delete")) { res.sendError(405); return; }
        try {
            int id = Integer.parseInt(req.getParameter("sessionId"));
            new AttendDAO().deleteSession(id);
            AttendServlet.redirectFlash(req, res, "/admin/history", "삭제되었습니다", "success");
        } catch (NumberFormatException | SQLException e) {
            throw new ServletException(e);
        }
    }

    private void exportCsv(HttpServletResponse res, List<AttendSession> list) throws IOException {
        res.setContentType("text/csv; charset=UTF-8");
        res.setHeader("Content-Disposition",
            "attachment; filename=\"history_" + LocalDate.now().format(DATE_FMT) + ".csv\"");
        try (PrintWriter w = res.getWriter()) {
            w.write('﻿'); // Excel용 BOM
            w.println("날짜,이름,연락처,좌석,입실,퇴실,이용시간(분)");
            for (AttendSession s : list) {
                w.printf("%s,%s,%s,%d,%s,%s,%s%n",
                    csv(s.getCheckIn().format(DATE_FMT)),
                    csv(s.getMemberName()),
                    csv(s.getMemberPhone()),
                    s.getSeatNo(),
                    csv(s.getCheckIn().format(DT_FMT)),
                    csv(s.getCheckOut() != null ? s.getCheckOut().format(DT_FMT) : "입실중"),
                    csv(s.getCheckOut() != null ? String.valueOf(s.getDurationMinutes()) : ""));
            }
        }
    }

    private static String csv(String s) {
        if (s == null) return "\"\"";
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

    private static LocalDate parseOrToday(String s) {
        try { return s == null || s.isEmpty() ? LocalDate.now() : LocalDate.parse(s); }
        catch (Exception e) { return LocalDate.now(); }
    }
    private static LocalDate parseOrDefault(String s, LocalDate def) {
        try { return s == null || s.isEmpty() ? def : LocalDate.parse(s); }
        catch (Exception e) { return def; }
    }
}
