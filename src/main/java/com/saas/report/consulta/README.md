# Módulo de Consultas

## Finalidad

Este módulo se encarga de registrar, consultar y convertir consultas de reporting dentro del sistema. Su objetivo principal es gestionar el historial de consultas realizadas por los usuarios, validando que sean correctas, limitando su uso según reglas del negocio y, cuando corresponde, transformando un reporte externo al formato interno de la plataforma.

El módulo cubre estas responsabilidades:

- registrar una consulta en el historial del tenant;
- listar consultas por tenant con paginación;
- recuperar una consulta concreta por su identificador;
- convertir un reporte externo en una consulta interna del sistema;
- validar SQL, límites de uso y disponibilidad del proveedor IA;
- ejecutar consultas sobre bases de datos conectadas a través de adaptadores de infraestructura.

El paquete raíz es `com.saas.report.consulta` y la API REST se expone bajo el prefijo `/consultas`.

---

## Estructura del módulo

```text
src/main/java/com/saas/report/consulta/
├── application/
│   ├── dto/
│   │   ├── ConsultaRequest.java
│   │   ├── ConsultaResponse.java
│   │   └── ConvertirConsultaRequest.java
│   ├── SanitizadorSql.java
│   └── usecase/
│       ├── ConvertirConsultaUseCase.java
│       ├── GetConsultaUseCase.java
│       ├── ListConsultasUseCase.java
│       └── RegisterConsultaUseCase.java
├── domain/
│   ├── ConsultaHistorial.java
│   ├── ConsultaHistorialRepository.java
│   ├── exception/
│   │   ├── ErrorEjecucionSqlException.java
│   │   ├── LimiteConsultasExcedidoException.java
│   │   ├── ProveedorIaNoDisponibleException.java
│   │   ├── RespuestaIaNoValidaException.java
│   │   └── SqlInvalidoException.java
│   ├── GeneradorSqlPort.java
│   ├── SqlEjecutorPort.java
│   ├── ValidadorLimiteConsultasPort.java
│   └── valueobject/
│       └── ConsultaHistorialUUID.java
├── infrastructure/
│   ├── ia/
│   │   └── PythonSqlGeneratorAdapter.java
│   ├── jdbc/
│   │   └── JdbcSqlEjecutor.java
│   ├── limite/
│   │   └── ValidadorLimiteConsultasAdapter.java
│   └── persistence/
│       ├── adapter/
│       ├── ConsultaHistorialJpaEntity.java
│       ├── ConsultaHistorialJpaRepository.java
│       └── mapper/
├── presentation/
│   └── ConsultaController.java
└── README.md
```

### Descripción de cada capa

#### `application`

Contiene la lógica de caso de uso del módulo y los DTOs para entrada/salida de la API.

- `dto/`: representaciones de las consultas a registrar o devolver.
- `SanitizadorSql.java`: limpia o normaliza SQL antes de su validación/ejecución.
- `usecase/`: casos de uso principales:
  - `ConvertirConsultaUseCase`: transforma un modelo de entrada en una consulta del sistema.
  - `RegisterConsultaUseCase`: registra la consulta en el historial.
  - `GetConsultaUseCase`: obtiene una consulta por identificador.
  - `ListConsultasUseCase`: lista consultas para un tenant con paginación.

#### `domain`

Mantiene las reglas de negocio y los contratos del dominio.

- `ConsultaHistorial`: entidad base del historial.
- `ConsultaHistorialRepository`: puerto para persistencia del historial.
- `GeneradorSqlPort`: contrato para generación de SQL.
- `SqlEjecutorPort`: contrato para ejecutar SQL contra una base de datos.
- `ValidadorLimiteConsultasPort`: puerto para validar límites de consultas por usuario o tenant.
- `exception/`: excepciones de validación, ejecución SQL, límites y proveedor IA.
- `valueobject/`: identificador del historial de consultas.

#### `infrastructure`

Implementa la parte tecnológica.

- `ia/`: integración con un generador SQL basado en Python.
- `jdbc/`: ejecución de SQL con JDBC.
- `limite/`: validación del límite de consultas.
- `persistence/`: JPA y mapeo del historial de consultas.

#### `presentation`

Contiene la API REST del módulo.

- `ConsultaController`: define los endpoints para registrar, listar y consultar historial de consultas.

---

## Endpoints del módulo

Todos los endpoints se encuentran bajo el prefijo `/consultas`.

| Método | Ruta | Descripción |
| --- | --- | --- |
| `POST` | `/consultas/convertir` | Convierte un reporte externo o entrada de negocio al formato interno de la plataforma usando el tenant y usuario autenticados del contexto de seguridad. |
| `POST` | `/consultas` | Registra una consulta en el historial del tenant. |
| `GET` | `/consultas/{consultaId}` | Recupera una consulta concreta por su identificador. |
| `GET` | `/consultas` | Lista el historial de consultas de un tenant con paginación usando `tenantId`, `page` y `size`. |

### Observaciones de uso

- La conversión de consultas toma el tenant y el usuario desde el contexto de seguridad, no desde el cuerpo.
- El historial se consulta por `tenantId`, con paginación opcional (`page` y `size`).
- La API exige autenticación para las rutas del módulo.
- Se validan límites de uso, SQL válido y disponibilidad del proveedor IA antes de aceptar o ejecutar una consulta.

---

## Casos de uso principales

1. `ConvertirConsultaUseCase`  
   Toma la entrada de consulta y la adapta al formato de la plataforma.

2. `RegisterConsultaUseCase`  
   Guarda la consulta en el historial asociado al tenant.

3. `GetConsultaUseCase`  
   Recupera detalles de una consulta específica.

4. `ListConsultasUseCase`  
   Devuelve el historial paginado para un tenant concreto.

5. `ConsultaController`  
   Expone la funcionalidad sobre REST para que la UI o servicios externos interactúen con el módulo.

---

## Observaciones de diseño

- El módulo separa claramente la capa de presentación, aplicación y dominio.
- La infraestructura encapsula acceso a base de datos, servicios IA y persistencia.
- Las reglas del negocio viven en el dominio para evitar acoplar la lógica a frameworks o bibliotecas.
- El historial de consultas se mantiene por tenant, lo que ayuda a aislar la información entre distintos clientes o organizaciones.
