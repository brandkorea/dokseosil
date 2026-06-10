<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="layout/header.jsp" />

<div class="bg-white rounded-xl shadow p-4">
  <h2 class="font-bold text-lg mb-3">📋 이용 기록</h2>

  <form method="get" action="${pageContext.request.contextPath}/admin/history"
        class="flex flex-wrap gap-2 items-center mb-4 no-print">
    <label class="text-sm">기간:</label>
    <input type="date" name="from" value="${from}" class="border rounded px-2 py-1" />
    <span>~</span>
    <input type="date" name="to" value="${to}" class="border rounded px-2 py-1" />
    <input type="text" name="name" value="<c:out value='${name}'/>" placeholder="이름 검색"
           class="border rounded px-2 py-1" />
    <button class="bg-blue-600 text-white px-3 py-1 rounded text-sm hover:bg-blue-700">조회</button>
    <a href="${pageContext.request.contextPath}/admin/history/export?from=${from}&to=${to}&name=${name}"
       class="bg-green-600 text-white px-3 py-1 rounded text-sm hover:bg-green-700">CSV 내보내기</a>
    <button type="button" onclick="window.print()"
            class="bg-gray-600 text-white px-3 py-1 rounded text-sm hover:bg-gray-700">🖨️ 인쇄</button>
  </form>

  <div class="overflow-x-auto">
    <table class="w-full text-sm">
      <thead class="bg-gray-100 text-gray-700">
        <tr>
          <th class="p-2 text-left">날짜</th>
          <th class="p-2 text-left">이름</th>
          <th class="p-2 text-center">좌석</th>
          <th class="p-2 text-center">입실</th>
          <th class="p-2 text-center">퇴실</th>
          <th class="p-2 text-center">이용시간</th>
          <th class="p-2 text-center no-print">관리</th>
        </tr>
      </thead>
      <tbody>
        <c:if test="${empty sessions}">
          <tr><td colspan="7" class="p-4 text-center text-gray-500">기록이 없습니다.</td></tr>
        </c:if>
        <c:forEach var="s" items="${sessions}">
          <tr class="border-b hover:bg-gray-50">
            <td class="p-2">${s.checkInDate}</td>
            <td class="p-2 font-medium"><c:out value="${s.memberName}"/></td>
            <td class="p-2 text-center">${s.seatNo}</td>
            <td class="p-2 text-center">${s.checkInTime}</td>
            <td class="p-2 text-center">
              <c:choose>
                <c:when test="${s.active}">
                  <span class="text-red-600 font-bold">입실중</span>
                </c:when>
                <c:otherwise>
                  ${s.checkOutTime}
                </c:otherwise>
              </c:choose>
            </td>
            <td class="p-2 text-center">${s.active ? '-' : s.durationStr}</td>
            <td class="p-2 text-center no-print">
              <c:choose>
                <c:when test="${s.active}">
                  <form method="post" action="${pageContext.request.contextPath}/admin/attend/checkout"
                        class="inline" onsubmit="return confirm('강제 퇴실 처리할까요?');">
                    <input type="hidden" name="sessionId" value="${s.sessionId}" />
                    <input type="hidden" name="returnTo" value="/admin/history" />
                    <button class="text-blue-600 hover:underline text-xs">퇴실처리</button>
                  </form>
                </c:when>
                <c:otherwise>
                  <form method="post" action="${pageContext.request.contextPath}/admin/history/delete"
                        class="inline" onsubmit="return confirm('이 기록을 삭제할까요?');">
                    <input type="hidden" name="sessionId" value="${s.sessionId}" />
                    <button class="text-red-600 hover:underline text-xs">삭제</button>
                  </form>
                </c:otherwise>
              </c:choose>
            </td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
  </div>
</div>

<jsp:include page="layout/footer.jsp" />
