# Forgot Password Update Guide

This document explains how to set up and test the forgot-password flow that was just implemented.

## Scope of This Update

- Added forgot-password API endpoint: `POST /api/auth/forgot-password`
- Added reset-password API endpoint: `POST /api/auth/reset-password`
- Added reset password page: `/reset-password?token=...`
- Added token storage + expiry fields on user table
- Added SMTP sending through Spring Mail
- Added rate limiting on forgot/reset endpoints
- Updated login page to call real forgot-password API

## Prerequisites

- Java 21
- Maven Wrapper (`mvnw` already in repo)
- MySQL running locally
- A Mailtrap SMTP inbox (for demo/testing emails)

## 1. Configure Environment Variables (PowerShell)

Run these commands in the same terminal that will start the app:

```powershell
cd "D:\VS-code save\EAS\demo"

$env:MAIL_HOST="sandbox.smtp.mailtrap.io"
$env:MAIL_PORT="587"
$env:MAIL_USERNAME="<YOUR_MAILTRAP_USERNAME>"
$env:MAIL_PASSWORD="<YOUR_MAILTRAP_PASSWORD>"
$env:RESET_MAIL_FROM="no-reply@eas.local"
$env:APP_PUBLIC_BASE_URL="http://localhost:8080"

# Optional DB overrides if your local DB differs from defaults
# $env:DB_USERNAME="root"
# $env:DB_PASSWORD="1234"
```

Notes:

- Do not commit real credentials.
- If credentials change, update env vars only. No code change needed.

## 2. Start Application

```powershell
./mvnw spring-boot:run
```

When startup is successful, you should see Tomcat on port 8080.

## 3. Test Login (Smoke Check)

```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" `
	-Method Post `
	-ContentType "application/json" `
	-Body '{"username":"john.doe","password":"Employee@123"}'
```

Expected: HTTP 200.

## 4. Test Forgot Password

```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/auth/forgot-password" `
	-Method Post `
	-ContentType "application/json" `
	-Body '{"email":"john.doe@company.com"}'
```

Expected: HTTP 200 and generic message:

```json
{"message":"If this email exists, a reset link will be sent."}
```

Then check your Mailtrap inbox for the reset email.

## 5. Test Reset Password

1. Open Mailtrap email.
2. Copy reset link (contains `token` query param).
3. Open it in browser and submit new password.

Or test API directly:

```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/auth/reset-password" `
	-Method Post `
	-ContentType "application/json" `
	-Body '{"token":"<TOKEN_FROM_EMAIL>","newPassword":"NewPass@123"}'
```

Expected: HTTP 200 with success message.

## 6. Security Behavior (Expected)

- Forgot-password response never leaks reset link/token in API response.
- Token is one-time use.
- Token expires (default: 15 minutes).
- Rate limiting is enabled:
	- Forgot password: 5 requests / 15 minutes per IP
	- Reset password: 10 requests / 15 minutes per IP

## 7. Common Issues

- `MailAuthenticationException`
	- Verify Mailtrap host/port/username/password.
	- Restart app in a new terminal after changing env vars.

- `Port 8080 was already in use`
	- Stop old Java process, then rerun app.

- No email appears in inbox
	- Make sure app is running with Mailtrap vars in the same process.
	- Check runtime logs for `Password reset email queued for ...`.

## 8. Team Workflow Recommendation

- Keep all credentials in environment variables, never in git.
- Each teammate can use personal Mailtrap inbox credentials.
- Use this file as the single setup/test checklist.

