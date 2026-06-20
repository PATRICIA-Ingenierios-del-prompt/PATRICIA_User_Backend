package com.escuelaing.usuarios.domain.port.in;

import com.escuelaing.usuarios.domain.model.EstadoUsuario;
import com.escuelaing.usuarios.domain.model.RolPlataforma;
import com.escuelaing.usuarios.domain.model.Usuario;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Puerto de entrada (caso de uso) para las operaciones principales sobre
 * el agregado Usuario: alta idempotente, consulta y administración.
 */
public interface UsuarioUseCase {

    /**
     * Busca un usuario por email; si no existe, lo crea. Operación idempotente
     * requerida por el contrato interno de auth-service.
     *
     * @return el usuario (existente o recién creado) junto con un indicador
     *         de si fue efectivamente creado en esta invocación.
     */
    ResultadoFindOrCreate buscarOCrear(String email, String nombre, String microsoftId);

    Usuario buscarPorId(UUID id);

    Optional<Usuario> buscarPorEmail(String email);

    Usuario cambiarEstado(UUID id, EstadoUsuario nuevoEstado);

    Usuario actualizarRoles(UUID id, Set<RolPlataforma> roles);

    record ResultadoFindOrCreate(Usuario usuario, boolean creado) {}
}
