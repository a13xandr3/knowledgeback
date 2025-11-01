package br.com.teixeiraesteves.atividades.controllers;

import dev.samstevens.totp.exceptions.QrGenerationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import br.com.teixeiraesteves.atividades.repositories.UserRepository;
import br.com.teixeiraesteves.atividades.services.TwoFactorAuthService;
import br.com.teixeiraesteves.atividades.services.JwtService;

import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/2fa")
public class TwoFactorAuthController {

    private final TwoFactorAuthService service;
    private final UserRepository userRepository;
    private final JwtService jwtService; // adicionado para gerar o token

    public TwoFactorAuthController(TwoFactorAuthService service, UserRepository userRepository, JwtService jwtService) {
        this.service = service;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    /** Configuração inicial – gera segredo e QRCode */
    @PostMapping("/setup")
    public ResponseEntity<?> setup(@RequestBody Map<String, String> body) throws QrGenerationException {
        String username = body.get("username");

        // Gera segredo e QRCode
        String secret = service.generateSecret();
        String qr = service.generateQrImage(username, secret);

        // Atualiza user no banco
        userRepository.updateSecret(username, secret);

        return ResponseEntity.ok(Map.of("qrCode", qr, "secret", secret));
    }

    /** Validação do código de 6 dígitos */
    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String code = body.get("code");

        String secret = userRepository.getSecret(username);
        boolean valid = service.verify(secret, code);

        if (valid) {
            String jwt = jwtService.generateToken(username);
            return ResponseEntity.ok(Map.of("token", jwt));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
