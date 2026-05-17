package ms_bff.bffService.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Valida JWT localmente usando el mismo secret que ms-auth.
 *
 * ¿Por qué localmente y no llamando a /auth/validate?
 * Porque validar localmente evita un viaje de red por cada petición,
 * haciendo el BFF más rápido y menos dependiente de ms-auth para rutas
 * que solo necesitan saber si el token es válido.
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public boolean isTokenValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getIdUsuario(String token) {
        return getClaims(token).getSubject();
    }

    @SuppressWarnings("unchecked")
    public Set<String> getRoles(String token) {
        Object rawRoles = getClaims(token).get("roles");
        if (rawRoles instanceof Collection<?> col) {
            return col.stream().map(Object::toString).collect(Collectors.toSet());
        }
        return Set.of();
    }
}
