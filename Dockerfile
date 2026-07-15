# ═══════════════════════════════════════════════════════════════════════════════
# Stage 1 – Build Java (Maven)
# ═══════════════════════════════════════════════════════════════════════════════
FROM maven:3.9-eclipse-temurin-21 AS java-build
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests

# ═══════════════════════════════════════════════════════════════════════════════
# Stage 2 – Descarga de pesos DeepFace (RetinaFace) en build-time
#           Así el contenedor final no necesita internet en runtime.
# ═══════════════════════════════════════════════════════════════════════════════
FROM python:3.11-slim AS model-downloader

RUN apt-get update && apt-get install -y --no-install-recommends \
        libgl1 libglib2.0-0 libsm6 libxrender1 libxext6 \
    && rm -rf /var/lib/apt/lists/*

COPY person-detector/requirements.txt /tmp/requirements.txt
RUN pip install --no-cache-dir -r /tmp/requirements.txt

# Precarga los pesos de RetinaFace (~100 MB) en ~/.deepface/weights/
RUN python - <<'EOF'
import pathlib, urllib.request

weights_dir = pathlib.Path.home() / ".deepface" / "weights"
weights_dir.mkdir(parents=True, exist_ok=True)

files = [
    (
        "https://github.com/serengil/deepface_models/releases/download/v1.0/retinaface.h5",
        weights_dir / "retinaface.h5",
    ),
]
for url, dest in files:
    if not dest.exists():
        print(f"Descargando {dest.name}...")
        urllib.request.urlretrieve(url, dest)
        print(f"  ✓ {dest.name}")
    else:
        print(f"  ✓ {dest.name} ya existe, se omite.")
EOF

# ═══════════════════════════════════════════════════════════════════════════════
# Stage 3 – Imagen final: Java (JRE) + Python + supervisord
# ═══════════════════════════════════════════════════════════════════════════════
FROM eclipse-temurin:21-jre-jammy

# Instalar Python 3.11, pip, supervisord y librerías de visión
RUN apt-get update && apt-get install -y --no-install-recommends \
        python3.11 python3-pip python3.11-distutils \
        supervisor \
        libgl1 libglib2.0-0 libsm6 libxrender1 libxext6 \
    && rm -rf /var/lib/apt/lists/* \
    && ln -sf /usr/bin/python3.11 /usr/bin/python3 \
    && ln -sf /usr/bin/python3    /usr/bin/python

WORKDIR /app

# ── Python: copiar paquetes instalados y pesos del modelo ────────────────────
COPY --from=model-downloader /usr/local/lib/python3.11 /usr/local/lib/python3.11
COPY --from=model-downloader /usr/local/bin            /usr/local/bin
COPY --from=model-downloader /root/.deepface           /root/.deepface

# ── Python: código del detector ──────────────────────────────────────────────
COPY person-detector/app ./person-detector/app

# ── Java: jar compilado ───────────────────────────────────────────────────────
COPY --from=java-build /app/target/usuarios-service-*.jar app.jar

# ── supervisord: arranca ambos procesos ──────────────────────────────────────
COPY supervisord.conf /etc/supervisor/conf.d/app.conf

EXPOSE 8082
# Puerto 8090 (detector) es interno; no se expone hacia el host.

CMD ["/usr/bin/supervisord", "-n", "-c", "/etc/supervisor/conf.d/app.conf"]
