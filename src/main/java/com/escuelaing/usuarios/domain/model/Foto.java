package com.escuelaing.usuarios.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Foto individual del álbum ("mona") de un usuario.
 */
public class Foto {

    private UUID id;
    private UUID usuarioId;
    private String urlFoto;
    private int orden;
    private Instant fechaSubida;

    protected Foto() {
        // Para reconstrucción desde infraestructura.
    }

    private Foto(UUID id, UUID usuarioId, String urlFoto, int orden, Instant fechaSubida) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.urlFoto = urlFoto;
        this.orden = orden;
        this.fechaSubida = fechaSubida;
    }

    public static Foto crear(UUID usuarioId, String urlFoto, int orden) {
        return new Foto(UUID.randomUUID(), usuarioId, urlFoto, orden, Instant.now());
    }

    public static Foto reconstruir(UUID id, UUID usuarioId, String urlFoto, int orden, Instant fechaSubida) {
        return new Foto(id, usuarioId, urlFoto, orden, fechaSubida);
    }

    void cambiarOrden(int nuevoOrden) {
        this.orden = nuevoOrden;
    }

    public void actualizarUrl(String nuevaUrl) {
        this.urlFoto = nuevaUrl;
    }

    public boolean esPrincipal() {
        return this.orden == 1;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public String getUrlFoto() {
        return urlFoto;
    }

    public int getOrden() {
        return orden;
    }

    public Instant getFechaSubida() {
        return fechaSubida;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Foto foto)) return false;
        return Objects.equals(id, foto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
