# Módulo de Planes

## Finalidad

Este módulo gestiona el catálogo de planes de la plataforma y su estado de activación. Su responsabilidad principal es permitir crear, consultar y activar o desactivar planes que luego se usarán en la lógica de suscripciones y límites del sistema.

Dentro del dominio del producto, este módulo representa la oferta comercial y la configuración del producto, incluyendo:

- definición del nombre y descripción del plan;
- precios por tramo de meses;
- número máximo de usuarios permitidos;
- límite mensual de consultas;
- control de activación/desactivación del plan para evitar que un plan inactivo sea contratado o reutilizado en cambios de suscripción.

El módulo vive bajo el paquete `com.saas.report.plan` y expone su API REST con el prefijo `/admin/plans`.

---

## Estructura del módulo

```text
src/main/java/com/saas/report/plan/
├── application/
│   ├── dto/
│   │   ├── PlanPrecioRequest.java
│   │   ├── PlanPrecioResponse.java
│   │   ├── PlanRequest.java
│   │   └── PlanResponse.java
│   └── usecase/
│       ├── ActivatePlanUseCase.java
│       ├── CreatePlanUseCase.java
│       ├── DeactivatePlanUseCase.java
│       ├── GetPlanUseCase.java
│       └── ListPlansUseCase.java
├── domain/
│   ├── exception/
│   │   └── PlanNombreAlreadyExistsException.java
│   ├── Plan.java
│   ├── PlanPrecio.java
│   ├── PlanRepository.java
│   └── valueobject/
│       ├── PlanLimiteConsultasMensuales.java
│       ├── PlanMaxUsuarios.java
│       ├── PlanPrecioUUID.java
│       └── PlanUUID.java
├── infrastructure/
│   └── persistence/
│       ├── adapter/
│       ├── mapper/
│       ├── PlanJpaEntity.java
│       ├── PlanJpaRepository.java
│       └── PlanPrecioJpaEntity.java
├── presentation/
│   └── PlanController.java
└── README.md
```

### Descripción de cada capa

#### `application`

Es la capa que expone los casos de uso del negocio.

- `dto/`: modelos de entrada y salida para crear y consultar planes.
- `usecase/`: casos de uso para:
  - crear un plan,
  - obtener un plan concreto,
  - listar el catálogo,
  - activar o desactivar un plan.

#### `domain`

Contiene la lógica de negocio y los modelos del dominio de planes.

- `Plan.java`: entidad principal del plan.
- `PlanPrecio.java`: precio asociado a cada tramo de meses.
- `PlanRepository.java`: puerto para la persistencia del plan.
- `valueobject/`: identificadores y restricciones del dominio, como límite de consultas o máximo de usuarios.
- `exception/`: excepciones del dominio, por ejemplo si el nombre del plan ya existe.

#### `infrastructure`

Implementa la capa técnica de persistencia JPA para los planes.

- `persistence/`: entidades JPA y repositorios que permiten guardar y consultar el catálogo de planes en base de datos.

#### `presentation`

Expone la funcionalidad como API REST.

- `PlanController`: controla la creación, lectura y cambio de estado de los planes.

---

## Endpoints del módulo

La API del módulo se encuentra bajo el prefijo `/admin/plans`.

| Método | Ruta | Descripción |
| --- | --- | --- |
| `POST` | `/admin/plans` | Crea un nuevo plan con su estructura de precios por tramo. |
| `GET` | `/admin/plans/{planId}` | Obtiene un plan concreto por su identificador. |
| `GET` | `/admin/plans` | Lista el catálogo completo de planes. |
| `POST` | `/admin/plans/{planId}/activar` | Activa un plan para que pueda usarse en nuevas suscripciones o cambios de plan. |
| `POST` | `/admin/plans/{planId}/desactivar` | Desactiva un plan para que no pueda ser contratado ni ser destino de cambios de plan. |

### Reglas de acceso

- El módulo está enfocado a administración y, por tanto, se expone bajo la ruta `/admin`.
- La activación y desactivación exigen rol de `PLATFORM_ADMIN`.
- El resto de operaciones admite también `PLATFORM_USER`.

---

## Casos de uso principales

1. `CreatePlanUseCase`  
   Crea un plan con sus precios y validaciones del dominio.

2. `GetPlanUseCase`  
   Recupera un plan específico según su identificador.

3. `ListPlansUseCase`  
   Lista el catálogo completo para administración o integración con otras partes del sistema.

4. `ActivatePlanUseCase`  
   Habilita el uso del plan en el flujo de contratación.

5. `DeactivatePlanUseCase`  
   Inhabilita el plan para impedir su uso en nuevas suscripciones.

6. `PlanController`  
   Expone estas operaciones en la API REST.

---

## Observaciones de diseño

- El módulo sigue la separación típica por capas: presentación, aplicación y dominio.
- El estado activo/inactivo del plan es crítico para el negocio, por eso se gestiona explícitamente mediante casos de uso dedicados.
- La persistencia se encapsula en infraestructura, manteniendo el dominio desacoplado de JPA.
- La lógica de precios y límites del plan se modela como value objects y entidades del dominio, reforzando la integridad de negocio.
