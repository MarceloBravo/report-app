package com.saas.report.consulta.infrastructure.persistence.adapter;

import com.saas.report.consulta.domain.ConsultaHistorial;
import com.saas.report.consulta.infrastructure.persistence.ConsultaHistorialJpaEntity;
import com.saas.report.consulta.infrastructure.persistence.ConsultaHistorialJpaRepository;
import com.saas.report.tenant.domain.valueobject.TenantUUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultaHistorialJpaAdapterTest {

    @Mock
    private ConsultaHistorialJpaRepository repository;

    private ConsultaHistorialJpaAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ConsultaHistorialJpaAdapter(repository);
    }

    private ConsultaHistorialJpaEntity entity(UUID tenantId) {
        ConsultaHistorialJpaEntity entity = new ConsultaHistorialJpaEntity();
        entity.setConsultaId(UUID.randomUUID());
        entity.setTenantId(tenantId);
        entity.setUsuarioId(UUID.randomUUID());
        entity.setPreguntaUsuario("¿Cuántas ventas?");
        entity.setQuerySqlGenerada("SELECT 1;");
        entity.setFechaConsulta(LocalDateTime.now().minusHours(1));
        entity.setExitosa(true);
        entity.setRegistrosCount(5);
        entity.setConsultasEnPeriodo(3);
        return entity;
    }

    private Pageable pageableCapturado(TenantUUID tenantId) {
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAllByTenantIdOrderByFechaConsultaDesc(eq(tenantId.getValue()), captor.capture());
        return captor.getValue();
    }

    @Test
    void mapeaLasEntidadesJpaAlDominio() {
        TenantUUID tenantId = TenantUUID.generate();
        UUID consultaId = UUID.randomUUID();
        ConsultaHistorialJpaEntity entity = entity(tenantId.getValue());
        entity.setConsultaId(consultaId);
        when(repository.findAllByTenantIdOrderByFechaConsultaDesc(eq(tenantId.getValue()), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1));

        Page<ConsultaHistorial> result = adapter.findByTenantId(tenantId, 0, 10);

        assertThat(result.getContent()).hasSize(1);
        ConsultaHistorial consulta = result.getContent().get(0);
        assertThat(consulta.getConsultaId().getValue()).isEqualTo(consultaId);
        assertThat(consulta.getTenantId()).isEqualTo(tenantId);
        assertThat(consulta.getPreguntaUsuario()).isEqualTo("¿Cuántas ventas?");
        assertThat(consulta.getConsultasEnPeriodo()).isEqualTo(3);
    }

    @Test
    void usaLaPaginaYSolicitados() {
        TenantUUID tenantId = TenantUUID.generate();
        when(repository.findAllByTenantIdOrderByFechaConsultaDesc(any(UUID.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        adapter.findByTenantId(tenantId, 2, 25);

        Pageable pageable = pageableCapturado(tenantId);
        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(25);
    }

    @Test
    void normalizaPageYNulosAPaginaCero() {
        TenantUUID tenantId = TenantUUID.generate();
        when(repository.findAllByTenantIdOrderByFechaConsultaDesc(any(UUID.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        adapter.findByTenantId(tenantId, null, null);

        Pageable pageable = pageableCapturado(tenantId);
        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(10);
    }

    @Test
    void normalizaPaginaYTamanoNegativos() {
        TenantUUID tenantId = TenantUUID.generate();
        when(repository.findAllByTenantIdOrderByFechaConsultaDesc(any(UUID.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        adapter.findByTenantId(tenantId, -5, -1);

        Pageable pageable = pageableCapturado(tenantId);
        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(10);
    }

    @Test
    void limitaElTamanoMaximoDePagina() {
        TenantUUID tenantId = TenantUUID.generate();
        when(repository.findAllByTenantIdOrderByFechaConsultaDesc(any(UUID.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        adapter.findByTenantId(tenantId, 0, 5000);

        assertThat(pageableCapturado(tenantId).getPageSize()).isEqualTo(200);
    }

    @Test
    void devuelvePaginaVaciaCuandoElTenantNoTieneConsultas() {
        TenantUUID tenantId = TenantUUID.generate();
        when(repository.findAllByTenantIdOrderByFechaConsultaDesc(any(UUID.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<ConsultaHistorial> result = adapter.findByTenantId(tenantId, 0, 10);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }
}