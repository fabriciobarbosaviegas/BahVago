package com.bahvago.repository;

import com.bahvago.model.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Integer> {
    List<Avaliacao> findByCodigoHotel(Integer codigoHotel);
    List<Avaliacao> findByCpf(String cpf);
    List<Avaliacao> findByCodigoHotelAndCpf(Integer codigoHotel, String cpf);
    long countByCodigoHotel(Integer codigoHotel);
    @Query("SELECT AVG(a.nota) FROM Avaliacao a WHERE a.codigoHotel = :codigoHotel")
    Double calcularMediaPorHotel(@Param("codigoHotel") Integer codigoHotel);
}