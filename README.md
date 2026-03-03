# Employee Attendance System

A comprehensive web-based Employee Attendance System built with Spring Boot and modern web technologies.

## Features

### 1. Employee Information Management
- Add, view, and delete employees
- Track employee details (ID, name, email, department, position)
- Monitor employee status (Active, Inactive, On Leave)

### 2. Attendance Records
- Employee time clock with check-in/check-out
- Automatic hours calculation
- Attendance status tracking (Present, Absent, Late, Half Day, On Leave)
- Real-time attendance monitoring

### 3. Shift Scheduling
- Create and manage work shifts
- Assign shifts to employees
- Track shift timings and descriptions
- Active/Inactive shift management

### 4. Leave Approval Workflow
- Submit leave requests with multiple types:
  - Sick Leave
  - Vacation
  - Personal
  - Maternity/Paternity
  - Unpaid Leave
- Approve/Reject leave requests
- Track leave status and approval history

### 5. Dashboard & Analytics
- Real-time statistics
- Total employees count
- Present employees today
- Pending leave requests
- Active shifts overview

### 6. User-Friendly Interface
- Modern, responsive design
- Intuitive navigation
- Color-coded status indicators
- Real-time data updates

## Technology Stack

### Backend
- **Spring Boot 4.0.3** - Application framework
- **Java 21 LTS** - Runtime environment
- **Spring Data JPA** - Database access
- **H2 Database** - In-memory database
- **Lombok** - Reduces boilerplate code
- **Maven** - Build tool

### Frontend
- **HTML5** - Structure
- **CSS3** - Styling with gradients and animations
- **JavaScript (Vanilla)** - Interactive functionality
- **RESTful API** - Communication with backend

## Getting Started

### Prerequisites
- Java 21 LTS or higher
- Maven 3.6+ (Maven Wrapper included)

### Installation & Running

1. **Clone or navigate to the project directory**
   ```bash
   cd d:\VS-code save\EAS\demo
   ```

2. **Build the project**
   ```bash
   mvnw clean install
   ```

3. **Run the application**
   ```bash
   mvnw spring-boot:run
   ```

4. **Access the application**
   - Web Interface: http://localhost:8080
   - H2 Database Console: http://localhost:8080/h2-console
     - JDBC URL: `jdbc:h2:mem:attendance_db`
     - Username: `sa`
     - Password: (leave empty)

## Sample Data

The application comes with pre-loaded sample data:
- 3 Employees (John Doe, Jane Smith, Mike Johnson)
- 3 Shifts (Morning, Evening, Night)
- Sample attendance records
- Sample leave requests

## API Endpoints

### Employee Management
- `GET /api/employees` - Get all employees
- `GET /api/employees/{id}` - Get employee by ID
- `POST /api/employees` - Create new employee
- `PUT /api/employees/{id}` - Update employee
- `DELETE /api/employees/{id}` - Delete employee

### Attendance Management
- `GET /api/attendance` - Get all attendance records
- `GET /api/attendance/employee/{employeeId}` - Get attendance by employee
- `POST /api/attendance/checkin/{employeeId}` - Check in employee
- `PUT /api/attendance/checkout/{recordId}` - Check out employee

### Leave Management
- `GET /api/leave` - Get all leave requests
- `GET /api/leave/pending` - Get pending leave requests
- `GET /api/leave/employee/{employeeId}` - Get employee leave requests
- `POST /api/leave` - Create leave request
- `PUT /api/leave/approve/{requestId}` - Approve leave
- `PUT /api/leave/reject/{requestId}` - Reject leave

### Shift Management
- `GET /api/shifts` - Get all shifts
- `GET /api/shifts/active` - Get active shifts
- `POST /api/shifts` - Create new shift
- `GET /api/shifts/assignments/employee/{employeeId}` - Get employee shift assignments
- `POST /api/shifts/assignments` - Assign shift to employee

## Database Schema

### Tables
1. **employees** - Employee information
2. **attendance_records** - Daily attendance tracking
3. **shifts** - Shift definitions
4. **shift_assignments** - Employee-shift mappings
5. **leave_requests** - Leave request management
6. **users** - System users and roles

## Project Structure

```
src/
├── main/
│   ├── java/com/example/demo/
│   │   ├── config/
│   │   │   └── DataInitializer.java
│   │   ├── controller/
│   │   │   ├── AttendanceController.java
│   │   │   ├── EmployeeController.java
│   │   │   ├── LeaveController.java
│   │   │   ├── ShiftController.java
│   │   │   └── WebController.java
│   │   ├── entity/
│   │   │   ├── AttendanceRecord.java
│   │   │   ├── Employee.java
│   │   │   ├── LeaveRequest.java
│   │   │   ├── Shift.java
│   │   │   ├── ShiftAssignment.java
│   │   │   └── User.java
│   │   ├── repository/
│   │   │   ├── AttendanceRecordRepository.java
│   │   │   ├── EmployeeRepository.java
│   │   │   ├── LeaveRequestRepository.java
│   │   │   ├── ShiftAssignmentRepository.java
│   │   │   ├── ShiftRepository.java
│   │   │   └── UserRepository.java
│   │   ├── service/
│   │   │   ├── AttendanceService.java
│   │   │   ├── EmployeeService.java
│   │   │   ├── LeaveService.java
│   │   │   └── ShiftService.java
│   │   └── DemoApplication.java
│   └── resources/
│       ├── static/
│       │   ├── css/
│       │   │   └── style.css
│       │   ├── js/
│       │   │   └── app.js
│       │   └── index.html
│       └── application.properties
└── test/
    └── java/com/example/demo/
        └── DemoApplicationTests.java
```

## Features in Detail

### Dashboard
The dashboard provides a quick overview of:
- Total number of employees in the system
- Employees present today
- Pending leave requests requiring approval
- Number of active shifts

### Employee Management
- Complete CRUD operations for employee records
- Track employee status and employment details
- Department and position management
- Contact information storage

### Attendance Tracking
- Simple check-in/check-out interface
- Automatic calculation of work hours
- Historical attendance records
- Employee-specific attendance views

### Shift Management
- Define custom shifts with start and end times
- Assign employees to specific shifts
- View shift schedules by date
- Manage shift availability

### Leave Management
- Employee self-service leave requests
- Multiple leave types supported
- Manager approval workflow
- Leave history tracking

## Future Enhancements

Potential features for future versions:
- User authentication and authorization
- Email notifications for leave approvals
- Advanced reporting and analytics
- Export data to Excel/PDF
- Mobile application
- Biometric integration for check-in/out
- Overtime tracking
- Holiday calendar management
- Performance reviews integration

## Upgrade History

### Java 17 → 21 LTS (March 2026)
- **Session ID**: 20260303052420
- **Status**: ✅ Complete
- **Changes**: Updated project to use Java 21 LTS
- **Test Results**: 100% pass rate maintained
- **CVE Scan**: No vulnerabilities detected
- **Documentation**: [View detailed upgrade report](.github/java-upgrade/20260303052420/summary.md)

For detailed upgrade information, see the upgrade documentation in `.github/java-upgrade/20260303052420/`.

## License

This project is created as a demonstration application.

## Support

For issues or questions, please contact your system administrator.

---

**Developed with Spring Boot & Modern Web Technologies**
