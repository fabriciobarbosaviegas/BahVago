package com.bahvago.controller;

import com.bahvago.model.Quarto;
import com.bahvago.service.QuartoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/quartos")
public class QuartoController {

    @Autowired
    private QuartoService quartoService;

    @GetMapping("/hotel/{codigoHotel}")
    public String listarQuartosPorHotel(@PathVariable Long codigoHotel, Model model) {
        List<Quarto> quartos = quartoService.buscarPorHotel(codigoHotel);
        model.addAttribute("quartos", quartos);
        model.addAttribute("codigoHotel", codigoHotel);
        return "novo-quarto";
    }

    @GetMapping("/hotel/{codigoHotel}/numero/{numero}")
    public String detalheQuarto(@PathVariable Long codigoHotel,
                                 @PathVariable Integer numero,
                                 Model model) {
        Quarto quarto = quartoService.buscarPorId(numero, codigoHotel)
                .orElseThrow(() -> new RuntimeException("Quarto não encontrado"));
        model.addAttribute("quarto", quarto);
        return "quarto";
    }

    @PostMapping("/criar")
    public String criarQuarto(@ModelAttribute Quarto quarto,
                               RedirectAttributes redirectAttributes) {
        quartoService.criarQuarto(quarto);
        redirectAttributes.addFlashAttribute("mensagem", "Quarto criado com sucesso!");
        return "redirect:/quartos/hotel/" + quarto.getCodigoHotel();
    }

    @PostMapping("/atualizar/{codigoHotel}/{numero}")
    public String atualizarQuarto(@PathVariable Long codigoHotel,
                                   @PathVariable Integer numero,
                                   @ModelAttribute Quarto quarto,
                                   RedirectAttributes redirectAttributes) {
        quarto.setNumero(numero);
        quarto.setCodigoHotel(codigoHotel);
        quartoService.atualizarQuarto(quarto);
        redirectAttributes.addFlashAttribute("mensagem", "Quarto atualizado com sucesso!");
        return "redirect:/quartos/hotel/" + codigoHotel + "/numero/" + numero;
    }

    @GetMapping("/deletar/{codigoHotel}/{numero}")
    public String deletarQuarto(@PathVariable Long codigoHotel,
                                 @PathVariable Integer numero,
                                 RedirectAttributes redirectAttributes) {
        quartoService.buscarPorId(numero, codigoHotel)
                .orElseThrow(() -> new RuntimeException("Quarto não encontrado"));
        quartoService.deletarQuarto(numero, codigoHotel);
        redirectAttributes.addFlashAttribute("mensagem", "Quarto deletado com sucesso!");
        return "redirect:/quartos/hotel/" + codigoHotel;
    }
}