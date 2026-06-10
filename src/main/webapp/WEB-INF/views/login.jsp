<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8" />
<title>관리자 로그인 — 독서실 출결관리</title>
<script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gradient-to-br from-blue-50 to-indigo-100 min-h-screen flex items-center justify-center">
<div class="bg-white rounded-2xl shadow-xl p-8 w-full max-w-sm">
  <h1 class="text-2xl font-bold text-center mb-1">📚 독서실 출결관리</h1>
  <p class="text-center text-gray-500 mb-6">관리자 로그인</p>

  <c:if test="${not empty error}">
    <div class="mb-3 px-3 py-2 rounded bg-red-100 text-red-700 text-sm border border-red-200">
      <c:out value="${error}" />
    </div>
  </c:if>

  <form method="post" action="${pageContext.request.contextPath}/login" class="space-y-3">
    <div>
      <label class="text-sm">아이디</label>
      <input name="username" required autofocus value="admin"
             class="w-full border rounded px-3 py-2 mt-1" />
    </div>
    <div>
      <label class="text-sm">비밀번호</label>
      <input type="password" name="password" required
             class="w-full border rounded px-3 py-2 mt-1" />
    </div>
    <button class="w-full bg-blue-600 text-white py-2 rounded font-bold hover:bg-blue-700">로그인</button>
  </form>

  <p class="text-xs text-gray-400 text-center mt-4">
    초기 비밀번호: <code>admin123</code> (로그인 후 변경하세요)
  </p>
  <p class="text-center mt-4">
    <a href="${pageContext.request.contextPath}/kiosk"
       class="text-sm text-indigo-600 hover:underline">회원 키오스크 모드로 →</a>
  </p>
</div>
</body>
</html>
