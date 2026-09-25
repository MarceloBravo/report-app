package com.saas.integration;

import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.DockerClientFactory;

import javax.sql.DataSource;

/**
 * Resuelve la base de datos real contra la que se ejecuta un test de integracion.
 *
 * <p>Prioridad de resolucion:
 * <ol>
 *   <li>Si se pasa la system property {@code it.postgres.url} / {@code it.mysql.url},
 *       se usa esa conexion externa (util para CI o entornos con Docker restringido).</li>
 *   <li>Si no, se levanta un contenedor efimero con Testcontainers.</li>
 *   <li>Si no hay Docker disponible, se aborta el test con un mensaje explicito
 *       en lugar de fallar la build.</li>
 * </ol>
 *
 * <p>En Windows con Docker Desktop, Testcontainers necesita que el servicio
 * {@code com.docker.service} este iniciado o {@code DOCKER_HOST} apuntando al pipe
 * del engine; si no, se aborta el test.
 */
public final class MotorTestDatabase {

    private final JdbcDatabaseContainer<?> contenedor;
    private final DataSource dataSource;
    private final String nombreBase;

    private MotorTestDatabase(JdbcDatabaseContainer<?> contenedor, DataSource dataSource, String nombreBase) {
        this.contenedor = contenedor;
        this.dataSource = dataSource;
        this.nombreBase = nombreBase;
    }

    public static MotorTestDatabase postgres(String nombreBase) {
        String url = System.getProperty("it.postgres.url");
        String user = System.getProperty("it.postgres.user", "saas");
        String password = System.getProperty("it.postgres.password", "saas");

        if (url != null && !url.isBlank()) {
            return new MotorTestDatabase(null,
                    dataSource("org.postgresql.Driver", url, user, password), nombreBase);
        }

        PostgreSQLContainer<?> contenedor = new PostgreSQLContainer<>("postgres:16-alpine")
                .withDatabaseName(nombreBase)
                .withUsername(user)
                .withPassword(password);
        return new MotorTestDatabase(contenedor,
                dataSource("org.postgresql.Driver", contenedor.getJdbcUrl(), user, password), nombreBase);
    }

    public static MotorTestDatabase mysql(String nombreBase) {
        String url = System.getProperty("it.mysql.url");
        String user = System.getProperty("it.mysql.user", "saas");
        String password = System.getProperty("it.mysql.password", "saas");

        if (url != null && !url.isBlank()) {
            return new MotorTestDatabase(null,
                    dataSource("com.mysql.cj.jdbc.Driver", conParametrosMysql(url), user, password), nombreBase);
        }

        MySQLContainer<?> contenedor = new MySQLContainer<>("mysql:8.4")
                .withDatabaseName(nombreBase)
                .withUsername(user)
                .withPassword(password);
        return new MotorTestDatabase(contenedor,
                dataSource("com.mysql.cj.jdbc.Driver", conParametrosMysql(contenedor.getJdbcUrl()), user, password),
                nombreBase);
    }

    public DataSource dataSource() {
        return dataSource;
    }

    public String nombreBase() {
        return nombreBase;
    }

    public void cerrar() {
        if (contenedor != null) {
            contenedor.stop();
        }
    }

    public static boolean dockerDisponible() {
        // El sondeo de Testcontainers loguea un error enorme con el detalle de cada
        // estrategia fallida; se silencia para no ensuciar la salida cuando lo
        // unico que se hace es decidir si el test se omite.
        ch.qos.logback.classic.Logger sondeador =
                (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger("org.testcontainers");
        ch.qos.logback.classic.Level nivelPrevio = sondeador.getLevel();
        sondeador.setLevel(ch.qos.logback.classic.Level.OFF);
        try {
            DockerClientFactory.instance().client();
            return true;
        } catch (RuntimeException e) {
            return false;
        } finally {
            sondeador.setLevel(nivelPrevio);
        }
    }

    public static void abortarSiNoHayMotor(String motor, String systemProperty) {
        String url = System.getProperty(systemProperty);
        if (url != null && !url.isBlank()) {
            return;
        }
        if (!dockerDisponible()) {
            throw new TestAbortado("No hay motor " + motor
                    + " disponible: se requiere Docker para Testcontainers o la property " + systemProperty);
        }
    }

    /**
     * MySQL 8.4 usa caching_sha2_password, que exige SSL o la recuperacion de la
     * clave publica para autenticar contra un servidor sin SSL configurado.
     */
    private static String conParametrosMysql(String url) {
        String separator = url.contains("?") ? "&" : "?";
        return url + separator + "useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }

    private static DataSource dataSource(String driver, String url, String user, String password) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driver);
        dataSource.setUrl(url);
        dataSource.setUsername(user);
        dataSource.setPassword(password);
        return dataSource;
    }

    /** Aborta el test en lugar de fallarlo cuando no hay motor disponible. */
    public static final class TestAbortado extends org.opentest4j.TestAbortedException {
        public TestAbortado(String mensaje) {
            super("[TEST OMITIDO] " + mensaje);
        }
    }
}
