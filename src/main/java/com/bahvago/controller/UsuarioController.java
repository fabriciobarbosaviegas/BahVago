package com.bahvago.controller;

import com.bahvago.controller.dto.CadastroUsuarioForm;
import com.bahvago.controller.dto.AtualizarPerfilForm;
import com.bahvago.controller.dto.AlterarSenhaForm;
import com.bahvago.model.Usuario;
import com.bahvago.service.UsuarioService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/cadastro")
    public String paginaCadastro(Model model) {
        if (!model.containsAttribute("cadastroUsuarioForm")) {
            model.addAttribute("cadastroUsuarioForm", new CadastroUsuarioForm());
        }
        return "cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastrarUsuario(@Valid @ModelAttribute("cadastroUsuarioForm") CadastroUsuarioForm form,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes) {
        if (usuarioService.existeEmail(form.getEmail())) {
            bindingResult.rejectValue("email", "email.duplicado", "Este e-mail ja esta cadastrado");
        }

        if (usuarioService.existeCpf(form.getCpf())) {
            bindingResult.rejectValue("cpf", "cpf.duplicado", "Este CPF ja esta cadastrado");
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.cadastroUsuarioForm", bindingResult);
            redirectAttributes.addFlashAttribute("cadastroUsuarioForm", form);
            return "redirect:/usuarios/cadastro";
        }

        usuarioService.criarUsuarioComSenhaSegura(form.getNome(), form.getEmail(), form.getCpf(), form.getSenha());
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Cadastro realizado com sucesso. Faca login para continuar.");
        return "redirect:/login";
    }

    @GetMapping("/perfil")
    public String perfilUsuario(Authentication authentication, Model model) {
        Usuario usuario = buscarUsuarioAutenticado(authentication);
        model.addAttribute("usuario", usuario);

        if (!model.containsAttribute("atualizarPerfilForm")) {
            AtualizarPerfilForm form = new AtualizarPerfilForm();
            form.setNome(usuario.getNome());
            form.setEmail(usuario.getEmail());
            model.addAttribute("atualizarPerfilForm", form);
        }

        if (!model.containsAttribute("alterarSenhaForm")) {
            model.addAttribute("alterarSenhaForm", new AlterarSenhaForm());
        }

        return "perfil";
    }

    @GetMapping("/perfil/{id}")
    public String redirecionarPerfilLegado() {
        return "redirect:/usuarios/perfil";
    }

    @PostMapping("/perfil")
    public String atualizarUsuario(@Valid @ModelAttribute("atualizarPerfilForm") AtualizarPerfilForm form,
                                   BindingResult bindingResult,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        Usuario usuario = buscarUsuarioAutenticado(authentication);

        if (!usuario.getEmail().equalsIgnoreCase(form.getEmail()) && usuarioService.existeEmail(form.getEmail())) {
            bindingResult.rejectValue("email", "email.duplicado", "Este e-mail ja esta cadastrado");
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.atualizarPerfilForm", bindingResult);
            redirectAttributes.addFlashAttribute("atualizarPerfilForm", form);
            return "redirect:/usuarios/perfil";
        }

        usuario.setNome(form.getNome().trim());
        usuario.setEmail(form.getEmail().trim().toLowerCase());
        usuarioService.atualizarUsuario(usuario);
        redirectAttributes.addFlashAttribute("mensagemPerfil", "Perfil atualizado com sucesso!");
        return "redirect:/usuarios/perfil";
    }

    @PostMapping("/perfil/senha")
    public String atualizarSenha(@Valid @ModelAttribute("alterarSenhaForm") AlterarSenhaForm form,
                                 BindingResult bindingResult,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        Usuario usuario = buscarUsuarioAutenticado(authentication);

        if (!usuarioService.senhaConfere(form.getSenhaAtual(), usuario.getSenha())) {
            bindingResult.rejectValue("senhaAtual", "senha.invalida", "A senha atual esta incorreta");
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.alterarSenhaForm", bindingResult);
            redirectAttributes.addFlashAttribute("alterarSenhaForm", form);
            redirectAttributes.addFlashAttribute("abaAtiva", "seguranca");
            return "redirect:/usuarios/perfil";
        }

        usuario.setSenha(form.getNovaSenha());
        usuarioService.atualizarUsuario(usuario);
        redirectAttributes.addFlashAttribute("mensagemSenha", "Senha atualizada com sucesso!");
        redirectAttributes.addFlashAttribute("abaAtiva", "seguranca");
        return "redirect:/usuarios/perfil";
    }

    @PostMapping("/deletar")
    public String deletarUsuario(Authentication authentication,
                                HttpServletRequest request,
                                HttpServletResponse response) {
        Usuario usuario = buscarUsuarioAutenticado(authentication);
        usuarioService.deletarUsuario(usuario.getCpf());

        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(request, response, authentication);
        return "redirect:/login?logout";
    }

    private Usuario buscarUsuarioAutenticado(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Usuario nao autenticado");
        }

        return usuarioService.buscarPorEmail(authentication.getName())
            .orElseThrow(() -> new RuntimeException("Usuario autenticado nao encontrado"));
    }
}
