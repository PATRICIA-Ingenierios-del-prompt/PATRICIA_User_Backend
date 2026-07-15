"""
PATRICIA – Person Detector Service
===================================
Microservicio Python que recibe la URL de una foto, descarga la imagen,
usa DeepFace (RetinaFace) para detectar si contiene una persona/cara, y
notifica al backend Java (usuarios-service) mediante su internal API.

Endpoints:
  POST /detect/foto-perfil   – verifica foto de perfil de un usuario
  POST /detect/foto-album    – verifica una foto del álbum de un usuario
  GET  /health               – health check
"""

from __future__ import annotations

import io
import logging
import os
import tempfile
from pathlib import Path

import httpx
import numpy as np
from deepface import DeepFace
from fastapi import FastAPI, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from PIL import Image
from pydantic import BaseModel, HttpUrl

# ---------------------------------------------------------------------------
# Configuración
# ---------------------------------------------------------------------------

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s – %(message)s",
)
log = logging.getLogger("person-detector")

# URL base del usuarios-service (ej. http://usuarios-service:8082)
USUARIOS_SERVICE_URL: str = os.getenv("USUARIOS_SERVICE_URL", "http://localhost:8082")

# API Key interna compartida con el backend Java
INTERNAL_API_KEY: str = os.getenv("INTERNAL_API_KEY", "")

# Detector a usar por DeepFace. RetinaFace = mejor precisión, un poco más lento.
# Alternativas: "opencv", "mtcnn", "ssd", "dlib"
DETECTOR_BACKEND: str = os.getenv("DETECTOR_BACKEND", "retinaface")

# Umbral de confianza mínima (0.0 – 1.0) para considerar que hay una persona.
# Aplica sólo cuando el detector devuelve score de confianza.
CONFIDENCE_THRESHOLD: float = float(os.getenv("CONFIDENCE_THRESHOLD", "0.75"))

# ---------------------------------------------------------------------------
# App
# ---------------------------------------------------------------------------

app = FastAPI(
    title="PATRICIA – Person Detector",
    description="Detecta si una imagen contiene una persona usando DeepFace.",
    version="1.0.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

# ---------------------------------------------------------------------------
# Schemas de request/response
# ---------------------------------------------------------------------------


class FotoPerfilDetectRequest(BaseModel):
    usuario_id: str
    url_foto: str  # URL pública de S3 (o cualquier URL accesible)


class FotoAlbumDetectRequest(BaseModel):
    usuario_id: str
    foto_id: str
    url_foto: str


class DetectionResult(BaseModel):
    tiene_persona: bool
    confianza: float | None = None
    mensaje: str


# ---------------------------------------------------------------------------
# Lógica de detección
# ---------------------------------------------------------------------------


def descargar_imagen(url: str) -> np.ndarray:
    """Descarga una imagen desde una URL y la devuelve como array numpy RGB."""
    with httpx.Client(timeout=20.0, follow_redirects=True) as client:
        resp = client.get(url)
        if resp.status_code != 200:
            raise HTTPException(
                status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
                detail=f"No se pudo descargar la imagen (HTTP {resp.status_code}): {url}",
            )
        content_type = resp.headers.get("content-type", "")
        if not content_type.startswith("image/"):
            raise HTTPException(
                status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
                detail=f"La URL no apunta a una imagen (content-type: {content_type})",
            )

    img = Image.open(io.BytesIO(resp.content)).convert("RGB")
    return np.array(img)


def detectar_persona(img_array: np.ndarray) -> tuple[bool, float | None]:
    """
    Ejecuta DeepFace para detectar caras/personas en la imagen.

    Retorna (tiene_persona: bool, confianza: float | None).
    DeepFace.extract_faces lanza ValueError si no encuentra ninguna cara,
    lo cual interpretamos como "no hay persona".
    """
    with tempfile.NamedTemporaryFile(suffix=".jpg", delete=False) as tmp:
        tmp_path = tmp.name
        Image.fromarray(img_array).save(tmp_path, format="JPEG")

    try:
        faces = DeepFace.extract_faces(
            img_path=tmp_path,
            detector_backend=DETECTOR_BACKEND,
            enforce_detection=False,   # No lanza excepción si no hay cara
            align=False,
        )

        if not faces:
            return False, None

        # Filtrar por confianza si el detector la proporciona
        caras_validas = []
        for face in faces:
            confidence = face.get("confidence")
            if confidence is None:
                # Detector sin score de confianza (ej. opencv): acepta si facial_area es razonable
                area = face.get("facial_area", {})
                w = area.get("w", 0)
                h = area.get("h", 0)
                if w > 20 and h > 20:
                    caras_validas.append((face, 1.0))
            else:
                if confidence >= CONFIDENCE_THRESHOLD:
                    caras_validas.append((face, confidence))

        if not caras_validas:
            return False, None

        mejor_confianza = max(c for _, c in caras_validas)
        return True, round(mejor_confianza, 4)

    except ValueError:
        # enforce_detection=False no debería lanzar, pero por si acaso
        return False, None
    finally:
        Path(tmp_path).unlink(missing_ok=True)


def _internal_headers() -> dict[str, str]:
    return {"X-Internal-Api-Key": INTERNAL_API_KEY}


def notificar_persona_en_foto_perfil(usuario_id: str) -> None:
    """Llama a PUT /api/v1/usuarios/{id}/foto/persona en el backend Java."""
    url = f"{USUARIOS_SERVICE_URL}/api/v1/usuarios/{usuario_id}/foto/persona"
    with httpx.Client(timeout=10.0) as client:
        resp = client.put(url, headers=_internal_headers())
    if resp.status_code not in (200, 204):
        log.warning(
            "Backend no aceptó marcar persona en foto de perfil "
            "(usuario=%s, status=%s): %s",
            usuario_id,
            resp.status_code,
            resp.text[:200],
        )


def notificar_persona_en_foto_album(usuario_id: str, foto_id: str) -> None:
    """Llama a PUT /api/v1/usuarios/{id}/fotos/{fotoId}/persona en el backend Java."""
    url = f"{USUARIOS_SERVICE_URL}/api/v1/usuarios/{usuario_id}/fotos/{foto_id}/persona"
    with httpx.Client(timeout=10.0) as client:
        resp = client.put(url, headers=_internal_headers())
    if resp.status_code not in (200, 204):
        log.warning(
            "Backend no aceptó marcar persona en foto de álbum "
            "(usuario=%s, foto=%s, status=%s): %s",
            usuario_id,
            foto_id,
            resp.status_code,
            resp.text[:200],
        )


# ---------------------------------------------------------------------------
# Endpoints
# ---------------------------------------------------------------------------


@app.get("/health", tags=["Admin"])
def health():
    return {"status": "ok", "detector": DETECTOR_BACKEND}


@app.post(
    "/detect/foto-perfil",
    response_model=DetectionResult,
    tags=["Detección"],
    summary="Detecta si la foto de perfil contiene una persona",
)
def detect_foto_perfil(req: FotoPerfilDetectRequest) -> DetectionResult:
    """
    1. Descarga la imagen desde `url_foto`.
    2. Usa DeepFace + RetinaFace para detectar si hay una persona.
    3. Si hay persona, notifica al usuarios-service via PUT /foto/persona.
    4. Devuelve el resultado de la detección.
    """
    log.info("Detectando persona en foto de perfil – usuario=%s", req.usuario_id)
    img = descargar_imagen(req.url_foto)
    tiene_persona, confianza = detectar_persona(img)

    log.info(
        "Resultado foto de perfil – usuario=%s tiene_persona=%s confianza=%s",
        req.usuario_id,
        tiene_persona,
        confianza,
    )

    if tiene_persona:
        try:
            notificar_persona_en_foto_perfil(req.usuario_id)
        except Exception as exc:
            log.error("Error notificando backend: %s", exc)

    return DetectionResult(
        tiene_persona=tiene_persona,
        confianza=confianza,
        mensaje="Persona detectada en foto de perfil." if tiene_persona
                else "No se detectó ninguna persona en la foto de perfil.",
    )


@app.post(
    "/detect/foto-album",
    response_model=DetectionResult,
    tags=["Detección"],
    summary="Detecta si una foto del álbum contiene una persona",
)
def detect_foto_album(req: FotoAlbumDetectRequest) -> DetectionResult:
    """
    1. Descarga la imagen desde `url_foto`.
    2. Usa DeepFace + RetinaFace para detectar si hay una persona.
    3. Si hay persona, notifica al usuarios-service via PUT /fotos/{fotoId}/persona.
    4. Devuelve el resultado de la detección.
    """
    log.info(
        "Detectando persona en foto de álbum – usuario=%s foto=%s",
        req.usuario_id,
        req.foto_id,
    )
    img = descargar_imagen(req.url_foto)
    tiene_persona, confianza = detectar_persona(img)

    log.info(
        "Resultado foto de álbum – usuario=%s foto=%s tiene_persona=%s confianza=%s",
        req.usuario_id,
        req.foto_id,
        tiene_persona,
        confianza,
    )

    if tiene_persona:
        try:
            notificar_persona_en_foto_album(req.usuario_id, req.foto_id)
        except Exception as exc:
            log.error("Error notificando backend: %s", exc)

    return DetectionResult(
        tiene_persona=tiene_persona,
        confianza=confianza,
        mensaje="Persona detectada en foto de álbum." if tiene_persona
                else "No se detectó ninguna persona en la foto de álbum.",
    )
