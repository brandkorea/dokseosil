<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8" />
<title>독서실 출결 - 회원모드</title>
<script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gradient-to-br from-indigo-50 to-blue-100 min-h-screen">
<div class="max-w-3xl mx-auto p-6">
  <div class="flex justify-between items-center mb-6">
    <h1 class="text-3xl font-bold text-indigo-900">📚 독서실 출결</h1>
    <div class="text-right">
      <p id="clock" class="text-2xl font-mono text-indigo-800"></p>
      <p class="text-sm text-gray-600">현재 입실 <b>${activeCount}</b>명</p>
    </div>
  </div>

  <div class="bg-white rounded-2xl shadow-xl p-8">
    <h2 class="text-xl font-bold mb-4 text-center text-gray-700">
      이름 또는 연락처 뒤 4자리를 입력하세요
    </h2>
    <form method="get" action="${pageContext.request.contextPath}/kiosk">
      <input name="q" value="<c:out value='${q}'/>" autocomplete="off" autofocus
             class="w-full text-2xl border-2 border-indigo-300 rounded-xl px-6 py-4 text-center focus:outline-none focus:border-indigo-600"
             placeholder="홍길동 또는 5678" />
    </form>

    <div class="mt-4 space-y-2">
      <c:if test="${not empty q and empty results}">
        <p class="text-center text-gray-500 py-4">일치하는 회원이 없습니다</p>
      </c:if>
      <c:forEach var="m" items="${results}">
        <a href="${pageContext.request.contextPath}/kiosk/member?id=${m.memberId}"
           class="block p-4 border-2 rounded-xl hover:bg-indigo-50 hover:border-indigo-400 flex justify-between items-center">
          <div>
            <p class="text-xl font-bold"><c:out value="${m.name}"/></p>
            <p class="text-sm text-gray-500">
              <c:if test="${not empty m.phone}">****-${fn:substring(m.phone, fn:length(m.phone) - 4, fn:length(m.phone))}</c:if>
            </p>
          </div>
          <c:choose>
            <c:when test="${m.active}">
              <span class="bg-red-100 text-red-700 px-3 py-1 rounded-full text-sm font-bold">${m.currentSeatNo}번 입실중</span>
            </c:when>
            <c:otherwise>
              <span class="bg-green-100 text-green-700 px-3 py-1 rounded-full text-sm font-bold">입실 가능</span>
            </c:otherwise>
          </c:choose>
        </a>
      </c:forEach>
    </div>

    <p class="text-sm text-gray-500 text-center mt-6">회원이 아니시면 카운터에 문의해주세요.</p>
  </div>

  <div class="text-center mt-6">
    <a href="${pageContext.request.contextPath}/login" class="text-xs text-gray-400 hover:text-gray-700">🔒 관리자 로그인</a>
  </div>
</div>

<script>
function tick() {
  const n = new Date();
  const p = x => String(x).padStart(2, '0');
  document.getElementById('clock').textContent =
    n.getFullYear() + '-' + p(n.getMonth()+1) + '-' + p(n.getDate()) + ' ' +
    p(n.getHours()) + ':' + p(n.getMinutes()) + ':' + p(n.getSeconds());
}
setInterval(tick, 1000); tick();
</script>
</body>
</html>
