CREATE TABLE fotos (
    id            UUID PRIMARY KEY,
    usuario_id    UUID NOT NULL,
    url_foto      VARCHAR(1000) NOT NULL,
    orden         INTEGER NOT NULL,
    fecha_subida  TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_fotos_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE,
    CONSTRAINT ck_fotos_orden CHECK (orden BETWEEN 1 AND 6),
    -- DEFERRABLE: al reordenar el álbum tras eliminar una foto (ver
    -- AlbumFotos.eliminarFoto / FotoRepositoryAdapter.guardarTodas), varias
    -- filas pueden cruzar temporalmente sus valores de "orden" dentro de la
    -- misma transacción. Se difiere la validación al COMMIT para evitar
    -- violaciones espurias de la unicidad durante el reordenamiento.
    CONSTRAINT uq_fotos_usuario_orden UNIQUE (usuario_id, orden) DEFERRABLE INITIALLY DEFERRED
);

CREATE INDEX idx_fotos_usuario_id ON fotos (usuario_id);
