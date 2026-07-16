package com.escuelaing.usuarios.domain.port.in;

import com.escuelaing.usuarios.domain.model.Usuario;

/**
 * Puerto de entrada (caso de uso) para el acceso de jurados externos:
 * autenticación por correo + contraseña contra credenciales precargadas
 * manualmente (sin OTP, sin Microsoft, sin restricción de dominio).
 */
public interface JuradoUseCase {

    /**
     * Autentica un jurado por correo + contraseña.
     * <p>
     * Si es el primer login exitoso, crea el Usuario (rol JURADO) y su
     * Perfil vacío, igual que cualquier otro usuario nuevo (mismo
     * onboarding), y enlaza la credencial con ese usuario.
     *
     * @throws com.escuelaing.usuarios.domain.exception.CredencialesInvalidasException
     *         si el correo no está registrado o la contraseña no coincide.
     */
    Usuario autenticar(String email, String passwordPlano);
}
