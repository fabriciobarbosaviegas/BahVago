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

    @GetMapping("/hotel/{idHotel}")
    public String listarQuartosPorHotel(@PathVariable Long idHotel, Model model) {
        List<Quarto> quartos = quartoService.buscarPorHotel(idHotel);
        model.addAttribute("quartos", quartos);
        model.addAttribute("idHotel", idHotel);
        return "novo-quarto";
    }

    @GetMapping("/{id}")
    public String detalheQuarto(@PathVariable Long id, Model model) {
        Quarto quarto = quartoService.buscarPorId(id)
            .orElseThrow(() -> new RuntimeException("Quarto não encontrado"));
        model.addAttribute("quarto", quarto);
        return "quarto";
    }

    @PostMapping("/criar")
    public String criarQuarto(@ModelAttribute Quarto quarto,
                             RedirectAttributes redirectAttributes) {
        Quarto novoQuarto = quartoService.criarQuarto(quarto);
        redirectAttributes.addFlashAttribute("mensagem", "Quarto criado com sucesso!");
        return "redirect:/quartos/hotel/" + quarto.getIdHotel();
    }

    @PostMapping("/atualizar/{id}")
    public String atualizarQuarto(@PathVariable Long id,
                                 @ModelAttribute Quarto quarto,
                                 RedirectAttributes redirectAttributes) {
        quarto.setId(id);
        Quarto quartoAtualizado = quartoService.atualizarQuarto(quarto);
        redirectAttributes.addFlashAttribute("mensagem", "Quarto atualizado com sucesso!");
        return "redirect:/quartos/" + id;
    }

    @GetMapping("/deletar/{id}")
    public String deletarQuarto(@PathVariable Long id, 
                               RedirectAttributes redirectAttributes) {
        Quarto quarto = quartoService.buscarPorId(id)
            .orElseThrow(() -> new RuntimeException("Quarto não encontrado"));
        Long idHotel = quarto.getIdHotel();
        quartoService.deletarQuarto(id);
        redirectAttributes.addFlashAttribute("mensagem", "Quarto deletado com sucesso!");
        return "redirect:/quartos/hotel/" + idHotel;
    }
}
