package com.bahvago.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "Oferta")
public class Oferta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CodigoOferta")
    private Integer id;

    @Column(name = "UrlOrigem", nullable = false, length = 1000)
    private String urlOrigem;

    @Column(name = "DataCheckIn", nullable = false)
    private LocalDate dataCheckIn;

    @Column(name = "DataCheckOut", nullable = false)
    private LocalDate dataCheckOut;

    @Column(name = "Preco", nullable = false)
    private Double preco;

    @Column(name = "Numero", nullable = false)
    private Integer numero;

    @Column(name = "CodigoHotel", nullable = false)
    private Integer codigoHotel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CodigoHotel", referencedColumnName = "CodigoHotel", insertable = false, updatable = false)
    private Hotel hotel;
}
