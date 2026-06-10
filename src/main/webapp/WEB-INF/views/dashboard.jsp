<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="layout/header.jsp" />

<div class="text-sm text-gray-600 mb-3">현재 입실 <b class="text-blue-600">${activeCount}</b>명 / 총 좌석 ${seatCount}</div>

<div class="grid grid-cols-1 lg:grid-cols-3 gap-4">
  <!-- 빠른 입/퇴실 -->
  <div class="bg-white rounded-xl shadow p-4 lg:col-span-1">
    <h2 class="font-bold text-lg mb-3">⚡ 빠른 입/퇴실</h2>
    <form method="get" action="${pageContext.request.contextPath}/admin/dashboard">
      <input name="q" value="<c:out value='${q}'/>" autofocus autocomplete="off"
             placeholder="이름 또는 연락처 뒷 4자리"
             class="w-full border rounded px-3 py-2" />
    </form>

    <div class="mt-3 max-h-80 overflow-y-auto space-y-2">
      <c:choose>
        <c:when test="${empty q}">
          <p class="text-sm text-gray-400">검색어를 입력하세요</p>
        </c:when>
        <c:when test="${empty searchResults}">
          <p class="text-sm text-gray-500">검색 결과 없음</p>
        </c:when>
        <c:otherwise>
          <c:forEach var="m" items="${searchResults}">
            <div class="border rounded p-2 flex justify-between items-center">
              <div>
                <p class="font-medium"><c:out value="${m.name}"/></p>
                <p class="text-xs text-gray-500"><c:out value="${m.phone}"/></p>
              </div>
              <c:choose>
                <c:when test="${m.active}">
                  <form method="post" action="${pageContext.request.contextPath}/admin/attend/checkout">
                    <input type="hidden" name="sessionId"
                           value="${activeSeats[m.currentSeatNo].sessionId}" />
                    <button class="bg-red-600 text-white px-3 py-1 rounded text-sm hover:bg-red-700">
                      퇴실 (${m.currentSeatNo}번)
                    </button>
                  </form>
                </c:when>
                <c:otherwise>
                  <a href="${pageContext.request.contextPath}/admin/attend/checkin?memberId=${m.memberId}"
                     class="bg-blue-600 text-white px-3 py-1 rounded text-sm hover:bg-blue-700">입실</a>
                </c:otherwise>
              </c:choose>
            </div>
          </c:forEach>
        </c:otherwise>
      </c:choose>
    </div>
  </div>

  <!-- 좌석 현황 -->
  <div class="bg-white rounded-xl shadow p-4 lg:col-span-2">
    <div class="flex justify-between items-center mb-3">
      <h2 class="font-bold text-lg">💺 좌석 현황</h2>
      <div class="text-sm flex gap-3">
        <span class="flex items-center gap-1"><span class="w-3 h-3 bg-green-400 rounded"></span> 사용가능</span>
        <span class="flex items-center gap-1"><span class="w-3 h-3 bg-red-400 rounded"></span> 사용중</span>
      </div>
    </div>
    <div class="grid grid-cols-6 sm:grid-cols-8 md:grid-cols-10 gap-2">
      <c:forEach begin="1" end="${seatCount}" var="i">
        <c:set var="occ" value="${activeSeats[i]}" />
        <c:choose>
          <c:when test="${not empty occ}">
            <form method="post" action="${pageContext.request.contextPath}/admin/attend/checkout"
                  onsubmit="return confirm('${occ.memberName}님 (${i}번) 퇴실 처리할까요?');">
              <input type="hidden" name="sessionId" value="${occ.sessionId}" />
              <button class="seat w-full aspect-square rounded-lg flex flex-col items-center justify-center text-xs cursor-pointer border-2 bg-red-400 text-white border-red-500 hover:scale-105 transition-transform">
                <span class="font-bold text-sm">${i}</span>
                <span class="truncate w-full text-center px-1"><c:out value="${occ.memberName}"/></span>
              </button>
            </form>
          </c:when>
          <c:otherwise>
            <div class="seat aspect-square rounded-lg flex flex-col items-center justify-center text-xs border-2 bg-green-400 text-white border-green-500">
              <span class="font-bold text-sm">${i}</span>
              <span>빈자리</span>
            </div>
          </c:otherwise>
        </c:choose>
      </c:forEach>
    </div>
  </div>
</div>

<jsp:include page="layout/footer.jsp" />
