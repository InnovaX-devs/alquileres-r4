package app.config;

import app.model.Rol;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String CLAIM_ROL = "rol";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String email, Rol rol) {
        Rol rolEfectivo = rol != null ? rol : Rol.USER;
        return Jwts.builder()
                .subject(email)
                .claim(CLAIM_ROL, rolEfectivo.name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    /** Si el token no trae rol (o trae uno desconocido) se trata como USER: nunca se escala a ADMIN por defecto. */
    public Rol extractRol(String token) {
        String valor = getClaims(token).get(CLAIM_ROL, String.class);
        if (valor == null) return Rol.USER;
        try {
            return Rol.valueOf(valor);
        } catch (IllegalArgumentException e) {
            return Rol.USER;
        }
    }

    public boolean isTokenValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
