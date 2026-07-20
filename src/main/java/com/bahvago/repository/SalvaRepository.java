package com.bahvago.repository;

import com.bahvago.model.FavoritoId;
import com.bahvago.model.Salva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalvaRepository extends JpaRepository<Salva, FavoritoId> {

    List<Salva> findByCpf(String cpf);

    boolean existsByCpfAndCodigoOferta(String cpf, Integer codigoOferta);

    void deleteByCpfAndCodigoOferta(String cpf, Integer codigoOferta);

    @Query("SELECT s.codigoOferta FROM Salva s WHERE s.cpf = ?1")
    List<Integer> findCodigosOfertaByCpf(String cpf);
}
