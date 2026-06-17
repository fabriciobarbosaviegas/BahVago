package com.bahvago.repository;

import com.bahvago.model.Favorito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FavoritoRepository extends JpaRepository<Favorito, Long> {
    List<Favorito> findByIdUsuario(Long idUsuario);
    Optional<Favorito> findByIdUsuarioAndIdHotel(Long idUsuario, Long idHotel);
    boolean existsByIdUsuarioAndIdHotel(Long idUsuario, Long idHotel);
}
