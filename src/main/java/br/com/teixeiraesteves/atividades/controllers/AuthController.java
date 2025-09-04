package br.com.teixeiraesteves.atividades.controllers;

import br.com.teixeiraesteves.atividades.infra.JwUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwUtil jwUtil;

    public AuthController(JwUtil jwUtil) {
        this.jwUtil = jwUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        if ("alexandre".equals(loginRequest.username()) && "1234".equals(loginRequest.password())) {
            String token = jwUtil.generateToken(loginRequest.username());
            return ResponseEntity.ok(new AuthResponse(token));
        } else {
            return ResponseEntity.status(401).body("Usuário ou senha inválidos");
        }
    }

    public record LoginRequest(String username, String password) {}
    public record AuthResponse(String token) {}

}