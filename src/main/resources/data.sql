USE employee_attendance;

INSERT INTO employees (id, first_name, last_name, email, employee_id, phone, department, position, hire_date, status) VALUES
(1, 'John', 'Doe', 'john.doe@company.com', 'EMP001', '555-0101', 'Engineering', 'Software Engineer', '2023-01-15', 'ACTIVE'),
(2, 'Jane', 'Smith', 'jane.smith@company.com', 'EMP002', '555-0102', 'Human Resources', 'HR Manager', '2022-06-01', 'ACTIVE'),
(3, 'Mike', 'Johnson', 'mike.johnson@company.com', 'EMP003', '555-0103', 'Sales', 'Sales Executive', '2023-03-20', 'ACTIVE'),
(4, 'Emily', 'Brown', 'emily.brown@company.com', 'EMP004', '555-0104', 'Finance', 'Accountant', '2021-11-10', 'ACTIVE'),
(5, 'David', 'Wilson', 'david.wilson@company.com', 'EMP005', '555-0105', 'Engineering', 'Backend Developer', '2024-02-12', 'ACTIVE'),
(6, 'Sophia', 'Taylor', 'sophia.taylor@company.com', 'EMP006', '555-0106', 'Marketing', 'Marketing Specialist', '2020-08-18', 'ON_LEAVE'),
(7, 'James', 'Anderson', 'james.anderson@company.com', 'EMP007', '555-0107', 'Operations', 'Operations Lead', '2019-05-05', 'ACTIVE'),
(8, 'Olivia', 'Thomas', 'olivia.thomas@company.com', 'EMP008', '555-0108', 'Engineering', 'QA Engineer', '2024-01-08', 'ACTIVE'),
(9, 'Liam', 'Martinez', 'liam.martinez@company.com', 'EMP009', '555-0109', 'Support', 'Support Agent', '2023-09-14', 'INACTIVE'),
(10, 'Ava', 'Garcia', 'ava.garcia@company.com', 'EMP010', '555-0110', 'Product', 'Product Owner', '2022-12-01', 'ACTIVE');

INSERT INTO shifts (id, shift_name, start_time, end_time, description, is_active) VALUES
(1, 'Morning Shift A', '08:00:00', '16:00:00', 'Standard morning shift', 1),
(2, 'Morning Shift B', '09:00:00', '17:00:00', 'Flex morning shift', 1),
(3, 'Evening Shift A', '16:00:00', '00:00:00', 'Standard evening shift', 1),
(4, 'Night Shift A', '00:00:00', '08:00:00', 'Standard night shift', 1),
(5, 'Support Shift 1', '07:00:00', '15:00:00', 'Support early shift', 1),
(6, 'Support Shift 2', '15:00:00', '23:00:00', 'Support late shift', 1),
(7, 'Split Shift 1', '10:00:00', '18:00:00', 'Business operations shift', 1),
(8, 'Weekend Shift A', '08:00:00', '14:00:00', 'Weekend coverage', 1),
(9, 'Weekend Shift B', '14:00:00', '20:00:00', 'Weekend coverage evening', 1),
(10, 'Legacy Shift', '06:00:00', '14:00:00', 'Deprecated shift template', 0);

INSERT INTO attendance_records (id, employee_id, check_in_time, check_out_time, hours_worked, status, notes) VALUES
(1, 1, '2026-03-10 08:05:00', '2026-03-10 16:10:00', 8.08, 'PRESENT', 'On-time'),
(2, 2, '2026-03-10 09:10:00', '2026-03-10 17:03:00', 7.88, 'LATE', 'Traffic delay'),
(3, 3, '2026-03-10 08:00:00', '2026-03-10 15:58:00', 7.97, 'PRESENT', 'Good attendance'),
(4, 4, '2026-03-10 08:15:00', '2026-03-10 12:20:00', 4.08, 'HALF_DAY', 'Medical visit'),
(5, 5, '2026-03-10 08:01:00', '2026-03-10 16:25:00', 8.40, 'PRESENT', 'Overtime support'),
(6, 6, '2026-03-10 00:00:00', NULL, NULL, 'ON_LEAVE', 'Approved leave'),
(7, 7, '2026-03-10 07:58:00', '2026-03-10 16:05:00', 8.12, 'PRESENT', 'On-time'),
(8, 8, '2026-03-10 08:50:00', '2026-03-10 17:00:00', 8.17, 'LATE', 'Late by 50 minutes'),
(9, 9, '2026-03-10 00:00:00', NULL, NULL, 'ABSENT', 'No show'),
(10, 10, '2026-03-10 08:00:00', '2026-03-10 16:00:00', 8.00, 'PRESENT', 'Perfect shift');

INSERT INTO shift_assignments (id, employee_id, shift_id, assignment_date, notes) VALUES
(1, 1, 1, '2026-03-11', 'Default assignment'),
(2, 2, 2, '2026-03-11', 'Flexible hours'),
(3, 3, 1, '2026-03-11', 'Sales morning coverage'),
(4, 4, 7, '2026-03-11', 'Finance business hours'),
(5, 5, 1, '2026-03-11', 'Engineering morning'),
(6, 6, 3, '2026-03-11', 'Marketing evening campaign'),
(7, 7, 5, '2026-03-11', 'Operations early support'),
(8, 8, 2, '2026-03-11', 'QA flex shift'),
(9, 9, 6, '2026-03-11', 'Support late shift'),
(10, 10, 7, '2026-03-11', 'Product business hours');

INSERT INTO leave_requests (id, employee_id, leave_type, start_date, end_date, reason, status, request_date, approval_date, approver_comments) VALUES
(1, 1, 'SICK_LEAVE', '2026-02-01', '2026-02-02', 'Seasonal flu', 'APPROVED', '2026-01-30 09:00:00', '2026-01-30 12:00:00', 'Take care and recover'),
(2, 2, 'VACATION', '2026-04-10', '2026-04-14', 'Family trip', 'PENDING', '2026-03-01 10:00:00', NULL, NULL),
(3, 3, 'PERSONAL', '2026-03-20', '2026-03-20', 'Personal appointment', 'APPROVED', '2026-03-05 11:30:00', '2026-03-06 08:45:00', 'Approved for one day'),
(4, 4, 'UNPAID', '2026-05-01', '2026-05-03', 'Urgent family matter', 'PENDING', '2026-03-07 13:00:00', NULL, NULL),
(5, 5, 'PATERNITY', '2026-06-01', '2026-06-10', 'Newborn care', 'APPROVED', '2026-02-20 09:30:00', '2026-02-21 10:00:00', 'Congratulations'),
(6, 6, 'VACATION', '2026-03-12', '2026-03-18', 'Annual leave', 'APPROVED', '2026-02-15 14:20:00', '2026-02-16 09:10:00', 'Approved in advance'),
(7, 7, 'SICK_LEAVE', '2026-03-09', '2026-03-09', 'Fever', 'REJECTED', '2026-03-08 18:00:00', '2026-03-08 19:00:00', 'Submit doctor note'),
(8, 8, 'PERSONAL', '2026-03-25', '2026-03-26', 'Home relocation', 'PENDING', '2026-03-10 16:45:00', NULL, NULL),
(9, 9, 'VACATION', '2026-07-01', '2026-07-05', 'Travel abroad', 'CANCELLED', '2026-02-25 08:00:00', '2026-02-26 08:10:00', 'Cancelled by employee'),
(10, 10, 'MATERNITY', '2026-08-01', '2026-10-30', 'Maternity period', 'APPROVED', '2026-03-01 09:00:00', '2026-03-02 09:45:00', 'Approved by HR');

INSERT INTO users (id, username, password, role, employee_id, is_active) VALUES
(1, 'admin', 'Admin@123', 'ADMIN', NULL, 1),
(2, 'manager.hr', 'Manager@123', 'MANAGER', 2, 1),
(3, 'john.doe', 'Employee@123', 'EMPLOYEE', 1, 1),
(4, 'mike.johnson', 'Employee@123', 'EMPLOYEE', 3, 1),
(5, 'emily.brown', 'Employee@123', 'EMPLOYEE', 4, 1),
(6, 'david.wilson', 'Employee@123', 'EMPLOYEE', 5, 1),
(7, 'sophia.taylor', 'Employee@123', 'EMPLOYEE', 6, 1),
(8, 'james.anderson', 'Manager@123', 'MANAGER', 7, 1),
(9, 'olivia.thomas', 'Employee@123', 'EMPLOYEE', 8, 1),
(10, 'ava.garcia', 'Employee@123', 'EMPLOYEE', 10, 1);
