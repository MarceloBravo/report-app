package com.saas.report.auth.infrastructure.persistence.adapter;

import com.saas.report.auth.domain.usuario.AuthCredentials;
import com.saas.report.auth.domain.usuario.AuthUser;
import com.saas.report.auth.domain.usuario.UsuarioAuthRepository;
import com.saas.report.identity.domain.user.UsuarioRol;
import com.saas.report.shared.domain.valueobject.TenantUUID;
import com.saas.report.shared.domain.valueobject.UsuarioUUID;
import com.saas.report.usuarios.domain.usuario.Usuario;
import com.saas.report.usuarios.domain.usuario.UsuarioRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UsuarioAuthJpaAdapter implements UsuarioAuthRepository {

    private final UsuarioRepository usuarioRepository;

    public UsuarioAuthJpaAdapter(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Optional<AuthCredentials> buscarCredenciales(String email) {
        return usuarioRepository.findByEmail(email).map(this::toCredentials);
    }

    @Override
    public Optional<AuthUser> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email).map(this::toAuthUser);
    }

    @Override
    public boolean existePorEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    @Override
    public AuthUser guardar(AuthUser usuario, String passwordHash) {
        Usuario nuevo = new Usuario(
                new UsuarioUUID(usuario.id()),
                usuario.tenantId() != null ? new TenantUUID(usuario.tenantId()) : null,
                usuario.nombre(),
                usuario.email(),
                passwordHash,
                UsuarioRol.valueOf(usuario.rol()),
                true);
        return toAuthUser(usuarioRepository.save(nuevo));
    }

    private AuthCredentials toCredentials(Usuario usuario) {
        return new AuthCredentials(
                usuario.getEmail(),
                usuario.getPassword(),
                usuario.getRol().name(),
                usuario.getActivo());
    }

    private AuthUser toAuthUser(Usuario usuario) {
        return new AuthUser(
                usuario.getUsuarioId().getValue(),
                usuario.getEmail(),
                usuario.getNombre(),
                usuario.getRol().name(),
                usuario.getTenantId() != null ? usuario.getTenantId().getValue() : null);
    }
}