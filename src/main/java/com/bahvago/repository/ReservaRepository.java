package com.bahvago.repository;

import com.bahvago.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findByIdUsuario(Long idUsuario);
    List<Reserva> findByIdQuarto(Long idQuarto);
    List<Reserva> findByIdUsuarioAndStatus(Long idUsuario, String status);
    List<Reserva> findByIdQuartoAndDataCheckinGreaterThanEqual(Long idQuarto, LocalDate data);
}
