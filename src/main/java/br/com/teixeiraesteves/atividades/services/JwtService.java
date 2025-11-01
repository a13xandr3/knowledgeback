package br.com.teixeiraesteves.atividades.services;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    // Chave secreta com 256 bits (32 caracteres). Gere uma segura em produção.
    private static final String SECRET_KEY = "8b5c3f6d9a247c8b2a74e998f0b6719d7e2f1c3e8a9b4f7d";

    // Tempo de expiração: 15 minutos
    private static final long EXPIRATION_TIME = 15 * 60 * 1000;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    /** Gera um token JWT simples com o username */
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** Gera token com claims customizados (roles, 2FA, etc.) */
    public String generateToken(Map<String, Object> claims, String username) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** Extrai o username do token */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /** Verifica se o token é válido e não expirou */
    public boolean isTokenValid(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (username.equals(extractedUsername)) && !isTokenExpired(token);
    }

    /** Extrai qualquer claim usando função genérica */
    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        final Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

}