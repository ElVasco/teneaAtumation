# TeneaAutomation Agent Guidelines (Updated)

## Architecture Overview
Spring Boot REST API that automates employee time tracking for the Tenea system. 

**Hybrid Architecture:**
- **Frontend Layer**: REST API endpoints for session management and time logging
- **Authentication Layer**: Selenium WebDriver (headless) for initial login
- **API Layer**: Apache HttpClient for Tenea API calls
- **Session Management**: In-memory UUID-based session storage with automatic expiration

Key components:
- **AuthController**: `/api/auth/login` - Generates session UUID with cookies
- **TimeLogController**: `/api/timelog` - Records work hours using session UUID
- **SeleniumAuthService**: Headless Chrome authentication
- **HttpClientService**: HTTP API calls to Tenea
- **SessionManager**: Manages active sessions with 60-minute expiration

## Build & Run Workflow
- Compile: `mvn clean compile`
- Run: `mvn spring-boot:run` (starts on port 8080)
- Requires ChromeDriver (auto-managed by WebDriverManager)

## REST API Endpoints

### POST /api/auth/login
Authenticates user and generates session UUID (or returns existing session if active).

**Behavior:**
1. Checks if user has active session (not expired) → Returns existing UUID
2. If no active session → Executes Selenium login → Creates new UUID

Request body:
```json
{
  "username": "user@example.com",
  "password": "password123"
}
```

Or empty to use default credentials from properties:
```json
{}
```

Response:
```json
{
  "sessionId": "uuid-string",
  "success": true,
  "message": "✅ Autenticación exitosa" // or "♻️ Sesión reutilizada"
}
```

### POST /api/timelog
Logs work hours using session UUID:
- `session_id`: UUID from login endpoint
- `date_time_inicio`: Start datetime (yyyy-MM-dd HH:mm)
- `date_time_fin`: End datetime (yyyy-MM-dd HH:mm)
- `ubicacion`: Location ("oficina", "teletrabajo", "onsite")

## Session Management Pattern
- Sessions stored in-memory HashMap (SessionManager) - indexed by UUID
- Sessions also tracked by username for intelligent reuse
- UUID-indexed for security
- Auto-expire after 60 minutes of inactivity
- Contain BasicCookieStore + VerificationToken + Username
- Created once per unique user login, reused for multiple operations
- On login retry: checks if user already has active session → returns existing UUID
- On session lookup: validates expiration and returns if still valid

## Location Codes
- "oficina" → "26#1#1" (Office)
- "teletrabajo" → "26#3#2" (Telework)
- "onsite" → "26#4#4" (On-site)

## Code Patterns
- Package: `org.tenea`
- Controllers: REST endpoints using Spring annotations
- DTOs: Request/Response objects in `org.tenea.dto`
- Services: Business logic (Auth, HttpClient, SessionManager)
- No configuration files; all hardcoded in properties

## Development Notes
- Single-threaded event loop for HTTP requests
- 300ms delay between Tenea API calls
- No database; sessions are in-memory only
- ChromeDriver runs in headless mode (no GUI)
- Context path: `/teneator` for all endpoints

## Error Handling
- 401 Unauthorized: Invalid or expired session
- 500 Internal Server Error: Authentication or API call failures
- All errors logged to console with ❌ prefix

