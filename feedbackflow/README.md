# FeedbackFlow — Course Feedback Collection System

A complete end-to-end web application for digitizing academic course feedback,
built with **Java 17 + Spring Boot 3** on the backend and **Bootstrap 5 +
vanilla JavaScript** on the frontend, with full **light/dark theme support**.

---

## 1. Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+ (or use the wrapper if you add one)
- Nothing else — the app ships with an in-memory **H2** database and seeds
  demo data automatically on first run.

### Run it
```bash
cd feedbackflow
mvn spring-boot:run
```

The app starts on **http://localhost:8080**. Open that URL in your browser —
you'll land on the login page.

### Demo accounts (auto-seeded by `DataSeeder`)
| Role    | Email                          | Password     |
|---------|---------------------------------|--------------|
| Admin   | admin@feedbackflow.edu          | Admin@123    |
| Staff   | rahul.sharma@feedbackflow.edu   | Staff@123    |
| Staff   | priya.nair@feedbackflow.edu     | Staff@123    |
| Student | nishanthi.s@feedbackflow.edu    | Student@123  |
| Student | arun.k@feedbackflow.edu         | Student@123  |

Two courses (**Web Development / CS101**, **Database Systems / CS102**) are
seeded, each with an active, open feedback form, so you can log in as the
student and submit feedback immediately.

### H2 Console (optional, for inspecting data)
Visit `http://localhost:8080/h2-console` with:
- JDBC URL: `jdbc:h2:mem:feedbackflow`
- User: `sa`, Password: *(blank)*

---

## 2. Switching to MySQL / PostgreSQL

Edit `src/main/resources/application.yml`: comment out the H2 `datasource`
block and uncomment/adjust the MySQL block (a PostgreSQL driver can be
substituted the same way — just swap the Maven dependency and JDBC URL).
`spring.jpa.hibernate.ddl-auto: update` will create the schema for you on
first boot.

---

## 3. Architecture Overview

```
com.feedbackflow
├── entity/       User, Course, FeedbackForm, Question, FeedbackResponse, QuestionAnswer
├── repository/   Spring Data JPA + custom JPQL average-rating queries
├── dto/          Request/response payloads (never expose entities directly)
├── service/      Business logic — THIS is where Rule 1 & Rule 2 are enforced
├── controller/   REST endpoints, role-gated with @PreAuthorize
├── security/     JWT generation/validation, Spring Security UserDetails glue
├── config/       SecurityConfig (route protection) + DataSeeder (demo data)
└── exception/    Custom runtime exceptions + a single @RestControllerAdvice
```

### Strict Business Rules (Section 3 of the spec)
Both rules are enforced **exclusively in `FeedbackResponseService`**, before
any database write — never trusted to the frontend:

- **Rule 1 — One response per student per course per semester.**
  Checked via `FeedbackResponseRepository.existsByFeedbackForm_IdAndStudent_Id(...)`.
  Violation → `DuplicateSubmissionException` → **HTTP 409 Conflict**.
  A DB-level `UNIQUE(form_id, student_id)` constraint on `feedback_responses`
  is a second line of defense against race conditions.

- **Rule 2 — No submissions once a form is closed / inactive / not yet open.**
  Checked against `form.isActive()`, `form.getOpenDate()`, `form.getCloseDate()`.
  Violation → `FormClosedException` → **HTTP 403 Forbidden**.

Both exceptions are translated to the correct HTTP status in
`GlobalExceptionHandler` — controllers never handle these directly.

---

## 4. REST API Reference

All endpoints are prefixed with `/api`. Authenticated endpoints require
`Authorization: Bearer <JWT>`.

### Auth
| Method | Path                | Access | Description |
|--------|----------------------|--------|-------------|
| POST   | `/auth/register`    | Public | Create a new user |
| POST   | `/auth/login`       | Public | Returns `{ token, userId, fullName, email, role }` |

### Courses
| Method | Path                                   | Access        | Description |
|--------|-----------------------------------------|---------------|-------------|
| GET    | `/courses/my`                          | Any role      | Enrolled (student) / taught (staff) / all (admin) |
| GET    | `/courses`                             | Admin         | All courses |
| POST   | `/courses`                             | Admin         | Create a course |
| POST   | `/courses/{courseId}/enroll/{studentId}` | Admin       | Enroll a student |

### Feedback Forms
| Method | Path                            | Access | Description |
|--------|----------------------------------|--------|-------------|
| POST   | `/forms`                        | Admin  | Create a form + its questions |
| PUT    | `/forms/{id}/close`             | Admin  | Manually close a form (Rule 2) |
| PUT    | `/forms/{id}/reopen`            | Admin  | Reopen a closed form |
| PUT    | `/forms/{id}/deadline`          | Admin  | Update the closing deadline |
| GET    | `/forms/{id}`                   | Any    | Form + questions + `alreadySubmittedByCurrentUser` flag |
| GET    | `/forms/course/{courseId}`      | Any    | All forms for a course |

### Responses (Submissions)
| Method | Path              | Access  | Description |
|--------|--------------------|---------|-------------|
| POST   | `/responses`      | Student | Submit feedback — enforces Rule 1 (409) & Rule 2 (403) |
| GET    | `/responses/my`   | Student | The caller's own submission history |

### Dashboards & Summaries
| Method | Path                                | Access       | Description |
|--------|--------------------------------------|--------------|-------------|
| GET    | `/dashboard/admin`                  | Admin        | Total courses, active forms, total submissions, course-wise averages, recent feedback |
| GET    | `/staff/forms/{formId}/summary`     | Staff/Admin  | Question-wise average ratings for one form |
| GET    | `/staff/performance`                | Staff/Admin  | Caller's own aggregated performance across all their courses |
| GET    | `/staff/{facultyId}/performance`    | Admin        | Any faculty member's aggregated performance |

---

## 5. Frontend

Plain HTML/CSS/JS served as static resources from `src/main/resources/static`
(no build step required):

- `index.html` — login
- `student-dashboard.html`, `submit-feedback.html` — student flow, with a
  5-star rating widget per question
- `staff-dashboard.html` — question-wise + course-wise performance
- `admin-dashboard.html` — system stats, course-wise chart, recent feedback,
  and a "New Course" modal

**Theme switcher**: the toggle in the top-right corner of every page flips a
`data-theme="dark"` attribute on `<html>` and persists the choice to
`localStorage` (`css/theme.css` defines every color as a CSS variable, so no
JS re-styling is needed — only the attribute changes).

All frontend calls go through `js/api.js`'s `apiRequest()` helper, which
attaches the JWT and surfaces the backend's error `message` field (including
the exact 409/403 business-rule messages) via toast notifications.

---

## 6. Notes for Extending

- Add pagination to `/dashboard/admin` recent feedback and `/staff/*` lists
  once data volume grows (currently `.limit(10)` in `DashboardService`).
- `RegisterRequest` is exposed via `/api/auth/register` for demo convenience;
  in production, restrict who can self-register as STAFF/ADMIN.
- Swap `app.jwt.secret` in `application.yml` for an environment variable
  before deploying anywhere real.
