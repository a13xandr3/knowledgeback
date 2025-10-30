package br.com.teixeiraesteves.atividades.controllers;

import br.com.teixeiraesteves.atividades.infra.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwUtil) {
        this.jwtUtil = jwUtil;
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> body) {
        String oldToken = body.get("token");
        if (jwtUtil.validateToken(oldToken)) {
            String username = jwtUtil.extractUsername(oldToken);
            String newToken = jwtUtil.generateToken(username);
            return ResponseEntity.ok(Map.of("token", newToken));
        }
        return ResponseEntity.status(401).body("Token inválido ou expirado");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {

        System.out.println("Username recebido: " + loginRequest.username());
        System.out.println("Password recebido: " + loginRequest.password());

        String expectedUsername = "a13xandr3ea@gmail.com";
        String expectedHash = "369db8006226e27113b22ddf6ace5eadc3437e4175d5895f8a630e0633698e0d";

        if (expectedUsername.equals(loginRequest.username())
            && expectedHash.equals(loginRequest.password())) {

            String token = jwtUtil.generateToken(expectedUsername);

            return ResponseEntity.ok(new AuthResponse(token));

        } else {
            return ResponseEntity.status(401).body("Usuário ou senha inválidos");
        }
    }

    public record LoginRequest(String username, String password) {}

    public record AuthResponse(String token) {}


}