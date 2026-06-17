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
@Table(name = "quartos")
public class Quarto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_hotel", nullable = false)
    private Long idHotel;

    @Column(nullable = false)
    private String numero;

    @Column(nullable = false)
    private String tipo; // 'simples', 'duplo', 'suite', etc

    @Column(nullable = false)
    private Integer capacidade;

    @Column(name = "preco_noite", nullable = false, columnDefinition = "DECIMAL(10, 2)")
    private Double precoNoite;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false)
    private Boolean disponivel;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @PrePersist
    protected void onCreate() {
        this.dataCriacao = LocalDateTime.now();
        if (this.disponivel == null) {
            this.disponivel = true;
        }
    }
}
