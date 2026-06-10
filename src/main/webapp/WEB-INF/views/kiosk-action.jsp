<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8" />
<title>입실/퇴실</title>
<script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gradient-to-br from-indigo-50 to-blue-100 min-h-screen">
<div class="max-w-md mx-auto p-6">
  <div class="bg-white rounded-2xl shadow-xl p-8">
    <h2 class="text-2xl font-bold text-center mb-2"><c:out value="${member.name}"/>님</h2>

    <c:choose>
      <c:when test="${not empty active}">
        <p class="text-center text-gray-600 mb-6">
          ${active.seatNo}번 좌석 · ${active.checkInTime} 입실 (<c:out value="${active.durationStr}"/> 이용 중)
        </p>
        <form method="post" action="${pageContext.request.contextPath}/kiosk/checkout">
          <input type="hidden" name="memberId" value="${member.memberId}" />
          <button class="w-full bg-red-500 text-white py-8 rounded-2xl text-2xl font-bold hover:bg-red-600">
            🚪 퇴실하기
          </button>
        </form>
      </c:when>
      <c:otherwise>
        <p class="text-center text-gray-600 mb-6">현재 대기 중입니다</p>
        <a href="${pageContext.request.contextPath}/kiosk/seat?memberId=${member.memberId}"
           class="block w-full bg-blue-600 text-white py-8 rounded-2xl text-2xl font-bold hover:bg-blue-700 text-center">
          📥 입실하기 (좌석 선택)
        </a>
      </c:otherwise>
    </c:choose>

    <a href="${pageContext.request.contextPath}/kiosk"
       class="block w-full mt-4 bg-gray-200 text-gray-700 py-3 rounded-xl font-bold text-center">처음으로</a>
  </div>
</div>
</body>
</html>
