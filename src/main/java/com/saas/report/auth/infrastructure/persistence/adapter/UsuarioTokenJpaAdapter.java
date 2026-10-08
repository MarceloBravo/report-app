package com.saas.report.auth.infrastructure.persistence.adapter;

import com.saas.report.auth.domain.token.UsuarioToken;
import com.saas.report.auth.domain.token.UsuarioTokenRepository;
import com.saas.report.auth.infrastructure.persistence.UsuarioTokenJpaEntity;
import com.saas.report.auth.infrastructure.persistence.UsuarioTokenJpaRepository;
import com.saas.report.auth.infrastructure.persistence.mapper.UsuarioTokenMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class UsuarioTokenJpaAdapter implements UsuarioTokenRepository {

    private final UsuarioTokenJpaRepository repository;

    public UsuarioTokenJpaAdapter(UsuarioTokenJpaRepository repository) {
        this.repository = repository;
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
        UsuarioTokenJpaEntity entity = UsuarioTokenMapper.toJpa(token);
        return UsuarioTokenMapper.toDomain(repository.save(entity));
    }
}