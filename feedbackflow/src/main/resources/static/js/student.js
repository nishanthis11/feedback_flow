const user = Auth.requireRole("STUDENT");

if (user) {
  document.getElementById("navUserName").textContent = user.fullName;
  document.getElementById("heroName").textContent = user.fullName;
}

document.getElementById("logoutLink").addEventListener("click", (e) => { e.preventDefault(); Auth.logout(); });
document.getElementById("myFeedbackLink").addEventListener("click", (e) => {
  e.preventDefault();
  showToast("Tip: submitted forms show 'Feedback Submitted' on their course card below.", "success");
});

const ICONS = {
  "Web Development": "bi-code-slash",
  "Database Systems": "bi-hdd-stack-fill",
  "Data Structures & Algorithms": "bi-diagram-3-fill",
  "Computer Networks": "bi-wifi"
};

function iconFor(courseName) {
  return ICONS[courseName] || "bi-book-half";
}

async function loadCourses() {
  const grid = document.getElementById("coursesGrid");
  try {
    const courses = await apiRequest("/courses/my");

    if (!courses.length) {
      grid.innerHTML = `<div class="col-12 text-muted-ff">You are not enrolled in any courses yet.</div>`;
      return;
    }

    grid.innerHTML = courses.map(c => {
      const hasActiveForm = !!c.activeFormId;
      const btn = hasActiveForm
        ? `<a href="submit-feedback.html?formId=${c.activeFormId}" class="btn btn-ff-primary btn-sm w-100">Give Feedback <i class="bi bi-chevron-right"></i></a>`
        : `<button class="btn btn-ff-outline btn-sm w-100" disabled>No Active Form</button>`;

      return `
        <div class="col-md-6 col-lg-3">
          <div class="ff-course-card d-flex flex-column">
            <div class="ff-icon-badge"><i class="bi ${iconFor(c.courseName)}"></i></div>
            <div class="fw-semibold">${c.courseName}</div>
            <div class="text-muted-ff small mb-2">${c.courseCode}</div>
            <div class="text-muted-ff small mb-3">Faculty: ${c.facultyName}</div>
            <div class="mt-auto">${btn}</div>
          </div>
        </div>`;
    }).join("");
  } catch (err) {
    grid.innerHTML = `<div class="col-12 text-danger">Failed to load courses: ${err.message}</div>`;
  }
}

loadCourses();
