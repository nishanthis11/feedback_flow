const user = Auth.requireRole("STUDENT");
if (user) document.getElementById("navUserName").textContent = user.fullName;
document.getElementById("logoutLink").addEventListener("click", (e) => { e.preventDefault(); Auth.logout(); });

const params = new URLSearchParams(window.location.search);
const formId = params.get("formId");

const ratings = {}; // questionId -> 1..5

function starRatingHtml(questionId) {
  let stars = "";
  for (let i = 1; i <= 5; i++) {
    stars += `<i class="bi bi-star star" data-q="${questionId}" data-val="${i}"></i>`;
  }
  return `<div class="star-rating" id="stars-${questionId}">${stars}</div> <span class="text-muted-ff small ms-2" id="ratingText-${questionId}">Not rated</span>`;
}

function renderStars(questionId, value) {
  const container = document.getElementById(`stars-${questionId}`);
  container.querySelectorAll(".star").forEach(star => {
    const val = parseInt(star.dataset.val, 10);
    star.className = "bi " + (val <= value ? "bi-star-fill star filled" : "bi-star star");
  });
  document.getElementById(`ratingText-${questionId}`).textContent = value ? `${value}/5` : "Not rated";
}

function attachStarHandlers() {
  document.querySelectorAll(".star").forEach(star => {
    star.addEventListener("click", () => {
      const qid = star.dataset.q;
      const val = parseInt(star.dataset.val, 10);
      ratings[qid] = val;
      renderStars(qid, val);
    });
  });
}

function disableForm(message, type = "error") {
  document.getElementById("formStatusBanner").innerHTML =
    `<div class="alert ${type === "error" ? "alert-danger" : "alert-warning"} py-2 px-3 small">${message}</div>`;
  document.getElementById("feedbackForm").querySelectorAll("input, textarea, button").forEach(el => el.disabled = true);
}

async function loadForm() {
  if (!formId) {
    document.getElementById("courseTitle").textContent = "No feedback form selected.";
    disableForm("Missing form reference. Go back to your dashboard and click 'Give Feedback' on a course.");
    return;
  }

  try {
    const form = await apiRequest(`/forms/${formId}`);

    document.getElementById("courseTitle").textContent = `${form.courseName} (${form.courseCode})`;
    document.getElementById("facultyLine").textContent = form.title;

    if (form.alreadySubmittedByCurrentUser) {
      disableForm("You have already submitted feedback for this course this semester.", "warning");
    } else if (!form.currentlyOpen) {
      disableForm("This feedback form is currently closed and is not accepting new responses.", "warning");
    }

    const container = document.getElementById("questionsContainer");
    container.innerHTML = form.questions.map(q => {
      if (q.type === "RATING") {
        return `
          <div class="mb-3 d-flex align-items-center justify-content-between flex-wrap gap-2">
            <label class="fw-medium mb-0">${q.text}</label>
            ${starRatingHtml(q.id)}
          </div>`;
      }
      return `
        <div class="mb-3">
          <label class="fw-medium mb-2">${q.text}</label>
          <textarea class="form-control" rows="3" data-question-id="${q.id}" data-type="TEXT" placeholder="Share your thoughts..."></textarea>
        </div>`;
    }).join("<hr style='border-color:var(--border-color)'>");

    attachStarHandlers();
  } catch (err) {
    document.getElementById("courseTitle").textContent = "Could not load feedback form.";
    disableForm(err.message);
  }
}

document.getElementById("feedbackForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const btn = document.getElementById("submitBtn");

  const answers = [];
  document.querySelectorAll("#questionsContainer [data-question-id]").forEach(el => {
    answers.push({ questionId: parseInt(el.dataset.questionId, 10), textValue: el.value || null });
  });
  Object.keys(ratings).forEach(qid => {
    answers.push({ questionId: parseInt(qid, 10), ratingValue: ratings[qid] });
  });

  if (!answers.length) { showToast("Please answer at least one question.", "error"); return; }

  btn.disabled = true;
  btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span> Submitting...';

  try {
    await apiRequest("/responses", { method: "POST", body: { formId: parseInt(formId, 10), answers } });
    showToast("Thank you! Your feedback has been submitted successfully.", "success");
    setTimeout(() => window.location.href = "student-dashboard.html", 1200);
  } catch (err) {
    // Rule 1 -> 409 Conflict, Rule 2 -> 403 Forbidden. Both come through with a clear message.
    if (err.status === 409) {
      disableForm(err.message, "warning");
    } else if (err.status === 403) {
      disableForm(err.message, "warning");
    } else {
      showToast(err.message || "Submission failed", "error");
    }
    btn.disabled = false;
    btn.innerHTML = '<i class="bi bi-send-fill me-1"></i> Submit Feedback';
  }
});

loadForm();
