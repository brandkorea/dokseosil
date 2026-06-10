<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="layout/header.jsp" />

<div class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-4">
  <div class="bg-white rounded-xl shadow p-4">
    <p class="text-sm text-gray-600">총 회원수</p>
    <p class="text-3xl font-bold text-blue-600">${totalMembers}</p>
  </div>
  <div class="bg-white rounded-xl shadow p-4">
    <p class="text-sm text-gray-600">오늘 방문</p>
    <p class="text-3xl font-bold text-green-600">${todayVisits}</p>
  </div>
  <div class="bg-white rounded-xl shadow p-4">
    <p class="text-sm text-gray-600">이번 달 방문</p>
    <p class="text-3xl font-bold text-purple-600">${monthVisits}</p>
  </div>
  <div class="bg-white rounded-xl shadow p-4">
    <p class="text-sm text-gray-600">평균 이용시간</p>
    <p class="text-3xl font-bold text-orange-600">${avgHours}h</p>
  </div>
</div>

<div class="grid grid-cols-1 md:grid-cols-2 gap-4">
  <div class="bg-white rounded-xl shadow p-4">
    <h3 class="font-bold mb-3">최근 7일 방문자수</h3>
    <c:set var="maxCnt" value="1" />
    <c:forEach var="row" items="${weekly}">
      <c:if test="${row[2] > maxCnt}"><c:set var="maxCnt" value="${row[2]}" /></c:if>
    </c:forEach>
    <c:forEach var="row" items="${weekly}">
      <div class="flex items-center gap-2 mb-1">
        <span class="text-xs w-10 text-gray-600">${row[0]}/${row[1]}</span>
        <div class="flex-1 bg-gray-100 rounded h-6 relative">
          <div class="bg-blue-500 h-6 rounded" style="width: ${row[2] * 100 / maxCnt}%"></div>
          <span class="absolute right-2 top-0.5 text-xs font-medium">${row[2]}</span>
        </div>
      </div>
    </c:forEach>
  </div>

  <div class="bg-white rounded-xl shadow p-4">
    <h3 class="font-bold mb-3">이번 달 출석 TOP 10</h3>
    <c:if test="${empty topAttendees}">
      <p class="text-gray-500 text-sm">데이터 없음</p>
    </c:if>
    <ol class="space-y-1 text-sm">
      <c:forEach var="t" items="${topAttendees}" varStatus="st">
        <li class="flex justify-between border-b py-1">
          <span>${st.index + 1}. <c:out value="${t[1]}"/></span>
          <span class="font-bold text-blue-600">${t[2]}회</span>
        </li>
      </c:forEach>
    </ol>
  </div>
</div>

<jsp:include page="layout/footer.jsp" />
