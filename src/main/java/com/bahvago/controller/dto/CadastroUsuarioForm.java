package com.bahvago.controller.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CadastroUsuarioForm {

    @NotBlank(message = "O nome e obrigatorio")
    @Size(max = 120, message = "O nome deve ter no maximo 120 caracteres")
    private String nome;

    @NotBlank(message = "O e-mail e obrigatorio")
    @Email(message = "Informe um e-mail valido")
    @Size(max = 255, message = "O e-mail deve ter no maximo 255 caracteres")
    private String email;

    @NotBlank(message = "O CPF e obrigatorio")
    @Pattern(
        regexp = "^(\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}|\\d{11})$",
        message = "Informe um CPF valido"
    )
    private String cpf;

    @NotBlank(message = "A senha e obrigatoria")
    @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres")
    private String senha;

    @NotBlank(message = "Confirme a senha")
    private String confirmarSenha;

    @AssertTrue(message = "As senhas nao coincidem")
    public boolean isSenhasIguais() {
        if (senha == null || confirmarSenha == null) {
            return true;
        }
        return senha.equals(confirmarSenha);
    }
}
