package com.saas.report.usuarios.infrastructure.persistence.adapter;

import com.saas.report.identity.domain.user.UsuarioRol;
import com.saas.report.shared.domain.valueobject.UsuarioUUID;
import com.saas.report.usuarios.domain.usuario.Usuario;
import com.saas.report.usuarios.domain.usuario.UsuarioRepository;
import com.saas.report.usuarios.infrastructure.persistence.UsuarioJpaEntity;
import com.saas.report.usuarios.infrastructure.persistence.UsuarioJpaRepository;
import com.saas.report.usuarios.infrastructure.persistence.mapper.UsuarioMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UsuarioJpaAdapter implements UsuarioRepository {

    private final UsuarioJpaRepository repository;

    public UsuarioJpaAdapter(UsuarioJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Usuario save(Usuario usuario) {
        UsuarioJpaEntity entity = UsuarioMapper.toJpa(usuario);
        return UsuarioMapper.toDomain(repository.save(entity));
    }

    @Override
    public Optional<Usuario> findById(UsuarioUUID id) {
        return repository.findById(id.getValue()).map(UsuarioMapper::toDomain);
    }

    @Override
    public Optional<Usuario> findByEmail(String email) {
        return repository.findByEmail(email).map(UsuarioMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public Page<Usuario> findAll(UsuarioRol rol, Boolean activo, Integer page, Integer size) {
        Pageable pageable = page != null && size != null ? PageRequest.of(page, size) : Pageable.unpaged();
        return repository.findAllByFiltros(rol, activo, pageable).map(UsuarioMapper::toDomain);
    }

    @Override
    public void delete(UsuarioUUID id) {
        repository.deleteById(id.getValue());
    }
}