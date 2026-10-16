# cliente-service

Microservicio REST de gestión de clientes construido con **Spring Boot 3.5**, **Java 21** y **Spring Data JPA**. Organizado en capas (`domain` / `delivery`) con manejo centralizado de excepciones.

## Requisitos

- Java 21
- No necesitas instalar Maven (se usa el wrapper `./mvnw`)
- Docker solo si quieres usar PostgreSQL real (opcional)

## Ejecutar

```bash
./mvnw spring-boot:run
```

La API queda en `http://localhost:8080` y usa **H2 en memoria** (modo PostgreSQL), así que no necesita ninguna base de datos externa. Los datos de ejemplo se cargan desde `src/main/resources/import.sql`.

### Con PostgreSQL (opcional)

```bash
docker compose up -d
./mvnw spring-boot:run -Dspring.profiles.active=postgres
```

La configuración está en `src/main/resources/application-postgres.properties`.

### Tests

```bash
./mvnw test
```

Ejecuta 36 tests en tres niveles de la pirámide de testing y genera el reporte de cobertura en `target/site/jacoco/index.html`.

| Clase | Nivel | Herramienta | Tests |
|---|---|---|---|
| `ClienteServiceImplTest` | Unitario | Mockito | 11 |
| `ClienteRestControllerTest` | Integración (web slice) | `@WebMvcTest` + MockMvc | 11 |
| `IClienteDaoTest` | Integración (data slice) | `@DataJpaTest` + H2 | 6 |
| `ClienteIntegrationTest` | E2E | Testcontainers + PostgreSQL | 7 |
| `ClienteServiceApplicationTests` | Contexto | `@SpringBootTest` | 1 |

Los 7 tests E2E requieren Docker; si no está disponible se omiten (`disabledWithoutDocker`). Para ejecutar una sola clase: `./mvnw test -Dtest=ClienteServiceImplTest`.

## Estructura del proyecto

```
src/main/java/com/denkitronik/clienteservice/
├── ClienteServiceApplication.java
├── domain/                          # Lógica de negocio
│   ├── entities/                    # Cliente, Region
│   ├── exception/                   # ClienteNotFoundException, ClienteServiceException
│   ├── repositories/                # IClienteDao
│   └── services/                    # IClienteService, ClienteServiceImpl
└── delivery/                        # Capa de entrada (HTTP)
    ├── rest/                        # ClienteRestController
    └── exception/                   # GlobalExceptionHandler, ApiErrorResponse
```

`delivery` depende de `domain`, nunca al revés.

## Endpoints

Prefijo base: `/api/v1/cliente-service`

| Método | Ruta | Respuesta exitosa | Error |
|---|---|---|---|
| GET | `/clientes` | 200 lista de clientes | |
| GET | `/clientes/page/{page}` | 200 página de 4 clientes | |
| GET | `/clientes/{id}` | 200 cliente | 404 |
| POST | `/clientes` | 201 cliente creado | 400 validación |
| PUT | `/clientes/{id}` | 201 cliente actualizado | 400 / 404 |
| DELETE | `/clientes/{id}` | 204 sin body | 404 |
| GET | `/clientes/regiones` | 200 lista de regiones | |

Hay peticiones listas para probar en `request.http` y `src/test/http/`.

## Manejo de errores

`GlobalExceptionHandler` (`@RestControllerAdvice`) traduce las excepciones a HTTP:

| Excepción | HTTP | Body |
|---|---|---|
| `ClienteNotFoundException` | 404 | `ApiErrorResponse` |
| `ClienteServiceException` | 500 | `ApiErrorResponse` genérico |
| `MethodArgumentNotValidException` | 400 | mapa campo → mensaje |
| `Exception` | 500 | `ApiErrorResponse` genérico |

Ejemplo de 404:

```json
{
  "status": 404,
  "error": "Not Found",
  "mensaje": "Cliente con id 999 no encontrado",
  "timestamp": "2025-04-20T14:30:00.123"
}
```

Los errores 500 nunca exponen el stacktrace. Los logs usan SLF4J: `WARN` para 404, `ERROR` para 500 y `DEBUG` para validaciones.

## Tecnologías

Spring Boot Web · Spring Data JPA · Bean Validation · Actuator (puerto 8081) · H2 / PostgreSQL · Lombok · JUnit 5 · Mockito · Testcontainers · JaCoCo
