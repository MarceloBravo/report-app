package com.saas.report.shared.domain.valueobject;

import java.util.Arrays;

/**
 * Motores de base de datos soportados para las conexiones de los tenants.
 *
 * <p>Es un concepto compartido entre módulos (conexión, consulta y onboarding),
 * por eso vive en {@code shared} como <em>shared kernel</em>. Cada valor conoce
 * únicamente el subprotocolo JDBC con el que se construye la URL de conexión;
 * la etiqueta para mostrar es una preocupación de presentación que se resuelve
 * en {@code com.saas.report.shared.presentation.mapper.MotorBaseDatosPresenter}.
 *
 * <p>El backend del SaaS sigue usando PostgreSQL; este enum solo describe la
 * base <em>del tenant</em>.
 */
public enum MotorBaseDatos {

    POSTGRESQL("postgresql"),
    MYSQL("mysql");

    private final String subprotocoloJdbc;

    MotorBaseDatos(String subprotocoloJdbc) {
        this.subprotocoloJdbc = subprotocoloJdbc;
    }

    public String getSubprotocoloJdbc() {
        return subprotocoloJdbc;
    }

    /**
     * Normaliza el nombre que devuelve {@code DatabaseMetaData.getDatabaseProductName()}.
     *
     * @param producto nombre del producto devuelto por el driver JDBC
     * @return el motor soportado correspondiente
     * @throws IllegalArgumentException si el motor no está soportado
     */
    public static MotorBaseDatos desdeProducto(String producto) {
        if (producto == null || producto.isBlank()) {
            throw new IllegalArgumentException("El nombre del motor de base de datos no puede estar vacío");
        }
        String normalizado = producto.trim().toLowerCase();
        return Arrays.stream(values())
                .filter(motor -> normalizado.contains(motor.subprotocoloJdbc))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Motor de base de datos no soportado: " + producto));
    }
}
