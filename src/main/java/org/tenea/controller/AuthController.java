package org.tenea.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tenea.dto.LoginRequest;
import org.tenea.dto.LoginResponse;
import org.tenea.model.SessionData;
import org.tenea.service.SessionManager;
import org.tenea.service.SeleniumAuthService;
import org.tenea.service.HttpClientService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final SeleniumAuthService seleniumAuthService;
    private final HttpClientService httpClientService;
    private final SessionManager sessionManager;

    public AuthController(SeleniumAuthService seleniumAuthService, HttpClientService httpClientService, SessionManager sessionManager) {
        this.seleniumAuthService = seleniumAuthService;
        this.httpClientService = httpClientService;
        this.sessionManager = sessionManager;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        System.out.println(" Iniciando proceso de autenticación para usuario: " + 
                (request != null && request.getUsername() != null ? request.getUsername() : "default"));
        try {
            // Obtener credenciales del request o usar null para usar valores por defecto
            String username = (request != null) ? request.getUsername() : null;
            String password = (request != null) ? request.getPassword() : null;

            // Resolver el usuario a usar (parámetro o default)
            String userToAuth = (username != null && !username.isEmpty()) ? username : null;
            if (userToAuth == null) {
                // Si no se proporcionó usuario, intentar obtenerlo de properties
                // Esto requeriría acceso a las properties, así que usamos un placeholder
                userToAuth = "default-user";
            }

            // Verificar si el usuario ya tiene una sesión activa
            var existingSession = sessionManager.getSessionByUsername(userToAuth);
            if (existingSession.isPresent()) {
                SessionData sessionData = existingSession.get();
                System.out.println("♻️  Sesión existente encontrada para usuario: " + userToAuth);
                LoginResponse response = new LoginResponse(
                        sessionData.getSessionId(),
                        "♻️  Sesión reutilizada. Session ID (existente): " + sessionData.getSessionId(),
                        true
                );
                return ResponseEntity.ok(response);
            }

            System.out.println(" Creando nueva sesión para usuario: " + userToAuth);
            // Ejecutar autenticación con Selenium (usuario no tiene sesión previa)
            var cookieStore = seleniumAuthService.authenticate(username, password);

            // Extraer token de verificación
            String verificationToken = httpClientService.extractVerificationToken(cookieStore);

            // Crear sesión
            SessionData sessionData = new SessionData(userToAuth, cookieStore, verificationToken);
            sessionManager.storeSession(sessionData);

            LoginResponse response = new LoginResponse(
                    sessionData.getSessionId(),
                    "✅ Autenticación exitosa. Session ID generado.",
                    true
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ Error en autenticación: " + e.getMessage());
            LoginResponse response = new LoginResponse(
                    null,
                    "❌ Error en autenticación: " + e.getMessage(),
                    false
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}
