<%@ page contentType="text/html;charset=UTF-8" language="java" session="true" %>
<%
    Object adm = session.getAttribute("admin");
    String target = (adm != null && Boolean.TRUE.equals(adm))
        ? request.getContextPath() + "/admin/dashboard"
        : request.getContextPath() + "/login";
    response.sendRedirect(target);
%>
