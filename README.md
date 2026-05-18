# BFF — Backend For Frontend

Colegio Bernardo O'Higgins · Proyecto Libro de Clases Digital

El **BFF (Backend For Frontend)** actúa como intermediario entre la aplicación Angular y los microservicios internos. Centraliza CORS, valida el JWT en cada petición y reenvía las solicitudes al microservicio correspondiente usando un patrón **Proxy transparente** con `RestTemplate`.

---

## Arquitectura

```
Angular (4200)
    │
    ▼ HTTP + Bearer JWT
BFF (8080)  ──── valida JWT localmente ────────────────────────┐
    │                                                           │
    ├── /auth/**         → ms-auth (8082)                      │
    ├── /cuenta-acceso/** → ms-auth (8082)                     │
    ├── /refresh-token/** → ms-auth (8082)                     │
    │                                                           │
    ├── /usuario/**      → ms-usuario (8081)                   │
    ├── /docente/**      → ms-usuario (8081)                   │
    ├── /estudiante/**   → ms-usuario (8081)                   │
    ├── /apoderado/**    → ms-usuario (8081)                   │
    └── /usuario-rol/**  → ms-usuario (8081)                   │
                                                                │
         JWT Secret compartido: mismo en BFF y ms-auth ────────┘
```

---

## Requisitos previos

| Herramienta | Versión |
|---|---|
| Java JDK | 21 |
| Maven | 3.8 o superior |
| ms-auth | Corriendo en `http://localhost:8082` |
| ms-usuario | Corriendo en `http://localhost:8081` |

---

## Instalación y ejecución

```bash
# 1. Clonar el repositorio
git clone https://github.com/iscalles/bff-service.git
cd bff-service/bffService

# 2. Compilar el proyecto
mvn clean package -DskipTests

# 3. Ejecutar
mvn spring-boot:run
```
El servidor iniciará en `http://localhost:8080`.

---

## Configuración (`application.properties`)

```properties
server.port=8080

# URLs de los microservicios internos
ms-auth.url=http://localhost:8082
ms-usuario.url=http://localhost:8081

# Secret JWT — debe coincidir exactamente con el de ms-auth
jwt.secret=misecretatuysuperaguantadadesdeprotegidodelmundoentero
```

---

## Endpoints expuestos

### Rutas públicas (sin JWT)
| Método | Ruta | Descripción |
|---|---|---|
| POST | `/auth/login` | Autenticación con RUT y contraseña |
| POST | `/auth/refresh` | Renovación de access token |

### Rutas protegidas (requieren `Authorization: Bearer <token>`)
| Patrón | Proxy hacia |
|---|---|
| `/auth/**` | ms-auth |
| `/cuenta-acceso/**` | ms-auth |
| `/usuario/**` | ms-usuario |
| `/docente/**` | ms-usuario |
| `/estudiante/**` | ms-usuario |
| `/apoderado/**` | ms-usuario |
| `/administrativo/**` | ms-usuario |
| `/usuario-rol/**` | ms-usuario |
| `/apoderado-estudiante/**` | ms-usuario |

---

## Verificación de funcionamiento

```bash
# Login de prueba (respuesta esperada: JSON con accessToken)
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"rutUsuario":"11.111.111-1","password":"admin123"}'

# Verificar que una ruta protegida retorna 401 sin token
curl http://localhost:8080/usuario
# → 401 Unauthorized
```

---

## Patrón arquitectónico: BFF (Backend For Frontend)

El BFF resuelve tres problemas del patrón de microservicios:

1. **CORS centralizado**: Angular solo necesita confiar en un origen (`http://localhost:8080`)
2. **Validación JWT en el perímetro**: el JWT se valida una sola vez en el BFF antes de reenviar; los microservicios internos no necesitan seguridad propia
3. **Abstracción de la arquitectura interna**: el frontend no conoce los puertos ni URLs internas de los microservicios

---

## Tecnologías

- Spring Boot 3.2.12
- Java 21
- Spring Security + JWT (jjwt 0.12.6)
- RestTemplate (proxy transparente)
- Maven (arquetipo `spring-boot-starter-parent`)
