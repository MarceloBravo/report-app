package com.saas.report.auth.infrastructure.persistence.adapter;

import com.saas.report.auth.domain.token.UsuarioToken;
import com.saas.report.auth.domain.token.UsuarioTokenRepository;
import com.saas.report.auth.infrastructure.persistence.UsuarioJpaRepository;
import com.saas.report.auth.infrastructure.persistence.UsuarioTokenJpaEntity;
import com.saas.report.auth.infrastructure.persistence.UsuarioTokenJpaRepository;
import com.saas.report.auth.infrastructure.persistence.mapper.UsuarioTokenMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class UsuarioTokenJpaAdapter implements UsuarioTokenRepository {

    private final UsuarioTokenJpaRepository repository;
    private final UsuarioJpaRepository usuarioRepository;

    public UsuarioTokenJpaAdapter(UsuarioTokenJpaRepository repository, UsuarioJpaRepository usuarioRepository) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UsuarioToken> findByAccessToken(String accessToken) {
        return repository.findByAccessToken(accessToken).map(UsuarioTokenMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UsuarioToken> findByRefreshToken(String refreshToken) {
        return repository.findByRefreshToken(refreshToken).map(UsuarioTokenMapper::toDomain);
    }

    @Override
    @Transactional
    public UsuarioToken save(UsuarioToken token) {
        var usuarioJpa = usuarioRepository.getReferenceById(token.getUsuario().getUsuarioId().getValue());
        UsuarioTokenJpaEntity entity = UsuarioTokenMapper.toJpa(token, usuarioJpa);
        return UsuarioTokenMapper.toDomain(repository.save(entity));
    }
}