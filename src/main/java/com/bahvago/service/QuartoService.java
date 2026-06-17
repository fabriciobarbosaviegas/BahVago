package com.bahvago.service;

import com.bahvago.model.Quarto;
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

    public Optional<Quarto> buscarPorId(Long id) {
        return quartoRepository.findById(id);
    }

    public List<Quarto> listarTodos() {
        return quartoRepository.findAll();
    }

    public List<Quarto> buscarPorHotel(Long idHotel) {
        return quartoRepository.findByIdHotel(idHotel);
    }

    public List<Quarto> buscarDisponiveisPorHotel(Long idHotel) {
        return quartoRepository.findByIdHotelAndDisponivel(idHotel, true);
    }

    public Quarto atualizarQuarto(Quarto quarto) {
        return quartoRepository.save(quarto);
    }

    public void deletarQuarto(Long id) {
        quartoRepository.deleteById(id);
    }
}
