package com.bahvago.controller;

import com.bahvago.controller.dto.CadastroUsuarioForm;
import com.bahvago.model.Usuario;
import com.bahvago.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @GetMapping("/perfil/{id}")
    public String perfilUsuario(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioService.buscarPorId(id)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        model.addAttribute("usuario", usuario);
        return "perfil";
    }

    @PostMapping("/atualizar/{id}")
    public String atualizarUsuario(@PathVariable Long id, 
                                   @ModelAttribute Usuario usuario,
                                   RedirectAttributes redirectAttributes) {
        usuarioService.atualizarUsuario(usuario);
        redirectAttributes.addFlashAttribute("mensagem", "Perfil atualizado com sucesso!");
        return "redirect:/usuarios/perfil/" + id;
    }

    @GetMapping("/deletar/{id}")
    public String deletarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        usuarioService.deletarUsuario(id);
        redirectAttributes.addFlashAttribute("mensagem", "Usuário deletado com sucesso!");
        return "redirect:/";
    }
}
