document.getElementById("loginForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const btn = document.getElementById("loginBtn");
  const email = document.getElementById("email").value.trim();
  const password = document.getElementById("password").value;

  btn.disabled = true;
  btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span> Signing in...';

  try {
    const res = await apiRequest("/auth/login", { method: "POST", body: { email, password } });
    Auth.saveSession(res);

    const redirects = {
      STUDENT: "student-dashboard.html",
      STAFF: "staff-dashboard.html",
      ADMIN: "admin-dashboard.html"
    };
    window.location.href = redirects[res.role] || "index.html";
  } catch (err) {
    showToast(err.message || "Login failed", "error");
    btn.disabled = false;
    btn.innerHTML = '<i class="bi bi-box-arrow-in-right me-1"></i> Sign In';
  }
});
