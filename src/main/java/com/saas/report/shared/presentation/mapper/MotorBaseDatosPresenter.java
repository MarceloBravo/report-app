package com.saas.report.shared.presentation.mapper;

import com.saas.report.shared.domain.valueobject.MotorBaseDatos;

/**
 * Traducción del motor de base de datos a su etiqueta para mostrar.
 *
 * <p>El nombre visible es una preocupación de presentación, por eso no vive en
 * el enum de dominio: aquí se centraliza para que las respuestas de la API no
 * acoplen el modelo a un texto concreto. El {@code switch} exhaustivo garantiza
 * que añadir un motor nuevo obliga a actualizar también su etiqueta.
 */
public final class MotorBaseDatosPresenter {

    private MotorBaseDatosPresenter() {
    }

    public static String nombreAMostrar(MotorBaseDatos motor) {
        if (motor == null) {
            return null;
        }
        return switch (motor) {
            case POSTGRESQL -> "PostgreSQL";
            case MYSQL -> "MySQL";
        };
    }
}
