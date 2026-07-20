package com.bahvago.repository;

import com.bahvago.model.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Integer> {

    List<Hotel> findByCpf(String cpf);

    @Query("SELECT h FROM Hotel h WHERE LOWER(h.localizacao.cidade) = LOWER(?1)")
    List<Hotel> findByCidade(String cidade);

    @Query("SELECT h FROM Hotel h WHERE LOWER(h.localizacao.estado) = LOWER(?1)")
    List<Hotel> findByEstado(String estado);

    @Query("SELECT h FROM Hotel h WHERE LOWER(h.nome) LIKE LOWER(CONCAT('%', ?1, '%')) " +
           "OR LOWER(h.localizacao.cidade) LIKE LOWER(CONCAT('%', ?1, '%'))")
    List<Hotel> buscarPorNomeOuCidade(String termo);
}