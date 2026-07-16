package com.escuelaing.usuarios.domain.port.outbound;

/**
 * Puerto de salida para verificar una contraseña en claro contra un hash
 * almacenado (bcrypt). Implementado en infraestructura para no acoplar el
 * dominio/aplicación a Spring Security.
 */
public interface PasswordVerifierPort {

    boolean coincide(String passwordPlano, String hash);
}
