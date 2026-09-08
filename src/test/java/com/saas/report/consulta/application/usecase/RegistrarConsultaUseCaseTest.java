package com.saas.report.consulta.application.usecase;

import com.saas.report.consulta.application.dto.ConsultaRequest;
import com.saas.report.consulta.application.dto.ConsultaResponse;
import com.saas.report.consulta.domain.ConsultaHistorial;
import com.saas.report.consulta.domain.ConsultaHistorialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrarConsultaUseCaseTest {

    @Mock
    private ConsultaHistorialRepository consultaRepository;

    private RegistrarConsultaUseCase registrarConsultaUseCase;

    @BeforeEach
    void setUp() {
        registrarConsultaUseCase = new RegistrarConsultaUseCase(consultaRepository);
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
        UUID tenantId = request.tenantId();

        when(consultaRepository.countByTenantIdAndFechaConsultaAfter(any(), any(LocalDateTime.class)))
                .thenReturn(4L);
        when(consultaRepository.save(any(ConsultaHistorial.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConsultaResponse response = registrarConsultaUseCase.registrar(request);

        ArgumentCaptor<ConsultaHistorial> captor = ArgumentCaptor.forClass(ConsultaHistorial.class);
        verify(consultaRepository).save(captor.capture());
        ConsultaHistorial guardada = captor.getValue();
        assertThat(guardada.getTenantId().getValue()).isEqualTo(tenantId);
        assertThat(guardada.getPreguntaUsuario()).isEqualTo("¿Cuántas ventas hubo en marzo?");
        assertThat(guardada.getQuerySqlGenerada()).isEqualTo("SELECT * FROM ventas;");
        assertThat(guardada.getExitosa()).isTrue();
        assertThat(guardada.getRegistrosCount()).isEqualTo(42);
        assertThat(guardada.getConsultasEnPeriodo()).isEqualTo(5);

        assertThat(response.consultasEnPeriodo()).isEqualTo(5);
        assertThat(response.consultaId()).isEqualTo(guardada.getConsultaId().getValue());
        assertThat(response.fechaConsulta()).isNotNull();
    }
}