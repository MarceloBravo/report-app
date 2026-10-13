package com.saas.integration;

import com.saas.report.shared.domain.valueobject.MotorBaseDatos;
import com.saas.report.conexion.domain.schema.ColumnaEsquema;
import com.saas.report.conexion.domain.schema.EsquemaTenant;
import com.saas.report.conexion.domain.schema.TablaEsquema;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.saas.integration.EsquemaIntegrationSupport.columna;
import static com.saas.integration.EsquemaIntegrationSupport.conexionInterna;
import static com.saas.integration.EsquemaIntegrationSupport.ejecutarDdl;
import static com.saas.integration.EsquemaIntegrationSupport.extractorPara;
import static com.saas.integration.EsquemaIntegrationSupport.tabla;
import static com.saas.integration.EsquemaIntegrationSupport.verificaReferenciasResolubles;
import static com.saas.integration.EsquemaIntegrationSupport.verificaSinColumnasDuplicadas;
import static com.saas.integration.MotorTestDatabase.abortarSiNoHayMotor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

/**
 * Valida la query de esquema de MySQL contra un motor real.
 *
 * <p>MySQL si expone {@code referenced_table_name} y {@code referenced_column_name}
 * en {@code information_schema.key_column_usage}, y detecta el autoincremental via
 * la columna {@code extra} en lugar de {@code is_identity}.
 */
@DisplayName("Integracion: extraccion de esquema en MySQL")
class MySqlSchemaExtractorIT {

    private static final String BASE = "saas_test";

    private static MotorTestDatabase base;
    private static EsquemaTenant esquema;

    @BeforeAll
    static void crearEsquemaYExtraer() {
        abortarSiNoHayMotor("MySQL", "it.mysql.url");
        base = MotorTestDatabase.mysql(BASE);

        // En MySQL el esquema es la base de datos. Se usa la base del contenedor
        // directamente (el usuario de prueba solo tiene grants sobre ella) y las
        // tablas se dropean antes para que el test sea idempotente.
        ejecutarDdl(base.dataSource(),
                "DROP TABLE IF EXISTS " + BASE + ".pedido_detalle",
                "DROP TABLE IF EXISTS " + BASE + ".pagos",
                "DROP TABLE IF EXISTS " + BASE + ".sucursales",
                "DROP TABLE IF EXISTS " + BASE + ".pedidos",
                "DROP TABLE IF EXISTS " + BASE + ".usuarios",
                "DROP TABLE IF EXISTS " + BASE + ".regiones",
                "CREATE TABLE " + BASE + ".usuarios ("
                        + "id int AUTO_INCREMENT PRIMARY KEY, "
                        + "email varchar(255) NOT NULL UNIQUE, "
                        + "edad int, "
                        + "created_at timestamp NOT NULL)",
                "CREATE TABLE " + BASE + ".pedidos ("
                        + "id bigint AUTO_INCREMENT PRIMARY KEY, "
                        + "usuario_id int NOT NULL, "
                        + "fecha datetime NOT NULL, "
                        + "total decimal(12,2), "
                        + "CONSTRAINT fk_pedidos_usuario FOREIGN KEY (usuario_id) "
                        + "REFERENCES " + BASE + ".usuarios (id))",
                "CREATE TABLE " + BASE + ".pedido_detalle ("
                        + "pedido_id bigint NOT NULL, "
                        + "linea int NOT NULL, "
                        + "descripcion text, "
                        + "PRIMARY KEY (pedido_id, linea), "
                        + "CONSTRAINT fk_detalle_pedido FOREIGN KEY (pedido_id) "
                        + "REFERENCES " + BASE + ".pedidos (id))",
                "CREATE TABLE " + BASE + ".pagos ("
                        + "id int AUTO_INCREMENT PRIMARY KEY, "
                        + "pedido_id bigint NOT NULL, "
                        + "usuario_id int NOT NULL, "
                        + "monto decimal(12,2) NOT NULL, "
                        + "CONSTRAINT fk_pago_pedido FOREIGN KEY (pedido_id) "
                        + "REFERENCES " + BASE + ".pedidos (id), "
                        + "CONSTRAINT fk_pago_usuario FOREIGN KEY (usuario_id) "
                        + "REFERENCES " + BASE + ".usuarios (id))",
                "CREATE TABLE " + BASE + ".regiones ("
                        + "pais varchar(2) NOT NULL, "
                        + "codigo int NOT NULL, "
                        + "nombre varchar(100) NOT NULL, "
                        + "PRIMARY KEY (pais, codigo))",
                "CREATE TABLE " + BASE + ".sucursales ("
                        + "id int AUTO_INCREMENT PRIMARY KEY, "
                        + "region_pais varchar(2) NOT NULL, "
                        + "region_codigo int NOT NULL, "
                        + "nombre varchar(100) NOT NULL, "
                        + "CONSTRAINT fk_sucursal_region FOREIGN KEY (region_pais, region_codigo) "
                        + "REFERENCES " + BASE + ".regiones (pais, codigo))");

        esquema = extractorPara(base.dataSource()).extraer(
                conexionInterna(BASE, BASE, MotorBaseDatos.MYSQL));
    }

    @AfterAll
    static void cerrarBase() {
        if (base != null) {
            base.cerrar();
        }
    }

    @Test
    void laQueryEsValidaEnMysqlYElExtractorNoFalla() {
        assertThat(esquema.tablas()).isNotEmpty();
        assertThat(esquema.tenantId()).isNotNull();
        assertThat(esquema.extraidoEn()).isNotNull();
    }

    @Test
    void detectaLasTablasDelEsquemaIndicado() {
        assertThat(esquema.tablas())
                .extracting(TablaEsquema::nombre)
                .containsExactlyInAnyOrder("usuarios", "pedidos", "pedido_detalle", "pagos", "regiones", "sucursales");
    }

    @Test
    void noGeneraColumnasDuplicadasEnNingunaTabla() {
        verificaSinColumnasDuplicadas(esquema);
    }

    @Test
    void resuelveLosTiposDeDatoYLaNullabilityDeCadaColumna() {
        assertThat(tabla(esquema, "usuarios").columnas())
                .extracting(ColumnaEsquema::nombre)
                .containsExactlyInAnyOrder("id", "email", "edad", "created_at");

        assertThat(columna(esquema, "usuarios", "id").tipoDato()).isEqualTo("int");
        assertThat(columna(esquema, "usuarios", "id").nullable()).isFalse();
        assertThat(columna(esquema, "usuarios", "email").tipoDato()).isEqualTo("varchar");
        assertThat(columna(esquema, "usuarios", "email").nullable()).isFalse();
        assertThat(columna(esquema, "usuarios", "edad").tipoDato()).isEqualTo("int");
        assertThat(columna(esquema, "usuarios", "edad").nullable()).isTrue();
        assertThat(columna(esquema, "pedidos", "total").tipoDato()).isEqualTo("decimal");
    }

    @Test
    void detectaLasPrimaryKeysSimplesYCompuestas() {
        assertThat(columna(esquema, "usuarios", "id").esPrimaryKey()).isTrue();
        assertThat(columna(esquema, "pedidos", "id").esPrimaryKey()).isTrue();

        assertThat(columna(esquema, "pedido_detalle", "pedido_id").esPrimaryKey()).isTrue();
        assertThat(columna(esquema, "pedido_detalle", "linea").esPrimaryKey()).isTrue();
        assertThat(columna(esquema, "pedido_detalle", "descripcion").esPrimaryKey()).isFalse();

        assertThat(columna(esquema, "regiones", "pais").esPrimaryKey()).isTrue();
        assertThat(columna(esquema, "regiones", "codigo").esPrimaryKey()).isTrue();
        assertThat(columna(esquema, "regiones", "nombre").esPrimaryKey()).isFalse();
    }

    @Test
    void noConfundeUnaUniqueConUnaPrimaryKey() {
        assertThat(columna(esquema, "usuarios", "email").esPrimaryKey()).isFalse();
        assertThat(columna(esquema, "usuarios", "email").esForeignKey()).isFalse();
        assertThat(columna(esquema, "usuarios", "email").tablaReferenciada()).isNull();
    }

    @Test
    void resuelveLaTablaYColumnaReferenciadaDeUnaForeignKeySimple() {
        ColumnaEsquema fk = columna(esquema, "pedidos", "usuario_id");

        assertThat(fk.esForeignKey()).isTrue();
        assertThat(fk.esPrimaryKey()).isFalse();
        assertThat(fk.tablaReferenciada()).isEqualToIgnoringCase("usuarios");
        assertThat(fk.columnaReferenciada()).isEqualToIgnoringCase("id");
    }

    @Test
    void resuelveTodasLasForeignKeysDeUnaTablaSinDuplicarColumnas() {
        assertThat(tabla(esquema, "pagos").columnas())
                .filteredOn(ColumnaEsquema::esForeignKey)
                .extracting(ColumnaEsquema::nombre, ColumnaEsquema::tablaReferenciada,
                        ColumnaEsquema::columnaReferenciada)
                .containsExactlyInAnyOrder(
                        tuple("pedido_id", "pedidos", "id"),
                        tuple("usuario_id", "usuarios", "id"));

        verificaSinColumnasDuplicadas(esquema);
    }

    @Test
    void resuelveLasColumnasDeUnaForeignKeyCompuesta() {
        assertThat(columna(esquema, "sucursales", "region_pais").esForeignKey()).isTrue();
        assertThat(columna(esquema, "sucursales", "region_pais").tablaReferenciada())
                .isEqualToIgnoringCase("regiones");
        assertThat(columna(esquema, "sucursales", "region_pais").columnaReferenciada())
                .isEqualToIgnoringCase("pais");

        assertThat(columna(esquema, "sucursales", "region_codigo").esForeignKey()).isTrue();
        assertThat(columna(esquema, "sucursales", "region_codigo").columnaReferenciada())
                .isEqualToIgnoringCase("codigo");
    }

    @Test
    void noMarcaComoForeignKeyLasColumnasSinRestriccion() {
        assertThat(columna(esquema, "usuarios", "email").esForeignKey()).isFalse();
        assertThat(columna(esquema, "usuarios", "edad").esForeignKey()).isFalse();
        assertThat(columna(esquema, "pedidos", "total").esForeignKey()).isFalse();
        assertThat(columna(esquema, "pedido_detalle", "descripcion").esForeignKey()).isFalse();
    }

    @Test
    void todasLasForeignKeysDetectadasApuntanAColumnasQueExisten() {
        verificaReferenciasResolubles(esquema);
    }
}
