package com.saas.report.usuarios.infrastructure.persistence.adapter;

import com.saas.report.identity.domain.user.UsuarioRol;
import com.saas.report.usuarios.domain.usuario.Usuario;
import com.saas.report.usuarios.infrastructure.persistence.UsuarioJpaEntity;
import com.saas.report.usuarios.infrastructure.persistence.UsuarioJpaRepository;
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

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioJpaAdapterTest {

    @Mock
    private UsuarioJpaRepository repository;

    private UsuarioJpaAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new UsuarioJpaAdapter(repository);
    }

    private UsuarioJpaEntity entity() {
        UsuarioJpaEntity entity = new UsuarioJpaEntity();
        entity.setUsuarioId(UUID.randomUUID());
        entity.setTenantId(UUID.randomUUID());
        entity.setNombre("Ana");
        entity.setEmail("ana@empresa.com");
        entity.setPasswordHash("hash");
        entity.setRol(UsuarioRol.VIEWER);
        entity.setActivo(true);
        entity.setEmailVerificado(true);
        return entity;
    }

    private Pageable pageableCapturado() {
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAllByFiltros(any(), any(), captor.capture());
        return captor.getValue();
    }

    @Test
    void mapeaLasEntidadesJpaAlDominio() {
        UsuarioJpaEntity entity = entity();
        UUID usuarioId = entity.getUsuarioId();
        when(repository.findAllByFiltros(any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1));

        Page<Usuario> result = adapter.findAll(null, null, 0, 10);

        assertThat(result.getContent()).hasSize(1);
        Usuario usuario = result.getContent().get(0);
        assertThat(usuario.getUsuarioId().getValue()).isEqualTo(usuarioId);
        assertThat(usuario.getNombre()).isEqualTo("Ana");
        assertThat(usuario.getEmail()).isEqualTo("ana@empresa.com");
        assertThat(usuario.getRol()).isEqualTo(UsuarioRol.VIEWER);
        assertThat(usuario.getActivo()).isTrue();
    }

    @Test
    void usaLaPaginaYSolicitados() {
        when(repository.findAllByFiltros(any(), any(), any(Pageable.class))).thenReturn(Page.empty());

        adapter.findAll(null, null, 2, 25);

        Pageable pageable = pageableCapturado();
        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(25);
    }

    @Test
    void sinPaginaNiTamanoDevuelveResultadoNoPaginado() {
        when(repository.findAllByFiltros(any(), any(), any(Pageable.class))).thenReturn(Page.empty());

        adapter.findAll(null, null, null, null);

        assertThat(pageableCapturado().isUnpaged()).isTrue();
    }

    @Test
    void delegaLosFiltrosDeRolYEstadoAlRepositorio() {
        when(repository.findAllByFiltros(eq(UsuarioRol.ADMIN), eq(false), any(Pageable.class)))
                .thenReturn(Page.empty());

        adapter.findAll(UsuarioRol.ADMIN, false, 0, 10);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAllByFiltros(eq(UsuarioRol.ADMIN), eq(false), captor.capture());
        assertThat(captor.getValue().getPageNumber()).isZero();
        assertThat(captor.getValue().getPageSize()).isEqualTo(10);
    }

    @Test
    void conservaLosMetadatosDePaginacion() {
        when(repository.findAllByFiltros(isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity()), PageRequest.of(0, 10), 42));

        Page<Usuario> result = adapter.findAll(null, null, 0, 10);

        assertThat(result.getTotalElements()).isEqualTo(42);
        assertThat(result.getTotalPages()).isEqualTo(5);
    }

    @Test
    void devuelvePaginaVaciaSinUsuarios() {
        when(repository.findAllByFiltros(any(), any(), any(Pageable.class))).thenReturn(Page.empty());

        Page<Usuario> result = adapter.findAll(null, null, 0, 10);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }
}