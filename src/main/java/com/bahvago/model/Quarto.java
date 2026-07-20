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
@Table(name = "Quarto")
@IdClass(QuartoId.class)
public class Quarto {

    @Id
    @Column(name = "Numero", nullable = false)
    private Integer numero;

    @Id
    @Column(name = "CodigoHotel", nullable = false)
    private Long codigoHotel;

    @Column(name = "Tipo", nullable = false)
    private String tipo; // 'simples', 'duplo', 'suite', etc

    @Column(name = "Preco", nullable = false)
    private Double preco;

    @Column(name = "Capacidade", nullable = false)
    private Integer capacidade;

    @Column(name = "Descricao", nullable = false, length = 2000)
    private String descricao;

    @Column(name = "AceitaPet", nullable = false)
    private Boolean aceitaPet;

    @Column(name = "Disponivel", nullable = false)
    private Boolean disponivel;

    @Column(name = "DataCriacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @PrePersist
    protected void onCreate() {
        this.dataCriacao = LocalDateTime.now();
        if (this.disponivel == null) {
            this.disponivel = true;
        }
        if (this.aceitaPet == null) {
            this.aceitaPet = false;
        }
    }
}