package com.escuelaing.usuarios.domain.port.in;

import com.escuelaing.usuarios.domain.model.Disponibilidad;
import com.escuelaing.usuarios.domain.model.Genero;
import com.escuelaing.usuarios.domain.model.Perfil;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Puerto de entrada (caso de uso) para la gestión del perfil de usuario.
 */
public interface PerfilUseCase {

    Perfil obtenerPerfil(UUID usuarioId);

    Perfil actualizarPerfil(UUID usuarioId, String bio, String carrera, Integer semestre,
                            List<String> intereses, Disponibilidad disponibilidad);

    /**
     * Completa el onboarding de un usuario por primera vez. Si se provee
     * `fotoDataUrl` (base64 data-URL), se sube a almacenamiento externo (S3)
     * y la URL resultante queda como urlFotoPerfil del perfil.
     */
    Perfil completarOnboarding(UUID usuarioId, String nombre, String apellidos, String carrera,
                               String segundaCarrera, Integer semestre, LocalDate fechaNacimiento,
                               Genero genero, String fotoDataUrl, List<String> intereses);

    Disponibilidad obtenerDisponibilidad(UUID usuarioId);

    List<String> obtenerIntereses(UUID usuarioId);

    List<String> actualizarIntereses(UUID usuarioId, List<String> intereses);
}
