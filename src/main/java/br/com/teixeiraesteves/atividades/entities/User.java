package br.com.teixeiraesteves.atividades.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "users") // mantém minúsculo e plural para evitar problemas em Unix
public class User {

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getTwoFaSecret() {
        return twoFaSecret;
    }

    public void setTwoFaSecret(String twoFaSecret) {
        this.twoFaSecret = twoFaSecret;
    }

    public boolean isTwoFaEnabled() {
        return twoFaEnabled;
    }

    public void setTwoFaEnabled(boolean twoFaEnabled) {
        this.twoFaEnabled = twoFaEnabled;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", unique = true, nullable = false, length = 100)
    private String username;

    @Column(name = "passwordHash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "twoFaSecret", length = 255)
    private String twoFaSecret; // segredo TOTP (criptografado)

    @Column(name = "twoFaEnabled", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean twoFaEnabled = false;
}
