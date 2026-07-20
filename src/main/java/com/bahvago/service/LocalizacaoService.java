package com.bahvago.service;

import com.bahvago.model.Localizacao;
import com.bahvago.model.LocalizacaoId;
import com.bahvago.repository.LocalizacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class LocalizacaoService {

    @Autowired
    private LocalizacaoRepository localizacaoRepository;

    public Optional<Localizacao> buscarPorCoordenadas(Integer latitude, Integer longitude) {
        return localizacaoRepository.findById(new LocalizacaoId(latitude, longitude));
    }

    public List<Localizacao> listarTodas() {
        return localizacaoRepository.findAll();
    }

    public Localizacao criarLocalizacao(Localizacao localizacao) {
        return localizacaoRepository.save(localizacao);
    }

    /**
     * Retorna a Localizacao existente para a coordenada informada,
     * ou cria uma nova caso ainda não exista (usado antes de salvar um Hotel).
     */
    public Localizacao buscarOuCriar(Integer latitude, Integer longitude,
                                      String cidade, String estado,
                                      String pais, String cep,
                                      String enderecoAproximado) {
        LocalizacaoId id = new LocalizacaoId(latitude, longitude);
        return localizacaoRepository.findById(id)
                .orElseGet(() -> localizacaoRepository.save(
                        Localizacao.builder()
                                .id(id)
                                .cidade(cidade)
                                .estado(estado)
                                .pais(pais)
                                .cep(cep)
                                .enderecoAproximado(enderecoAproximado)
                                .build()
                ));
    }

    public Localizacao atualizarLocalizacao(Localizacao localizacao) {
        return localizacaoRepository.save(localizacao);
    }

    public void deletarLocalizacao(Integer latitude, Integer longitude) {
        localizacaoRepository.deleteById(new LocalizacaoId(latitude, longitude));
    }
}