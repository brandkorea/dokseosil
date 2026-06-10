<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="layout/header.jsp" />

<div class="bg-white rounded-xl shadow p-6 max-w-3xl">
  <h2 class="font-bold text-lg mb-2">좌석 선택</h2>
  <p class="text-gray-600 mb-4"><b><c:out value="${member.name}"/></b>님의 좌석을 선택해주세요.</p>

  <div class="grid grid-cols-6 sm:grid-cols-8 gap-2">
    <c:forEach begin="1" end="${seatCount}" var="i">
      <c:set var="occ" value="${activeSeats[i]}" />
      <c:choose>
        <c:when test="${not empty occ}">
          <button disabled class="aspect-square rounded-lg bg-gray-300 text-gray-500 font-bold cursor-not-allowed">${i}</button>
        </c:when>
        <c:otherwise>
          <form method="post" action="${pageContext.request.contextPath}/admin/attend/checkin">
            <input type="hidden" name="memberId" value="${member.memberId}" />
            <input type="hidden" name="seatNo" value="${i}" />
            <button class="w-full aspect-square rounded-lg bg-green-400 text-white font-bold hover:bg-green-500">${i}</button>
          </form>
        </c:otherwise>
      </c:choose>
    </c:forEach>
  </div>

  <div class="mt-4">
    <a href="${pageContext.request.contextPath}/admin/dashboard"
       class="inline-block bg-gray-200 text-gray-700 px-4 py-2 rounded">취소</a>
  </div>
</div>

<jsp:include page="layout/footer.jsp" />
