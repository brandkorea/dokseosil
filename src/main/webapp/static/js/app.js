// 헤더 시계
(function () {
  const el = document.getElementById('now-clock');
  if (!el) return;
  const p = (x) => String(x).padStart(2, '0');
  function tick() {
    const n = new Date();
    el.textContent =
      n.getFullYear() + '-' + p(n.getMonth() + 1) + '-' + p(n.getDate()) + ' ' +
      p(n.getHours()) + ':' + p(n.getMinutes()) + ':' + p(n.getSeconds());
  }
  setInterval(tick, 1000); tick();
})();
