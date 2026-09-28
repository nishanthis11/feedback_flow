/* ==========================================================================
   Theme Switcher — Light / Dark mode with localStorage persistence
   ========================================================================== */
(function () {
  const STORAGE_KEY = "feedbackflow-theme";

  function applyTheme(theme) {
    document.documentElement.setAttribute("data-theme", theme);
    const icon = document.getElementById("themeIcon");
    if (icon) {
      icon.className = theme === "dark" ? "bi bi-sun-fill" : "bi bi-moon-stars-fill";
    }
    const label = document.getElementById("themeLabel");
    if (label) label.textContent = theme === "dark" ? "Dark" : "Light";
  }

  function getSavedTheme() {
    return localStorage.getItem(STORAGE_KEY) ||
      (window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light");
  }

  function toggleTheme() {
    const current = document.documentElement.getAttribute("data-theme") === "dark" ? "dark" : "light";
    const next = current === "dark" ? "light" : "dark";
    localStorage.setItem(STORAGE_KEY, next);
    applyTheme(next);
  }

  // Apply saved theme as early as possible (before paint where feasible)
  applyTheme(getSavedTheme());

  document.addEventListener("DOMContentLoaded", () => {
    applyTheme(getSavedTheme());
    const toggle = document.getElementById("themeToggle");
    if (toggle) toggle.addEventListener("click", toggleTheme);
  });

  window.FF_toggleTheme = toggleTheme;
})();
