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
# Stage 3 – Imagen final: python:3.11-slim (MISMA base que el stage 2) + JRE.
#
# Antes la base era eclipse-temurin (Ubuntu) + Python 3.11 de deadsnakes, y se
# copiaban encima los paquetes pip del stage 2 (Debian). Dos Pythons distintos:
# el de Ubuntu busca en dist-packages y NUNCA ve el site-packages copiado →
# "ModuleNotFoundError: No module named 'uvicorn'" y person-detector en FATAL.
#
# Ahora el runtime ES python:3.11-slim (idéntico al builder, el COPY de
# site-packages es seguro por construcción) y el JRE de Temurin se copia como
# directorio autocontenido — patrón estándar, solo necesita glibc.
# ═══════════════════════════════════════════════════════════════════════════════
FROM python:3.11-slim

ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="${JAVA_HOME}/bin:${PATH}"
COPY --from=eclipse-temurin:21-jre-jammy /opt/java/openjdk /opt/java/openjdk

RUN apt-get update && apt-get install -y --no-install-recommends \
        supervisor \
        libgl1 libglib2.0-0 libsm6 libxrender1 libxext6 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# ── Python: copiar paquetes instalados y pesos del modelo ────────────────────
# Misma imagen base que el stage 2 → mismo layout /usr/local, copia 1:1 segura.
COPY --from=model-downloader /usr/local/lib/python3.11 /usr/local/lib/python3.11
COPY --from=model-downloader /usr/local/bin            /usr/local/bin
COPY --from=model-downloader /root/.deepface           /root/.deepface

# Fail-fast en build: si el JRE o uvicorn no funcionan, que reviente AQUÍ y no
# en el arranque del pod.
RUN java -version && python -m uvicorn --version

# ── Python: código del detector ──────────────────────────────────────────────
COPY person-detector/app ./person-detector/app

# ── Java: jar compilado ───────────────────────────────────────────────────────
COPY --from=java-build /app/target/usuarios-service-*.jar app.jar

# ── supervisord: arranca ambos procesos ──────────────────────────────────────
COPY supervisord.conf /etc/supervisor/conf.d/app.conf

EXPOSE 8082
# Puerto 8090 (detector) es interno; no se expone hacia el host.

CMD ["/usr/bin/supervisord", "-n", "-c", "/etc/supervisor/conf.d/app.conf"]
