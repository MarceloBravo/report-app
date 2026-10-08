# Módulo de Autenticación

## Finalidad

Este módulo centraliza la autenticación, autorización y gestión de sesiones del sistema. Su responsabilidad es:

- autenticar usuarios mediante email y contraseña;
- emitir y validar tokens JWT;
- gestionar la renovación del refresh token;
- cerrar sesiones y limpiar cookies de navegador;
- registrar usuarios desde el flujo público y desde administración;
- exponer información del usuario autenticado al resto de la aplicación;
- integrar la capa de seguridad con Spring Security.

El módulo se encuentra bajo el paquete `com.saas.report.auth` y expone la API REST bajo el prefijo `/auth`.

---

## Estructura del módulo

```text
src/main/java/com/saas/report/auth/
├── application/
│   ├── dto/
│   │   ├── LoginRequest.java
│   │   ├── RefreshTokenRequest.java
│   │   ├── RegisterRequest.java
│   │   ├── SessionResponse.java
│   │   └── TokenResponse.java
│   ├── usecase/
│   │   ├── LoginUseCase.java
│   │   ├── LogoutUseCase.java
│   │   ├── RefreshTokenUseCase.java
│   │   └── RegisterUseCase.java
│   ├── AuthCookieService.java
│   ├── AuthPrincipal.java
│   ├── AuthTokenIssuer.java
│   └── TokenService.java
├── domain/
│   ├── exception/
│   │   ├── InvalidCredentialsException.java
│   │   └── TokenRevokedException.java
│   ├── token/
│   │   ├── UsuarioToken.java
│   │   └── UsuarioTokenRepository.java
│   ├── usuario/
│   │   ├── AuthCredentials.java
│   │   ├── AuthUser.java
│   │   └── UsuarioAuthRepository.java
│   └── valueobject/
│       └── UsuarioTokenUUID.java
├── infrastructure/
│   ├── config/
│   │   ├── CsrfHeaderSwaggerIndexTransformer.java
│   │   ├── OpenApiConfig.java
│   │   ├── SecurityConfig.java
│   │   └── SwaggerCsrfConfig.java
│   ├── persistence/
│   │   ├── adapter/
│   │   ├── mapper/
│   │   ├── UsuarioTokenJpaEntity.java
│   │   └── UsuarioTokenJpaRepository.java
│   └── security/
│       ├── CustomHeaderCsrfFilter.java
│       ├── CustomUserDetailsService.java
│       ├── JwtAuthorizationFilter.java
│       ├── JwtTokenProvider.java
│       ├── RestAccessDeniedHandler.java
│       └── RestAuthenticationEntryPoint.java
├── presentation/
│   └── AuthController.java
└── README.md
```

### Descripción de cada capa

#### `application`

Contiene la lógica de uso y los contratos de la capa de negocio visibles para el controlador.

- `dto/`: objetos de entrada/salida para la API.
- `usecase/`: casos de uso de autenticación:
  - `LoginUseCase`: valida credenciales y genera tokens.
  - `RegisterUseCase`: crea o registra usuarios.
  - `RefreshTokenUseCase`: renueva la sesión.
  - `LogoutUseCase`: invalida la sesión actual.
- `AuthCookieService`: construye y elimina cookies de sesión (`access_token` y `refresh_token`).
- `AuthTokenIssuer`: responsable de emitir JWT.
- `TokenService`: interfaz para servicios de tokens.
- `AuthPrincipal`: representación del usuario autenticado dentro del contexto de seguridad.

#### `domain`

Es la capa de reglas de negocio y modelos del dominio de autenticación.

- `exception/`: excepciones de autenticación y validación de tokens.
- `token/`: dominio de tokens de sesión e interfaces de repositorio.
- `usuario/`: modelos para credenciales, usuario autenticado y acceso a datos del usuario.
- `valueobject/`: value objects asociados al manejo de tokens y identidad.

#### `infrastructure`

Implementa detalles técnicos y dependencias externas.

- `config/`: configuración de Spring Security, OpenAPI y CSRF.
- `persistence/`: almacenamiento de tokens de sesión mediante JPA.
- `security/`: filtros, provider JWT, carga de usuarios y manejo de errores de seguridad.

#### `presentation`

Contiene los endpoints REST expuestos al cliente.

- `AuthController`: define la API pública del módulo autenticación.

---

## Endpoints del módulo

Todos los endpoints están bajo el prefijo `/auth`.

| Método | Ruta | Descripción |
| --- | --- | --- |
| `POST` | `/auth/login` | Inicia sesión con email y contraseña. Emite cookies HttpOnly con `access_token` y `refresh_token`. Soporta `?raw=true` para devolver los tokens en el cuerpo a clientes no navegador. |
| `POST` | `/auth/register` | Registro público del usuario. El rol queda fijado a `VIEWER` y no puede elegirse por el cliente. |
| `POST` | `/auth/admin/register` | Registro desde administración, permitiendo definir un rol. Requiere permisos de plataforma. |
| `POST` | `/auth/refresh` | Renueva la sesión usando el refresh token enviado en cookie o en el cuerpo. |
| `POST` | `/auth/logout` | Cierra la sesión actual, invalida el token de acceso y elimina las cookies de sesión. |
| `GET` | `/auth/me` | Devuelve el perfil del usuario autenticado: email, `usuarioId`, `tenantId` y `rol`. |

### Detalles relevantes de la API

- Los tokens de acceso y refresh se gestionan principalmente a través de cookies `HttpOnly`, lo que evita almacenamiento en memoria del navegador.
- El flujo `/auth/login` puede devolver o no los tokens en el cuerpo:
  - sin `raw=true`: responde con metadatos de sesión;
  - con `raw=true`: devuelve el contenido del token para clientes como Swagger, Postman o curl.
- La autenticación y autorización se integran con Spring Security mediante filtros y validación JWT.
- El módulo también es responsable de la seguridad CSRF y de la configuración de OpenAPI/Swagger.

---

## Casos de uso principales

1. `LoginUseCase`  
   Verifica credenciales y genera la sesión.

2. `RegisterUseCase`  
   Registra un nuevo usuario y en algunos flujos envía una verificación por email.

3. `RefreshTokenUseCase`  
   Renueva tokens activos para extender la sesión sin perder autenticación.

4. `LogoutUseCase`  
   Revoca el estado del usuario actual y elimina cookies de navegación.

5. `AuthController`  
   Expone estos procesos como API REST para el frontend o clientes externos.

---

## Observaciones de diseño

- El módulo sigue una separación clara entre capa de presentación, aplicación y dominio.
- La infraestructura encapsula detalles del framework y del almacenamiento.
- La seguridad se implementa usando filtros y proveedores JWT, manteniendo la lógica de negocio desacoplada de la tecnología de autenticación.
- El módulo está pensado para ser reutilizado por otros módulos del sistema, especialmente cuando requieren validar identidad, roles o pertenencia a tenant.
