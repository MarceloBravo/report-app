package com.saas.integration;

import com.saas.report.tenant.domain.schema.ColumnaEsquema;
import com.saas.report.tenant.domain.schema.EsquemaTenant;
import com.saas.report.tenant.domain.schema.TablaEsquema;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.saas.integration.EsquemaIntegrationSupport.columna;
import static com.saas.integration.EsquemaIntegrationSupport.conexionInterna;
import static com.saas.integration.EsquemaIntegrationSupport.ejecutarDdl;
import static com.saas.integration.EsquemaIntegrationSupport.eliminarEsquema;
import static com.saas.integration.EsquemaIntegrationSupport.extractorPara;
import static com.saas.integration.EsquemaIntegrationSupport.tabla;
import static com.saas.integration.EsquemaIntegrationSupport.verificaReferenciasResolubles;
import static com.saas.integration.EsquemaIntegrationSupport.verificaSinColumnasDuplicadas;
import static com.saas.integration.MotorTestDatabase.abortarSiNoHayMotor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

/**
 * Valida la query de esquema de PostgreSQL contra un motor real.
 *
 * <p>Regresion principal: {@code information_schema.key_column_usage} en PostgreSQL
 * no expone {@code referenced_table_name} ni {@code referenced_column_name} (columnas
 * exclusivas de MySQL), por lo que la query debe resolver la FK emparejando
 * {@code referential_constraints} con la posicion de la columna referenciada.
 */
@DisplayName("Integracion: extraccion de esquema en PostgreSQL")
class PostgresqlSchemaExtractorIT {

    private static final String ESQUEMA = "esquema_prueba";
    private static final String BASE = "saas_test";

    private static MotorTestDatabase base;
    private static EsquemaTenant esquema;

    @BeforeAll
    static void crearEsquemaYExtraer() {
        abortarSiNoHayMotor("PostgreSQL", "it.postgres.url");
        base = MotorTestDatabase.postgres(BASE);

        eliminarEsquema(base.dataSource(), ESQUEMA, false);
        ejecutarDdl(base.dataSource(),
                "CREATE SCHEMA " + ESQUEMA,
                "CREATE TABLE " + ESQUEMA + ".usuarios ("
                        + "id serial PRIMARY KEY, "
                        + "email varchar(255) NOT NULL UNIQUE, "
                        + "edad integer, "
                        + "created_at timestamp NOT NULL)",
                "CREATE TABLE " + ESQUEMA + ".pedidos ("
                        + "id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, "
                        + "usuario_id integer NOT NULL, "
                        + "fecha timestamptz NOT NULL, "
                        + "total numeric(12,2), "
                        + "CONSTRAINT fk_pedidos_usuario FOREIGN KEY (usuario_id) "
                        + "REFERENCES " + ESQUEMA + ".usuarios (id))",
                "CREATE TABLE " + ESQUEMA + ".pedido_detalle ("
                        + "pedido_id bigint NOT NULL, "
                        + "linea integer NOT NULL, "
                        + "descripcion text, "
                        + "PRIMARY KEY (pedido_id, linea), "
                        + "CONSTRAINT fk_detalle_pedido FOREIGN KEY (pedido_id) "
                        + "REFERENCES " + ESQUEMA + ".pedidos (id))",
                "CREATE TABLE " + ESQUEMA + ".pagos ("
                        + "id serial PRIMARY KEY, "
                        + "pedido_id bigint NOT NULL, "
                        + "usuario_id integer NOT NULL, "
                        + "monto numeric(12,2) NOT NULL, "
                        + "CONSTRAINT fk_pago_pedido FOREIGN KEY (pedido_id) "
                        + "REFERENCES " + ESQUEMA + ".pedidos (id), "
                        + "CONSTRAINT fk_pago_usuario FOREIGN KEY (usuario_id) "
                        + "REFERENCES " + ESQUEMA + ".usuarios (id))",
                "CREATE TABLE " + ESQUEMA + ".regiones ("
                        + "pais varchar(2) NOT NULL, "
                        + "codigo integer NOT NULL, "
                        + "nombre varchar(100) NOT NULL, "
                        + "PRIMARY KEY (pais, codigo))",
                "CREATE TABLE " + ESQUEMA + ".sucursales ("
                        + "id serial PRIMARY KEY, "
                        + "region_pais varchar(2) NOT NULL, "
                        + "region_codigo integer NOT NULL, "
                        + "nombre varchar(100) NOT NULL, "
                        + "CONSTRAINT fk_sucursal_region FOREIGN KEY (region_pais, region_codigo) "
                        + "REFERENCES " + ESQUEMA + ".regiones (pais, codigo))");

        esquema = extractorPara(base.dataSource()).extraer(conexionInterna(BASE, ESQUEMA));
    }

    @AfterAll
    static void cerrarBase() {
        if (base != null) {
            base.cerrar();
        }
    }

    @Test
    void laQueryEsValidaEnPostgresqlYElExtractorNoFalla() {
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

        assertThat(columna(esquema, "usuarios", "id").tipoDato()).isEqualTo("integer");
        assertThat(columna(esquema, "usuarios", "id").nullable()).isFalse();
        assertThat(columna(esquema, "usuarios", "email").tipoDato()).isEqualTo("character varying");
        assertThat(columna(esquema, "usuarios", "email").nullable()).isFalse();
        assertThat(columna(esquema, "usuarios", "edad").tipoDato()).isEqualTo("integer");
        assertThat(columna(esquema, "usuarios", "edad").nullable()).isTrue();
        assertThat(columna(esquema, "pedidos", "total").tipoDato()).isEqualTo("numeric");
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
    void emparejaCadaColumnaDeUnaForeignKeyCompuestaConSuColumnaOrigen() {
        assertThat(columna(esquema, "sucursales", "region_pais").esForeignKey()).isTrue();
        assertThat(columna(esquema, "sucursales", "region_pais").tablaReferenciada())
                .isEqualToIgnoringCase("regiones");
        assertThat(columna(esquema, "sucursales", "region_pais").columnaReferenciada())
                .isEqualToIgnoringCase("pais");

        assertThat(columna(esquema, "sucursales", "region_codigo").esForeignKey()).isTrue();
        assertThat(columna(esquema, "sucursales", "region_codigo").tablaReferenciada())
                .isEqualToIgnoringCase("regiones");
        assertThat(columna(esquema, "sucursales", "region_codigo").columnaReferenciada())
                .isEqualToIgnoringCase("codigo");

        verificaSinColumnasDuplicadas(esquema);
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
