package com.bahvago.repository;

import com.bahvago.model.Oferta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OfertaRepository extends JpaRepository<Oferta, Integer> {

    List<Oferta> findByCodigoHotel(Integer codigoHotel);

    Optional<Oferta> findFirstByCodigoHotelOrderByPrecoAsc(Integer codigoHotel);
}
