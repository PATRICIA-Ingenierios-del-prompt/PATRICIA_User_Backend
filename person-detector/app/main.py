"""
PATRICIA – Person Detector (sidecar interno)
=============================================
Corre en localhost:8090 DENTRO del mismo contenedor que el usuarios-service Java.
No está expuesto hacia el exterior; solo el Java lo llama internamente.

El Java llama a este servicio y luego él mismo actualiza la BD y publica
el evento RabbitMQ — no hay callback desde Python.

Endpoints:
  POST /detect   – recibe URL de imagen, retorna si hay persona
  GET  /health   – health check
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
from PIL import Image
from pydantic import BaseModel

# ---------------------------------------------------------------------------
# Configuración
# ---------------------------------------------------------------------------

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s – %(message)s",
)
log = logging.getLogger("person-detector")

DETECTOR_BACKEND: str = os.getenv("DETECTOR_BACKEND", "retinaface")
CONFIDENCE_THRESHOLD: float = float(os.getenv("CONFIDENCE_THRESHOLD", "0.75"))

# ---------------------------------------------------------------------------
# App
# ---------------------------------------------------------------------------

app = FastAPI(
    title="PATRICIA – Person Detector (internal)",
    description="Sidecar interno. Solo accesible desde localhost.",
    version="1.0.0",
)

# ---------------------------------------------------------------------------
# Schemas
# ---------------------------------------------------------------------------


class DetectRequest(BaseModel):
    url_foto: str  # URL pública de S3 accesible desde internet


class DetectResponse(BaseModel):
    tiene_persona: bool
    confianza: float | None = None


# ---------------------------------------------------------------------------
# Lógica de detección
# ---------------------------------------------------------------------------


def descargar_imagen(url: str) -> np.ndarray:
    """Descarga una imagen desde una URL y la retorna como array numpy RGB."""
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
    Ejecuta DeepFace para detectar caras en la imagen.
    Retorna (tiene_persona, confianza_maxima).
    """
    with tempfile.NamedTemporaryFile(suffix=".jpg", delete=False) as tmp:
        tmp_path = tmp.name
        Image.fromarray(img_array).save(tmp_path, format="JPEG")

    try:
        faces = DeepFace.extract_faces(
            img_path=tmp_path,
            detector_backend=DETECTOR_BACKEND,
            enforce_detection=False,
            align=False,
        )

        if not faces:
            return False, None

        caras_validas = []
        for face in faces:
            confidence = face.get("confidence")
            if confidence is None:
                # Detectores sin score (opencv): validar por tamaño del área
                area = face.get("facial_area", {})
                if area.get("w", 0) > 20 and area.get("h", 0) > 20:
                    caras_validas.append(1.0)
            elif confidence >= CONFIDENCE_THRESHOLD:
                caras_validas.append(confidence)

        if not caras_validas:
            return False, None

        return True, round(max(caras_validas), 4)

    except ValueError:
        return False, None
    finally:
        Path(tmp_path).unlink(missing_ok=True)


# ---------------------------------------------------------------------------
# Endpoints
# ---------------------------------------------------------------------------


@app.get("/health", tags=["Admin"])
def health():
    return {"status": "ok", "detector": DETECTOR_BACKEND}


@app.post("/detect", response_model=DetectResponse, tags=["Detección"])
def detect(req: DetectRequest) -> DetectResponse:
    """
    Descarga la imagen desde `url_foto` y detecta si contiene una persona.
    El Java llama este endpoint y luego él mismo decide qué hacer con el resultado.
    """
    log.info("Analizando imagen: %s", req.url_foto)
    img = descargar_imagen(req.url_foto)
    tiene_persona, confianza = detectar_persona(img)
    log.info("Resultado: tiene_persona=%s confianza=%s", tiene_persona, confianza)
    return DetectResponse(tiene_persona=tiene_persona, confianza=confianza)
