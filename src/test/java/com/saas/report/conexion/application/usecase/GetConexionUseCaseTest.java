package com.saas.report.conexion.application.usecase;

import com.saas.report.conexion.application.dto.ConexionDetailResponse;
import com.saas.report.shared.domain.valueobject.MotorBaseDatos;
import com.saas.report.conexion.domain.connection.TenantDbConnection;
import com.saas.report.conexion.domain.connection.TenantDbConnectionRepository;
import com.saas.report.conexion.domain.connection.TipoConexion;
import com.saas.report.conexion.domain.valueobject.TenantDbConnectionUUID;
import com.saas.report.shared.exception.ResourceNotFoundException;
import com.saas.report.shared.domain.valueobject.TenantUUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetConexionUseCaseTest {

    private static final TenantUUID TENANT_ID = new TenantUUID(UUID.fromString("11111111-2222-3333-4444-555555555555"));
    private static final TenantUUID OTRO_TENANT = new TenantUUID(UUID.fromString("99999999-8888-7777-6666-555555555555"));
    private static final TenantDbConnectionUUID CONEXION_ID =
            new TenantDbConnectionUUID(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"));

    @Mock
    private TenantDbConnectionRepository connectionRepository;

    private GetConexionUseCase getConexionUseCase;

    @BeforeEach
    void setUp() {
        getConexionUseCase = new GetConexionUseCase(connectionRepository);
    }

    @Test
    void devuelveElDetalleDeUnaConexionDelTenant() {
        when(connectionRepository.findById(CONEXION_ID))
                .thenReturn(Optional.of(conexion(TENANT_ID, "db.acme.com", "ventas", "public")));

        ConexionDetailResponse response = getConexionUseCase.obtener(TENANT_ID, CONEXION_ID);

        assertThat(response.dbConnectionId()).isEqualTo(CONEXION_ID.getValue());
        assertThat(response.host()).isEqualTo("db.acme.com");
        assertThat(response.dbName()).isEqualTo("ventas");
        assertThat(response.schemaName()).isEqualTo("public");
        assertThat(response.motorBd()).isEqualTo("PostgreSQL");
    }

    @Test
    void rechazaUnaConexionDeOtroTenant() {
        when(connectionRepository.findById(CONEXION_ID)).thenReturn(Optional.of(conexion(OTRO_TENANT, "db.acme.com", "ventas", "public")));

        assertThatThrownBy(() -> getConexionUseCase.obtener(TENANT_ID, CONEXION_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No se encontró la conexión " + CONEXION_ID.getValue());
    }

    @Test
    void rechazaUnaConexionInexistente() {
        when(connectionRepository.findById(CONEXION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getConexionUseCase.obtener(TENANT_ID, CONEXION_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No se encontró la conexión " + CONEXION_ID.getValue());
    }

    private TenantDbConnection conexion(TenantUUID tenantId, String host, String dbName, String schemaName) {
        return new TenantDbConnection(CONEXION_ID, tenantId, host, 5432, dbName,
                "report_app", "enc:clave", TipoConexion.EXTERNAL_DB, schemaName, MotorBaseDatos.POSTGRESQL);
    }
}