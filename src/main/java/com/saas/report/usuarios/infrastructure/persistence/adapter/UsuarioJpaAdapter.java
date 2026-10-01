package com.saas.report.usuarios.infrastructure.persistence.adapter;

import com.saas.report.usuarios.domain.usuario.Usuario;
import com.saas.report.usuarios.domain.usuario.UsuarioRepository;
import com.saas.report.usuarios.domain.valueobject.UsuarioUUID;
import com.saas.report.usuarios.infrastructure.persistence.UsuarioJpaEntity;
import com.saas.report.usuarios.infrastructure.persistence.UsuarioJpaRepository;
import com.saas.report.usuarios.infrastructure.persistence.mapper.UsuarioMapper;
import com.saas.report.identity.domain.user.UsuarioRol;
import com.saas.report.tenant.infrastructure.persistence.TenantJpaEntity;
import com.saas.report.tenant.infrastructure.persistence.TenantJpaRepository;
import com.saas.report.shared.helpers.PaginationHelper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class UsuarioJpaAdapter implements UsuarioRepository {

    private final UsuarioJpaRepository repository;
    private final TenantJpaRepository tenantRepository;

    public UsuarioJpaAdapter(UsuarioJpaRepository repository, TenantJpaRepository tenantRepository) {
        this.repository = repository;
        this.tenantRepository = tenantRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> findByEmail(String email) {
        return repository.findByEmail(email).map(UsuarioMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> findById(UsuarioUUID usuarioId) {
        return repository.findById(usuarioId.getValue()).map(UsuarioMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Usuario> findAll(UsuarioRol rol, Boolean activo, Integer page, Integer size) {
        Pageable pageable = PaginationHelper.getPageable(page, size);
        return repository.findAllByFiltros(rol, activo, pageable)
                .map(UsuarioMapper::toDomain);
    }

    @Override
    @Transactional
    public Usuario save(Usuario usuario) {
        TenantJpaEntity tenantJpa = usuario.getTenantId() != null
                ? tenantRepository.getReferenceById(usuario.getTenantId().getValue())
                : null;
        UsuarioJpaEntity entity = UsuarioMapper.toJpa(usuario, tenantJpa);
        return UsuarioMapper.toDomain(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(UsuarioUUID usuarioId) {
        repository.deleteById(usuarioId.getValue());
    }
}