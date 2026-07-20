package com.bahvago.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class FavoritoId implements Serializable {

    @jakarta.persistence.Column(name = "CPF", length = 11)
    private String cpf;

    @jakarta.persistence.Column(name = "CodigoHotel")
    private Integer codigoHotel;
}