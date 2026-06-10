<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="layout/header.jsp" />

<div class="grid grid-cols-1 lg:grid-cols-3 gap-4">
  <!-- 목록 -->
  <div class="bg-white rounded-xl shadow p-4 lg:col-span-2">
    <div class="flex justify-between items-center mb-4">
      <h2 class="font-bold text-lg">👤 회원 목록 (${fn:length(members)}명)</h2>
      <form method="get" action="${pageContext.request.contextPath}/admin/members">
        <input name="q" value="<c:out value='${q}'/>" placeholder="이름/연락처 검색"
               class="border rounded px-3 py-1 text-sm" />
      </form>
    </div>
    <div class="overflow-x-auto">
      <table class="w-full text-sm">
        <thead class="bg-gray-100 text-gray-700">
          <tr>
            <th class="p-2 text-left">ID</th>
            <th class="p-2 text-left">이름</th>
            <th class="p-2 text-left">연락처</th>
            <th class="p-2 text-center">PIN</th>
            <th class="p-2 text-left">등록일</th>
            <th class="p-2 text-left">상태</th>
            <th class="p-2 text-center">관리</th>
          </tr>
        </thead>
        <tbody>
          <c:if test="${empty members}">
            <tr><td colspan="7" class="p-4 text-center text-gray-500">회원이 없습니다.</td></tr>
          </c:if>
          <c:forEach var="m" items="${members}">
            <tr class="border-b hover:bg-gray-50">
              <td class="p-2">${m.memberId}</td>
              <td class="p-2 font-medium"><c:out value="${m.name}"/></td>
              <td class="p-2"><c:out value="${m.phone}"/></td>
              <td class="p-2 text-center">${empty m.pin ? '-' : '●●●●'}</td>
              <td class="p-2">${m.createdDate}</td>
              <td class="p-2">
                <c:choose>
                  <c:when test="${m.active}">
                    <span class="bg-red-100 text-red-700 px-2 py-0.5 rounded text-xs">입실중 (${m.currentSeatNo}번)</span>
                  </c:when>
                  <c:otherwise>
                    <span class="bg-gray-100 text-gray-600 px-2 py-0.5 rounded text-xs">대기</span>
                  </c:otherwise>
                </c:choose>
              </td>
              <td class="p-2 text-center text-sm">
                <a href="${pageContext.request.contextPath}/admin/members?edit=${m.memberId}<c:if test='${not empty q}'>&q=${q}</c:if>"
                   class="text-blue-600 hover:underline">수정</a>
                <form method="post" action="${pageContext.request.contextPath}/admin/members/delete"
                      class="inline ml-2" onsubmit="return confirm('정말 삭제하시겠습니까?\n(이용 기록도 함께 삭제됩니다)');">
                  <input type="hidden" name="memberId" value="${m.memberId}" />
                  <button class="text-red-600 hover:underline" ${m.active ? 'disabled title="입실중인 회원은 삭제 불가"' : ''}>삭제</button>
                </form>
              </td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </div>
  </div>

  <!-- 등록/수정 폼 -->
  <div class="bg-white rounded-xl shadow p-4">
    <h2 class="font-bold text-lg mb-3">
      <c:choose>
        <c:when test="${not empty edit}">회원 수정 #${edit.memberId}</c:when>
        <c:otherwise>회원 등록</c:otherwise>
      </c:choose>
    </h2>
    <form method="post" action="${pageContext.request.contextPath}/admin/members/save" class="space-y-3">
      <c:if test="${not empty edit}">
        <input type="hidden" name="memberId" value="${edit.memberId}" />
      </c:if>
      <div>
        <label class="text-sm">이름 *</label>
        <input name="name" required value="<c:out value='${edit.name}'/>"
               class="w-full border rounded px-3 py-2 mt-1" />
      </div>
      <div>
        <label class="text-sm">연락처</label>
        <input name="phone" value="<c:out value='${edit.phone}'/>" placeholder="010-1234-5678"
               class="w-full border rounded px-3 py-2 mt-1" />
      </div>
      <div>
        <label class="text-sm">PIN (4자리, 회원모드용)</label>
        <input name="pin" maxlength="4" pattern="\d{4}" value="<c:out value='${edit.pin}'/>" placeholder="0000"
               class="w-full border rounded px-3 py-2 mt-1" />
      </div>
      <div>
        <label class="text-sm">메모</label>
        <textarea name="memo" rows="2" class="w-full border rounded px-3 py-2 mt-1"><c:out value="${edit.memo}"/></textarea>
      </div>
      <div class="flex gap-2">
        <button class="bg-blue-600 text-white px-4 py-2 rounded font-medium hover:bg-blue-700">저장</button>
        <c:if test="${not empty edit}">
          <a href="${pageContext.request.contextPath}/admin/members" class="px-4 py-2 rounded border">취소</a>
        </c:if>
      </div>
    </form>
  </div>
</div>

<jsp:include page="layout/footer.jsp" />
