# PATRICIA – Person Detector

Microservicio Python que detecta si una foto contiene una persona real
usando **DeepFace** con el detector **RetinaFace** (red neuronal pre-entrenada).

## ¿Cómo funciona?

```
  Frontend / usuarios-service
        │
        │  POST /detect/foto-perfil  { usuario_id, url_foto }
        │  POST /detect/foto-album   { usuario_id, foto_id, url_foto }
        ▼
  person-detector  (puerto 8090)
        │
        ├─ Descarga la imagen desde la URL (S3 u otra URL pública)
        ├─ Ejecuta DeepFace.extract_faces() con RetinaFace
        ├─ Si detecta cara con confianza ≥ CONFIDENCE_THRESHOLD:
        │     PUT /api/v1/usuarios/{id}/foto/persona          (foto de perfil)
        │     PUT /api/v1/usuarios/{id}/fotos/{fotoId}/persona (foto de álbum)
        │     → usando X-Internal-Api-Key para autenticarse
        └─ Devuelve { tiene_persona, confianza, mensaje }
```

## Endpoints

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET`  | `/health` | Health check |
| `POST` | `/detect/foto-perfil` | Verifica foto de perfil |
| `POST` | `/detect/foto-album`  | Verifica foto del álbum |

Swagger UI disponible en `http://localhost:8090/docs`.

### Ejemplo – foto de perfil
```json
POST /detect/foto-perfil
{
  "usuario_id": "550e8400-e29b-41d4-a716-446655440000",
  "url_foto": "https://mi-bucket.s3.amazonaws.com/fotos/perfil/abc123.jpg"
}
```
Respuesta:
```json
{
  "tiene_persona": true,
  "confianza": 0.9821,
  "mensaje": "Persona detectada en foto de perfil."
}
```

### Ejemplo – foto de álbum
```json
POST /detect/foto-album
{
  "usuario_id": "550e8400-e29b-41d4-a716-446655440000",
  "foto_id":    "7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "url_foto":   "https://mi-bucket.s3.amazonaws.com/fotos/album/xyz456.jpg"
}
```

## Variables de entorno

| Variable | Default | Descripción |
|----------|---------|-------------|
| `USUARIOS_SERVICE_URL` | `http://localhost:8082` | URL interna del usuarios-service |
| `INTERNAL_API_KEY` | *(vacío)* | API Key compartida con el backend Java |
| `DETECTOR_BACKEND` | `retinaface` | Motor de detección de DeepFace |
| `CONFIDENCE_THRESHOLD` | `0.75` | Confianza mínima para confirmar persona |

### Detectores disponibles

| Backend | Velocidad | Precisión | Notas |
|---------|-----------|-----------|-------|
| `retinaface` | Lento | ★★★★★ | **Recomendado para producción** |
| `mtcnn` | Medio | ★★★★☆ | Buena alternativa |
| `opencv` | Rápido | ★★★☆☆ | Para pruebas / hardware limitado |
| `ssd` | Muy rápido | ★★★☆☆ | Para pruebas / hardware limitado |

## Correr en local (sin Docker)

```bash
cd person-detector
pip install -r requirements.txt
INTERNAL_API_KEY=mi-clave USUARIOS_SERVICE_URL=http://localhost:8082 \
  uvicorn app.main:app --host 0.0.0.0 --port 8090 --reload
```

## Correr con Docker Compose

Desde la raíz del proyecto:

```bash
docker compose up --build person-detector
```

El build descarga automáticamente los pesos de RetinaFace (~100 MB)
para que el contenedor funcione sin acceso a internet en runtime.
