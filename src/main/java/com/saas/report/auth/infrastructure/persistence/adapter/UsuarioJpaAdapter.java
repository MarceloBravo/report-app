package com.saas.report.auth.infrastructure.persistence.adapter;

import com.saas.report.auth.domain.user.Usuario;
import com.saas.report.auth.domain.user.UsuarioRepository;
import com.saas.report.auth.domain.valueobject.UsuarioUUID;
import com.saas.report.auth.infrastructure.persistence.UsuarioJpaEntity;
import com.saas.report.auth.infrastructure.persistence.UsuarioJpaRepository;
import com.saas.report.auth.infrastructure.persistence.mapper.UsuarioMapper;
import com.saas.report.infrastructure.persistence.TenantJpaRepository;
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
    @Transactional
    public Usuario save(Usuario usuario) {
        var tenantJpa = tenantRepository.getReferenceById(usuario.getTenant().getTenantId().getValue());
        UsuarioJpaEntity entity = UsuarioMapper.toJpa(usuario, tenantJpa);
        return UsuarioMapper.toDomain(repository.save(entity));
    }
}