package com.escuelaing.usuarios.domain.port.outbound;

import com.escuelaing.usuarios.domain.model.CredencialJurado;

import java.util.Optional;

/**
 * Puerto de salida para la persistencia de credenciales de jurado.
 * Implementado por un adaptador en infrastructure.persistence.
 */
public interface CredencialJuradoRepositoryPort {

    Optional<CredencialJurado> buscarPorEmail(String email);

    CredencialJurado guardar(CredencialJurado credencial);
}
