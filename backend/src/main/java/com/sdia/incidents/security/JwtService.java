package com.sdia.incidents.security;

import com.sdia.incidents.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Generation et validation des Access Tokens JWT (HS256).
 *
 * Contenu du token (claims) :
 *  - sub  : email de l'utilisateur        (claim enregistre)
 *  - iat  : date de creation              (claim enregistre)
 *  - exp  : date d'expiration             (claim enregistre)
 *  - role : role de l'utilisateur         (claim prive / personnalise)
 *  - fullName : nom complet               (claim prive)
 */
@Service
public class JwtService {

    public static final String ROLE_CLAIM = "role";

    private final SecretKey signingKey;
    private final long accessExpirationMs;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.access-expiration-ms}") long accessExpirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessExpirationMs = accessExpirationMs;
    }

    public String generateToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessExpirationMs);
        return Jwts.builder()
                .subject(user.getEmail())
                .claim(ROLE_CLAIM, user.getRole().name())
                .claim("fullName", user.getFullName())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Verifie la signature ET l'expiration puis retourne les claims.
     * Leve ExpiredJwtException si exp < maintenant,
     * SignatureException / MalformedJwtException si le token est falsifie ou mal forme.
     */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return parseClaims(token).get(ROLE_CLAIM, String.class);
    }

    public long getAccessExpirationMs() {
        return accessExpirationMs;
    }
}
