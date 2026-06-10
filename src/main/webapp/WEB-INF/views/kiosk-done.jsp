<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8" />
<title>완료</title>
<script src="https://cdn.tailwindcss.com"></script>
<meta http-equiv="refresh" content="3; url=${pageContext.request.contextPath}/kiosk" />
</head>
<body class="bg-gradient-to-br from-indigo-50 to-blue-100 min-h-screen flex items-center justify-center">
<div class="max-w-md w-full mx-auto p-6">
  <c:choose>
    <c:when test="${not empty error}">
      <div class="bg-red-50 border-2 border-red-300 rounded-2xl p-8 text-center">
        <p class="text-2xl font-bold text-red-700 whitespace-pre-line"><c:out value="${error}"/></p>
        <p class="text-sm text-gray-500 mt-4">3초 후 처음 화면으로 돌아갑니다</p>
      </div>
    </c:when>
    <c:otherwise>
      <div class="bg-green-50 border-2 border-green-300 rounded-2xl p-8 text-center">
        <p class="text-5xl mb-4">✅</p>
        <p class="text-2xl font-bold text-green-700 whitespace-pre-line"><c:out value="${msg}"/></p>
        <p class="text-sm text-gray-500 mt-4">3초 후 처음 화면으로 돌아갑니다</p>
      </div>
    </c:otherwise>
  </c:choose>
  <div class="text-center mt-4">
    <a href="${pageContext.request.contextPath}/kiosk" class="text-sm text-indigo-600 hover:underline">바로 돌아가기 →</a>
  </div>
</div>
</body>
</html>
