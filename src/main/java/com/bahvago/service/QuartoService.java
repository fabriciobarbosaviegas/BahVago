package com.bahvago.service;

import com.bahvago.model.Quarto;
import com.bahvago.model.QuartoId;
import com.bahvago.repository.QuartoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class QuartoService {

    @Autowired
    private QuartoRepository quartoRepository;

    public Quarto criarQuarto(Quarto quarto) {
        return quartoRepository.save(quarto);
    }

    public Optional<Quarto> buscarPorId(Integer numero, Long codigoHotel) {
        return quartoRepository.findById(new QuartoId(numero, codigoHotel));
    }

    public List<Quarto> listarTodos() {
        return quartoRepository.findAll();
    }

    public List<Quarto> buscarPorHotel(Long codigoHotel) {
        return quartoRepository.findByCodigoHotel(codigoHotel);
    }

    public List<Quarto> buscarDisponiveisPorHotel(Long codigoHotel) {
        return quartoRepository.findByCodigoHotelAndDisponivel(codigoHotel, true);
    }

    public Quarto atualizarQuarto(Quarto quarto) {
        return quartoRepository.save(quarto);
    }

    public void deletarQuarto(Integer numero, Long codigoHotel) {
        quartoRepository.deleteById(new QuartoId(numero, codigoHotel));
    }
    public long contarTotal(Long codigoHotel) {
    return quartoRepository.findByCodigoHotel(codigoHotel).size();
        }

    public long contarDisponiveis(Long codigoHotel) {
        return quartoRepository.findByCodigoHotelAndDisponivel(codigoHotel, true).size();
    }

    public long contarIndisponiveis(Long codigoHotel) {
        return quartoRepository.findByCodigoHotelAndDisponivel(codigoHotel, false).size();
    }
}