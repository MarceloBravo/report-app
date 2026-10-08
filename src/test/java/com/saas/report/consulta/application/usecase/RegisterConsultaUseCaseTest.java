package com.saas.report.consulta.application.usecase;

import com.saas.report.consulta.application.dto.ConsultaRequest;
import com.saas.report.consulta.application.dto.ConsultaResponse;
import com.saas.report.consulta.domain.ConsultaHistorial;
import com.saas.report.consulta.domain.ConsultaHistorialRepository;
import com.saas.report.consulta.domain.ValidadorLimiteConsultasPort;
import com.saas.report.consulta.domain.exception.LimiteConsultasExcedidoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterConsultaUseCaseTest {

    @Mock
    private ConsultaHistorialRepository consultaRepository;

    @Mock
    private ValidadorLimiteConsultasPort validadorLimiteConsultasPort;

    private RegisterConsultaUseCase registerConsultaUseCase;

    @BeforeEach
    void setUp() {
        registerConsultaUseCase = new RegisterConsultaUseCase(
                consultaRepository, validadorLimiteConsultasPort);
    }

    private ConsultaRequest request() {
        return new ConsultaRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "¿Cuántas ventas hubo en marzo?",
                "SELECT * FROM ventas;",
                true,
                42,
                null);
    }

    @Test
    void registraConsultaCalculandoElConsumoDelPeriodo() {
        ConsultaRequest request = request();

        when(consultaRepository.countByTenantIdAndFechaConsultaAfter(any(), any(LocalDateTime.class)))
                .thenReturn(4L);
        when(consultaRepository.save(any(ConsultaHistorial.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = registerConsultaUseCase.registrar(request);

        var captor = org.mockito.ArgumentCaptor.forClass(ConsultaHistorial.class);
        verify(consultaRepository).save(captor.capture());
        ConsultaHistorial guardada = captor.getValue();
        assertThat(guardada.getTenantId().getValue()).isEqualTo(request.tenantId());
        assertThat(guardada.getPreguntaUsuario()).isEqualTo("¿Cuántas ventas hubo en marzo?");
        assertThat(guardada.getQuerySqlGenerada()).isEqualTo("SELECT * FROM ventas;");
        assertThat(guardada.getExitosa()).isTrue();
        assertThat(guardada.getRegistrosCount()).isEqualTo(42);
        assertThat(guardada.getConsultasEnPeriodo()).isEqualTo(5);

        assertThat(response.consultasEnPeriodo()).isEqualTo(5);
        assertThat(response.fechaConsulta()).isNotNull();
    }

    @Test
    void permiteCuandoElValidadorNoLanza() {
        when(consultaRepository.countByTenantIdAndFechaConsultaAfter(any(), any(LocalDateTime.class)))
                .thenReturn(4L);
        when(consultaRepository.save(any(ConsultaHistorial.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = registerConsultaUseCase.registrar(request());

        assertThat(response.consultasEnPeriodo()).isEqualTo(5);
    }

    @Test
    void rechazaCuandoSeExcedeElLimiteDelPlan() {
        when(consultaRepository.countByTenantIdAndFechaConsultaAfter(any(), any(LocalDateTime.class)))
                .thenReturn(5L);
        doThrow(new LimiteConsultasExcedidoException("Se excedió el límite del plan"))
                .when(validadorLimiteConsultasPort).validarLimiteMensual(any(), any());

        assertThatThrownBy(() -> registerConsultaUseCase.registrar(request()))
                .isInstanceOf(LimiteConsultasExcedidoException.class)
                .hasMessageContaining("límite");
    }
}