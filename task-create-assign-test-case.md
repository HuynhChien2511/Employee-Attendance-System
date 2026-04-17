# Test Case Document - Task Create and Assign

## Test Case Header

| Field | Value |
|---|---|
| Test Case # | TC-TASK-001 |
| Test Case Name | Create Task and Assign to Employees |
| System | Employee Attendance System |
| Subsystem | Task Management |
| Designed by | GitHub Copilot |
| Design Date | 2026-04-17 |
| Executed by | Not executed |
| Execution Date | Not executed |
| Short Description | Verify that an admin or manager can create a task, assign it to one or more employees, and that the assigned employees can see the task in their task list. |

## Pre-conditions

- User is logged in as an `ADMIN` or `MANAGER`.
- At least one active employee exists in the system.
- Task management page/API is available.
- Upload directory is writable if a file attachment is included.

## Test Steps

| Step | Action | Expected System Response | Pass/Fail | Comments |
|---|---|---|---|---|
| 1 | Open the task management screen or call `POST /api/tasks`. | The task creation form or API endpoint is available to an admin/manager user. |  |  |
| 2 | Enter a task title, description, and deadline. | The system accepts the input values for the new task. |  |  |
| 3 | Select one or more employee IDs to assign the task to. | The system records the selected employees as task members. |  |  |
| 4 | Optionally attach a file and submit the task. | The system creates the task with status `IN_PROCESS` and saves the optional attachment. |  |  |
| 5 | Open the task list or detail view after submission. | The new task appears in the list with the assigned members shown in the task detail. |  |  |
| 6 | Log in as one of the assigned employees and open the My Tasks page. | The assigned task appears in the employee task list and can be opened for details. |  |  |

## Post-Conditions

- A new task record exists in the database.
- The selected employees are linked to the task through task member records.
- The task status is `IN_PROCESS`.
- Assigned employees can view the task in their own task list.
- If an attachment was uploaded, the file is stored under the task upload directory.
