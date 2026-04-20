# Forgot Password Update Guide

This runbook explains exactly how to set up, migrate database changes, and test the forgot-password feature end-to-end.

## Scope of This Update

- Added forgot-password endpoint: POST /api/auth/forgot-password
- Added reset-password endpoint: POST /api/auth/reset-password
- Added reset page: /reset-password?token=...
- Added reset token fields in users table
- Added SMTP sending through Spring Mail
- Added rate limiting on forgot/reset endpoints
- Updated login UI to call the real forgot-password API

## Prerequisites

- Java 21
- MySQL running locally
- Maven Wrapper in repo
- A Mailtrap account

## 1) Mailtrap Setup (Required)

Follow these steps per teammate.

1. Create/sign in account at https://mailtrap.io
2. Open Email Testing (Email Sandboxes)
3. Create or open a Sandbox
4. In the Sandbox, open Integration tab
5. Select SMTP
6. Copy these values:
	 - Host (example: sandbox.smtp.mailtrap.io)
	 - Port (recommended: 587)
	 - Username
	 - Password

Important:

- Use credentials from the same Sandbox where you expect messages.
- If credentials are regenerated in Mailtrap, update local env vars.

## 2) Database Schema Changes (Required)

The users table now needs these fields:

- reset_password_token VARCHAR(120)
- reset_password_expires_at DATETIME(6)

### Option A - Automatic update (current project default)

Current app config uses spring.jpa.hibernate.ddl-auto=update, so starting the app will usually add missing columns automatically.

### Option B - Manual SQL update (safe fallback)

Run this SQL on your local database if columns are missing:

```sql
ALTER TABLE users
	ADD COLUMN IF NOT EXISTS reset_password_token VARCHAR(120),
	ADD COLUMN IF NOT EXISTS reset_password_expires_at DATETIME(6);

CREATE INDEX idx_users_reset_password_token ON users (reset_password_token);
```

If your MySQL version does not support IF NOT EXISTS on ADD COLUMN, run separate checks first.

### Verify schema

```sql
SHOW COLUMNS FROM users LIKE 'reset_password_token';
SHOW COLUMNS FROM users LIKE 'reset_password_expires_at';
```

## 3) Configure Environment Variables (PowerShell)

Run in the same terminal session that will start the app:

```powershell
cd "D:\VS-code save\EAS\demo"

$env:MAIL_HOST="sandbox.smtp.mailtrap.io"
$env:MAIL_PORT="587"
$env:MAIL_USERNAME="YOUR_MAILTRAP_USERNAME"
$env:MAIL_PASSWORD="YOUR_MAILTRAP_PASSWORD"
$env:RESET_MAIL_FROM="no-reply@eas.local"
$env:APP_PUBLIC_BASE_URL="http://localhost:8080"

# Optional DB overrides if needed
# $env:DB_USERNAME="root"
# $env:DB_PASSWORD="1234"
```

Do not include angle brackets in real values.

## 4) Start App

```powershell
./mvnw spring-boot:run
```

Expected startup signal: Tomcat started on port 8080.

## 5) Verify test data exists

Forgot-password only sends mail for an existing active user tied to an employee email.

Recommended test email:

- john.doe@company.com

If your seed data differs, use a valid email from your own users table.

## 6) Test Flow

### 6.1 Smoke login

```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" `
	-Method Post `
	-ContentType "application/json" `
	-Body '{"username":"john.doe","password":"Employee@123"}'
```

Expected: HTTP 200.

### 6.2 Forgot password

```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/auth/forgot-password" `
	-Method Post `
	-ContentType "application/json" `
	-Body '{"email":"john.doe@company.com"}'
```

Expected: HTTP 200 with generic message.

### 6.3 Check Mailtrap inbox

In Mailtrap, open the same Sandbox used in step 1 and refresh messages list.

Expected subject: Password reset request.

### 6.4 Reset password

Open email, copy reset link, then submit new password via browser.

Or call API directly:

```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/auth/reset-password" `
	-Method Post `
	-ContentType "application/json" `
	-Body '{"token":"TOKEN_FROM_EMAIL","newPassword":"NewPass@123"}'
```

Expected: HTTP 200.

## 7) Security Behavior (Expected)

- API never returns reset link/token in forgot-password response
- Token is one-time use
- Token expiry default is 15 minutes
- Rate limit:
	- Forgot password: 5 requests / 15 minutes per IP
	- Reset password: 10 requests / 15 minutes per IP

## 8) Troubleshooting

### Mail not visible in Mailtrap

1. Confirm app log contains: Password reset email queued for ...
2. Confirm you are checking the correct Sandbox
3. Confirm host/port/user/pass are from that same Sandbox
4. Restart app after env changes (new terminal session)

### SMTP auth error

- Recopy Mailtrap Username/Password from Integration -> SMTP
- Remove extra characters/spaces in env values

### Port 8080 in use

```powershell
$pid8080 = (Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue |
	Where-Object { $_.State -eq 'Listen' } |
	Select-Object -First 1 -ExpandProperty OwningProcess)
if ($pid8080) { Stop-Process -Id $pid8080 -Force }
```

## 9) Team Rules

- Never commit real SMTP credentials
- Keep credentials in local env vars only
- Use this file as the single onboarding and test checklist

