CREATE DATABASE IF NOT EXISTS employee_attendance;
USE employee_attendance;

DROP TABLE IF EXISTS bonus_penalty;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS leave_requests;
DROP TABLE IF EXISTS shift_assignments;
DROP TABLE IF EXISTS attendance_records;
DROP TABLE IF EXISTS shifts;
DROP TABLE IF EXISTS employees;

CREATE TABLE employees (
    id BIGINT NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    employee_id VARCHAR(50) NOT NULL,
    phone VARCHAR(30),
    department VARCHAR(100),
    position VARCHAR(100),
    hire_date DATE,
    status VARCHAR(30),
    base_salary DECIMAL(15,2) NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_employees_email (email),
    UNIQUE KEY uk_employees_employee_id (employee_id)
) ENGINE=InnoDB;

CREATE TABLE shifts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    shift_name VARCHAR(100) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    description VARCHAR(255),
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE attendance_records (
    id BIGINT NOT NULL AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    check_in_time DATETIME(6) NOT NULL,
    check_out_time DATETIME(6),
    hours_worked DOUBLE,    
    status VARCHAR(30),
    notes VARCHAR(255),
    PRIMARY KEY (id),
    KEY idx_attendance_employee (employee_id),
    CONSTRAINT fk_attendance_employee FOREIGN KEY (employee_id) REFERENCES employees (id)
) ENGINE=InnoDB;

CREATE TABLE shift_assignments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    shift_id BIGINT NOT NULL,
    assignment_date DATE NOT NULL,
    notes VARCHAR(255),
    PRIMARY KEY (id),
    KEY idx_shift_assignments_employee (employee_id),
    KEY idx_shift_assignments_shift (shift_id),
    CONSTRAINT fk_shift_assignments_employee FOREIGN KEY (employee_id) REFERENCES employees (id),
    CONSTRAINT fk_shift_assignments_shift FOREIGN KEY (shift_id) REFERENCES shifts (id)
) ENGINE=InnoDB;

CREATE TABLE leave_requests (
    id BIGINT NOT NULL AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    leave_type VARCHAR(30) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason VARCHAR(255),
    status VARCHAR(30),
    request_date DATETIME(6),
    approval_date DATETIME(6),
    approver_comments VARCHAR(255),
    PRIMARY KEY (id),
    KEY idx_leave_requests_employee (employee_id),
    CONSTRAINT fk_leave_requests_employee FOREIGN KEY (employee_id) REFERENCES employees (id)
) ENGINE=InnoDB;

CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    employee_id BIGINT,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_username (username),
    UNIQUE KEY uk_users_employee_id (employee_id),
    CONSTRAINT chk_users_password_len CHECK (CHAR_LENGTH(password) >= 8),
    CONSTRAINT fk_users_employee FOREIGN KEY (employee_id) REFERENCES employees (id)
) ENGINE=InnoDB;

CREATE TABLE bonus_penalty (
    id BIGINT NOT NULL AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    type VARCHAR(30) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    reason VARCHAR(255),
    effective_date DATE,
    month INT,
    year INT,
    created_by BIGINT,
    created_at DATETIME(6),
    PRIMARY KEY (id),
    KEY idx_bonus_penalty_employee (employee_id),
    KEY idx_bonus_penalty_created_by (created_by),
    CONSTRAINT fk_bonus_penalty_employee FOREIGN KEY (employee_id) REFERENCES employees (id),
    CONSTRAINT fk_bonus_penalty_created_by FOREIGN KEY (created_by) REFERENCES users (id)
) ENGINE=InnoDB;
