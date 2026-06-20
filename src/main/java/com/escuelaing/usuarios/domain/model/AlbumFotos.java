package com.escuelaing.usuarios.domain.model;

import com.escuelaing.usuarios.domain.exception.MaxFotosException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Agregado que representa el álbum de fotos ("monas") de un usuario.
 * Encapsula las reglas de negocio:
 * - Máximo 6 fotos por usuario.
 * - La foto con orden = 1 es la principal.
 * - Al eliminar la foto principal, se promueve automáticamente la siguiente.
 */
public class AlbumFotos {

    public static final int MAX_FOTOS = 6;

    private final UUID usuarioId;
    private final List<Foto> fotos;

    private AlbumFotos(UUID usuarioId, List<Foto> fotos) {
        this.usuarioId = usuarioId;
        this.fotos = new ArrayList<>(fotos);
        this.fotos.sort(Comparator.comparingInt(Foto::getOrden));
    }

    public static AlbumFotos de(UUID usuarioId, List<Foto> fotosExistentes) {
        return new AlbumFotos(usuarioId, fotosExistentes == null ? List.of() : fotosExistentes);
    }

    /**
     * Agrega una nueva foto al álbum. Si no existe ninguna foto, la nueva
     * se convierte automáticamente en principal (orden 1).
     */
    public Foto agregarFoto(String urlFoto) {
        if (fotos.size() >= MAX_FOTOS) {
            throw new MaxFotosException(MAX_FOTOS);
        }
        int siguienteOrden = fotos.isEmpty()
                ? 1
                : fotos.stream().mapToInt(Foto::getOrden).max().orElse(0) + 1;
        Foto nueva = Foto.crear(usuarioId, urlFoto, siguienteOrden);
        fotos.add(nueva);
        return nueva;
    }

    /**
     * Elimina una foto del álbum. Si la foto eliminada era la principal
     * (orden 1), promueve automáticamente la siguiente foto disponible
     * y reordena el resto de forma contigua.
     *
     * @return la foto que quedó como principal tras la operación, si existe.
     */
    public Optional<Foto> eliminarFoto(UUID fotoId) {
        Optional<Foto> aEliminar = fotos.stream()
                .filter(f -> f.getId().equals(fotoId))
                .findFirst();

        if (aEliminar.isEmpty()) {
            return Optional.empty();
        }

        boolean eraPrincipal = aEliminar.get().esPrincipal();
        fotos.remove(aEliminar.get());

        // Reordenar de forma contigua (1..N) preservando el orden relativo.
        fotos.sort(Comparator.comparingInt(Foto::getOrden));
        for (int i = 0; i < fotos.size(); i++) {
            fotos.get(i).cambiarOrden(i + 1);
        }

        if (eraPrincipal && !fotos.isEmpty()) {
            return Optional.of(fotos.get(0));
        }
        return Optional.empty();
    }

    public List<Foto> getFotos() {
        return List.copyOf(fotos);
    }

    public Optional<Foto> getFotoPrincipal() {
        return fotos.stream().filter(Foto::esPrincipal).findFirst();
    }

    public int size() {
        return fotos.size();
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }
}
