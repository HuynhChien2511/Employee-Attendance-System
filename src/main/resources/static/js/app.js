/*
 * FILE: app.js
 * PURPOSE: Legacy standalone frontend script for the older multi-page admin demo UI.
 *          Handles navigation, dashboard loading, employee CRUD, attendance actions,
 *          shift management, and leave workflows against the /api endpoints.
 *
 * KEY FUNCTIONS:
 *  - showPage(), loadDashboard(), loadEmployees(), loadAttendance(), loadShifts(),
 *    loadLeaveRequests(): switch pages and fetch data into the DOM.
 *  - showEmployeeForm()/hideEmployeeForm(), showShiftForm()/hideShiftForm(),
 *    showLeaveForm()/hideLeaveForm(): toggle inline forms.
 *  - checkIn(), checkOut(), approveLeave(), rejectLeave(), deleteEmployee(): write actions.
 *  - formatDateTime(), isToday(): formatting helpers.
 *
 * HOW TO MODIFY:
 *  - This script targets the older /api/employees, /api/attendance, /api/shifts,
 *    and /api/leave endpoints. Keep it in sync if those legacy controllers change.
 *  - If this file becomes unused, remove its script tag from any pages before deleting it.
 */
const API_BASE_URL = 'http://localhost:8080/api';

// Navigation
document.querySelectorAll('.nav-btn').forEach(btn => {
    btn.addEventListener('click', () => {
        const page = btn.dataset.page;
        showPage(page);
    });
});

function showPage(pageName) {
    document.querySelectorAll('.page').forEach(page => page.classList.remove('active'));
    document.querySelectorAll('.nav-btn').forEach(btn => btn.classList.remove('active'));
    
    document.getElementById(pageName).classList.add('active');
    document.querySelector(`[data-page="${pageName}"]`).classList.add('active');
    
    // Load data for the page
    switch(pageName) {
        case 'dashboard':
            loadDashboard();
            break;
        case 'employees':
            loadEmployees();
            break;
        case 'attendance':
            loadAttendance();
            loadEmployeeDropdown();
            break;
        case 'shifts':
            loadShifts();
            break;
        case 'leave':
            loadLeaveRequests();
            loadLeaveEmployeeDropdown();
            break;
    }
}

// Dashboard
async function loadDashboard() {
    try {
        const [employees, attendance, leaves, shifts] = await Promise.all([
            fetch(`${API_BASE_URL}/employees`).then(r => r.json()),
            fetch(`${API_BASE_URL}/attendance`).then(r => r.json()),
            fetch(`${API_BASE_URL}/leave/pending`).then(r => r.json()),
            fetch(`${API_BASE_URL}/shifts/active`).then(r => r.json())
        ]);

        document.getElementById('totalEmployees').textContent = employees.length;
        document.getElementById('presentToday').textContent = attendance.filter(a => 
            a.status === 'PRESENT' && isToday(a.checkInTime)
        ).length;
        document.getElementById('pendingLeaves').textContent = leaves.length;
        document.getElementById('activeShifts').textContent = shifts.length;
    } catch (error) {
        console.error('Error loading dashboard:', error);
    }
}

function isToday(dateString) {
    const date = new Date(dateString);
    const today = new Date();
    return date.toDateString() === today.toDateString();
}

// Employees
async function loadEmployees() {
    try {
        const employees = await fetch(`${API_BASE_URL}/employees`).then(r => r.json());
        const tbody = document.querySelector('#employeeTable tbody');
        tbody.innerHTML = employees.map(emp => `
            <tr>
                <td>${emp.employeeId}</td>
                <td>${emp.firstName} ${emp.lastName}</td>
                <td>${emp.email}</td>
                <td>${emp.department || 'N/A'}</td>
                <td>${emp.position || 'N/A'}</td>
                <td><span class="status-badge status-${emp.status.toLowerCase()}">${emp.status}</span></td>
                <td>
                    <button class="btn-danger" onclick="deleteEmployee(${emp.id})">Delete</button>
                </td>
            </tr>
        `).join('');
    } catch (error) {
        console.error('Error loading employees:', error);
    }
}

function showEmployeeForm() {
    document.getElementById('employeeFormContainer').style.display = 'block';
}

function hideEmployeeForm() {
    document.getElementById('employeeFormContainer').style.display = 'none';
    document.getElementById('employeeForm').reset();
}

document.getElementById('employeeForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const employee = {
        firstName: document.getElementById('firstName').value,
        lastName: document.getElementById('lastName').value,
        email: document.getElementById('email').value,
        employeeId: document.getElementById('employeeId').value,
        phone: document.getElementById('phone').value,
        department: document.getElementById('department').value,
        position: document.getElementById('position').value,
        hireDate: document.getElementById('hireDate').value,
        status: document.getElementById('status').value
    };

    try {
        const response = await fetch(`${API_BASE_URL}/employees`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(employee)
        });

        if (response.ok) {
            alert('Employee added successfully!');
            hideEmployeeForm();
            loadEmployees();
        }
    } catch (error) {
        console.error('Error adding employee:', error);
        alert('Error adding employee');
    }
});

async function deleteEmployee(id) {
    if (confirm('Are you sure you want to delete this employee?')) {
        try {
            await fetch(`${API_BASE_URL}/employees/${id}`, { method: 'DELETE' });
            loadEmployees();
        } catch (error) {
            console.error('Error deleting employee:', error);
        }
    }
}

// Attendance
async function loadAttendance() {
    try {
        const attendance = await fetch(`${API_BASE_URL}/attendance`).then(r => r.json());
        const tbody = document.querySelector('#attendanceTable tbody');
        tbody.innerHTML = attendance.map(record => `
            <tr>
                <td>${record.employee.firstName} ${record.employee.lastName}</td>
                <td>${formatDateTime(record.checkInTime)}</td>
                <td>${record.checkOutTime ? formatDateTime(record.checkOutTime) : 'Not checked out'}</td>
                <td>${record.hoursWorked ? record.hoursWorked.toFixed(2) : 'N/A'}</td>
                <td><span class="status-badge status-${record.status.toLowerCase()}">${record.status}</span></td>
                <td>
                    ${!record.checkOutTime ? `<button class="btn-danger" onclick="checkOut(${record.id})">Check Out</button>` : ''}
                </td>
            </tr>
        `).join('');
    } catch (error) {
        console.error('Error loading attendance:', error);
    }
}

async function loadEmployeeDropdown() {
    try {
        const employees = await fetch(`${API_BASE_URL}/employees`).then(r => r.json());
        const select = document.getElementById('attendanceEmployee');
        select.innerHTML = '<option value="">Select Employee</option>' + 
            employees.map(emp => `<option value="${emp.id}">${emp.firstName} ${emp.lastName}</option>`).join('');
    } catch (error) {
        console.error('Error loading employees:', error);
    }
}

async function checkIn() {
    const employeeId = document.getElementById('attendanceEmployee').value;
    if (!employeeId) {
        alert('Please select an employee');
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/attendance/checkin/${employeeId}`, {
            method: 'POST'
        });

        if (response.ok) {
            alert('Check-in successful!');
            loadAttendance();
        }
    } catch (error) {
        console.error('Error checking in:', error);
        alert('Error checking in');
    }
}

async function checkOut(recordId) {
    try {
        const response = await fetch(`${API_BASE_URL}/attendance/checkout/${recordId}`, {
            method: 'PUT'
        });

        if (response.ok) {
            alert('Check-out successful!');
            loadAttendance();
        }
    } catch (error) {
        console.error('Error checking out:', error);
        alert('Error checking out');
    }
}

// Shifts
async function loadShifts() {
    try {
        const shifts = await fetch(`${API_BASE_URL}/shifts`).then(r => r.json());
        const tbody = document.querySelector('#shiftTable tbody');
        tbody.innerHTML = shifts.map(shift => `
            <tr>
                <td>${shift.shiftName}</td>
                <td>${shift.startTime}</td>
                <td>${shift.endTime}</td>
                <td>${shift.description || 'N/A'}</td>
                <td><span class="status-badge status-${shift.isActive ? 'active' : 'inactive'}">${shift.isActive ? 'Active' : 'Inactive'}</span></td>
            </tr>
        `).join('');
    } catch (error) {
        console.error('Error loading shifts:', error);
    }
}

function showShiftForm() {
    document.getElementById('shiftFormContainer').style.display = 'block';
}

function hideShiftForm() {
    document.getElementById('shiftFormContainer').style.display = 'none';
    document.getElementById('shiftForm').reset();
}

document.getElementById('shiftForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const shift = {
        shiftName: document.getElementById('shiftName').value,
        startTime: document.getElementById('startTime').value,
        endTime: document.getElementById('endTime').value,
        description: document.getElementById('shiftDescription').value,
        isActive: true
    };

    try {
        const response = await fetch(`${API_BASE_URL}/shifts`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(shift)
        });

        if (response.ok) {
            alert('Shift created successfully!');
            hideShiftForm();
            loadShifts();
        }
    } catch (error) {
        console.error('Error creating shift:', error);
        alert('Error creating shift');
    }
});

// Leave Requests
async function loadLeaveRequests() {
    try {
        const leaves = await fetch(`${API_BASE_URL}/leave`).then(r => r.json());
        const tbody = document.querySelector('#leaveTable tbody');
        tbody.innerHTML = leaves.map(leave => `
            <tr>
                <td>${leave.employee.firstName} ${leave.employee.lastName}</td>
                <td>${leave.leaveType.replace('_', ' ')}</td>
                <td>${leave.startDate}</td>
                <td>${leave.endDate}</td>
                <td>${leave.reason}</td>
                <td><span class="status-badge status-${leave.status.toLowerCase()}">${leave.status}</span></td>
                <td>
                    ${leave.status === 'PENDING' ? `
                        <button class="btn-success" onclick="approveLeave(${leave.id})">Approve</button>
                        <button class="btn-danger" onclick="rejectLeave(${leave.id})">Reject</button>
                    ` : ''}
                </td>
            </tr>
        `).join('');
    } catch (error) {
        console.error('Error loading leave requests:', error);
    }
}

async function loadLeaveEmployeeDropdown() {
    try {
        const employees = await fetch(`${API_BASE_URL}/employees`).then(r => r.json());
        const select = document.getElementById('leaveEmployee');
        select.innerHTML = '<option value="">Select Employee</option>' + 
            employees.map(emp => `<option value="${emp.id}">${emp.firstName} ${emp.lastName}</option>`).join('');
    } catch (error) {
        console.error('Error loading employees:', error);
    }
}

function showLeaveForm() {
    document.getElementById('leaveFormContainer').style.display = 'block';
}

function hideLeaveForm() {
    document.getElementById('leaveFormContainer').style.display = 'none';
    document.getElementById('leaveForm').reset();
}

document.getElementById('leaveForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const employeeId = document.getElementById('leaveEmployee').value;
    if (!employeeId) {
        alert('Please select an employee');
        return;
    }

    const leave = {
        employee: { id: parseInt(employeeId) },
        leaveType: document.getElementById('leaveType').value,
        startDate: document.getElementById('startDate').value,
        endDate: document.getElementById('endDate').value,
        reason: document.getElementById('reason').value
    };

    try {
        const response = await fetch(`${API_BASE_URL}/leave`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(leave)
        });

        if (response.ok) {
            alert('Leave request submitted successfully!');
            hideLeaveForm();
            loadLeaveRequests();
        }
    } catch (error) {
        console.error('Error submitting leave request:', error);
        alert('Error submitting leave request');
    }
});

async function approveLeave(id) {
    const comments = prompt('Enter approval comments (optional):');
    try {
        const response = await fetch(`${API_BASE_URL}/leave/approve/${id}?comments=${encodeURIComponent(comments || '')}`, {
            method: 'PUT'
        });

        if (response.ok) {
            alert('Leave approved!');
            loadLeaveRequests();
        }
    } catch (error) {
        console.error('Error approving leave:', error);
        alert('Error approving leave');
    }
}

async function rejectLeave(id) {
    const comments = prompt('Enter rejection reason:');
    if (!comments) return;

    try {
        const response = await fetch(`${API_BASE_URL}/leave/reject/${id}?comments=${encodeURIComponent(comments)}`, {
            method: 'PUT'
        });

        if (response.ok) {
            alert('Leave rejected!');
            loadLeaveRequests();
        }
    } catch (error) {
        console.error('Error rejecting leave:', error);
        alert('Error rejecting leave');
    }
}

// Utility Functions
function formatDateTime(dateTimeString) {
    if (!dateTimeString) return 'N/A';
    const date = new Date(dateTimeString);
    return date.toLocaleString();
}

// Initialize dashboard on load
loadDashboard();
