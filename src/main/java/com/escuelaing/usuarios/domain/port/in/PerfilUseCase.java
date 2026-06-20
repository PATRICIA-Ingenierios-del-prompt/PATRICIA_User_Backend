package com.escuelaing.usuarios.domain.port.in;

import com.escuelaing.usuarios.domain.model.Disponibilidad;
import com.escuelaing.usuarios.domain.model.Perfil;

import java.util.List;
import java.util.UUID;

/**
 * Puerto de entrada (caso de uso) para la gestión del perfil de usuario.
 */
public interface PerfilUseCase {

    Perfil obtenerPerfil(UUID usuarioId);

    Perfil actualizarPerfil(UUID usuarioId, String bio, String carrera, Integer semestre,
                             List<String> intereses, Disponibilidad disponibilidad);

    Disponibilidad obtenerDisponibilidad(UUID usuarioId);

    List<String> obtenerIntereses(UUID usuarioId);

    List<String> actualizarIntereses(UUID usuarioId, List<String> intereses);
}
