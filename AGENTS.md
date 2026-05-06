# TeneaAutomation Agent Guidelines

## Architecture Overview
This is a Java 11 Maven project that automates employee time tracking for the Tenea system. It uses a hybrid approach: Selenium WebDriver for initial authentication and cookie capture, then Apache HttpClient for subsequent API calls to log work hours.

Key components:
- **Main.java**: Single entry point containing all logic (auth, token extraction, bulk logging)
- **pom.xml**: Defines dependencies (Selenium 4.18.1, HttpClient 5.2.1, SLF4J logging)

## Build & Run Workflow
- Compile: `mvn compile` (outputs to `target/classes/`)
- Run: `java -cp target/classes org.tenea.Main`
- Requires ChromeDriver in PATH for Selenium headless mode

## Authentication Pattern
Uses headless Chrome to login at `https://schaefer.tenea.com/GestionAccesos-3.2.4/base/Login` with hardcoded credentials:
- Username: `txomin.gutierrez`
- Password: `P@r@l3l3p1p3d0.07`

Captures session cookies and `__RequestVerificationToken` from HTML for subsequent requests.

## API Interaction Pattern
HTTP GET requests to `/GestionAccesos-3.2.4/ControlAccesos/InsertRegisterAccess` with parameters:
- `punto_acceso`: Location codes (e.g., "26#1#1" for Office, "26#3#2" for Telework)
- `fecha_in`/`hora_in`: Start date/time (dd/MM/yyyy format, HH:mm)
- `fecha_out`/`hora_out`: End date/time
- `create_local_dt`: Current timestamp (yyyy-M-d HH:mm:ss.SSS)

Headers: `X-Requested-With: XMLHttpRequest`, `X-Request-Verification-Token`, `Referer`

## Date Handling
- Input format: dd/MM/yyyy (e.g., "15/05/2023")
- Skips weekends automatically
- Logs standard 8-hour days: 08:00-12:00 and 13:00-17:00

## Code Patterns
- Package: `org.tenea`
- Imports: Selenium WebDriver, Apache HttpClient 5, Java Time API
- Error handling: Basic try-catch with console output
- No configuration files; all values hardcoded in Main.java

## Development Notes
- Single-threaded execution with 300ms delays between requests
- User input via Scanner for dates and location selection
- Logging via SLF4J simple (console output)</content>
<parameter name="filePath">C:\Users\tgutierrez\Documents\PErsonal\WS_PERSONAL\teneator\TeneaAutomation\AGENTS.md