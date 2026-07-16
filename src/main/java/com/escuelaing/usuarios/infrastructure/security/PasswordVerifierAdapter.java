package com.escuelaing.usuarios.infrastructure.security;

import com.escuelaing.usuarios.domain.port.outbound.PasswordVerifierPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Verifica contraseñas de jurado contra el hash bcrypt guardado en
 * credenciales_jurado.password_hash (cargado manualmente por un admin).
 */
@Component
public class PasswordVerifierAdapter implements PasswordVerifierPort {

    private final PasswordEncoder passwordEncoder;

    public PasswordVerifierAdapter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean coincide(String passwordPlano, String hash) {
        if (passwordPlano == null || hash == null) {
            return false;
        }
        return passwordEncoder.matches(passwordPlano, hash);
    }
}
