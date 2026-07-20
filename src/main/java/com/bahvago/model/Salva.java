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
@Table(name = "Salva")
@IdClass(FavoritoId.class)
public class Salva {

    @Id
    @Column(name = "CPF", length = 11)
    private String cpf;

    @Id
    @Column(name = "CodigoOferta")
    private Integer codigoOferta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CodigoOferta", referencedColumnName = "CodigoOferta", insertable = false, updatable = false)
    private Oferta oferta;
}
