package com.bahvago.controller;

import com.bahvago.model.Reserva;
import com.bahvago.service.ReservaService;
import com.bahvago.service.QuartoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/reservas")
public class ReservaController {

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private QuartoService quartoService;

    @GetMapping("/usuario/{idUsuario}")
    public String minhasReservas(@PathVariable Long idUsuario, Model model) {
        List<Reserva> reservas = reservaService.buscarPorUsuario(idUsuario);
        model.addAttribute("reservas", reservas);
        return "dashboard";
    }

    @GetMapping("/{id}")
    public String detalheReserva(@PathVariable Long id, Model model) {
        Reserva reserva = reservaService.buscarPorId(id)
            .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));
        model.addAttribute("reserva", reserva);
        return "dashboard";
    }

    @PostMapping("/criar")
    public String criarReserva(@ModelAttribute Reserva reserva,
                              RedirectAttributes redirectAttributes) {
        Reserva novaReserva = reservaService.criarReserva(reserva);
        redirectAttributes.addFlashAttribute("mensagem", "Reserva criada com sucesso!");
        return "redirect:/reservas/" + novaReserva.getId();
    }

    @PostMapping("/atualizar/{id}")
    public String atualizarReserva(@PathVariable Long id,
                                  @ModelAttribute Reserva reserva,
                                  RedirectAttributes redirectAttributes) {
        reserva.setId(id);
        reservaService.atualizarReserva(reserva);
        redirectAttributes.addFlashAttribute("mensagem", "Reserva atualizada com sucesso!");
        return "redirect:/reservas/" + id;
    }

    @GetMapping("/cancelar/{id}")
    public String cancelarReserva(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {
        Reserva reserva = reservaService.buscarPorId(id)
            .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));
        reservaService.cancelarReserva(id);
        redirectAttributes.addFlashAttribute("mensagem", "Reserva cancelada com sucesso!");
        return "redirect:/reservas/usuario/" + reserva.getIdUsuario();
    }
}
