<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8" />
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<title>독서실 출결관리</title>
<script src="https://cdn.tailwindcss.com"></script>
<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/app.css" />
</head>
<body class="bg-gray-100 min-h-screen">
<div class="max-w-7xl mx-auto p-4">

<header class="flex items-center justify-between mb-4 no-print">
  <h1 class="text-2xl font-bold text-gray-800">📚 독서실 출결관리</h1>
  <div class="text-sm text-gray-600 flex items-center gap-3">
    <span id="now-clock"></span>
    <span>안녕하세요, <b>${sessionScope.username}</b>님</span>
    <a href="${pageContext.request.contextPath}/kiosk" target="_blank"
       class="bg-indigo-600 text-white px-3 py-1.5 rounded text-sm hover:bg-indigo-700">🖥️ 회원모드</a>
    <a href="${pageContext.request.contextPath}/logout"
       class="text-gray-500 hover:text-gray-800">로그아웃</a>
  </div>
</header>

<nav class="flex gap-2 mb-4 no-print">
  <c:set var="cur" value="${requestScope.activePage}" />
  <a href="${pageContext.request.contextPath}/admin/dashboard"
     class="px-4 py-2 rounded-lg font-medium ${cur=='dashboard' ? 'bg-blue-600 text-white' : 'bg-gray-200 text-gray-700 hover:bg-gray-300'}">대시보드</a>
  <a href="${pageContext.request.contextPath}/admin/members"
     class="px-4 py-2 rounded-lg font-medium ${cur=='members' ? 'bg-blue-600 text-white' : 'bg-gray-200 text-gray-700 hover:bg-gray-300'}">회원관리</a>
  <a href="${pageContext.request.contextPath}/admin/history"
     class="px-4 py-2 rounded-lg font-medium ${cur=='history' ? 'bg-blue-600 text-white' : 'bg-gray-200 text-gray-700 hover:bg-gray-300'}">이용기록</a>
  <a href="${pageContext.request.contextPath}/admin/stats"
     class="px-4 py-2 rounded-lg font-medium ${cur=='stats' ? 'bg-blue-600 text-white' : 'bg-gray-200 text-gray-700 hover:bg-gray-300'}">통계</a>
  <a href="${pageContext.request.contextPath}/admin/settings"
     class="px-4 py-2 rounded-lg font-medium ${cur=='settings' ? 'bg-blue-600 text-white' : 'bg-gray-200 text-gray-700 hover:bg-gray-300'}">설정</a>
</nav>

<%-- 플래시 메시지 --%>
<c:if test="${not empty sessionScope['flash.msg']}">
  <div class="mb-3 px-4 py-2 rounded
      ${sessionScope['flash.type'] == 'error' ? 'bg-red-100 text-red-800 border border-red-300' :
        sessionScope['flash.type'] == 'success' ? 'bg-green-100 text-green-800 border border-green-300' :
        'bg-blue-100 text-blue-800 border border-blue-300'}">
    <c:out value="${sessionScope['flash.msg']}" />
  </div>
  <c:remove var="flash.msg" scope="session" />
  <c:remove var="flash.type" scope="session" />
</c:if>
