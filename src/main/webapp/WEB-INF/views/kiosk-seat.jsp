<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8" />
<title>좌석 선택</title>
<script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gradient-to-br from-indigo-50 to-blue-100 min-h-screen">
<div class="max-w-2xl mx-auto p-6">
  <div class="bg-white rounded-2xl shadow-xl p-8">
    <h2 class="text-xl font-bold text-center mb-2"><c:out value="${member.name}"/>님, 좌석을 선택하세요</h2>
    <p class="text-center text-sm text-gray-500 mb-4">
      <span class="inline-flex items-center gap-1"><span class="w-3 h-3 bg-green-400 rounded"></span> 사용가능</span>
      <span class="inline-flex items-center gap-1 ml-3"><span class="w-3 h-3 bg-red-300 rounded"></span> 사용중</span>
    </p>

    <div class="grid grid-cols-6 sm:grid-cols-8 gap-2 max-h-96 overflow-y-auto">
      <c:forEach begin="1" end="${seatCount}" var="i">
        <c:set var="occ" value="${activeSeats[i]}" />
        <c:choose>
          <c:when test="${not empty occ}">
            <button disabled class="aspect-square rounded-lg text-base font-bold bg-red-300 text-white cursor-not-allowed">${i}</button>
          </c:when>
          <c:otherwise>
            <form method="post" action="${pageContext.request.contextPath}/kiosk/checkin">
              <input type="hidden" name="memberId" value="${member.memberId}" />
              <input type="hidden" name="seatNo" value="${i}" />
              <button class="w-full aspect-square rounded-lg text-base font-bold bg-green-400 text-white hover:bg-green-500">${i}</button>
            </form>
          </c:otherwise>
        </c:choose>
      </c:forEach>
    </div>

    <a href="${pageContext.request.contextPath}/kiosk"
       class="block w-full mt-4 bg-gray-200 text-gray-700 py-3 rounded-xl font-bold text-center">취소</a>
  </div>
</div>
</body>
</html>
