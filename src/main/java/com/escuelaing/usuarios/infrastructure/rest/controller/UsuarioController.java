package com.escuelaing.usuarios.infrastructure.rest.controller;

import com.escuelaing.usuarios.domain.model.Usuario;
import com.escuelaing.usuarios.domain.port.in.UsuarioUseCase;
import com.escuelaing.usuarios.infrastructure.rest.dto.request.ActualizarRolesRequest;
import com.escuelaing.usuarios.infrastructure.rest.dto.response.UsuarioResponse;
import com.escuelaing.usuarios.infrastructure.rest.mapper.UsuarioRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Endpoints públicos (JWT requerido) para la administración de usuarios.
 */
@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuarios", description = "Administración de usuarios")
public class UsuarioController {

    private final UsuarioUseCase usuarioUseCase;
    private final UsuarioRestMapper mapper;

    public UsuarioController(UsuarioUseCase usuarioUseCase, UsuarioRestMapper mapper) {
        this.usuarioUseCase = usuarioUseCase;
        this.mapper = mapper;
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Operation(summary = "Actualiza los roles de un usuario (solo ADMIN/MODERATOR)")
    public ResponseEntity<UsuarioResponse> actualizarRoles(@PathVariable UUID id,
                                                            @Valid @RequestBody ActualizarRolesRequest request) {
        Usuario actualizado = usuarioUseCase.actualizarRoles(id, request.roles());
        return ResponseEntity.ok(mapper.toResponse(actualizado));
    }

    @DeleteMapping("/{id}/cuenta")
    @Operation(summary = "Solicita el cierre permanente de la cuenta. "
            + "Se aplica un período de gracia de 24 h antes del borrado definitivo.")
    public ResponseEntity<UsuarioResponse> cerrarCuenta(@PathVariable UUID id) {
        Usuario actualizado = usuarioUseCase.cerrarCuenta(id);
        return ResponseEntity.ok(mapper.toResponse(actualizado));
    }

    @DeleteMapping("/{id}/cuenta/cancelar")
    @Operation(summary = "Cancela una solicitud de cierre de cuenta pendiente "
            + "dentro del período de gracia de 24 h.")
    public ResponseEntity<UsuarioResponse> cancelarCierreCuenta(@PathVariable UUID id) {
        Usuario actualizado = usuarioUseCase.cancelarCierreCuenta(id);
        return ResponseEntity.ok(mapper.toResponse(actualizado));
    }
}
