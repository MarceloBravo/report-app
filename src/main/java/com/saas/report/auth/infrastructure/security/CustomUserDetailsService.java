package com.saas.report.auth.infrastructure.security;

import com.saas.report.auth.domain.usuario.AuthCredentials;
import com.saas.report.auth.domain.usuario.UsuarioAuthRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioAuthRepository usuarioAuthRepository;

    public CustomUserDetailsService(UsuarioAuthRepository usuarioAuthRepository) {
        this.usuarioAuthRepository = usuarioAuthRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AuthCredentials usuario = usuarioAuthRepository.buscarCredenciales(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        return User.builder()
                .username(usuario.email())
                .password(usuario.passwordHash())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + usuario.rol())))
                .disabled(!usuario.activo())
                .accountLocked(false)
                .accountExpired(false)
                .credentialsExpired(false)
                .build();
    }
}