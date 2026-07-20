package com.bahvago.repository;

import com.bahvago.model.Favorito;
import com.bahvago.model.FavoritoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FavoritoRepository extends JpaRepository<Favorito, FavoritoId> {
    List<Favorito> findByIdCpf(String cpf);
    Optional<Favorito> findByIdCpfAndIdCodigoHotel(String cpf, Integer codigoHotel);
    boolean existsByIdCpfAndIdCodigoHotel(String cpf, Integer codigoHotel);
}