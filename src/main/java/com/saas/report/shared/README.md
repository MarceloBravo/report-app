# Módulo Shared

## Finalidad

Este módulo centraliza elementos reutilizables que se usan en varios módulos del sistema. Su objetivo no es ofrecer funcionalidad de negocio específica, sino proporcionar tipos comunes, utilidades, excepciones base, DTOs de respuesta, autenticación contextual y helpers transversales que permitan mantener una base consistente en toda la aplicación.

En la práctica, este módulo contiene los artefactos compartidos por el resto del proyecto, como:

- identificadores de dominio reutilizables (`TenantUUID`, `UsuarioUUID`);
- excepciones globales del sistema (`ForbiddenException`, `ResourceNotFoundException`, etc.);
- DTOs de respuesta comunes, como paginación y errores;
- utilidades de apoyo para serialización, paginación y extracción del usuario autenticado;
- objetos de seguridad para acceder al principal autenticado desde el contexto.

Se trata de un módulo transversal que sirve como infraestructura compartida para otros módulos del sistema.

---

## Estructura del módulo

```text
src/main/java/com/saas/report/shared/
├── domain/
│   └── valueobject/
│       ├── TenantUUID.java
│       └── UsuarioUUID.java
├── exception/
│   ├── DomainException.java
│   ├── EmailAlreadyExistsException.java
│   ├── ForbiddenException.java
│   ├── InvalidAmountException.java
│   ├── InvalidIntegerValue.java
│   ├── InvalidStatusException.java
│   ├── InvalidUUIDException.java
│   └── ResourceNotFoundException.java
├── helpers/
│   └── PaginationHelper.java
├── presentation/
│   ├── dto/
│   │   ├── ErrorResponse.java
│   │   └── PageResponse.java
│   └── rest/
│       └── SecurityContextAuth.java
├── security/
│   └── AuthPrincipal.java
├── README.md
└── (no expone controladores REST)
```

### Descripción de cada parte

#### `domain`

Contiene los value objects reutilizables en toda la aplicación.

- `TenantUUID`: identificador de tenant.
- `UsuarioUUID`: identificador de usuario.

Estos tipos ayudan a mantener una semántica clara y a centralizar validación de UUIDs del dominio.

#### `exception`

Agrupa excepciones base y excepciones de uso común en todo el sistema.

- `DomainException`: excepción base para el dominio.
- `ForbiddenException`: acceso no autorizado.
- `ResourceNotFoundException`: recurso inexistente.
- `InvalidStatusException`, `InvalidAmountException`, `InvalidIntegerValue`, `InvalidUUIDException`: validaciones de negocio y datos.
- `EmailAlreadyExistsException`: caso frecuente en registro de usuarios.

Estas excepciones son reutilizadas desde diferentes módulos para mantener un manejo uniforme de errores.

#### `helpers`

Incluye utilidades transversales.

- `PaginationHelper.java`: ayuda a normalizar o manejar paginación de consultas.

#### `presentation`

Contiene artefactos de respuesta y utilidades de contexto HTTP/REST.

- `dto/`: DTOs comunes de respuesta:
  - `PageResponse`: representación paginada del resultado.
  - `ErrorResponse`: payload uniformado para errores.
- `rest/`: clase `SecurityContextAuth` para obtener el principal autenticado, el tenant y el usuario actual desde el contexto Spring Security.

#### `security`

Define el principal autenticado compartido por todos los módulos.

- `AuthPrincipal`: representa el usuario autenticado con su identidad y permisos.

---

## Endpoints del módulo

Este módulo no expone endpoints REST directamente porque no contiene controladores `@RestController` ni rutas HTTP propias.

En otras palabras, el módulo `shared` no define API pública por sí mismo; su responsabilidad es apoyar a otros módulos: auth, plan, onboarding, consulta, etc., proporcionando:

- contexto de seguridad,
- errores estandarizados,
- DTOs compartidos,
- value objects,
- utilidades transversales.

Por esta razón, no hay una colección de rutas específicas bajo `/shared` o similares.

---

## Casos de uso principales

1. `SecurityContextAuth`  
   Obtiene el usuario autenticado y sus datos del contexto de seguridad.

2. `PageResponse` y `ErrorResponse`  
   Estandarizan la respuesta HTTP para otros módulos.

3. `TenantUUID` y `UsuarioUUID`  
   Centralizan validación y representación de identificadores compartidos.

4. `PaginationHelper`  
   Simplifica lógica de paginación sin duplicar código en múltiples módulos.

5. Excepciones compartidas  
   Permiten uniformar el manejo de errores en toda la aplicación.

---

## Observaciones de diseño

- El módulo `shared` es transversal, reutilizable y sin comportamiento de negocio específico.
- El objetivo es reducir duplicación y centralizar decisiones comunes de seguridad, errores y respuestas.
- No es un módulo de dominio funcional, sino una capa de apoyo para todo el sistema.
- Su diseño ayuda a mantener consistencia en validaciones, autenticación y contratos HTTP a lo largo de la aplicación.
