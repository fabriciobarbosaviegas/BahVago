package com.bahvago.service;

import com.bahvago.model.Avaliacao;
import com.bahvago.repository.AvaliacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class AvaliacaoService {

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    public Avaliacao criarAvaliacao(Avaliacao avaliacao) {
        return avaliacaoRepository.save(avaliacao);
    }

    public Optional<Avaliacao> buscarPorId(Integer id) {
        return avaliacaoRepository.findById(id);
    }

    public List<Avaliacao> listarTodas() {
        return avaliacaoRepository.findAll();
    }

    public List<Avaliacao> buscarPorHotel(Integer codigoHotel) {
        return avaliacaoRepository.findByCodigoHotel(codigoHotel);
    }

    public List<Avaliacao> buscarPorUsuario(String cpf) {
        return avaliacaoRepository.findByCpf(cpf);
    }

    public Avaliacao atualizarAvaliacao(Avaliacao avaliacao) {
        return avaliacaoRepository.save(avaliacao);
    }

    public void deletarAvaliacao(Integer id) {
        avaliacaoRepository.deleteById(id);
    }

    public Double calcularMediaAvaliacoes(Integer codigoHotel) {
        List<Avaliacao> avaliacoes = buscarPorHotel(codigoHotel);
        if (avaliacoes.isEmpty()) {
            return 0.0;
        }
        return avaliacoes.stream()
            .mapToDouble(Avaliacao::getNota)
            .average()
            .orElse(0.0);
    }
}