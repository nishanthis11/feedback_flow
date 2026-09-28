/* ==========================================================================
   Fetch API wrapper — attaches JWT, handles JSON + errors uniformly
   ========================================================================== */
const API_BASE = "/api";

const Auth = {
  getToken() { return localStorage.getItem("ff_token"); },
  getUser() {
    const raw = localStorage.getItem("ff_user");
    return raw ? JSON.parse(raw) : null;
  },
  saveSession(authResponse) {
    localStorage.setItem("ff_token", authResponse.token);
    localStorage.setItem("ff_user", JSON.stringify({
      id: authResponse.userId,
      fullName: authResponse.fullName,
      email: authResponse.email,
      role: authResponse.role
    }));
  },
  logout() {
    localStorage.removeItem("ff_token");
    localStorage.removeItem("ff_user");
    window.location.href = "index.html";
  },
  requireRole(role) {
    const user = Auth.getUser();
    if (!user || !Auth.getToken()) { window.location.href = "index.html"; return null; }
    if (role && user.role !== role) { window.location.href = "index.html"; return null; }
    return user;
  }
};

async function apiRequest(path, { method = "GET", body = null } = {}) {
  const headers = { "Content-Type": "application/json" };
  const token = Auth.getToken();
  if (token) headers["Authorization"] = "Bearer " + token;

  const response = await fetch(API_BASE + path, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined
  });

  let data = null;
  const text = await response.text();
  try { data = text ? JSON.parse(text) : null; } catch (e) { data = text; }

  if (!response.ok) {
    const message = (data && data.message) ? data.message
      : (typeof data === "string" ? data : "Request failed (" + response.status + ")");
    const err = new Error(message);
    err.status = response.status;
    err.data = data;
    throw err;
  }
  return data;
}

function showToast(message, type = "success") {
  const el = document.createElement("div");
  el.className = "toast-ff " + type;
  el.textContent = message;
  document.body.appendChild(el);
  setTimeout(() => el.remove(), 3500);
}
