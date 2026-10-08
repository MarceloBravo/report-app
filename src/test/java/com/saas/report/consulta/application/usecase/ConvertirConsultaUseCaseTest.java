package com.saas.report.consulta.application.usecase;

import com.saas.report.consulta.application.dto.ConvertirConsultaRequest;
import com.saas.report.consulta.application.dto.ConsultaResponse;
import com.saas.report.consulta.domain.ConsultaHistorial;
import com.saas.report.consulta.domain.ConsultaHistorialRepository;
import com.saas.report.consulta.domain.GeneradorSqlPort;
import com.saas.report.consulta.domain.SqlEjecutorPort;
import com.saas.report.consulta.domain.ValidadorLimiteConsultasPort;
import com.saas.report.consulta.domain.exception.LimiteConsultasExcedidoException;
import com.saas.report.consulta.domain.exception.SqlInvalidoException;
import com.saas.report.plan.domain.Plan;
import com.saas.report.plan.domain.PlanPrecio;
import com.saas.report.plan.domain.PlanRepository;
import com.saas.report.plan.domain.valueobject.PlanLimiteConsultasMensuales;
import com.saas.report.plan.domain.valueobject.PlanMaxUsuarios;
import com.saas.report.plan.domain.valueobject.PlanPrecioUUID;
import com.saas.report.plan.domain.valueobject.PlanUUID;
import com.saas.report.shared.exception.ResourceNotFoundException;
import com.saas.report.suscripcion.domain.Suscripcion;
import com.saas.report.suscripcion.domain.SuscripcionEstado;
import com.saas.report.suscripcion.domain.SuscripcionRepository;
import com.saas.report.suscripcion.domain.valueobject.SuscripcionUUID;
import com.saas.report.conexion.domain.connection.TenantDbConnection;
import com.saas.report.conexion.domain.connection.TenantDbConnectionRepository;
import com.saas.report.conexion.domain.connection.TipoConexion;
import com.saas.report.conexion.domain.schema.ColumnaEsquema;
import com.saas.report.conexion.domain.schema.EsquemaTenant;
import com.saas.report.conexion.domain.schema.SchemaExtractor;
import com.saas.report.conexion.domain.schema.TablaEsquema;
import com.saas.report.conexion.domain.valueobject.TenantDbConnectionUUID;
import com.saas.report.shared.domain.valueobject.TenantUUID;
import com.saas.report.shared.domain.valueobject.UsuarioUUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConvertirConsultaUseCaseTest {

    @Mock
    private ConsultaHistorialRepository consultaRepository;

    @Mock
    private SuscripcionRepository suscripcionRepository;

    @Mock
    private PlanRepository planRepository;

    @Mock
    private TenantDbConnectionRepository dbConnectionRepository;

    @Mock
    private SchemaExtractor schemaExtractor;

    @Mock
    private GeneradorSqlPort generadorSqlPort;

    @Mock
    private SqlEjecutorPort sqlEjecutorPort;

    @Mock
    private ValidadorLimiteConsultasPort validadorLimiteConsultasPort;

    private ConvertirConsultaUseCase convertirConsultaUseCase;

    @BeforeEach
    void setUp() {
        convertirConsultaUseCase = new ConvertirConsultaUseCase(
                consultaRepository, dbConnectionRepository, schemaExtractor,
                generadorSqlPort, sqlEjecutorPort, validadorLimiteConsultasPort);
    }

    private ConvertirConsultaRequest request() {
        return new ConvertirConsultaRequest(
                "¿Cuántas ventas hubo en marzo?",
                null);
    }

    private EsquemaTenant esquema(TenantUUID tenantId) {
        return new EsquemaTenant(tenantId, List.of(new TablaEsquema("ventas",
                List.of(new ColumnaEsquema("ventas", "monto", "numeric", false, false, false, null, null)))), Instant.now());
    }

    private TenantDbConnection conexion(TenantUUID tenantId) {
        return new TenantDbConnection(
                TenantDbConnectionUUID.generate(), tenantId, "db.acme.com", 5432,
                "ventas", "report_ro", "CIFRADO:secreto", TipoConexion.EXTERNAL_DB, "public");
    }

    private Plan planConLimite(Integer limite) {
        PlanUUID planId = PlanUUID.generate();
        PlanPrecio tramo = new PlanPrecio(PlanPrecioUUID.generate(), planId, 1, null, new BigDecimal("9.90"));
        return new Plan(planId, "STARTER", "Plan starter", new PlanMaxUsuarios(5),
                new PlanLimiteConsultasMensuales(limite), true, List.of(tramo));
    }

    private Suscripcion suscripcionActiva(PlanUUID planId) {
        return new Suscripcion(SuscripcionUUID.generate(), TenantUUID.generate(), planId, LocalDate.now(),
                SuscripcionEstado.ACTIVE, LocalDate.now().plusMonths(1));
    }

    @Test
    void convierteLaPreguntaYSanitizaElSqlGenerado() {
        ConvertirConsultaRequest request = request();
        TenantUUID tenantId = TenantUUID.generate();
        UUID usuarioId = UUID.randomUUID();
        EsquemaTenant esquema = esquema(tenantId);

        when(consultaRepository.countByTenantIdAndFechaConsultaAfter(any(), any(LocalDateTime.class)))
                .thenReturn(4L);
        when(dbConnectionRepository.findByTenantId(tenantId)).thenReturn(Optional.of(conexion(tenantId)));
        when(schemaExtractor.extraer(any(TenantDbConnection.class))).thenReturn(esquema);
        when(generadorSqlPort.generarSql(eq(esquema), eq(request.preguntaUsuario()), eq(usuarioId)))
                .thenReturn("  SELECT * FROM ventas  ");
        when(sqlEjecutorPort.ejecutar(any(TenantDbConnection.class), any(String.class)))
                .thenReturn(List.of(Map.of("monto", 150)));
        when(consultaRepository.save(any(ConsultaHistorial.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConsultaResponse response = convertirConsultaUseCase.convertir(tenantId.getValue(), usuarioId, request);

        ArgumentCaptor<ConsultaHistorial> captor = ArgumentCaptor.forClass(ConsultaHistorial.class);
        verify(consultaRepository).save(captor.capture());
        ConsultaHistorial guardada = captor.getValue();
        assertThat(guardada.getTenantId()).isEqualTo(tenantId);
        assertThat(guardada.getUsuarioId()).isEqualTo(new UsuarioUUID(usuarioId));
        assertThat(guardada.getQuerySqlGenerada()).isEqualTo("SELECT * FROM ventas");
        assertThat(guardada.getPreguntaUsuario()).isEqualTo(request.preguntaUsuario());
        assertThat(guardada.getConsultasEnPeriodo()).isEqualTo(5);

        assertThat(response.consultasEnPeriodo()).isEqualTo(5);
        assertThat(response.querySqlGenerada()).isEqualTo("SELECT * FROM ventas");
    }

    @Test
    void ejecutaElSqlSanitizadoSobreLaConexionDelTenantYDevuelveLosDatos() {
        ConvertirConsultaRequest request = request();
        TenantUUID tenantId = TenantUUID.generate();
        UUID usuarioId = UUID.randomUUID();
        TenantDbConnection conexion = conexion(tenantId);
        List<Map<String, Object>> filas = List.of(
                Map.of("monto", 150),
                Map.of("monto", 320));

        when(consultaRepository.countByTenantIdAndFechaConsultaAfter(any(), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(dbConnectionRepository.findByTenantId(tenantId)).thenReturn(Optional.of(conexion));
        when(schemaExtractor.extraer(conexion)).thenReturn(esquema(tenantId));
        when(generadorSqlPort.generarSql(any(), any(), any())).thenReturn("SELECT monto FROM ventas");
        when(sqlEjecutorPort.ejecutar(conexion, "SELECT monto FROM ventas")).thenReturn(filas);
        when(consultaRepository.save(any(ConsultaHistorial.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConsultaResponse response = convertirConsultaUseCase.convertir(tenantId.getValue(), usuarioId, request);

        verify(sqlEjecutorPort).ejecutar(conexion, "SELECT monto FROM ventas");
        assertThat(response.data()).isEqualTo(filas);
        assertThat(response.registrosCount()).isEqualTo(2);
        assertThat(response.exitosa()).isTrue();
    }

    @Test
    void noEjecutaSqlSiLaGeneracionEsInvalida() {
        ConvertirConsultaRequest request = request();
        TenantUUID tenantId = TenantUUID.generate();
        UUID usuarioId = UUID.randomUUID();

        when(consultaRepository.countByTenantIdAndFechaConsultaAfter(any(), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(dbConnectionRepository.findByTenantId(tenantId)).thenReturn(Optional.of(conexion(tenantId)));
        when(schemaExtractor.extraer(any(TenantDbConnection.class))).thenReturn(esquema(tenantId));
        when(generadorSqlPort.generarSql(any(), any(), any())).thenReturn("DELETE FROM ventas");

        assertThatThrownBy(() -> convertirConsultaUseCase.convertir(tenantId.getValue(), usuarioId, request))
                .isInstanceOf(SqlInvalidoException.class)
                .hasMessageContaining("SELECT");

        verify(sqlEjecutorPort, never()).ejecutar(any(), any());
        verify(consultaRepository, never()).save(any());
    }

    @Test
    void rechazaLaConversionSiElSqlNoEsDeSoloLectura() {
        ConvertirConsultaRequest request = request();
        TenantUUID tenantId = TenantUUID.generate();
        UUID usuarioId = UUID.randomUUID();

        when(consultaRepository.countByTenantIdAndFechaConsultaAfter(any(), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(dbConnectionRepository.findByTenantId(tenantId)).thenReturn(Optional.of(conexion(tenantId)));
        when(schemaExtractor.extraer(any(TenantDbConnection.class))).thenReturn(esquema(tenantId));
        when(generadorSqlPort.generarSql(any(), any(), any())).thenReturn("DELETE FROM ventas");

        assertThatThrownBy(() -> convertirConsultaUseCase.convertir(tenantId.getValue(), usuarioId, request))
                .isInstanceOf(SqlInvalidoException.class)
                .hasMessageContaining("SELECT");
    }

    @Test
    void rechazaCuandoSeExcedeElLimiteDelPlan() {
        ConvertirConsultaRequest request = request();
        TenantUUID tenantId = TenantUUID.generate();
        UUID usuarioId = UUID.randomUUID();

        when(consultaRepository.countByTenantIdAndFechaConsultaAfter(any(), any(LocalDateTime.class)))
                .thenReturn(5L);
        org.mockito.Mockito.doThrow(new LimiteConsultasExcedidoException("Se excedió el límite del plan"))
                .when(validadorLimiteConsultasPort).validarLimiteMensual(any(), any());

        assertThatThrownBy(() -> convertirConsultaUseCase.convertir(tenantId.getValue(), usuarioId, request))
                .isInstanceOf(LimiteConsultasExcedidoException.class)
                .hasMessageContaining("límite");
    }

    @Test
    void rechazaCuandoNoExisteConexionParaElTenant() {
        ConvertirConsultaRequest request = request();
        TenantUUID tenantId = TenantUUID.generate();
        UUID usuarioId = UUID.randomUUID();

        when(consultaRepository.countByTenantIdAndFechaConsultaAfter(any(), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(dbConnectionRepository.findByTenantId(tenantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> convertirConsultaUseCase.convertir(tenantId.getValue(), usuarioId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("conexión");
    }

    @Test
    void usaLaConexionExplicitaCuandoPerteneceAlTenant() {
        TenantUUID tenantId = TenantUUID.generate();
        UUID usuarioId = UUID.randomUUID();
        TenantDbConnection propia = conexion(tenantId);
        ConvertirConsultaRequest request = new ConvertirConsultaRequest(
                "¿Cuántas ventas hubo en marzo?", propia.getDbConnectionId().getValue());

        when(consultaRepository.countByTenantIdAndFechaConsultaAfter(any(), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(dbConnectionRepository.findById(propia.getDbConnectionId())).thenReturn(Optional.of(propia));
        when(schemaExtractor.extraer(propia)).thenReturn(esquema(tenantId));
        when(generadorSqlPort.generarSql(any(), any(), any())).thenReturn("SELECT * FROM ventas");
        when(sqlEjecutorPort.ejecutar(propia, "SELECT * FROM ventas")).thenReturn(List.of(Map.of("monto", 150)));
        when(consultaRepository.save(any(ConsultaHistorial.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConsultaResponse response = convertirConsultaUseCase.convertir(tenantId.getValue(), usuarioId, request);

        verify(schemaExtractor).extraer(propia);
        verify(dbConnectionRepository, never()).findByTenantId(any());
        assertThat(response.exitosa()).isTrue();
    }

    @Test
    void rechazaLaConexionExplicitaQuePerteneceAOtroTenant() {
        TenantUUID tenantId = TenantUUID.generate();
        UUID usuarioId = UUID.randomUUID();
        TenantDbConnection ajena = conexion(TenantUUID.generate());
        ConvertirConsultaRequest request = new ConvertirConsultaRequest(
                "¿Cuántas ventas hubo en marzo?", ajena.getDbConnectionId().getValue());

        when(consultaRepository.countByTenantIdAndFechaConsultaAfter(any(), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(dbConnectionRepository.findById(ajena.getDbConnectionId())).thenReturn(Optional.of(ajena));

        assertThatThrownBy(() -> convertirConsultaUseCase.convertir(tenantId.getValue(), usuarioId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("conexión");

        verify(schemaExtractor, never()).extraer(any());
        verify(sqlEjecutorPort, never()).ejecutar(any(), any());
        verify(consultaRepository, never()).save(any());
    }
}