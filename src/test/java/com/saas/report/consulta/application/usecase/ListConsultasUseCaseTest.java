package com.saas.report.consulta.application.usecase;

import com.saas.report.consulta.application.dto.ConsultaResponse;
import com.saas.report.consulta.domain.ConsultaHistorial;
import com.saas.report.consulta.domain.ConsultaHistorialRepository;
import com.saas.report.consulta.domain.valueobject.ConsultaHistorialUUID;
import com.saas.report.usuarios.domain.valueobject.UsuarioUUID;
import com.saas.report.tenant.domain.valueobject.TenantDbConnectionUUID;
import com.saas.report.tenant.domain.valueobject.TenantUUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListConsultasUseCaseTest {

    @Mock
    private ConsultaHistorialRepository consultaRepository;

    private ListConsultasUseCase listConsultasUseCase;

    @BeforeEach
    void setUp() {
        listConsultasUseCase = new ListConsultasUseCase(consultaRepository);
    }

    private ConsultaHistorial consulta(String pregunta, int periodo) {
        return new ConsultaHistorial(ConsultaHistorialUUID.generate(), TenantUUID.generate(),
                UsuarioUUID.generate(), pregunta, "SELECT 1;", LocalDateTime.now(), true, 5,
                TenantDbConnectionUUID.generate(), periodo);
    }

    @Test
    void mapeaCadaConsultaDeLaPaginaAResponse() {
        TenantUUID tenantId = TenantUUID.generate();
        when(consultaRepository.findByTenantId(tenantId, 0, 10))
                .thenReturn(new PageImpl<>(List.of(consulta("¿Cuántas ventas?", 3), consulta("¿Cuántos clientes?", 2)),
                        PageRequest.of(0, 10), 2));

        Page<ConsultaResponse> response = listConsultasUseCase.listar(tenantId, 0, 10);

        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getContent().get(0).preguntaUsuario()).isEqualTo("¿Cuántas ventas?");
        assertThat(response.getContent().get(0).consultasEnPeriodo()).isEqualTo(3);
    }

    @Test
    void conservaLosMetadatosDePaginacion() {
        TenantUUID tenantId = TenantUUID.generate();
        when(consultaRepository.findByTenantId(tenantId, 2, 5))
                .thenReturn(new PageImpl<>(List.of(consulta("¿Cuántas ventas?", 3)), PageRequest.of(2, 5), 20));

        Page<ConsultaResponse> response = listConsultasUseCase.listar(tenantId, 2, 5);

        assertThat(response.getNumber()).isEqualTo(2);
        assertThat(response.getSize()).isEqualTo(5);
        assertThat(response.getTotalElements()).isEqualTo(20);
        assertThat(response.getTotalPages()).isEqualTo(4);
        assertThat(response.isFirst()).isFalse();
        assertThat(response.isLast()).isFalse();
    }

    @Test
    void devuelvePaginaVaciaSiElTenantNoTieneConsultas() {
        TenantUUID tenantId = TenantUUID.generate();
        when(consultaRepository.findByTenantId(tenantId, null, null))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

        Page<ConsultaResponse> response = listConsultasUseCase.listar(tenantId, null, null);

        assertThat(response.getContent()).isEmpty();
        assertThat(response.getTotalElements()).isZero();
    }

    @Test
    void delegaTenantPageYSizeAlRepositorio() {
        TenantUUID tenantId = TenantUUID.generate();
        when(consultaRepository.findByTenantId(any(TenantUUID.class), any(), any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

        listConsultasUseCase.listar(tenantId, 1, 25);

        verify(consultaRepository).findByTenantId(tenantId, 1, 25);
    }
}