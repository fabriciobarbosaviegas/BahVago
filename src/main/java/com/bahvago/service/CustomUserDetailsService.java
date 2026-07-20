package com.bahvago.service;

import com.bahvago.model.Usuario;
import com.bahvago.repository.UsuarioRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(username.trim().toLowerCase())
            .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado"));

        Integer tipoUsuario = usuario.getTipoUsuario();
        String role = "ROLE_" + (tipoUsuario == null || tipoUsuario == 0
            ? 0
            : 0);

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));

        return new User(usuario.getEmail(), usuario.getSenha(), authorities);
    }
}