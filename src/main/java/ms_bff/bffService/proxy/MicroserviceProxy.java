package ms_bff.bffService.proxy;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collections;

/**
 * Corazón del BFF: reenvía cualquier petición HTTP a un microservicio destino
 * y devuelve la respuesta tal cual.
 *
 * ¿Por qué necesitamos esto?
 * El patrón BFF consiste en ser un "proxy inteligente". Esta clase hace el
 * trabajo sucio de copiar headers, leer el body y llamar al MS correcto.
 * Los controllers solo le dicen A DÓNDE enviar la petición.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MicroserviceProxy {

    private final RestTemplate restTemplate;

    /**
     * Reenvía la petición al microservicio indicado.
     *
     * @param request       La petición original que llegó del Angular
     * @param targetBaseUrl URL base del microservicio (ej: "http://localhost:8082")
     * @param path          Ruta completa (ej: "/auth/login")
     */
    public ResponseEntity<String> forward(HttpServletRequest request,
                                          String targetBaseUrl,
                                          String path) {
        String url = buildUrl(targetBaseUrl, path, request.getQueryString());
        log.debug("{} {} → {}", request.getMethod(), request.getRequestURI(), url);

        HttpHeaders headers = extractHeaders(request);
        HttpEntity<byte[]> entity = buildEntity(request, headers);
        HttpMethod method = HttpMethod.valueOf(request.getMethod());

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, method, entity, String.class);

            // Reenviamos la respuesta conservando el status y content-type originales
            HttpHeaders responseHeaders = new HttpHeaders();
            MediaType contentType = response.getHeaders().getContentType();
            if (contentType != null) {
                responseHeaders.setContentType(contentType);
            }
            return new ResponseEntity<>(response.getBody(), responseHeaders, response.getStatusCode());

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            // El microservicio devolvió 4xx o 5xx → lo pasamos tal cual al Angular
            log.warn("Microservicio respondió {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(e.getResponseBodyAsString());

        } catch (Exception e) {
            // El microservicio no respondió (está caído, timeout, etc.)
            log.error("No se pudo contactar {}: {}", url, e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\":\"Microservicio no disponible\",\"detalle\":\"" + e.getMessage() + "\"}");
        }
    }

    // ─── Métodos auxiliares ────────────────────────────────────────────────────

    private String buildUrl(String base, String path, String queryString) {
        String url = base + path;
        if (queryString != null && !queryString.isBlank()) {
            url += "?" + queryString;
        }
        return url;
    }

    private HttpHeaders extractHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        Collections.list(request.getHeaderNames()).forEach(name -> {
            // Omitimos "host" y "content-length" porque RestTemplate los recalcula
            if (!name.equalsIgnoreCase("host") && !name.equalsIgnoreCase("content-length")) {
                headers.set(name, request.getHeader(name));
            }
        });
        return headers;
    }

    private HttpEntity<byte[]> buildEntity(HttpServletRequest request, HttpHeaders headers) {
        try {
            byte[] body = request.getInputStream().readAllBytes();
            return new HttpEntity<>(body.length > 0 ? body : null, headers);
        } catch (IOException e) {
            log.error("Error leyendo body de la petición: {}", e.getMessage());
            return new HttpEntity<>(null, headers);
        }
    }
}
