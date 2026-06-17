package com.bahvago.service;

import com.bahvago.model.Reserva;
import com.bahvago.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    public Reserva criarReserva(Reserva reserva) {
        return reservaRepository.save(reserva);
    }

    public Optional<Reserva> buscarPorId(Long id) {
        return reservaRepository.findById(id);
    }

    public List<Reserva> listarTodas() {
        return reservaRepository.findAll();
    }

    public List<Reserva> buscarPorUsuario(Long idUsuario) {
        return reservaRepository.findByIdUsuario(idUsuario);
    }

    public List<Reserva> buscarReservasAtivasPorUsuario(Long idUsuario) {
        return reservaRepository.findByIdUsuarioAndStatus(idUsuario, "ativa");
    }

    public List<Reserva> buscarPorQuarto(Long idQuarto) {
        return reservaRepository.findByIdQuarto(idQuarto);
    }

    public Reserva atualizarReserva(Reserva reserva) {
        return reservaRepository.save(reserva);
    }

    public void deletarReserva(Long id) {
        reservaRepository.deleteById(id);
    }

    public void cancelarReserva(Long id) {
        Optional<Reserva> reserva = reservaRepository.findById(id);
        if (reserva.isPresent()) {
            reserva.get().setStatus("cancelada");
            reservaRepository.save(reserva.get());
        }
    }
}
