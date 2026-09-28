const user = Auth.requireRole("STAFF");
if (user) document.getElementById("navUserName").textContent = user.fullName;
document.getElementById("logoutLink").addEventListener("click", (e) => { e.preventDefault(); Auth.logout(); });

function fmt(n) { return (n === null || n === undefined) ? "—" : Number(n).toFixed(1); }

function barColor(v) {
  if (v >= 4) return "var(--success)";
  if (v >= 3) return "var(--warning)";
  return "var(--danger)";
}

async function loadPerformance() {
  try {
    const perf = await apiRequest("/staff/performance");

    document.getElementById("statCourses").textContent = perf.courseBreakdown.length;
    document.getElementById("statAvg").textContent = fmt(perf.overallAverageRating);
    document.getElementById("statSubmissions").textContent =
      perf.courseBreakdown.reduce((sum, c) => sum + c.totalSubmissions, 0);

    document.getElementById("courseChart").innerHTML = perf.courseBreakdown.map(c => {
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
    }).join("");

    document.getElementById("myCoursesBody").innerHTML = perf.courseBreakdown.map(c => `
      <tr>
        <td>${c.courseName}</td>
        <td>${c.courseCode}</td>
        <td>${fmt(c.averageRating)} / 5</td>
        <td>${c.totalSubmissions}</td>
        <td><button class="btn btn-sm btn-ff-outline" onclick="viewCourseSummary(${c.courseId}, '${c.courseName.replace(/'/g, "")}')">View Details</button></td>
      </tr>`).join("") || `<tr><td colspan="5" class="text-muted-ff">No courses assigned yet.</td></tr>`;

  } catch (err) {
    showToast(err.message || "Failed to load performance data", "error");
  }
}

async function viewCourseSummary(courseId, courseName) {
  try {
    const forms = await apiRequest(`/forms/course/${courseId}`);
    if (!forms.length) { showToast(`No feedback forms found for ${courseName}`, "error"); return; }
    const form = forms[0]; // most recent/only form for this demo
    const summary = await apiRequest(`/staff/forms/${form.id}/summary`);

    document.getElementById("qSummaryFormTitle").textContent = `${summary.courseName} (${summary.courseCode})`;
    document.getElementById("questionSummaryBody").innerHTML = `
      <p class="text-muted-ff small mb-3">Overall average: <strong>${fmt(summary.overallAverageRating)}/5</strong> across ${summary.totalSubmissions} response(s)</p>
      ${summary.questionAverages.map(q => `
        <div class="mb-2">
          <div class="d-flex justify-content-between small mb-1">
            <span>${q.questionText}</span><span class="fw-semibold">${fmt(q.averageRating)}/5</span>
          </div>
          <div style="height:8px; background:var(--bg-surface-alt); border-radius:6px; overflow:hidden;">
            <div style="height:100%; width:${(q.averageRating/5)*100}%; background:${barColor(q.averageRating)};"></div>
          </div>
        </div>`).join("")}
    `;
    document.getElementById("questionSummaryCard").style.display = "block";
    document.getElementById("questionSummaryCard").scrollIntoView({ behavior: "smooth" });
  } catch (err) {
    showToast(err.message || "Failed to load question summary", "error");
  }
}

loadPerformance();
