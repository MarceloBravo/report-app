Identity — Shared Kernel
=========================

Propósito
- `identity` contiene artefactos de dominio compartidos relacionados con identidad y autenticación. Mantener este paquete pequeño y con API estable evita duplicación entre `auth`, `users` y otros Bounded Contexts.

Contenido actual
- `com.saas.report.identity.domain.user.UsuarioRol` — enum con roles actuales (`ADMIN`, `VIEWER`).

Política de cambios
- Mantener breaking changes al mínimo.
- Documentar y versionar cualquier cambio público (añadir `README.md` y CHANGELOG si se publica como módulo Maven).
- No incluir implementaciones de infraestructura (repositorios, adaptadores) aquí salvo interfaces necesarias.

Futuro (cuando roles sean dinámicos)
- Reemplazar `UsuarioRol` por una entidad `Role` en `com.saas.report.identity.domain.role`.
- Añadir repositorio y migración dentro del mismo módulo `identity`.

Contacto
- Owner: (por definir)
