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

    public Optional<Avaliacao> buscarPorId(Long id) {
        return avaliacaoRepository.findById(id);
    }

    public List<Avaliacao> listarTodas() {
        return avaliacaoRepository.findAll();
    }

    public List<Avaliacao> buscarPorHotel(Long idHotel) {
        return avaliacaoRepository.findByIdHotel(idHotel);
    }

    public List<Avaliacao> buscarPorUsuario(Long idUsuario) {
        return avaliacaoRepository.findByIdUsuario(idUsuario);
    }

    public Avaliacao atualizarAvaliacao(Avaliacao avaliacao) {
        return avaliacaoRepository.save(avaliacao);
    }

    public void deletarAvaliacao(Long id) {
        avaliacaoRepository.deleteById(id);
    }

    public Double calcularMediaAvaliacoes(Long idHotel) {
        List<Avaliacao> avaliacoes = buscarPorHotel(idHotel);
        if (avaliacoes.isEmpty()) {
            return 0.0;
        }
        return avaliacoes.stream()
            .mapToInt(Avaliacao::getNota)
            .average()
            .orElse(0.0);
    }
}
