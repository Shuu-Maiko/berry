# API Reference

The Berry scheduler exposes a set of RESTful APIs to manage jobs and notification channels. All endpoints (except OAuth login) require authentication via the `auth_token` cookie obtained after successful Google OAuth2 login.

---

## Authentication

### `GET /oauth2/authorization/google`
Initiates the Google OAuth2 flow. Upon successful login, the server sets an HttpOnly, Secure (in prod) `auth_token` cookie and redirects the user to the frontend dashboard.

---

## Jobs API

### `POST /api/jobs/create`
Schedules a new recurring webhook job.
**Request Body:**
```json
{
  "name": "Health Check Ping",
  "cronExp": "*/5 * * * *",
  "url": "https://api.example.com/health",
  "httpMethod": "GET"
}
```

### `GET /api/jobs`
Retrieves a list of all active jobs for the authenticated user.

### `GET /api/jobs/{secureJobId}/details`
Fetches detailed information and configuration for a specific job.

### `PATCH /api/jobs/{secureJobId}/settings`
Updates the notification preferences for a job.
**Request Body:**
```json
{
  "notifyOnFailure": true,
  "notifyOnSuccess": false
}
```

### `DELETE /api/jobs/{secureJobId}`
Deletes a scheduled job and halts all future executions.

### `GET /api/jobs/{secureJobId}/history`
Retrieves the execution history (success/failure logs) for a specific job.

### `GET /api/jobs/{secureJobId}/responses`
Retrieves the raw HTTP responses from recent executions of the job. You can pass a `?limit=50` query parameter (defaults to 100).

---

## Notification Channels API

### `GET /api/notifications/channels`
Lists all configured notification channels (e.g., Discord webhooks) for the authenticated user.

### `POST /api/notifications/channels`
Adds a new notification channel to receive alerts for job executions.
**Request Body:**
```json
{
  "name": "My Discord Alert Channel",
  "type": "DISCORD",
  "webhookUrl": "https://discord.com/api/webhooks/..."
}
```

### `DELETE /api/notifications/channels/{id}`
Deletes a specific notification channel.

---

## Local Development (Dev Profile Only)

### `GET /users`
*Only available when `APP_ENV=dev`.* Returns a list of all users. Used for debugging and administrative verification in local environments. Disabled completely in `prod`.
