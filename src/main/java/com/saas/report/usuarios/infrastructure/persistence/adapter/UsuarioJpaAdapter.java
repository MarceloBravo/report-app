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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    public List<Usuario> findAll() {
        return repository.findAll().stream().map(UsuarioMapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> findByRol(UsuarioRol rol) {
        return repository.findByRol(rol).stream().map(UsuarioMapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> findByActivo(boolean activo) {
        return repository.findByActivo(activo).stream().map(UsuarioMapper::toDomain).toList();
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