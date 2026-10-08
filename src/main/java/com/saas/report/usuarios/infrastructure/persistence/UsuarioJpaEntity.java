package com.saas.report.usuarios.infrastructure.persistence;

import com.saas.report.identity.domain.user.UsuarioRol;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
public class UsuarioJpaEntity {

    @Id
    @Column(name = "usuario_id", columnDefinition = "uuid")
    private UUID usuarioId;

    @Column(name = "tenant_id", columnDefinition = "uuid", nullable = true)
    private UUID tenantId;
    
    @Column(name = "nombre", nullable = false)
    private String nombre;

    // Fuente autoritativa del email. 'usuario_token' guarda una copia (snapshot tomada al emitir
    // el token) para que el esquema del BC de auth no dependa de esta tabla ni de un JOIN hacia
    // ella; si este valor cambia, los tokens ya emitidos conservan el valor anterior hasta expirar.
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    // Este campo se utiliza para almacenar el rol del usuario.
    // El campo es duplicado en la entidad UsuarioTokenJpaEntity para evitar acoplamiento entre las 
    // entidades y permitir identificar el rol del usuario sin necesidad de realizar una consulta 
    // adicional a la tabla de usuarios desde la entidad UsuarioTokenJpaEntity.
    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false)
    private UsuarioRol rol;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Column(name = "email_verificado", nullable = false)
    private Boolean emailVerificado = true;
}