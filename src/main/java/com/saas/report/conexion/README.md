# Módulo de Conexiones

## Finalidad

Este módulo gestiona las conexiones de base de datos asociadas a cada tenant del sistema. Su responsabilidad principal es permitir consultar, validar y exponer las conexiones configuradas para un tenant concreto, así como recuperar el esquema de cada origen de datos cuando sea necesario.

Dentro del dominio del producto, este módulo sirve para:

- mantener la información de conexión a bases de datos por tenant;
- proteger credenciales mediante cifrado antes de persistirlas;
- leer y representar el esquema de tablas y columnas de una conexión;
- permitir la consulta de conexiones desde la capa REST sin exponer detalles sensibles;
- resolver el tenant correcto del usuario autenticado o, en caso de roles de plataforma, de un tenant solicitado explícitamente.

El módulo queda bajo el paquete `com.saas.report.conexion` y expone la API REST bajo el prefijo `/conexiones`.

---

## Estructura del módulo

```text
src/main/java/com/saas/report/conexion/
├── application/
│   ├── dto/
│   │   ├── ConexionDetailResponse.java
│   │   └── ConexionListResponse.java
│   ├── usecase/
│   │   ├── GetConexionUseCase.java
│   │   └── ListConexionesUseCase.java
├── domain/
│   ├── connection/
│   │   ├── CredentialCipher.java
│   │   ├── TenantDbConnection.java
│   │   ├── TenantDbConnectionRepository.java
│   │   └── TipoConexion.java
│   ├── schema/
│   │   ├── ColumnaEsquema.java
│   │   ├── EsquemaTenant.java
│   │   ├── SchemaExtractor.java
│   │   └── TablaEsquema.java
│   └── valueobject/
│       └── TenantDbConnectionUUID.java
├── infrastructure/
│   ├── persistence/
│   │   ├── adapter/
│   │   ├── mapper/
│   │   ├── TenantDbConnectionJpaEntity.java
│   │   └── TenantDbConnectionJpaRepository.java
│   ├── schema/
│   │   ├── factory/
│   │   ├── JdbcSchemaExtractor.java
│   │   └── strategy/
│   └── security/
│       └── AesCredentialCipher.java
├── presentation/
│   └── ConexionController.java
└── README.md
```

### Descripción de cada capa

#### `application`

Contiene la lógica de caso de uso para obtener información de conexiones.

- `dto/`: objetos de salida para el cliente REST.
- `usecase/`: casos de uso del módulo:
  - `ListConexionesUseCase`: lista las conexiones del tenant actual o del tenant solicitado.
  - `GetConexionUseCase`: obtiene el detalle de una conexión concreta.

#### `domain`

Representa la lógica de negocio del dominio de conexiones.

- `connection/`: modelos y repositorios de la conexión a la base de datos, incluyendo el tipo de conexión y el cifrado de credenciales.
- `schema/`: entidades que representan el esquema de la base de datos y la extracción de estructuras (tablas, columnas).
- `valueobject/`: identificadores específicos del dominio, como `TenantDbConnectionUUID`.

#### `infrastructure`

Implementa detalles técnicos y de persistencia.

- `persistence/`: mapeo JPA y adaptadores para almacenar las conexiones.
- `schema/`: extracción real del esquema de la base de datos mediante JDBC y estrategias de lectura.
- `security/`: cifrado de credenciales con AES, para evitar almacenar datos sensibles en texto plano.

#### `presentation`

Contiene la API REST que expone el módulo al exterior.

- `ConexionController`: define los endpoints de consulta de conexiones.

---

## Endpoints del módulo

La API del módulo se expone con el prefijo `/conexiones`.

| Método | Ruta | Descripción |
| --- | --- | --- |
| `GET` | `/conexiones` | Lista las conexiones del tenant autenticado. Si el usuario tiene rol de plataforma, además admite el parámetro opcional `tenantId` para consultar otro tenant. |
| `GET` | `/conexiones/{dbConnectionId}` | Devuelve el detalle de una conexión concreta del tenant autenticado o del tenant indicado por `tenantId` si el usuario es de plataforma. |

### Comportamiento importante

- Si no se pasa `tenantId`, el tenant se resuelve desde el contexto de seguridad del usuario autenticado.
- Si se pasa `tenantId`, solo los roles de plataforma pueden usarlo.
- Un rol de tenant que intente usar `tenantId` recibe `403 Forbidden`.
- Si el identificador de conexión o tenant no es válido, la API responde con `404` del tipo `ResourceNotFoundException`.
- Los endpoints están protegidos y requieren autenticación.

---

## Casos de uso principales

1. `ListConexionesUseCase`  
   Recupera todas las conexiones de un tenant para mostrarlas en la interfaz.

2. `GetConexionUseCase`  
   Obtiene el detalle de una conexión específica para consultar sus datos y facilitar la operación sobre esa conexión.

3. `JdbcSchemaExtractor`  
   Extrae metadatos del esquema de la base de datos para reconocer tablas y columnas.

4. `AesCredentialCipher`  
   Cifra credenciales antes de persistirlas, evitando almacenamiento en texto plano.

5. `ConexionController`  
   Expone estas operaciones a clientes externos mediante REST.

---

## Observaciones de diseño

- El módulo separa claramente la capa de presentación, aplicación y dominio.
- La infraestructura encapsula la persistencia, el esquema y la seguridad de credenciales.
- El acceso a datos está limitado por tenant para evitar que un usuario vea conexiones ajenas.
- El control de `tenantId` en la capa REST refuerza la seguridad y restringe consultas cruzadas a usuarios de plataforma.
