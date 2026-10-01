package com.saas.report.usuarios.infrastructure.persistence;

import com.saas.report.identity.domain.user.UsuarioRol;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioJpaEntity, UUID> {

    Optional<UsuarioJpaEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("""
            SELECT u FROM UsuarioJpaEntity u
            WHERE (:rol IS NULL OR u.rol = :rol)
                AND (:activo IS NULL OR u.activo = :activo)
            ORDER BY u.nombre, u.usuarioId
            """)
    Page<UsuarioJpaEntity> findAllByFiltros(@Param("rol") UsuarioRol rol,
                                            @Param("activo") Boolean activo,
                                            Pageable pageable);
}