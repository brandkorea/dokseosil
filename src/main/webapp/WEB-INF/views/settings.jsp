<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="layout/header.jsp" />

<div class="grid grid-cols-1 md:grid-cols-2 gap-4">
  <div class="bg-white rounded-xl shadow p-4">
    <h2 class="font-bold text-lg mb-4">⚙️ 운영 설정</h2>
    <form method="post" action="${pageContext.request.contextPath}/admin/settings/save" class="space-y-4">
      <div>
        <label class="block text-sm font-medium mb-1">좌석 수</label>
        <input type="number" name="seatCount" min="1" max="500"
               value="${settings['seat_count']}" class="border rounded px-3 py-2 w-32" required />
        <p class="text-xs text-gray-500 mt-1">현재 사용중인 좌석보다 적게 설정할 수 없습니다.</p>
      </div>
      <div>
        <label class="flex items-center gap-2">
          <input type="checkbox" name="requirePin" value="true"
                 <c:if test="${settings['require_pin'] == 'true'}">checked</c:if> />
          <span class="text-sm">회원모드에서 PIN 확인 필수 (PIN 등록된 회원만)</span>
        </label>
      </div>
      <div>
        <label class="block text-sm font-medium mb-1">관리자 PIN (회원모드 해제 시)</label>
        <c:set var="adminPinVal" value="${settings['admin_pin']}" />
        <input type="text" name="adminPin" maxlength="6" pattern="\d{4,6}"
               value="${adminPinVal}"
               placeholder="비우면 사용 안함"
               class="border rounded px-3 py-2 w-32" />
        <p class="text-xs text-gray-500 mt-1">숫자 4~6자리.</p>
      </div>
      <button class="bg-blue-600 text-white px-4 py-2 rounded font-medium hover:bg-blue-700">저장</button>
    </form>
  </div>

  <div class="bg-white rounded-xl shadow p-4">
    <h2 class="font-bold text-lg mb-4">🔑 관리자 비밀번호 변경</h2>
    <form method="post" action="${pageContext.request.contextPath}/admin/settings/password" class="space-y-3">
      <div>
        <label class="text-sm">현재 비밀번호</label>
        <input type="password" name="current" required class="w-full border rounded px-3 py-2 mt-1" />
      </div>
      <div>
        <label class="text-sm">새 비밀번호 (4자 이상)</label>
        <input type="password" name="next" required minlength="4" class="w-full border rounded px-3 py-2 mt-1" />
      </div>
      <div>
        <label class="text-sm">새 비밀번호 확인</label>
        <input type="password" name="confirm" required class="w-full border rounded px-3 py-2 mt-1" />
      </div>
      <button class="bg-red-600 text-white px-4 py-2 rounded font-medium hover:bg-red-700">변경</button>
    </form>
  </div>
</div>

<jsp:include page="layout/footer.jsp" />
