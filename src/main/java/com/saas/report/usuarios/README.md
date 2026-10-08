# Módulo de Usuarios

## Finalidad

Este módulo gestiona la entidad de usuario dentro del sistema. Su responsabilidad principal es administrar los usuarios del sistema, incluyendo alta, consulta, modificación, eliminación y verificación de email. También forma parte del flujo de onboarding y del control de acceso por roles y tenant.

El módulo cubre funcionalidades clave como:

- crear usuarios desde la administración o desde el flujo de registro;
- listar usuarios con paginación y filtros;
- consultar un usuario concreto por su identificador;
- actualizar datos de usuario;
- eliminar usuarios;
- verificar la dirección de email mediante un token seguro;
- garantizar que el tenant y el rol del usuario se respeten en la autorización.

El módulo se encuentra bajo el paquete `com.saas.report.usuarios` y expone su API REST bajo dos prefijos principales:

- `/usuarios` para gestión administrativa y operativa de usuarios;
- `/self-serve/verificar-email` para la verificación pública de email.

---

## Estructura del módulo

```text
src/main/java/com/saas/report/usuarios/
├── application/
│   ├── dto/
│   │   ├── CreadorPor.java
│   │   ├── UpdateUsuarioRequest.java
│   │   ├── UsuarioRequest.java
│   │   ├── UsuarioResponse.java
│   │   └── UsuariosPageResponse.java
│   └── usecase/
│       ├── CreateUsuarioUseCase.java
│       ├── DeleteUsuarioUseCase.java
│       ├── GetUsuarioUseCase.java
│       ├── ListUsuariosUseCase.java
│       ├── UpdateUsuarioUseCase.java
│       └── VerificarEmailUseCase.java
├── domain/
│   ├── emailverificacion/
│   │   └── EmailVerificationTokenService.java
│   ├── usuario/
│   │   ├── Usuario.java
│   │   └── UsuarioRepository.java
│   └── valueobject/
│       └── UsuarioUUID.java
├── infrastructure/
│   ├── persistence/
│   │   ├── adapter/
│   │   ├── mapper/
│   │   ├── UsuarioJpaEntity.java
│   │   └── UsuarioJpaRepository.java
│   └── security/
│       └── HmacEmailVerificationTokenService.java
├── presentation/
│   ├── EmailVerificacionController.java
│   └── UsuarioController.java
└── README.md
```

### Descripción de cada capa

#### `application`

Es la capa que contiene los casos de uso del módulo y los DTOs para los contratos de entrada y salida.

- `dto/`: representaciones de requests/responses para creación, actualización y listado de usuarios.
- `usecase/`: casos de uso para crear, consultar, listar, modificar, borrar y verificar email de usuarios.

#### `domain`

Contiene la lógica y modelo del negocio de usuarios.

- `usuario/`: entidad `Usuario` y repositorio del dominio.
- `emailverificacion/`: contrato del servicio de token para verificación por email.
- `valueobject/`: identificador de usuario `UsuarioUUID`.

#### `infrastructure`

Encapsula la parte técnica de persistencia y seguridad.

- `persistence/`: entidades JPA y repositorios para usuarios.
- `security/`: implementación del servicio de firma/validación del token de verificación por email usando HMAC.

#### `presentation`

Contiene los controladores REST del módulo.

- `UsuarioController`: operaciones de gestión del usuario.
- `EmailVerificacionController`: confirmación pública del email tras seguir el enlace enviado por correo.

---

## Endpoints del módulo

### Gestión de usuarios bajo `/usuarios`

| Método | Ruta | Descripción |
| --- | --- | --- |
| `POST` | `/usuarios` | Crea un usuario. Solo roles de plataforma (`PLATFORM_ADMIN`, `PLATFORM_USER`). |
| `GET` | `/usuarios/{id}` | Obtiene un usuario por su identificador. Requiere permisos según rol. |
| `GET` | `/usuarios` | Lista usuarios con paginación y filtros opcionales por rol y estado activo. |
| `PUT` | `/usuarios/{id}` | Actualiza un usuario. Solo roles de plataforma y administración. |
| `DELETE` | `/usuarios/{id}` | Elimina un usuario. Solo roles de plataforma. |

### Verificación de email bajo `/self-serve/verificar-email`

| Método | Ruta | Descripción |
| --- | --- | --- |
| `GET` | `/self-serve/verificar-email?token=...` | Verifica el token recibido por email y confirma la dirección del usuario. |

### Reglas de acceso

- La gestión de usuarios está restringida por rol y, en algunos casos, por tenant.
- Los usuarios con rol `VIEWER` no pueden consultar otros usuarios ni realizar operaciones de administración.
- Los administradores solo pueden consultar usuarios del mismo tenant si están bajo rol `ADMIN`.
- La verificación de email es pública porque se accede desde un enlace recibido por correo sin sesión activa.

---

## Casos de uso principales

1. `CreateUsuarioUseCase`  
   Crea un usuario con la validación necesaria y el contexto del creador.

2. `GetUsuarioUseCase`  
   Recupera un usuario por identificador.

3. `ListUsuariosUseCase`  
   Lista usuarios con filtros y paginación.

4. `UpdateUsuarioUseCase`  
   Actualiza los datos del usuario.

5. `DeleteUsuarioUseCase`  
   Elimina la entidad de usuario y su registro asociado.

6. `VerificarEmailUseCase`  
   Valida el token de correo para verificar la cuenta del usuario.

7. `HmacEmailVerificationTokenService`  
   Genera y valida tokens firmados HMAC para asegurar la integridad del enlace de verificación.

---

## Observaciones de diseño

- El módulo separa claramente las capas de presentación, aplicación, dominio e infraestructura.
- La seguridad de verificación de email se implementa con un token firmado, evitando manipulación del enlace.
- La lógica de autorización se resuelve en la capa REST usando el contexto de seguridad y el rol del usuario actual.
- La entidad de usuario está pensada para operar con roles y tenant, ajustando el acceso según el contexto del usuario autenticado.
