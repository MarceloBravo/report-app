package com.saas.integration;

import com.saas.report.conexion.domain.connection.CredentialCipher;
import com.saas.report.conexion.domain.connection.TenantDbConnection;
import com.saas.report.conexion.domain.connection.TipoConexion;
import com.saas.report.conexion.domain.schema.ColumnaEsquema;
import com.saas.report.conexion.domain.schema.EsquemaTenant;
import com.saas.report.conexion.domain.schema.TablaEsquema;
import com.saas.report.conexion.domain.valueobject.TenantDbConnectionUUID;
import com.saas.report.tenant.domain.valueobject.TenantUUID;
import com.saas.report.conexion.infrastructure.schema.JdbcSchemaExtractor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Utilidades compartidas por los tests de integracion de extraccion de esquema.
 *
 * <p>Los tests de integracion ejecutan las queries de
 * {@link com.saas.report.conexion.infrastructure.schema.strategy.SchemaQueryProvider}
 * contra motores reales, porque las columnas disponibles en {@code information_schema}
 * difieren entre MySQL y PostgreSQL y un mock de {@code ResultSet} no puede detectarlas.
 */
public final class EsquemaIntegrationSupport {

    private EsquemaIntegrationSupport() {
    }

    /**
     * Crea un extractor real usando conexion interna, para que reutilice el
     * {@code DataSource} provisto en lugar de abrir una conexion propia.
     */
    public static JdbcSchemaExtractor extractorPara(DataSource dataSource) {
        return new JdbcSchemaExtractor(credentialCipherSinUso(), dataSource);
    }

    public static TenantDbConnection conexionInterna(String dbName, String schemaName) {
        return new TenantDbConnection(
                TenantDbConnectionUUID.generate(),
                TenantUUID.generate(),
                "localhost", 5432, dbName, "saas", "CIFRADO:no-se-usa",
                TipoConexion.INTERNAL_SCHEMA, schemaName);
    }

    public static void ejecutarDdl(DataSource dataSource, String... sentencias) {
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            for (String sentencia : sentencias) {
                statement.execute(sentencia);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo crear el esquema de prueba: " + e.getMessage(), e);
        }
    }

    /**
     * Elimina el esquema de prueba para que el test sea idempotente entre
     * ejecuciones. En MySQL el esquema es la base de datos, por lo que se Dropea
     * como base; en PostgreSQL se Dropea como schema con cascada.
     */
    public static void eliminarEsquema(DataSource dataSource, String nombre, boolean esMysql) {
        ejecutarDdl(dataSource, esMysql
                ? "DROP DATABASE IF EXISTS " + nombre
                : "DROP SCHEMA IF EXISTS " + nombre + " CASCADE");
    }

    public static TablaEsquema tabla(EsquemaTenant esquema, String nombre) {
        return esquema.tablas().stream()
                .filter(t -> t.nombre().equalsIgnoreCase(nombre))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "No se encontro la tabla '" + nombre + "'. Tablas: " + nombresDe(esquema)));
    }

    public static ColumnaEsquema columna(EsquemaTenant esquema, String tabla, String columna) {
        return tabla(esquema, tabla).columnas().stream()
                .filter(c -> c.nombre().equalsIgnoreCase(columna))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "No se encontro la columna '" + tabla + "." + columna + "'"));
    }

    /**
     * Verifica que cada columna referenciada por una FK exista efectivamente en
     * la tabla destino reportada por la query.
     */
    public static void verificaReferenciasResolubles(EsquemaTenant esquema) {
        esquema.tablas().forEach(tabla -> tabla.columnas().stream()
                .filter(ColumnaEsquema::esForeignKey)
                .forEach(columna -> {
                    if (columna.tablaReferenciada() == null || columna.columnaReferenciada() == null) {
                        throw new AssertionError("FK marcada pero sin datos de referencia: " + columna);
                    }
                    boolean existe = esquema.tablas().stream()
                            .anyMatch(t -> t.nombre().equalsIgnoreCase(columna.tablaReferenciada())
                                    && t.columnas().stream()
                                    .anyMatch(c -> c.nombre()
                                            .equalsIgnoreCase(columna.columnaReferenciada())));
                    if (!existe) {
                        throw new AssertionError("La FK " + columna + " apunta a una columna inexistente: "
                                + columna.tablaReferenciada() + "." + columna.columnaReferenciada());
                    }
                }));
    }

    public static void verificaSinColumnasDuplicadas(EsquemaTenant esquema) {
        esquema.tablas().forEach(tabla -> {
            List<String> nombres = tabla.columnas().stream().map(ColumnaEsquema::nombre).toList();
            if (nombres.size() != nombres.stream().distinct().count()) {
                throw new AssertionError("Columnas duplicadas en " + tabla.nombre() + ": " + nombres);
            }
        });
    }

    public static List<String> nombresDe(EsquemaTenant esquema) {
        return esquema.tablas().stream().map(TablaEsquema::nombre).toList();
    }

    private static CredentialCipher credentialCipherSinUso() {
        return new CredentialCipher() {
            @Override
            public String cifrar(String textoPlano) {
                throw new UnsupportedOperationException("No se usa en conexion interna");
            }

            @Override
            public String descifrar(String textoCifrado) {
                throw new UnsupportedOperationException("No se usa en conexion interna");
            }
        };
    }
}
