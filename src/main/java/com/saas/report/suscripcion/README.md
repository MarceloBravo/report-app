# Módulo de Suscripciones

## Finalidad

Este módulo gestiona el ciclo de vida de las suscripciones de los tenants dentro de la plataforma. Su objetivo principal es controlar qué plan tiene contratado cada tenant, cómo cambia ese plan, y qué estados puede tomar una suscripción (activa, suspendida o cancelada) según la lógica de negocio.

El módulo cubre escenarios como:

- creación de una suscripción para un tenant;
- cambio de plan contratado;
- suspensión temporal de la suscripción;
- cancelación de la suscripción;
- consulta de la suscripción actual del tenant autenticado;
- persistencia y consulta del historial de precios asociados a la suscripción.

El paquete raíz es `com.saas.report.suscripcion` y expone la API REST en dos prefijos principales:

- `/admin/suscripciones` para administración
- `/suscripcion` para la consulta del tenant autenticado

---

## Estructura del módulo

```text
src/main/java/com/saas/report/suscripcion/
├── application/
│   ├── dto/
│   │   ├── ActualizarPlanRequest.java
│   │   ├── MiSuscripcionResponse.java
│   │   ├── SuscripcionRequest.java
│   │   └── SuscripcionResponse.java
│   └── usecase/
│       ├── CancelSuscripcionUseCase.java
│       ├── CreateSuscripcionUseCase.java
│       ├── GetMiSuscripcionUseCase.java
│       ├── SuspendSuscripcionUseCase.java
│       └── UpdateSuscripcionPlanUseCase.java
├── domain/
│   ├── exception/
│   │   └── SuscripcionAlreadyActiveException.java
│   ├── Suscripcion.java
│   ├── SuscripcionEstado.java
│   ├── SuscripcionHistorialPrecio.java
│   ├── SuscripcionRepository.java
│   └── valueobject/
│       ├── SuscripcionHistorialPrecioUUID.java
│       └── SuscripcionUUID.java
├── infrastructure/
│   └── persistence/
│       ├── adapter/
│       ├── mapper/
│       ├── SuscripcionHistorialPrecioJpaEntity.java
│       ├── SuscripcionJpaEntity.java
│       └── SuscripcionJpaRepository.java
├── presentation/
│   ├── MiSuscripcionController.java
│   └── SuscripcionController.java
└── README.md
```

### Descripción de cada capa

#### `application`

Contiene los casos de uso y los DTOs de entrada/salida del módulo.

- `dto/`: define los contratos para crear una suscripción, actualizar plan y devolver información de suscripción.
- `usecase/`: cases de uso para:
  - crear suscripción,
  - cancelar suscripción,
  - suspender suscripción,
  - actualizar plan,
  - obtener la suscripción del tenant autenticado.

#### `domain`

Es la capa de negocio del módulo.

- `Suscripcion.java`: entidad principal de la suscripción.
- `SuscripcionEstado.java`: enum de estados de la suscripción.
- `SuscripcionHistorialPrecio.java`: historial de precios asociados a la suscripción.
- `SuscripcionRepository.java`: puerto para persistencia.
- `valueobject/`: identificadores específicos del dominio.
- `exception/`: excepciones del dominio, como una suscripción ya activa.

#### `infrastructure`

Implementa la persistencia JPA para la entidad de suscripción y su historial de precios.

- `persistence/`: entidades JPA y repositorios para guardar y consultar suscripciones.

#### `presentation`

Expone la funcionalidad del módulo como API REST.

- `SuscripcionController`: administración de suscripciones por parte de plataforma.
- `MiSuscripcionController`: consulta de la suscripción del tenant autenticado.

---

## Endpoints del módulo

### Administración bajo `/admin/suscripciones`

| Método | Ruta | Descripción |
| --- | --- | --- |
| `POST` | `/admin/suscripciones` | Crea una nueva suscripción. |
| `POST` | `/admin/suscripciones/{suscripcionId}/cancelar` | Cancela la suscripción, dejando el estado en `CANCELED`. |
| `POST` | `/admin/suscripciones/{suscripcionId}/suspender` | Suspende la suscripción, dejando el estado en `SUSPENDED`. |
| `PUT` | `/admin/suscripciones/{suscripcionId}/plan` | Actualiza el plan de una suscripción existente. |

### Consulta del tenant actual bajo `/suscripcion`

| Método | Ruta | Descripción |
| --- | --- | --- |
| `GET` | `/suscripcion/mia` | Devuelve la suscripción actual del tenant autenticado. |

### Reglas de acceso

- El panel administrativo (`/admin/suscripciones`) está orientado a roles de plataforma.
- La consulta de la propia suscripción (`/suscripcion/mia`) usa el tenant del contexto de seguridad, por lo que cada tenant solo puede ver la suya.

---

## Casos de uso principales

1. `CreateSuscripcionUseCase`  
   Crea una suscripción asociada a un tenant y plan.

2. `UpdateSuscripcionPlanUseCase`  
   Permite cambiar el plan contratado por otra oferta válida.

3. `SuspendSuscripcionUseCase`  
   Pone la suscripción en estado suspendido cuando la lógica de negocio lo requiere.

4. `CancelSuscripcionUseCase`  
   Cancela la suscripción y marca el ciclo de vida final.

5. `GetMiSuscripcionUseCase`  
   Obtiene la suscripción del tenant autenticado para uso del cliente o la propia aplicación.

6. `SuscripcionController` y `MiSuscripcionController`  
   Exponen la funcionalidad hacia la capa REST.

---

## Observaciones de diseño

- El módulo tiene una clara separación entre dominio, aplicación, infraestructura y presentación.
- El estado de la suscripción es un concepto central del negocio, por lo que se modela explícitamente con enumeraciones y validaciones.
- La lógica de actualización de plan, suspensión y cancelación está encapsulada en casos de uso para mantener el dominio consistente.
- La consulta del tenant actual evita que un usuario pueda ver suscripciones ajenas, usando el contexto de seguridad del usuario autenticado.
