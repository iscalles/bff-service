package ms_bff.bffService.controller;

import ms_bff.bffService.proxy.MicroserviceProxy;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Redirige al MS-Academico (puerto 8083) todo lo relacionado con la gestión académica.
 *
 * Rutas que maneja (todas protegidas — requieren JWT):
 *  /cursos/**           → CRUD de cursos
 *  /asignaturas/**      → CRUD de asignaturas
 *  /evaluaciones/**     → CRUD de evaluaciones
 *  /matriculas/**       → CRUD de matrículas
 *  /curso-asignatura/** → CRUD de asignaciones curso-asignatura
 *  /calificaciones/**   → CRUD de calificaciones
 *
 * El JWT filter ya validó el token antes de llegar aquí.
 * MS-Academico no tiene seguridad propia, así que el BFF es la única barrera.
 */
@RestController
@RequiredArgsConstructor
public class AcademicoMsController {

    private final MicroserviceProxy proxy;

    @Value("${ms-academico.url}")
    private String msAcademicoUrl;

    @RequestMapping({
        "/cursos/**",
        "/asignaturas/**",
        "/evaluaciones/**",
        "/matriculas/**",
        "/curso-asignatura/**",
        "/calificaciones/**"
    })
    public ResponseEntity<String> proxyToAcademicoMs(HttpServletRequest request) {
        return proxy.forward(request, msAcademicoUrl, request.getRequestURI());
    }
}
