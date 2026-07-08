package com.escuelaing.usuarios.domain.exception;

import java.util.UUID;

/**
 * Se lanza cuando se intenta completar el onboarding de un usuario que ya
 * lo había completado previamente. Debe traducirse a HTTP 409 en la capa
 * de presentación: el onboarding es una operación de una sola vez.
 */
public class OnboardingException extends DomainException {

    public OnboardingException(UUID usuarioId) {
        super("El usuario " + usuarioId + " ya completó el onboarding previamente");
    }
}
