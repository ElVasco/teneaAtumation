# TeneaAutomation API REST

## Descripción
Servicio REST para automatizar el logging de tiempo en el sistema Tenea. La API gestiona sesiones con UUID para mantener las cookies y permite registrar entradas de tiempo.

## Endpoints

### 1. Autenticación (Login)
**URL:** `POST /teneator/api/auth/login`

**Descripción:** Realiza la autenticación mediante Selenium y genera un UUID de sesión. Si el usuario ya tiene una sesión activa (no expirada), simplemente devuelve el UUID existente sin ejecutar Selenium nuevamente.

**Comportamiento inteligente:**
- ✅ Si el usuario ya tiene una sesión activa → Devuelve UUID existente (rápido)
- ✅ Si el usuario NO tiene sesión → Ejecuta Selenium y crea nueva sesión (primeras 2-3 segundos)
- ♻️ Las sesiones expiran en 60 minutos sin uso

**Request:**
```json
{
  "username": "txomin.gutierrez",
  "password": "P@r@l3l3p1p3d0.07"
}
```

O sin credenciales (usa valores por defecto de `application.properties`):
```json
{}
```

**Response (200 OK - Sesión Existente):**
```json
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "message": "♻️ Sesión reutilizada. Session ID (existente): 550e8400-e29b-41d4-a716-446655440000",
  "success": true
}
```

**Response (200 OK - Nueva Sesión):**
```json
{
  "sessionId": "660f9401-f39c-52e5-b827-557766551111",
  "message": "✅ Autenticación exitosa. Session ID generado.",
  "success": true
}
```

**Response (401 Unauthorized):**
```json
{
  "sessionId": null,
  "message": "❌ Error en autenticación: ...",
  "success": false
}
```

---

### 2. Registrar Entrada de Tiempo
**URL:** `POST /teneator/api/timelog`

**Descripción:** Registra una entrada de tiempo usando la sesión previamente autenticada.

**Request:**
```json
{
  "session_id": "550e8400-e29b-41d4-a716-446655440000",
  "date_time_inicio": "2026-05-04 08:00",
  "date_time_fin": "2026-05-04 12:00",
  "ubicacion": "oficina"
}
```

**Parámetros:**
- `session_id` (string): UUID retornado por el endpoint de login
- `date_time_inicio` (string): Fecha y hora de inicio en formato `yyyy-MM-dd HH:mm`
- `date_time_fin` (string): Fecha y hora de fin en formato `yyyy-MM-dd HH:mm`
- `ubicacion` (string): Una de las siguientes opciones:
  - `"oficina"` (Planta 1) - Código: 26#1#1
  - `"teletrabajo"` - Código: 26#3#2
  - `"onsite"` - Código: 26#4#4

**Response (200 OK):**
```json
{
  "success": true,
  "message": "✅ Entrada de tiempo registrada exitosamente",
  "statusCode": 200
}
```

**Response (401 Unauthorized):**
```json
{
  "success": false,
  "message": "❌ Sesión no encontrada o expirada",
  "statusCode": 401
}
```

---

### 3. Health Check
**URL:** `GET /teneator/api/health`

**Descripción:** Verifica el estado del servidor y muestra información sobre sesiones activas.

**Response (200 OK):**
```json
{
  "status": "✅ UP",
  "service": "TeneaAutomation API Server",
  "active_sessions": 1,
  "endpoints": {
    "login": "POST /teneator/api/auth/login",
    "timelog": "POST /teneator/api/timelog",
    "health": "GET /teneator/api/health"
  }
}
```

---

## Flujo de Uso

### Ejemplo: Comportamiento Inteligente de Sesiones

#### Primer Login (Crear sesión)
```bash
curl -X POST http://localhost:8080/teneator/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "txomin.gutierrez",
    "password": "P@r@l3l3p1p3d0.07"
  }'
```
**Respuesta:** Ejecuta Selenium, crea nueva sesión (tarda ~3 segundos)
```json
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "message": "✅ Autenticación exitosa. Session ID generado.",
  "success": true
}
```

#### Segundo Login (Reutilizar sesión)
Si haces login nuevamente en los próximos 60 minutos:
```bash
curl -X POST http://localhost:8080/teneator/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "txomin.gutierrez",
    "password": "P@r@l3l3p1p3d0.07"
  }'
```
**Respuesta:** Devuelve la sesión existente (instantáneo)
```json
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "message": "♻️ Sesión reutilizada. Session ID (existente): 550e8400-e29b-41d4-a716-446655440000",
  "success": true
}
```

### Paso 1: Autenticarse (con el UUID obtenido)
```bash
curl -X POST http://localhost:8080/teneator/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "txomin.gutierrez",
    "password": "P@r@l3l3p1p3d0.07"
  }'
```

**Nota:** Usa el `sessionId` retornado para las siguientes operaciones

### Paso 2: Registrar Entrada de Tiempo (Mañana)
```bash
curl -X POST http://localhost:8080/teneator/api/timelog \
  -H "Content-Type: application/json" \
  -d '{
    "session_id": "550e8400-e29b-41d4-a716-446655440000",
    "date_time_inicio": "2026-05-04 08:00",
    "date_time_fin": "2026-05-04 12:00",
    "ubicacion": "oficina"
  }'
```

### Paso 3: Registrar Entrada de Tiempo (Tarde)
```bash
curl -X POST http://localhost:8080/teneator/api/timelog \
  -H "Content-Type: application/json" \
  -d '{
    "session_id": "550e8400-e29b-41d4-a716-446655440000",
    "date_time_inicio": "2026-05-04 13:00",
    "date_time_fin": "2026-05-04 17:00",
    "ubicacion": "oficina"
  }'
```

✨ **Ventaja:** Una sola autenticación, múltiples registros de tiempo. La sesión se reutiliza automáticamente.

---

## Características

- ✅ **Gestión Inteligente de Sesiones**: Si el usuario ya está autenticado, devuelve su sesión existente sin ejecutar Selenium nuevamente
- ✅ **Identificación por Usuario**: Cada sesión está vinculada al usuario que la creó
- ✅ **Expiración Automática**: Las sesiones expiran después de 60 minutos de inactividad
- ✅ **Reutilización de Cookies**: Una sesión se puede usar para múltiples registros de tiempo
- ✅ **Sin Interfaz Gráfica**: Opera completamente en modo headless (sin ventanas de Chrome visibles)
- ✅ **REST API**: Interfaz limpia basada en HTTP

## Configuración

Los parámetros de configuración se encuentran en `application.properties`:

- `server.port=8080` - Puerto del servidor
- `tenea.base.url` - URL del sistema Tenea
- `tenea.username` - Usuario para autenticación
- `tenea.password` - Contraseña para autenticación

## Arquitectura

```
TeneaAutomation (Spring Boot REST API)
├── AuthController -> Maneja login
├── TimeLogController -> Maneja logging de tiempo
├── HealthController -> Verifica salud del servicio
├── SeleniumAuthService -> Ejecuta autenticación con Selenium
├── HttpClientService -> Realiza peticiones HTTP al sistema Tenea
└── SessionManager -> Gestiona sesiones activas
```



