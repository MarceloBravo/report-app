package com.saas.report.conexion.domain.connection;

public interface CredentialCipher {

    String cifrar(String valorPlano);

    String descifrar(String valorCifrado);
}