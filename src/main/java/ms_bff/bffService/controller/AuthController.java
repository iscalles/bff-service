package ms_bff.bffService.controller;

import ms_bff.bffService.proxy.MicroserviceProxy;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Redirige al MS-Auth (puerto 8082) todo lo relacionado con autenticación.
 *
 * Rutas que maneja:
 *  POST /auth/login          → público  (definido en SecurityConfig)
 *  POST /auth/refresh         → público
 *  POST /auth/logout          → protegido (requiere JWT)
 *  GET  /auth/validate        → protegido
 *  GET/POST/PUT/DELETE /cuenta-acceso/**  → protegido (gestión de cuentas)
 *  GET/POST/PUT/DELETE /refresh-token/**  → protegido (gestión de tokens)
 *
 * El "/**" en @RequestMapping captura cualquier sub-ruta, incluyendo
 * path variables como /cuenta-acceso/5 o /refresh-token/12.
 */
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final MicroserviceProxy proxy;

    @Value("${ms-auth.url}")
    private String msAuthUrl;

    @RequestMapping({"/auth/**", "/cuenta-acceso/**", "/refresh-token/**"})
    public ResponseEntity<String> proxyToAuthMs(HttpServletRequest request) {
        return proxy.forward(request, msAuthUrl, request.getRequestURI());
    }
}
