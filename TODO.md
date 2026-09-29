# TODO / Próximos pasos

Trabajo pendiente del microservicio `report`. El estado de "qué hace hoy el código" está en el
[README.md](README.md); este archivo solo lista lo que falta o quedó a medias.

## Propuesto (de los próximos pasos sugeridos del README anterior)

- [ ] Crear un módulo Maven independiente `report-identity` para producir un artefacto compartido
      consumible por otros servicios.
- [ ] Añadir README y contrato de API (interfaces públicas) al Shared Kernel `identity` para
      gobernanza de cambios.
- [ ] Escribir pruebas de integración que cubran el flujo de creación/registro de usuarios y
      validación de roles.

## Sendero de arquitectura (Shared Kernel `identity`)

- [ ] Convertir `UsuarioRol` en entidad `Role` si los roles pasan a ser dinámicos (CRUD), con
      repositorio y migración de datos.
- [ ] Versionar el módulo `identity` si se publica como artefacto Maven.

## Onboarding y seguridad

- [ ] Implementar el envío real del correo de verificación (agregar `spring-boot-starter-mail` y
      configurar SMTP; hoy solo se genera y devuelve la URL).
- [ ] Persistir la verificación de email (token consumido, expiración) de forma explícita en base
      de datos si se requiere auditoría.

## Módulo `consulta` (NL → SQL)

- [ ] Caché del esquema por tenant/conexión (la extracción existe, la caché no).
- [ ] Integrar el agente consultor del microservicio Python (hoy solo se genera SQL).
- [ ] Completar la validación y el sanitado de SQL antes de ejecutarlo contra la BD del cliente.

## Base de datos y migraciones

- [ ] Incorporar Flyway o Liquibase: hoy el esquema se genera con `ddl-auto: update` (dev) /
      `validate` (prod) y solo hay una semilla SQL en `src/main/resources/db/`.
- [ ] Aplicar las decisiones de RLS documentadas en `docs/Arquitectura_Base_de_Datos_v2.md`.

## Calidad y pruebas

- [ ] Test de carga de contexto (`@SpringBootTest`) y de `GlobalExceptionHandler`.
- [ ] Tests de los filtros de seguridad (`JwtAuthorizationFilter`, `SelfServeRateLimitFilter`),
      `SecurityConfig`, `AesCredentialCipher` y los adaptadores/mapeadores JPA.
- [ ] Configurar CI (no existe `.github/workflows/`) con `./mvnw verify`.

## Higiene del repositorio

- [ ] Eliminar el árbol duplicado `bin/` (copia completa del proyecto) y
      `docs/refactorizacion-arquitectura-clean-ddd copy.md` (idéntico al original).
- [ ] Actualizar `docs/ESTRUCTURA_ARCHIVOS.md` y el README de `identity`
      (referencian clases y roles que ya no existen).
- [ ] Añadir `LICENSE` y `CHANGELOG.md`.

## Notas importantes

- El paquete `identity` actúa como Shared Kernel y debe mantenerse pequeño y estable. Cualquier
  cambio público en él debe documentarse; si en el futuro se publica como artefacto Maven, hay que
  versionar el módulo (`src/main/java/com/saas/report/identity/domain/user/UsuarioRol.java`).
- Si los roles pasan a ser dinámicos (gestión CRUD), habría que convertir `UsuarioRol` en una
  entidad `Role` dentro del mismo módulo `identity`, con repositorio y migración de datos.
- El alta administrativa de tenants fue removida: `/admin/tenants` solo permite consultar y cambiar
  el estado (activar / suspender / cancelar).
- La verificación de email genera y devuelve el enlace, pero el envío de correo no está implementado
  (no hay `spring-boot-starter-mail`); la URL se construye con `FRONTEND_BASE_URL` y se incluye en
  la respuesta de `POST /self-serve/registro`.
- `docs/ESTRUCTURA_ARCHIVOS.md` y el README de `identity` tienen secciones desactualizadas respecto
  al código actual; el código es la fuente de verdad.