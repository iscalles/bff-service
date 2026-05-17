package ms_bff.bffService.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Filtro que se ejecuta UNA VEZ por cada petición HTTP que llega al BFF.
 *
 * Flujo:
 * 1. ¿Tiene el header "Authorization: Bearer <token>"? → extrae el token
 * 2. ¿Es el token válido? → autentica al usuario en el SecurityContext
 * 3. Si no hay token o es inválido → deja pasar (Spring Security decide si la ruta lo requiere)
 *
 * OncePerRequestFilter garantiza que este código no se ejecute dos veces
 * por la misma petición, lo cual puede pasar con forwards internos.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Sin token → continúa. Spring Security bloqueará si la ruta lo exige.
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7); // recorta "Bearer "

        try {
            if (jwtService.isTokenValid(token)) {
                String idUsuario = jwtService.getIdUsuario(token);
                Set<String> roles = jwtService.getRoles(token);

                // Convertimos los roles a GrantedAuthority que entiende Spring Security
                var authorities = roles.stream()
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                        .collect(Collectors.toSet());

                // Registramos al usuario como autenticado en el contexto de seguridad
                var auth = new UsernamePasswordAuthenticationToken(idUsuario, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);

                log.debug("Token válido — idUsuario={} roles={}", idUsuario, roles);
            }
        } catch (Exception e) {
            log.warn("Token inválido: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
