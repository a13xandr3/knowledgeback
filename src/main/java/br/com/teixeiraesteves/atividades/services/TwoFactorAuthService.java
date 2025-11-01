package br.com.teixeiraesteves.atividades.services;

import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class TwoFactorAuthService {
    private final SecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final TimeProvider timeProvider = new SystemTimeProvider();
    private final QrGenerator qrGenerator = new ZxingPngQrGenerator();
    private final CodeVerifier verifier = new DefaultCodeVerifier(new DefaultCodeGenerator(), timeProvider);

    /** Gera segredo base32 */
    public String generateSecret() {
        return secretGenerator.generate();
    }

    /** Gera QRCode Base64 */
    public String generateQrImage(String username, String secret) throws QrGenerationException {
        QrData data = new QrData.Builder()
                .label(username)
                .secret(secret)
                .issuer("KnowledgeApp")
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();

        return "data:image/png;base64," +
                Base64.getEncoder().encodeToString(qrGenerator.generate(data));
    }

    /** Valida o código digitado */
    public boolean verify(String secret, String code) {
        return verifier.isValidCode(secret, code);
    }
}
