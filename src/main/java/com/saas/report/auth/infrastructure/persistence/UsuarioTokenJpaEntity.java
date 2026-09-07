package com.saas.report.auth.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "usuario_token")
@Getter
@Setter
@NoArgsConstructor
public class UsuarioTokenJpaEntity {

    @Id
    @Column(name = "token_id", columnDefinition = "uuid")
    private UUID tokenId;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioJpaEntity usuario;

    @Column(name = "access_token", nullable = false, length = 4096)
    private String accessToken;

    @Column(name = "refresh_token", nullable = false, length = 4096)
    private String refreshToken;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "access_token_expira_en", nullable = false)
    private LocalDateTime accessTokenExpiraEn;

    @Column(name = "refresh_token_expira_en", nullable = false)
    private LocalDateTime refreshTokenExpiraEn;

    @Column(name = "activo", nullable = false)
    private Boolean activo;
}