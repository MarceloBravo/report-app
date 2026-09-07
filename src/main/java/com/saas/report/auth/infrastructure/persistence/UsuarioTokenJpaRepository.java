package com.saas.report.auth.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioTokenJpaRepository extends JpaRepository<UsuarioTokenJpaEntity, UUID> {

    Optional<UsuarioTokenJpaEntity> findByAccessToken(String accessToken);

    Optional<UsuarioTokenJpaEntity> findByRefreshToken(String refreshToken);
}