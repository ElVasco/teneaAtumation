package org.tenea;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Anteriormente ejecutaba la automatización al iniciar la app.
 * Ahora la app es un servicio REST.
 */
@Component
public class TeneaAutomationRunner implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== TENEATOR API SERVER ===");
        System.out.println("POST /teneator/api/auth/login - Para autenticarse");
        System.out.println("POST /teneator/api/timelog - Para registrar entrada de tiempo");
        System.out.println("POST /teneator/api/timeentries/list - Para obtener listado de imputaciones");
        System.out.println("GET /teneator/api/health - Health check");
    }
}