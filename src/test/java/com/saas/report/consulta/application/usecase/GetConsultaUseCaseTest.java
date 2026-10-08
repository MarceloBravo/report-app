package com.saas.report.consulta.application.usecase;

import com.saas.report.consulta.application.dto.ConsultaResponse;
import com.saas.report.consulta.domain.ConsultaHistorial;
import com.saas.report.consulta.domain.ConsultaHistorialRepository;
import com.saas.report.consulta.domain.valueobject.ConsultaHistorialUUID;
import com.saas.report.shared.domain.valueobject.UsuarioUUID;
import com.saas.report.shared.exception.ResourceNotFoundException;
import com.saas.report.shared.domain.valueobject.TenantUUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetConsultaUseCaseTest {

    @Mock
    private ConsultaHistorialRepository consultaRepository;

    private GetConsultaUseCase getConsultaUseCase;

    @BeforeEach
    void setUp() {
        getConsultaUseCase = new GetConsultaUseCase(consultaRepository);
    }

    private ConsultaHistorial consulta() {
        return new ConsultaHistorial(ConsultaHistorialUUID.generate(), TenantUUID.generate(),
                UsuarioUUID.generate(), "¿Cuántas ventas?", "SELECT 1;", LocalDateTime.now(),
                true, 5, null, 1);
    }

    @Test
    void obtieneYExponeLaConsulta() {
        ConsultaHistorial consulta = consulta();

        when(consultaRepository.findById(any(ConsultaHistorialUUID.class))).thenReturn(Optional.of(consulta));

        ConsultaResponse response = getConsultaUseCase.obtener(consulta.getConsultaId());

        assertThat(response.consultaId()).isEqualTo(consulta.getConsultaId().getValue());
        assertThat(response.preguntaUsuario()).isEqualTo("¿Cuántas ventas?");
    }

    @Test
    void lanzaExcepcionSiNoExiste() {
        when(consultaRepository.findById(any(ConsultaHistorialUUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getConsultaUseCase.obtener(ConsultaHistorialUUID.generate()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}