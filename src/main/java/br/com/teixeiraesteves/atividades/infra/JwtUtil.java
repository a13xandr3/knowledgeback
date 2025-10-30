package br.com.teixeiraesteves.atividades.infra;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretBase64;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    public String generateToken(String username) {
        byte[] keyBytes = Decoders.BASE64.decode(secretBase64);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(secretBase64);
            SecretKey key = Keys.hmacShaKeyFor(keyBytes);
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("Token expirado: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Token inválido: " + e.getMessage());
        }
        return false;
    }

    public String extractUsername(String token) {
        byte[] keyBytes = Decoders.BASE64.decode(secretBase64);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }
}
