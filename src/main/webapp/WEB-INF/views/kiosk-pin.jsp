<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8" />
<title>PIN 입력</title>
<script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gradient-to-br from-indigo-50 to-blue-100 min-h-screen">
<div class="max-w-md mx-auto p-6">
  <div class="bg-white rounded-2xl shadow-xl p-8">
    <h2 class="text-xl font-bold mb-2 text-center"><c:out value="${member.name}"/>님</h2>
    <p class="text-center text-gray-500 mb-4">PIN 4자리를 입력하세요</p>

    <c:if test="${not empty error}">
      <div class="mb-3 px-3 py-2 rounded bg-red-100 text-red-700 text-sm border border-red-200 text-center">
        <c:out value="${error}"/>
      </div>
    </c:if>

    <form id="pinForm" method="post" action="${pageContext.request.contextPath}/kiosk/verify">
      <input type="hidden" name="memberId" value="${member.memberId}" />
      <input id="pin" type="password" name="pin" inputmode="numeric" maxlength="4" autofocus
             class="w-full text-3xl tracking-widest border-2 border-indigo-300 rounded-xl px-6 py-4 text-center focus:outline-none focus:border-indigo-600"
             placeholder="****" />

      <div class="grid grid-cols-3 gap-2 mt-4" id="keypad"></div>

      <div class="flex gap-2 mt-4">
        <a href="${pageContext.request.contextPath}/kiosk"
           class="flex-1 bg-gray-200 text-gray-700 py-3 rounded-xl font-bold text-center">취소</a>
        <button type="submit" class="flex-1 bg-indigo-600 text-white py-3 rounded-xl font-bold">확인</button>
      </div>
    </form>
  </div>
</div>

<script>
const pin = document.getElementById('pin');
const pad = document.getElementById('keypad');
const keys = ['1','2','3','4','5','6','7','8','9','←','0','✓'];
keys.forEach(k => {
  const b = document.createElement('button');
  b.type = 'button';
  b.textContent = k;
  b.className = 'py-4 text-xl font-bold rounded-xl ' +
    (k === '✓' ? 'bg-indigo-600 text-white' :
     k === '←' ? 'bg-gray-200 text-gray-700' :
     'bg-gray-100 hover:bg-gray-200');
  b.onclick = () => {
    if (k === '←') pin.value = pin.value.slice(0, -1);
    else if (k === '✓') document.getElementById('pinForm').submit();
    else if (pin.value.length < 4) pin.value += k;
  };
  pad.appendChild(b);
});
</script>
</body>
</html>
