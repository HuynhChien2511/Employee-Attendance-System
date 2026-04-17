# Employee Attendance System

A full-stack **Employee Attendance Management System** built with Spring Boot and MySQL. The system supports three distinct roles � **Admin**, **Manager**, and **Employee** � each with a dedicated dashboard and feature set.

---

## Features

### Authentication & Role Management
- Admin creates and manages employee accounts
- Role-based login with separate dashboards per role
- Session-based authentication with route interceptors

### Attendance
- Employee **Check-in / Check-out** with timestamped records
- **Re-check-in requests** for missed or incorrect check-ins (requires approval)
- 30-day attendance history viewable by employee, manager, and admin

### Leave Management
- Employees submit leave requests with type and reason
- Managers and Admins review, approve, or reject requests

### Announcements
- Admin broadcasts to **all employees**, a **specific department**, or an **individual**
- Managers can announce to **their own team members only**

### Suggestion Box
- All users can submit suggestions directed to **Admin** or their **Manager**

### Bonus & Penalty
- Admin and Managers assign bonuses or penalties to employees
- Employees receive **in-app notifications** when records are created

### Monthly Summary
- Automated monthly report per employee: working days, leave days, bonuses, penalties, and **total estimated income**

### Task Management
- Managers assign tasks to employees with deadlines
- Employees submit task completions for review

### Shift Management
- Admin configures work shifts
- Employees are assigned to shifts via shift assignments

---

## Tech Stack

| Layer          | Technology                       |
| -------------- | -------------------------------- |
| Language       | Java 21                          |
| Framework      | Spring Boot 4.0.3                |
| ORM            | Spring Data JPA (Hibernate)      |
| Database       | MySQL 8+                         |
| Frontend       | HTML5, CSS3, Vanilla JavaScript  |
| Template Engine| Thymeleaf                        |
| Build Tool     | Maven                            |
| Utilities      | Lombok, Spring Boot DevTools, Spring Actuator |

---

## Project Structure

```
src/main/
+-- java/com/example/demo/
�   +-- config/          # Auth interceptor, WebMvc config, Data initializer
�   +-- controller/      # REST & MVC controllers for all roles
�   +-- entity/          # JPA entities (User, Employee, Shift, Task, etc.)
�   +-- repository/      # Spring Data JPA repositories
�   +-- service/         # Business logic layer
+-- resources/
    +-- application.properties
    +-- schema.sql
    +-- data.sql
    +-- static/
        +-- admin.html
        +-- manager.html
        +-- employee.html
        +-- login.html
        +-- css/style.css
        +-- js/app.js
```

---

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.8+
- MySQL 8+

### 1. Clone the repository

```bash
git clone https://github.com/HuynhChien2511/Employee-Attendance-System.git
cd Employee-Attendance-System
```

### 2. Configure the database

The app will auto-create the database on first run. Set your MySQL credentials via environment variables:

```bash
# Windows
set DB_USERNAME=root
set DB_PASSWORD=yourpassword

# Linux / macOS
export DB_USERNAME=root
export DB_PASSWORD=yourpassword
```

Or directly edit `src/main/resources/application.properties`:

```properties
spring.datasource.username=root
spring.datasource.password=yourpassword
```

### Important update for existing local databases

If you pulled the latest task-management changes on an existing database, run this SQL patch once:

`src/main/resources/dev-data-patch-2026-04-17.sql`

This patch adds/aligns task-related tables and manager-team mapping data required by the new Task UI and APIs.

### 3. Run the application

```bash
./mvnw spring-boot:run
```

The app starts on **http://localhost:8080**

### 4. First Login

On startup, the `DataInitializer` seeds a default **admin** account. Use those credentials to log in and begin creating employee accounts through the Admin dashboard.

---

## API Overview

| Controller             | Path Prefix       | Description                        |
| ---------------------- | ----------------- | ---------------------------------- |
| `AuthController`       | `/auth`           | Login / logout                     |
| `AdminApiController`   | `/api/admin`      | Admin management actions           |
| `ManagerApiController` | `/api/manager`    | Manager-scoped actions             |
| `EmployeeApiController`| `/api/employee`   | Employee self-service              |
| `SharedApiController`  | `/api/shared`     | Cross-role shared endpoints        |
| `AttendanceController` | `/api/attendance` | Check-in / check-out               |
| `LeaveController`      | `/api/leave`      | Leave requests                     |
| `TaskApiController`    | `/api/tasks`      | Task assignment & submission       |
| `ShiftController`      | `/api/shifts`     | Shift management                   |

---

## Environment Variables

| Variable      | Default | Description     |
| ------------- | ------- | --------------- |
| `DB_USERNAME` | `root`  | MySQL username  |
| `DB_PASSWORD` | `1234`  | MySQL password  |

---

## License

This project is for educational purposes.
