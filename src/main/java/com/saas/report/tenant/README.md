# Módulo de Tenant

## Finalidad

Este módulo gestiona la entidad `Tenant`, que representa a cada cliente o organización dentro de la plataforma. Su propósito principal es administrar el ciclo de vida de un tenant, incluyendo su listado, consulta individual, activación, suspensión y cancelación, así como la lógica asociada a su identificación y estado.

El tenant es una unidad de negocio fundamental en la aplicación, ya que agrupa:

- usuarios asociados,
- suscripciones,
- conexiones a bases de datos,
- acceso a reportes y configuración del cliente.

Por eso, este módulo permite controlar en qué estado se encuentra cada tenant y asegurar que solo los roles de plataforma puedan administrar la colección global de tenants.

El módulo se encuentra bajo el paquete `com.saas.report.tenant` y expone su API REST bajo el prefijo `/admin/tenants`.

---

## Estructura del módulo

```text
src/main/java/com/saas/report/tenant/
├── application/
│   ├── dto/
│   │   └── TenantResponse.java
│   └── usecase/
│       ├── ActivateTenantUseCase.java
│       ├── CancelTenantUseCase.java
│       ├── GetTenantUseCase.java
│       ├── ListTenantUseCase.java
│       └── SuspendTenantUseCase.java
├── domain/
│   ├── exception/
│   │   └── TenantSlugAlreadyExistsException.java
│   ├── Tenant.java
│   ├── TenantEstado.java
│   ├── TenantRepository.java
│   └── valueobject/
│       └── TenantUUID.java
├── infrastructure/
│   └── persistence/
│       ├── adapter/
│       ├── mapper/
│       ├── TenantJpaEntity.java
│       └── TenantJpaRepository.java
├── presentation/
│   └── TenantController.java
└── README.md
```

### Descripción de cada capa

#### `application`

Es la capa de casos de uso del módulo.

- `dto/`: define el payload de salida para devolver datos del tenant.
- `usecase/`: casos de uso para:
  - listar tenants,
  - obtener un tenant por id,
  - activar un tenant,
  - suspender un tenant,
  - cancelar un tenant.

#### `domain`

Contiene la lógica del dominio de los tenants.

- `Tenant.java`: entidad principal.
- `TenantEstado.java`: estado del tenant.
- `TenantRepository.java`: puerto para persistencia.
- `valueobject/`: identificador del tenant (`TenantUUID`).
- `exception/`: excepciones del dominio, como un slug duplicado.

#### `infrastructure`

Implementa la persistencia JPA del tenant.

- `persistence/`: entidades JPA y repositorios usados para guardar y consultar tenants en base de datos.

#### `presentation`

Exposición REST del módulo.

- `TenantController`: define los endpoints para gestionar la colección de tenants.

---

## Endpoints del módulo

Todos los endpoints están bajo el prefijo `/admin/tenants`.

| Método | Ruta | Descripción |
| --- | --- | --- |
| `GET` | `/admin/tenants` | Lista los tenants paginados. Admite filtro opcional `search`, `page` y `size`. |
| `GET` | `/admin/tenants/{tenantId}` | Obtiene un tenant por su identificador. |
| `POST` | `/admin/tenants/{tenantId}/activate` | Activa un tenant para volverlo operativo. |
| `POST` | `/admin/tenants/{tenantId}/suspend` | Suspende un tenant sin cancelarlo definitivamente. |
| `POST` | `/admin/tenants/{tenantId}/cancel` | Cancela un tenant de forma definitiva. |

### Reglas de acceso

- Todas estas rutas están orientadas a administración y requieren roles de plataforma.
- La gestión de tenants es exclusiva de operaciones del sistema, no del tenant autenticado.

---

## Casos de uso principales

1. `ListTenantUseCase`  
   Devuelve la lista paginada de tenants con filtrado opcional.

2. `GetTenantUseCase`  
   Recupera un tenant por su identificador.

3. `ActivateTenantUseCase`  
   Reactiva un tenant para que vuelva a operar.

4. `SuspendTenantUseCase`  
   Cambia el estado del tenant a suspendido.

5. `CancelTenantUseCase`  
   Marca el tenant como cancelado.

6. `TenantController`  
   Expone estas operaciones como API REST para administración.

---

## Observaciones de diseño

- El módulo encapsula el ciclo de vida del tenant y mantiene las reglas de negocio en el dominio.
- La separación por capas evita mezclar persistencia y reglas de negocio.
- El listado es paginado para que la administración pueda navegar grandes volúmenes de tenants de forma eficiente.
- El control de acceso se mantiene en la capa de presentación y en la configuración global de seguridad, reforzando que solo roles de plataforma gestionen estos recursos.
