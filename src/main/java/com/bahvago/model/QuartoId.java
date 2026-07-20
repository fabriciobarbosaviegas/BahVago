package com.bahvago.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuartoId implements Serializable {

    private Integer numero;
    private Long codigoHotel;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuartoId)) return false;
        QuartoId that = (QuartoId) o;
        return Objects.equals(numero, that.numero)
                && Objects.equals(codigoHotel, that.codigoHotel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numero, codigoHotel);
    }
}