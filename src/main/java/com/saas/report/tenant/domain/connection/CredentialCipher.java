package com.saas.report.tenant.domain.connection;

public interface CredentialCipher {

    String cifrar(String valorPlano);

    String descifrar(String valorCifrado);
}