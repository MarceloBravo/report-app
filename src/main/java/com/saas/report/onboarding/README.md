# Módulo de Onboarding

## Finalidad

Este módulo gestiona el alta autoservicio de un cliente nuevo en la plataforma. Su objetivo principal es encapsular el flujo de registro inicial de un tenant, incluyendo la creación de la suscripción inicial, la cuenta del usuario propietario y la configuración de la conexión a la base de datos externa.

El onboarding es el punto de entrada comercial del sistema: permite que un cliente nuevo se registre sin intervención manual del equipo de operación y, a la vez, levanta toda la infraestructura mínima necesaria para que el cliente pueda empezar a usar la aplicación.

Entre sus responsabilidades destacan:

- crear un nuevo tenant;
- asociar el cliente con un plan inicial gratuito configurado en la aplicación;
- registrar al usuario propietario;
- crear la conexión a la base de datos externa del tenant;
- devolver la URL o referencia de verificación de email;
- aplicar control de tasa para evitar abuso del flujo de registro.

El módulo se encuentra bajo el paquete `com.saas.report.onboarding` y expone una API REST bajo el prefijo `/self-serve`.

---

## Estructura del módulo

```text
src/main/java/com/saas/report/onboarding/
├── application/
│   ├── dto/
│   │   ├── OnboardingRequest.java
│   │   └── OnboardingResponse.java
│   ├── event/
│   │   └── (vacío actualmente)
│   └── usecase/
│       └── OnboardClienteUseCase.java
├── infrastructure/
│   └── security/
│       ├── SelfServeRateLimiter.java
│       └── SelfServeRateLimitFilter.java
├── presentation/
│   └── OnboardingController.java
└── README.md
```

### Descripción de cada capa

#### `application`

Es la capa donde se define la lógica del caso de uso de alta de cliente.

- `dto/`: objetos de entrada y salida del proceso de onboarding.
- `usecase/`: caso de uso principal `OnboardClienteUseCase`, responsable de ejecutar la operación de registro completo del cliente.
- `event/`: espacio reservado para eventos o notificaciones futuras del flujo de onboarding.

#### `infrastructure`

Contiene la parte técnica del módulo.

- `security/`: componentes para limitar el abuso del alta autoservicio, como el rate limiter y el filtro asociado.

#### `presentation`

Contiene la interfaz HTTP del módulo.

- `OnboardingController`: expone el endpoint público de registro del cliente.

---

## Endpoints del módulo

Los endpoints del módulo viven bajo `/self-serve`.

| Método | Ruta | Descripción |
| --- | --- | --- |
| `POST` | `/self-serve/registro` | Registra un cliente nuevo, crea su tenant, suscripción inicial, usuario propietario y conexión a la base de datos. |

### Comportamiento del endpoint

- La operación es pública y sirve como entrada principal del proceso de alta comercial del cliente.
- El plan inicial no lo selecciona el cliente; se toma del plan gratuito configurado en la aplicación.
- El endpoint devuelve identificadores y la URL de verificación de email asociada al alta.
- Se aplica control de tasa para mitigar abuso del registro masivo.

---

## Casos de uso principales

1. `OnboardClienteUseCase`  
   Ejecuta la transacción que crea el tenant, usuario, suscripción, conexión y datos iniciales del cliente.

2. `SelfServeRateLimiter` / `SelfServeRateLimitFilter`  
   Aseguran que el flujo de auto registro tenga un control de carga y no sea explotado.

3. `OnboardingController`  
   Expondrá la funcionalidad de registro por vía HTTP para la parte pública del sistema.

---

## Observaciones de diseño

- El módulo está pensado como un punto de entrada exclusivo del alta autoservicio, no como un módulo general de administración.
- La operación se modela como un flujo transaccional para asegurar que todo el onboarding salga correctamente o no se cree parcialmente.
- La capa de infraestructura protege el endpoint con limitación de tasa, dado que es un proceso potencialmente expuesto a intentos masivos.
- El registro está desacoplado de la lógica comercial de planes y suscripciones para mantener el flujo centralizado y más fácil de mantener.
