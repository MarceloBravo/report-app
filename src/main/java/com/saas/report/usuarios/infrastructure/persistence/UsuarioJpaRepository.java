package com.saas.report.usuarios.infrastructure.persistence;

import com.saas.report.identity.domain.user.UsuarioRol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioJpaEntity, UUID> {

    Optional<UsuarioJpaEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    List<UsuarioJpaEntity> findByRol(UsuarioRol rol);

    List<UsuarioJpaEntity> findByActivo(Boolean activo);
}