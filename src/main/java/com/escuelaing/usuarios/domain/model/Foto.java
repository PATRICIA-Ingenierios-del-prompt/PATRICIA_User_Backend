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
    /**
     * Indica si se detectó una persona en la foto. Se activa al recibir la
     * confirmación de análisis y dispara el evento album.foto.persona.detectada.
     */
    private boolean tienePersonaEnFoto;

    protected Foto() {
        // Para reconstrucción desde infraestructura.
    }

    private Foto(UUID id, UUID usuarioId, String urlFoto, int orden, Instant fechaSubida,
                  boolean tienePersonaEnFoto) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.urlFoto = urlFoto;
        this.orden = orden;
        this.fechaSubida = fechaSubida;
        this.tienePersonaEnFoto = tienePersonaEnFoto;
    }

    public static Foto crear(UUID usuarioId, String urlFoto, int orden) {
        return new Foto(UUID.randomUUID(), usuarioId, urlFoto, orden, Instant.now(), false);
    }

    public static Foto reconstruir(UUID id, UUID usuarioId, String urlFoto, int orden,
                                    Instant fechaSubida) {
        return new Foto(id, usuarioId, urlFoto, orden, fechaSubida, false);
    }

    public static Foto reconstruir(UUID id, UUID usuarioId, String urlFoto, int orden,
                                    Instant fechaSubida, boolean tienePersonaEnFoto) {
        return new Foto(id, usuarioId, urlFoto, orden, fechaSubida, tienePersonaEnFoto);
    }

    void cambiarOrden(int nuevoOrden) {
        this.orden = nuevoOrden;
    }

    public void actualizarUrl(String nuevaUrl) {
        this.urlFoto = nuevaUrl;
    }

    /**
     * Marca la foto como "con persona detectada". Retorna true si el campo
     * cambió (era false), false si ya estaba activo (idempotente).
     */
    public boolean marcarPersonaDetectada() {
        if (this.tienePersonaEnFoto) {
            return false;
        }
        this.tienePersonaEnFoto = true;
        return true;
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

    public boolean isTienePersonaEnFoto() {
        return tienePersonaEnFoto;
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
