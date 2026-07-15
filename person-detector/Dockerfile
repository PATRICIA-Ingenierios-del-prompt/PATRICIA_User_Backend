# ── Stage 1: descarga de modelos de DeepFace en tiempo de build ──────────────
# Así el contenedor no necesita internet en runtime.
FROM python:3.11-slim AS model-downloader

RUN apt-get update && apt-get install -y --no-install-recommends \
    libgl1 libglib2.0-0 libsm6 libxrender1 libxext6 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Precarga los pesos de RetinaFace (se guardan en ~/.deepface/weights/)
# El script fuerza la descarga sin necesitar una imagen real.
RUN python - <<'EOF'
import os, urllib.request, pathlib

weights_dir = pathlib.Path.home() / ".deepface" / "weights"
weights_dir.mkdir(parents=True, exist_ok=True)

models = {
    "retinaface": [
        ("https://github.com/serengil/deepface_models/releases/download/v1.0/retinaface.h5",
         weights_dir / "retinaface.h5"),
    ],
}

for backend, files in models.items():
    for url, dest in files:
        if not dest.exists():
            print(f"Descargando {dest.name} …")
            urllib.request.urlretrieve(url, dest)
            print(f"  ✓ {dest.name}")
        else:
            print(f"  ✓ {dest.name} ya existe, se omite.")
EOF

# ── Stage 2: imagen final ─────────────────────────────────────────────────────
FROM python:3.11-slim

RUN apt-get update && apt-get install -y --no-install-recommends \
    libgl1 libglib2.0-0 libsm6 libxrender1 libxext6 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copiar dependencias instaladas y pesos del modelo
COPY --from=model-downloader /usr/local/lib/python3.11 /usr/local/lib/python3.11
COPY --from=model-downloader /usr/local/bin /usr/local/bin
COPY --from=model-downloader /root/.deepface /root/.deepface

COPY app/ ./app/

ENV PYTHONUNBUFFERED=1

EXPOSE 8090

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8090"]
