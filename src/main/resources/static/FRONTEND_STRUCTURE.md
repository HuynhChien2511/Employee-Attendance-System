# Frontend UI/UX Structure

This project now has a reusable frontend structure under `src/main/resources/static/assets`.

## Folder Layout

- `assets/css/tokens.css`  
  Color palette, radius, shadows, and design tokens.
- `assets/css/base.css`  
  Global reset, base typography, and core HTML element defaults.
- `assets/css/components.css`  
  Shared components (`.btn`, `.card`, `.badge`, `.form-control`).
- `assets/css/layouts.css`  
  Reusable layout rules (sidebar, topbar, responsive behavior).
- `assets/css/themes/attendance-theme.css`  
  Theme layer customized for this Employee Attendance System.
- `assets/js/core/ui.js`  
  Shared frontend helper functions.

## Recommended Page Convention

Keep role pages split by concern:

- `login.html`
- `admin.html`
- `manager.html`
- `employee.html`

And load shared assets on each page:

```html
<link rel="stylesheet" href="/assets/css/tokens.css">
<link rel="stylesheet" href="/assets/css/base.css">
<link rel="stylesheet" href="/assets/css/components.css">
<link rel="stylesheet" href="/assets/css/layouts.css">
<link rel="stylesheet" href="/assets/css/themes/attendance-theme.css">
<script src="/assets/js/core/ui.js"></script>
```

## UX Guidelines For This Attendance App

- Use `primary` buttons for key actions (Check In, Save, Approve).
- Use `danger` buttons only for destructive actions (Delete, Reject, Cancel request).
- Keep most content inside cards and table blocks for readability.
- Prefer compact forms with clear labels (`Leave Type`, `Requested Date`, `Reason`).
- Show status with badges (`PENDING`, `APPROVED`, `REJECTED`, `PRESENT`, `LATE`).
