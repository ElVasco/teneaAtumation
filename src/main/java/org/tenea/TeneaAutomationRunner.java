package org.tenea;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Anteriormente ejecutaba la automatización al iniciar la app.
 * Ahora la app es un servicio REST.
 */
@Component
public class TeneaAutomationRunner implements CommandLineRunner {
    private static final Logger logger = LogManager.getLogger(TeneaAutomationRunner.class);

    @Override
    public void run(String... args) throws Exception {
        logger.info("=== TENEATOR API SERVER ===");
        logger.info("POST /teneator/api/auth/login - Para autenticarse");
        logger.info("POST /teneator/api/timelog - Para registrar entrada de tiempo");
        logger.info("POST /teneator/api/timeentries/list - Para obtener listado de imputaciones");
        logger.info("GET /teneator/api/health - Health check");
    }
}