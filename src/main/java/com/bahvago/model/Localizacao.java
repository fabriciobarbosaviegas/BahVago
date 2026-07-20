package com.bahvago.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "Localizacao")
public class Localizacao {

    @EmbeddedId
    private LocalizacaoId id;

    @Column(name = "Cidade", nullable = false, length = 50)
    private String cidade;

    @Column(name = "Estado", nullable = false, length = 50)
    private String estado;

    @Column(name = "Pais", nullable = false, length = 50)
    private String pais;

    @Column(name = "CEP", nullable = false, length = 20)
    private String cep;

    @Column(name = "EnderecoAproximado", length = 60)
    private String enderecoAproximado;
}