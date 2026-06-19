package ms_bff.bffService.controller;

import ms_bff.bffService.proxy.MicroserviceProxy;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Redirige al MS-Asistencia (puerto 8084) todo lo relacionado con asistencia y conducta.
 *
 * Rutas que maneja (todas protegidas — requieren JWT):
 *  /asistencia/**  → CRUD de registros de asistencia
 *  /conducta/**    → CRUD de registros de conducta
 */
@RestController
@RequiredArgsConstructor
public class AsistenciaMsController {

    private final MicroserviceProxy proxy;

    @Value("${ms-asistencia.url}")
    private String msAsistenciaUrl;

    @RequestMapping({
        "/asistencia/**",
        "/conducta/**"
    })
    public ResponseEntity<String> proxyToAsistenciaMs(HttpServletRequest request) {
        return proxy.forward(request, msAsistenciaUrl, request.getRequestURI());
    }
}
