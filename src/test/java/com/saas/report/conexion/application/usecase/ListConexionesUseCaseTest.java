package com.saas.report.conexion.application.usecase;

import com.saas.report.conexion.application.dto.ConexionListResponse;
import com.saas.report.conexion.domain.connection.TenantDbConnection;
import com.saas.report.conexion.domain.connection.TenantDbConnectionRepository;
import com.saas.report.conexion.domain.connection.TipoConexion;
import com.saas.report.conexion.domain.valueobject.TenantDbConnectionUUID;
import com.saas.report.tenant.domain.valueobject.TenantUUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListConexionesUseCaseTest {

    private static final TenantUUID TENANT_ID = new TenantUUID(UUID.fromString("11111111-2222-3333-4444-555555555555"));

    @Mock
    private TenantDbConnectionRepository connectionRepository;

    private ListConexionesUseCase listConexionesUseCase;

    @BeforeEach
    void setUp() {
        listConexionesUseCase = new ListConexionesUseCase(connectionRepository);
    }

    @Test
    void mapeaCadaConexionDelTenantAResponse() {
        TenantDbConnection primera = conexion("ventas", "public");
        TenantDbConnection segunda = conexion("finanzas", "finanzas");
        when(connectionRepository.findAllByTenantId(TENANT_ID))
                .thenReturn(List.of(primera, segunda));

        List<ConexionListResponse> response = listConexionesUseCase.listar(TENANT_ID);

        assertThat(response).hasSize(2);
        assertThat(response.get(0).dbConnectionId()).isEqualTo(primera.getDbConnectionId().getValue());
        assertThat(response.get(0).dbName()).isEqualTo("ventas");
        assertThat(response.get(0).schemaName()).isEqualTo("public");
        assertThat(response.get(1).dbName()).isEqualTo("finanzas");
        assertThat(response.get(1).schemaName()).isEqualTo("finanzas");
        verify(connectionRepository).findAllByTenantId(TENANT_ID);
    }

    @Test
    void devuelveListaVaciaSiElTenantNoTieneConexiones() {
        when(connectionRepository.findAllByTenantId(TENANT_ID)).thenReturn(List.of());

        List<ConexionListResponse> response = listConexionesUseCase.listar(TENANT_ID);

        assertThat(response).isEmpty();
    }

    private TenantDbConnection conexion(String dbName, String schemaName) {
        return new TenantDbConnection(TenantDbConnectionUUID.generate(), TENANT_ID, "db.acme.com", 5432,
                dbName, "report_app", "enc:clave", TipoConexion.EXTERNAL_DB, schemaName);
    }
}