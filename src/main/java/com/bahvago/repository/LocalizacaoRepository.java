package com.bahvago.repository;

import com.bahvago.model.Localizacao;
import com.bahvago.model.LocalizacaoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocalizacaoRepository extends JpaRepository<Localizacao, LocalizacaoId> {
}