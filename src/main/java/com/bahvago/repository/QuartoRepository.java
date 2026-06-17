package com.bahvago.repository;

import com.bahvago.model.Quarto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuartoRepository extends JpaRepository<Quarto, Long> {
    List<Quarto> findByIdHotel(Long idHotel);
    List<Quarto> findByIdHotelAndDisponivel(Long idHotel, Boolean disponivel);
}
