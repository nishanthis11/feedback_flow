const user = Auth.requireRole("ADMIN");
if (user) document.getElementById("navUserName").textContent = user.fullName;
document.getElementById("logoutLink").addEventListener("click", (e) => { e.preventDefault(); Auth.logout(); });
document.getElementById("manageCoursesLink").addEventListener("click", (e) => {
  e.preventDefault();
  new bootstrap.Modal(document.getElementById("createCourseModal")).show();
});

function fmt(n) { return (n === null || n === undefined) ? "—" : Number(n).toFixed(1); }
function barColor(v) {
  if (v >= 4) return "var(--success)";
  if (v >= 3) return "var(--warning)";
  return "var(--danger)";
}
function starsHtml(avg) {
  if (avg === null || avg === undefined) return "—";
  const full = Math.round(avg);
  let html = "";
  for (let i = 1; i <= 5; i++) html += `<i class="bi ${i <= full ? "bi-star-fill" : "bi-star"}" style="color:var(--star-filled); font-size:0.85rem;"></i>`;
  return html;
}

async function loadDashboard() {
  try {
    const stats = await apiRequest("/dashboard/admin");

    document.getElementById("statTotalFeedback").textContent = stats.totalSubmissions;
    document.getElementById("statAvgRating").textContent = fmt(stats.systemWideAverageRating);
    document.getElementById("statTotalCourses").textContent = stats.totalCourses;
    document.getElementById("statActiveForms").textContent = stats.activeFormsCount;

    document.getElementById("courseChart").innerHTML = stats.courseWiseAverages.map(c => {
      const val = c.averageRating || 0;
      const heightPct = Math.max(4, (val / 5) * 100);
      return `
        <div class="col-3 text-center">
          <div class="small text-muted-ff mb-1">${fmt(c.averageRating)}</div>
          <div style="height:120px; display:flex; align-items:flex-end; background:var(--bg-surface-alt); border-radius:8px; overflow:hidden;">
            <div style="width:100%; height:${heightPct}%; background:${barColor(val)};"></div>
          </div>
          <div class="small text-muted-ff mt-1">${c.courseCode}</div>
        </div>`;
    }).join("") || `<div class="col-12 text-muted-ff">No courses yet.</div>`;

    document.getElementById("recentFeedbackBody").innerHTML = stats.recentFeedback.map(r => `
      <tr>
        <td>${r.studentName}</td>
        <td>${r.courseName}</td>
        <td>${starsHtml(r.averageRatingGiven)}</td>
        <td>${new Date(r.submittedAt).toLocaleDateString()}</td>
      </tr>`).join("") || `<tr><td colspan="4" class="text-muted-ff">No feedback submitted yet.</td></tr>`;

  } catch (err) {
    showToast(err.message || "Failed to load dashboard", "error");
  }
}

document.getElementById("createCourseForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  try {
    await apiRequest("/courses", {
      method: "POST",
      body: {
        courseCode: document.getElementById("cCode").value.trim(),
        courseName: document.getElementById("cName").value.trim(),
        description: document.getElementById("cDesc").value.trim(),
        semester: document.getElementById("cSemester").value.trim(),
        facultyId: parseInt(document.getElementById("cFacultyId").value, 10)
      }
    });
    showToast("Course created successfully!", "success");
    bootstrap.Modal.getInstance(document.getElementById("createCourseModal")).hide();
    e.target.reset();
    loadDashboard();
  } catch (err) {
    showToast(err.message || "Failed to create course", "error");
  }
});

loadDashboard();
