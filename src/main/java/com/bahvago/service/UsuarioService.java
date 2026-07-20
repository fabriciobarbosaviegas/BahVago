package com.bahvago.service;

import com.bahvago.model.Usuario;
import com.bahvago.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Usuario criarUsuario(Usuario usuario) {
        usuario.setEmail(usuario.getEmail().trim().toLowerCase());
        usuario.setCpf(normalizarCpf(usuario.getCpf()));
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        if (usuario.getTipoUsuario() == null || usuario.getTipoUsuario() == 0) {
            usuario.setTipoUsuario(0);
        }
        return usuarioRepository.save(usuario);
    }

    public Usuario criarUsuarioComSenhaSegura(String nome, String email, String cpf, String senha) {
        Usuario usuario = Usuario.builder()
            .nome(nome == null ? null : nome.trim())
            .email(email == null ? null : email.trim().toLowerCase())
            .cpf(normalizarCpf(cpf))
            .senha(passwordEncoder.encode(senha))
            .tipoUsuario(0)
            .build();
        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> buscarPorCpf(String cpf) {
        return usuarioRepository.findById(normalizarCpf(cpf));
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario atualizarUsuario(Usuario usuario) {
        if (usuario.getEmail() != null) {
            usuario.setEmail(usuario.getEmail().trim().toLowerCase());
        }

        if (usuario.getCpf() != null) {
            usuario.setCpf(normalizarCpf(usuario.getCpf()));
        }

        if (usuario.getSenha() != null && !usuario.getSenha().isBlank() && !usuario.getSenha().startsWith("$2a$")
            && !usuario.getSenha().startsWith("$2b$") && !usuario.getSenha().startsWith("$2y$")) {
            usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        }

        return usuarioRepository.save(usuario);
    }

    public boolean senhaConfere(String senhaEmTextoPlano, String senhaHash) {
        return passwordEncoder.matches(senhaEmTextoPlano, senhaHash);
    }

    public void deletarUsuario(String cpf) {
        usuarioRepository.deleteById(normalizarCpf(cpf));
    }

    public boolean existeEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    public boolean existeCpf(String cpf) {
        return usuarioRepository.existsByCpf(normalizarCpf(cpf));
    }

    private String normalizarCpf(String cpf) {
        return cpf == null ? null : cpf.replaceAll("\\D", "");
    }
}
