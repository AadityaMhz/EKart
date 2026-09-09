/* ============================================================
   AI SUPPORT CHATBOT — self-mounting widget
   Include this single <script> on any page and it builds its
   own floating button + chat window. No markup needed.
   Keyword-based automated replies (same logic family as
   mini chat.html) plus quick-reply chips.
   ============================================================ */

(function () {
  "use strict";

  var THIS_SCRIPT = document.currentScript;

  var STORAGE_KEY = "cb_chat_history";
  var GREETED_KEY = "cb_greeted_session";

  var QUICK_REPLIES = [
    "Track my order",
    "Return policy",
    "Delivery time",
    "Payment options",
    "Discounts"
  ];

  // Keyword -> answer rules, evaluated in order (first match wins).
  var RULES = [
    { keys: ["track order", "where is my order", "order status"],
      answer: "📦 Your order is currently being processed and will be shipped soon. You can check full details on the Order page." },
    { keys: ["under rs", "under 1000", "cheap product", "budget"],
      answer: "💰 We have plenty of options under Rs. 1000 — check the Featured Products section on the home page." },
    { keys: ["return"],
      answer: "🔄 Products can be returned within 7 days of delivery, as long as they're unused and in original packaging." },
    { keys: ["deliver", "shipping time", "how long"],
      answer: "🚚 Delivery usually takes 3–5 business days depending on your location." },
    { keys: ["price", "cost", "how much"],
      answer: "💲 Prices are listed on each product page — they may vary during sales, so check for the latest offer." },
    { keys: ["warranty"],
      answer: "🛡️ Most products come with a 1 year official warranty. Check the product page for specifics." },
    { keys: ["stock", "available", "availability"],
      answer: "📦 Most listed items are in stock. If something's sold out, it'll be marked clearly on the product page." },
    { keys: ["charge", "charging", "battery"],
      answer: "⚡ Electronics on our store generally support fast charging — check the product specs for details." },
    { keys: ["discount", "offer", "coupon", "sale", "promo"],
      answer: "🔥 We run regular flash sales, and you can also spin the 🎁 Spin & Win wheel for a surprise discount code!" },
    { keys: ["payment", "pay", "esewa", "khalti", "card", "cod"],
      answer: "💳 We support eSewa, Khalti, Debit/Credit Cards and Cash on Delivery." },
    { keys: ["cancel order", "cancel my order"],
      answer: "❌ Orders can be cancelled anytime before they're shipped, from your Order page." },
    { keys: ["refund"],
      answer: "💵 Refunds are processed within 3–5 working days after the return is received." },
    { keys: ["seller", "become a seller", "sell on"],
      answer: "🏪 Want to sell with us? Head to the 'Become a Seller' page to apply — it only takes a couple of minutes." },
    { keys: ["contact", "support", "help", "human", "agent"],
      answer: "🙋 You can reach our support team from the Contact page, or keep chatting with me — I can answer most common questions!" },
    { keys: ["thank", "thanks"],
      answer: "You're very welcome! 😊 Anything else I can help with?" },
    { keys: ["hello", "hi", "hey"],
      answer: "👋 Hi there! I'm your automated shopping assistant. Ask me about orders, delivery, returns, payments or discounts." },
    { keys: ["bye", "goodbye"],
      answer: "👋 Thanks for stopping by — happy shopping!" }
  ];

  var FALLBACK =
    "🤖 I'm not sure about that one yet — try asking about orders, delivery, returns, payment, or discounts. For anything else, visit our Contact page.";

  function findAnswer(text) {
    var q = text.toLowerCase();
    for (var i = 0; i < RULES.length; i++) {
      var rule = RULES[i];
      for (var j = 0; j < rule.keys.length; j++) {
        if (q.indexOf(rule.keys[j]) !== -1) return rule.answer;
      }
    }
    return FALLBACK;
  }

  function resolveAssetPath(relPath) {
    var script = THIS_SCRIPT;
    if (script && script.src) {
      try {
        var base = script.src.replace(/js\/chatbot\.js.*$/, "");
        return base + relPath;
      } catch (e) {}
    }
    return relPath;
  }

  function ensureAssets() {
    if (!document.querySelector('link[href*="chatbot.css"]')) {
      var link = document.createElement("link");
      link.rel = "stylesheet";
      link.href = resolveAssetPath("css/chatbot.css");
      document.head.appendChild(link);
    }
  }

  function build() {
    ensureAssets();

    var fab = document.createElement("button");
    fab.className = "cb-fab";
    fab.setAttribute("aria-label", "Chat with support assistant");
    fab.innerHTML = '🤖<span class="cb-dot"></span>';

    var win = document.createElement("div");
    win.className = "cb-window";
    win.innerHTML =
      '<div class="cb-header">' +
      '  <div class="cb-avatar">🤖</div>' +
      '  <div class="cb-title"><strong>Shopping Assistant</strong><span>Usually replies instantly</span></div>' +
      '  <button class="cb-close" aria-label="Close chat">×</button>' +
      "</div>" +
      '<div class="cb-messages" id="cbMessages"></div>' +
      '<div class="cb-quick" id="cbQuick"></div>' +
      '<div class="cb-input-row">' +
      '  <input type="text" id="cbInput" placeholder="Type a message..." />' +
      '  <button class="cb-send" id="cbSend" aria-label="Send">➤</button>' +
      "</div>";

    document.body.appendChild(fab);
    document.body.appendChild(win);

    var messagesEl = win.querySelector("#cbMessages");
    var quickEl = win.querySelector("#cbQuick");
    var inputEl = win.querySelector("#cbInput");
    var sendBtn = win.querySelector("#cbSend");
    var closeBtn = win.querySelector(".cb-close");

    function addMessage(text, who) {
      var el = document.createElement("div");
      el.className = "cb-msg cb-" + who;
      el.textContent = text;
      messagesEl.appendChild(el);
      messagesEl.scrollTop = messagesEl.scrollHeight;
    }

    function showTyping(cb) {
      var t = document.createElement("div");
      t.className = "cb-typing";
      t.innerHTML = "<span></span><span></span><span></span>";
      messagesEl.appendChild(t);
      messagesEl.scrollTop = messagesEl.scrollHeight;
      setTimeout(function () {
        t.remove();
        cb();
      }, 550 + Math.random() * 500);
    }

    function respondTo(text) {
      addMessage(text, "user");
      showTyping(function () {
        addMessage(findAnswer(text), "bot");
      });
    }

    function renderQuickReplies() {
      quickEl.innerHTML = "";
      QUICK_REPLIES.forEach(function (label) {
        var chip = document.createElement("button");
        chip.className = "cb-chip";
        chip.type = "button";
        chip.textContent = label;
        chip.addEventListener("click", function () {
          respondTo(label);
        });
        quickEl.appendChild(chip);
      });
    }

    function openChat() {
      win.classList.add("cb-open");
      if (!sessionStorage.getItem(GREETED_KEY)) {
        sessionStorage.setItem(GREETED_KEY, "1");
        showTyping(function () {
          addMessage(
            "👋 Hi! I'm your automated shopping assistant. Ask me about orders, delivery, returns, payments or discounts.",
            "bot"
          );
        });
      }
      inputEl.focus();
    }

    function closeChat() {
      win.classList.remove("cb-open");
    }

    fab.addEventListener("click", function () {
      win.classList.contains("cb-open") ? closeChat() : openChat();
    });
    closeBtn.addEventListener("click", closeChat);

    sendBtn.addEventListener("click", function () {
      var val = inputEl.value.trim();
      if (!val) return;
      respondTo(val);
      inputEl.value = "";
    });

    inputEl.addEventListener("keydown", function (e) {
      if (e.key === "Enter") {
        e.preventDefault();
        sendBtn.click();
      }
    });

    renderQuickReplies();
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", build);
  } else {
    build();
  }
})();
