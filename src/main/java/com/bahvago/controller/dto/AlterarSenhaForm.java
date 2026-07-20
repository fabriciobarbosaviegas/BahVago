package com.bahvago.controller.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AlterarSenhaForm {

    @NotBlank(message = "Informe sua senha atual")
    private String senhaAtual;

    @NotBlank(message = "A nova senha e obrigatoria")
    @Size(min = 8, max = 72, message = "A nova senha deve ter entre 8 e 72 caracteres")

    private String novaSenha;

    @NotBlank(message = "Confirme a nova senha")
    private String confirmarNovaSenha;

    @AssertTrue(message = "A confirmacao da senha nao confere")
    public boolean isConfirmacaoValida() {
        if (novaSenha == null || confirmarNovaSenha == null) {
            return true;
        }
        return novaSenha.equals(confirmarNovaSenha);
    }
}
