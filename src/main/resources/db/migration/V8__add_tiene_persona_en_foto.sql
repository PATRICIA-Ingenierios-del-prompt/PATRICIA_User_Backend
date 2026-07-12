-- Agrega el atributo booleano que indica si se detectó una persona en la foto.
-- El valor por defecto es false; se activa cuando el microservicio de análisis
-- confirma la detección y publica el evento album.foto.persona.detectada.

ALTER TABLE fotos
    ADD COLUMN tiene_persona_en_foto BOOLEAN NOT NULL DEFAULT FALSE;
