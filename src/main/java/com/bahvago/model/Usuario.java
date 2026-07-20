package com.bahvago.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "Usuario")
public class Usuario {
    
    @Id
    @Column(name = "CPF", nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(name = "Email", nullable = false, unique = true)
    private String email;

    @Column(name = "Senha", nullable = false)
    private String senha;

    @Column(name = "Nome", nullable = false)
    private String nome;

    @Column(name = "DataCriacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "Tipo")
    private Integer tipoUsuario; 

    @PrePersist
    protected void onCreate() {
        this.dataCriacao = LocalDateTime.now();
    }
}
