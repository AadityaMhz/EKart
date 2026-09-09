/* ============================================================
   SPIN & WIN — gamification widget (self-mounting)
   Include this single <script> on any page and it builds its
   own floating button + modal + wheel. No markup needed.
   Rules:
     - One free spin per calendar day (stored in localStorage)
     - Prizes are % discount coupons (mostly), usable in cart.html
     - Winning triggers a small confetti burst
   ============================================================ */

(function () {
  "use strict";

  var THIS_SCRIPT = document.currentScript;

  var CURRENT_USER = (typeof window !== "undefined" && window.EKART_USER) ? String(window.EKART_USER) : "guest";

  var STORAGE_KEY = "sw_last_spin_date_" + CURRENT_USER;
  var COUPON_KEY = "sw_active_coupon_" + CURRENT_USER;
  var COUPON_EXPIRY_KEY = "sw_coupon_expiry_" + CURRENT_USER;
  var COUPON_HOURS = 24;

  var PRIZES = [
    { label: "5% OFF",  sub: "on any order",  code: "SPIN5",  color: "#e53935" },
    { label: "Try Again", sub: "so close!",   code: null,     color: "#2b2b2b" },
    { label: "10% OFF", sub: "on any order",  code: "SPIN10", color: "#b71c1c" },
    { label: "Free Ship", sub: "next order",  code: "FREESHIP", color: "#e53935" },
    { label: "15% OFF", sub: "on any order",  code: "SPIN15", color: "#2b2b2b" },
    { label: "No Luck", sub: "try tomorrow",  code: null,     color: "#b71c1c" },
    { label: "20% OFF", sub: "biggest prize!", code: "SPIN20", color: "#e53935" },
    { label: "50 Points", sub: "loyalty points", code: "PTS50", color: "#2b2b2b" }
  ];

  function todayStr() {
    var d = new Date();
    return d.getFullYear() + "-" + (d.getMonth() + 1) + "-" + d.getDate();
  }

  function alreadySpunToday() {
    try { return localStorage.getItem(STORAGE_KEY) === todayStr(); } catch (e) { return false; }
  }

  function markSpunToday() {
    try { localStorage.setItem(STORAGE_KEY, todayStr()); } catch (e) {}
  }

  function saveCoupon(code) {
    try {
      localStorage.setItem(COUPON_KEY, code);
      localStorage.setItem(COUPON_EXPIRY_KEY, String(Date.now() + COUPON_HOURS * 3600 * 1000));
    } catch (e) {}
  }

  function buildWheelGradient() {
    var n = PRIZES.length;
    var slice = 360 / n;
    var stops = [];
    for (var i = 0; i < n; i++) {
      var start = i * slice;
      var end = start + slice;
      stops.push(PRIZES[i].color + " " + start + "deg " + end + "deg");
    }
    return "conic-gradient(" + stops.join(", ") + ")";
  }

  function buildWheelLabels() {
    var n = PRIZES.length;
    var slice = 360 / n;
    var labels = "";
    for (var i = 0; i < n; i++) {
      var angle = i * slice + slice / 2;
      labels +=
        '<div class="sw-slice-label" style="transform: translate(-50%,-50%) rotate(' + angle + 'deg) translateY(-88px) rotate(0deg);">' +
        '<span>' + PRIZES[i].label + '</span></div>';
    }
    return labels;
  }

  function toast(msg) {
    var t = document.createElement("div");
    t.className = "theme-toast";
    t.textContent = msg;
    document.body.appendChild(t);
    requestAnimationFrame(function () { t.classList.add("show"); });
    setTimeout(function () {
      t.classList.remove("show");
      setTimeout(function () { t.remove(); }, 300);
    }, 2600);
  }

  function confettiBurst(container) {
    var colors = ["#e53935", "#b71c1c", "#ffb300", "#2b2b2b", "#ff6f60"];
    for (var i = 0; i < 46; i++) {
      var piece = document.createElement("div");
      piece.className = "sw-confetti-piece";
      var size = 6 + Math.random() * 6;
      piece.style.width = size + "px";
      piece.style.height = (size * 0.4) + "px";
      piece.style.background = colors[i % colors.length];
      piece.style.left = 50 + (Math.random() * 60 - 30) + "%";
      piece.style.top = "40%";
      var dx = (Math.random() * 240 - 120) + "px";
      var dy = (140 + Math.random() * 140) + "px";
      var rot = (Math.random() * 720 - 360) + "deg";
      piece.style.setProperty("--dx", dx);
      piece.style.setProperty("--dy", dy);
      piece.style.setProperty("--rot", rot);
      piece.style.animationDelay = (Math.random() * 0.15) + "s";
      container.appendChild(piece);
      setTimeout(function (el) { return function () { el.remove(); }; }(piece), 1600);
    }
  }

  function ensureAssets() {
    if (!document.querySelector('link[href*="spin-wheel.css"]')) {
      var link = document.createElement("link");
      link.rel = "stylesheet";
      link.href = resolveAssetPath("css/spin-wheel.css");
      document.head.appendChild(link);
    }
  }

  function resolveAssetPath(relPath) {
    var script = THIS_SCRIPT;
    if (script && script.src) {
      try {
        var base = script.src.replace(/js\/spin-wheel\.js.*$/, "");
        return base + relPath;
      } catch (e) {}
    }
    return relPath;
  }

  function build() {
    ensureAssets();

    var fab = document.createElement("button");
    fab.className = "sw-fab";
    fab.setAttribute("aria-label", "Spin the wheel to win a discount");
    fab.innerHTML =
      '<span class="sw-fab-ping"></span>' +
      '<span class="sw-fab-emoji">🎁</span><span class="sw-fab-label">SPIN &amp; SAVE</span>';

    var overlay = document.createElement("div");
    overlay.className = "sw-overlay";
    overlay.innerHTML =
      '<div class="sw-modal" role="dialog" aria-modal="true">' +
      '  <button class="sw-close" aria-label="Close">×</button>' +
      '  <div class="sw-title">🎉 Spin &amp; Win a Discount</div>' +
      '  <div class="sw-subtitle">Free daily spin — every slice is a real reward, up to 20% off!</div>' +
      '  <div class="sw-wheel-wrap" id="swWheelWrap">' +
      '    <div class="sw-pointer"></div>' +
      '    <div class="sw-wheel" id="swWheel">' + buildWheelLabels() + '</div>' +
      '    <div class="sw-hub">★</div>' +
      '  </div>' +
      '  <button class="sw-spin-btn" id="swSpinBtn">Spin Now — It\'s Free</button>' +
      '  <div class="sw-result" id="swResult"></div>' +
      "</div>";

    document.body.appendChild(fab);
    document.body.appendChild(overlay);

    var wheel = overlay.querySelector("#swWheel");
    var wheelWrap = overlay.querySelector("#swWheelWrap");
    wheel.style.background = buildWheelGradient();

    var spinBtn = overlay.querySelector("#swSpinBtn");
    var resultEl = overlay.querySelector("#swResult");
    var closeBtn = overlay.querySelector(".sw-close");
    var rotation = 0;

    function openModal() {
      overlay.classList.add("sw-open");
      if (alreadySpunToday()) {
        spinBtn.disabled = true;
        spinBtn.textContent = "Come back tomorrow";
        var activeCoupon = null;
        try { activeCoupon = localStorage.getItem(COUPON_KEY); } catch (e) {}
        resultEl.innerHTML = activeCoupon
          ? 'You already won today! Your code <strong>' + activeCoupon + '</strong> is still active — use it at checkout.'
          : "You've used today's spin — come back tomorrow for another try!";
      }
    }

    function closeModal() { overlay.classList.remove("sw-open"); }

    fab.addEventListener("click", openModal);
    closeBtn.addEventListener("click", closeModal);
    overlay.addEventListener("click", function (e) { if (e.target === overlay) closeModal(); });

    spinBtn.addEventListener("click", function () {
      if (alreadySpunToday()) return;
      spinBtn.disabled = true;
      resultEl.textContent = "";

      var n = PRIZES.length;
      var slice = 360 / n;
      var winnerIndex = Math.floor(Math.random() * n);
      var targetAngle = 360 * 5 + (360 - (winnerIndex * slice + slice / 2));
      rotation = targetAngle;
      wheel.style.transform = "rotate(" + rotation + "deg)";

      setTimeout(function () {
        markSpunToday();
        var prize = PRIZES[winnerIndex];
        confettiBurst(wheelWrap);

        if (prize.code) {
          saveCoupon(prize.code);
          resultEl.innerHTML =
            "You won <strong>" + prize.label + "</strong> (" + prize.sub + ")!<br/>" +
            '<span class="sw-code-box">' + prize.code +
            ' <button class="sw-copy" id="swCopyBtn">Copy</button></span>' +
            '<div class="sw-expiry">Valid for ' + COUPON_HOURS + 'h — apply it in your cart</div>';
          var copyBtn = document.getElementById("swCopyBtn");
          if (copyBtn) {
            copyBtn.addEventListener("click", function () {
              navigator.clipboard && navigator.clipboard.writeText(prize.code);
              toast("Coupon copied: " + prize.code);
            });
          }
          toast("🎉 You won " + prize.label + "! Code: " + prize.code);
          if (typeof updateHeaderCounts === "function") updateHeaderCounts();
        } else {
          resultEl.innerHTML = "<strong>" + prize.label + "</strong> — better luck tomorrow!";
          toast("No prize this time — try again tomorrow!");
        }
        spinBtn.textContent = "Come back tomorrow";
      }, 4600);
    });
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", build);
  } else {
    build();
  }
})();
