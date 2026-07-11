**PATRICIA**
# Users Microservice

Microservicio responsable de la información de usuarios, perfiles, álbum de
fotos ("monas"), roles y eventos relacionados con usuarios dentro de la
plataforma universitaria PATRICIA.

## Arquitectura

Este servicio sigue **arquitectura hexagonal (puertos y adaptadores)**:

```
com.escuelaing.usuarios
├── domain                     # Núcleo de negocio. Sin dependencias de framework.
│   ├── model                  # Entidades y value objects (Usuario, Perfil, Foto, AlbumFotos, Interes...)
│   ├── exception               # Excepciones de negocio (InteresInvalidoException, MaxFotosException...)
│   └── port
│       ├── in                  # Casos de uso (UsuarioUseCase, PerfilUseCase, AlbumUseCase...)
│       └── out                 # Puertos hacia infraestructura (RepositoryPort, EventPublisherPort...)
│
├── application
│   └── service                 # Implementación de los casos de uso (orquestación, sin lógica de framework)
│
└── infrastructure              # Adaptadores: todo lo que conoce Spring/JPA/RabbitMQ/HTTP
    ├── persistence              # Entidades JPA, repositorios Spring Data, adaptadores de los RepositoryPort
    ├── messaging                 # Configuración RabbitMQ, publisher, consumers
    ├── rest                       # Controllers, DTOs, mappers REST, manejo global de excepciones
    ├── security                   # JWT filter, Internal API Key filter
    ├── client                     # Cliente Feign hacia auth-service
    └── config                     # Configuración de Spring (Security, Feign, Cache, OpenAPI)
```

**Regla de dependencia**: `domain` no importa nada de `infrastructure` ni de
Spring/JPA/AMQP. `application` solo depende de `domain`. `infrastructure`
depende de `domain` y `application`, nunca al revés.

## Stack

- Java 21 / Spring Boot 3.3.5
- Spring Data JPA + PostgreSQL + Flyway
- Spring AMQP + RabbitMQ (exchange `patricia.usuarios`)
- Spring Cloud OpenFeign (cliente hacia `auth-service`)
- Redis (caché opcional de perfiles)
- Lombok + MapStruct
- springdoc-openapi 2.6.0
- spring-dotenv

## Cómo correr en local

1. Copiar `.env.example` a `.env` y completar los valores (especialmente
   `JWT_SECRET`, que debe coincidir con el de `auth-service`).

2. Levantar la infraestructura:

   ```bash
   docker compose up -d postgres rabbitmq redis
   ```

3. Ejecutar la aplicación:

   ```bash
   mvn spring-boot:run
   ```

   O, para levantar todo (incluido el propio servicio) vía Docker:

   ```bash
   docker compose up --build
   ```

4. La API queda disponible en `http://localhost:8082`. Swagger UI en
   `http://localhost:8082/swagger-ui.html`.

## Tests

```bash
mvn test
```

Incluye tests unitarios (Mockito) sobre los casos de uso de `application.service`
y un test de slice de seguridad (`@WebMvcTest`) que verifica el rechazo de
peticiones internas sin `X-Internal-Api-Key`.

## Contratos internos (NO CAMBIAR)

- `POST /internal/usuarios/find-or-create` — usado por `auth-service`, idempotente.
- `GET /internal/usuarios/{id}` — usado por `auth-service`.
- Record `UsuarioResponse(id, email, nombre, roles, estado)`.

Ambos protegidos exclusivamente por el header `X-Internal-Api-Key`
(sin JWT) — ver `InternalApiKeyFilter` y `SecurityConfig`.

## Eventos RabbitMQ

Exchange propio: `patricia.usuarios` (topic, durable). Ver
`RabbitMqConfig` para el detalle de routing keys publicadas y colas
consumidas desde `patricia.auth`, `patricia.parches` y `patricia.sesiones`
(estos tres exchanges externos **no se declaran** en este servicio).
