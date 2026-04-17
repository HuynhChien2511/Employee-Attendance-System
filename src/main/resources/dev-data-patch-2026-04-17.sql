USE employee_attendance;

CREATE TABLE IF NOT EXISTS tasks (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    deadline DATE,
    status VARCHAR(30) NOT NULL,
    created_by_user_id BIGINT,
    created_at DATETIME(6),
    attached_file_name VARCHAR(255),
    attached_file_path VARCHAR(255),
    PRIMARY KEY (id),
    KEY idx_tasks_created_by (created_by_user_id),
    CONSTRAINT fk_tasks_created_by FOREIGN KEY (created_by_user_id) REFERENCES users (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS task_members (
    id BIGINT NOT NULL AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_task_members_task (task_id),
    KEY idx_task_members_employee (employee_id),
    CONSTRAINT fk_task_members_task FOREIGN KEY (task_id) REFERENCES tasks (id),
    CONSTRAINT fk_task_members_employee FOREIGN KEY (employee_id) REFERENCES employees (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS task_submissions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    text_content TEXT,
    file_name VARCHAR(255),
    file_path VARCHAR(255),
    submitted_at DATETIME(6),
    PRIMARY KEY (id),
    KEY idx_task_submissions_task (task_id),
    KEY idx_task_submissions_employee (employee_id),
    CONSTRAINT fk_task_submissions_task FOREIGN KEY (task_id) REFERENCES tasks (id),
    CONSTRAINT fk_task_submissions_employee FOREIGN KEY (employee_id) REFERENCES employees (id)
) ENGINE=InnoDB;

-- Manager user IDs in current seed:
-- 2 = manager.hr, 8 = james.anderson
UPDATE employees SET manager_user_id = 2 WHERE id IN (1, 4, 5, 8);
UPDATE employees SET manager_user_id = 8 WHERE id IN (3, 6, 9, 10);
UPDATE employees SET manager_user_id = NULL WHERE id IN (2, 7);

INSERT INTO tasks (id, title, description, deadline, status, created_by_user_id, created_at, attached_file_name, attached_file_path)
SELECT 1, 'Prepare Weekly Attendance Report', 'Compile attendance anomalies and submit summary to HR.', '2026-04-20', 'IN_PROCESS', 2, '2026-04-15 09:00:00', NULL, NULL
WHERE NOT EXISTS (SELECT 1 FROM tasks WHERE id = 1);

INSERT INTO tasks (id, title, description, deadline, status, created_by_user_id, created_at, attached_file_name, attached_file_path)
SELECT 2, 'Update Team Handover Notes', 'Finalize Q2 handover notes and upload to shared drive.', '2026-04-12', 'FINISHED', 8, '2026-04-10 10:30:00', NULL, NULL
WHERE NOT EXISTS (SELECT 1 FROM tasks WHERE id = 2);

INSERT INTO task_members (task_id, employee_id)
SELECT 1, 1 WHERE NOT EXISTS (SELECT 1 FROM task_members WHERE task_id = 1 AND employee_id = 1);
INSERT INTO task_members (task_id, employee_id)
SELECT 1, 5 WHERE NOT EXISTS (SELECT 1 FROM task_members WHERE task_id = 1 AND employee_id = 5);
INSERT INTO task_members (task_id, employee_id)
SELECT 1, 8 WHERE NOT EXISTS (SELECT 1 FROM task_members WHERE task_id = 1 AND employee_id = 8);
INSERT INTO task_members (task_id, employee_id)
SELECT 2, 3 WHERE NOT EXISTS (SELECT 1 FROM task_members WHERE task_id = 2 AND employee_id = 3);
INSERT INTO task_members (task_id, employee_id)
SELECT 2, 6 WHERE NOT EXISTS (SELECT 1 FROM task_members WHERE task_id = 2 AND employee_id = 6);
INSERT INTO task_members (task_id, employee_id)
SELECT 2, 10 WHERE NOT EXISTS (SELECT 1 FROM task_members WHERE task_id = 2 AND employee_id = 10);

INSERT INTO task_submissions (task_id, employee_id, text_content, file_name, file_path, submitted_at)
SELECT 2, 3, 'Completed handover draft and shared with manager.', NULL, NULL, '2026-04-11 16:45:00'
WHERE NOT EXISTS (SELECT 1 FROM task_submissions WHERE task_id = 2 AND employee_id = 3);
