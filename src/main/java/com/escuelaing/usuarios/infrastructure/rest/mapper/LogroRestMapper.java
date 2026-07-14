package com.escuelaing.usuarios.infrastructure.rest.mapper;

import com.escuelaing.usuarios.domain.model.Logro;
import com.escuelaing.usuarios.domain.model.LogrosUsuario;
import com.escuelaing.usuarios.infrastructure.rest.dto.response.LogroResponse;
import com.escuelaing.usuarios.infrastructure.rest.dto.response.LogrosResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class LogroRestMapper {

    public LogrosResponse toResponse(LogrosUsuario logrosUsuario) {
        if (logrosUsuario == null) {
            return null;
        }
        return new LogrosResponse(
                logrosUsuario.usuarioId(),
                logrosUsuario.xpTotal(),
                logrosUsuario.logros().stream().map(this::toResponse).toList()
        );
    }

    public LogroResponse toResponse(Logro logro) {
        if (logro == null) {
            return null;
        }
        return new LogroResponse(
                logro.tipo().name(),
                logro.tipo().getNombre(),
                logro.tipo().getDescripcion(),
                logro.tipo().getXp(),
                logro.desbloqueado(),
                logro.fechaDesbloqueo()
        );
    }
}
