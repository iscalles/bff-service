package ms_bff.bffService.controller;

import ms_bff.bffService.proxy.MicroserviceProxy;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Redirige al MS-Usuario (puerto 8081) todo lo relacionado con personas del colegio.
 *
 * Rutas que maneja (todas protegidas — requieren JWT):
 *  /usuario/**             → CRUD de usuarios base
 *  /docente/**             → CRUD de docentes
 *  /estudiante/**          → CRUD de estudiantes
 *  /apoderado/**           → CRUD de apoderados
 *  /administrativo/**      → CRUD de personal administrativo
 *  /usuario-rol/**         → asignación y consulta de roles
 *  /apoderado-estudiante/** → vínculos apoderado ↔ estudiante
 *
 * El JWT filter ya validó el token antes de llegar aquí.
 * MS-Usuario no tiene seguridad propia, así que el BFF es la única barrera.
 */
@RestController
@RequiredArgsConstructor
public class UsuarioMsController {

    private final MicroserviceProxy proxy;

    @Value("${ms-usuario.url}")
    private String msUsuarioUrl;

    @RequestMapping({
        "/usuario/**",
        "/docente/**",
        "/estudiante/**",
        "/apoderado/**",
        "/administrativo/**",
        "/usuario-rol/**",
        "/apoderado-estudiante/**"
    })
    public ResponseEntity<String> proxyToUsuarioMs(HttpServletRequest request) {
        return proxy.forward(request, msUsuarioUrl, request.getRequestURI());
    }
}
