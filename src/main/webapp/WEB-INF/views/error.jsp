<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8" />
<title>오류</title>
<script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gray-100 min-h-screen flex items-center justify-center">
<div class="bg-white rounded-xl shadow-lg p-8 max-w-lg w-full">
  <h1 class="text-2xl font-bold text-red-600 mb-2">⚠️ 오류가 발생했습니다</h1>
  <p class="text-gray-700">처리 중 문제가 발생했습니다. 잠시 후 다시 시도해주세요.</p>
  <% if (exception != null) { %>
    <pre class="text-xs bg-gray-100 p-3 rounded mt-3 overflow-x-auto"><%= exception.getClass().getName() %>: <%= exception.getMessage() %></pre>
  <% } %>
  <a href="<%= request.getContextPath() %>/" class="inline-block mt-4 bg-blue-600 text-white px-4 py-2 rounded">처음으로</a>
</div>
</body>
</html>
