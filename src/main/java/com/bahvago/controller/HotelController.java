package com.bahvago.controller;

import com.bahvago.model.Hotel;
import com.bahvago.service.HotelService;
import com.bahvago.service.AvaliacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/hoteis")
public class HotelController {

    @Autowired
    private HotelService hotelService;

    @Autowired
    private AvaliacaoService avaliacaoService;

    @GetMapping
    public String listarHoteis(Model model) {
        List<Hotel> hoteis = hotelService.listarTodos();
        model.addAttribute("hoteis", hoteis);
        return "hotel";
    }

    @GetMapping("/search")
    public String buscarHoteis(@RequestParam String termo, Model model) {
        List<Hotel> hoteis = hotelService.buscarPorNomeOuCidade(termo);
        model.addAttribute("hoteis", hoteis);
        model.addAttribute("termo", termo);
        return "resultados";
    }

    @GetMapping("/cidade/{cidade}")
    public String hotelsPorCidade(@PathVariable String cidade, Model model) {
        List<Hotel> hoteis = hotelService.buscarPorCidade(cidade);
        model.addAttribute("hoteis", hoteis);
        model.addAttribute("cidade", cidade);
        return "resultados";
    }

    @GetMapping("/{id}")
    public String detalheHotel(@PathVariable Long id, Model model) {
        Hotel hotel = hotelService.buscarPorId(id)
            .orElseThrow(() -> new RuntimeException("Hotel não encontrado"));
        Double mediaAvaliacoes = avaliacaoService.calcularMediaAvaliacoes(id);
        model.addAttribute("hotel", hotel);
        model.addAttribute("mediaAvaliacoes", mediaAvaliacoes);
        model.addAttribute("avaliacoes", avaliacaoService.buscarPorHotel(id));
        return "hotel";
    }

    @PostMapping("/criar")
    public String criarHotel(@ModelAttribute Hotel hotel, 
                            RedirectAttributes redirectAttributes) {
        Hotel novoHotel = hotelService.criarHotel(hotel);
        redirectAttributes.addFlashAttribute("mensagem", "Hotel criado com sucesso!");
        return "redirect:/hoteis/" + novoHotel.getId();
    }

    @PostMapping("/atualizar/{id}")
    public String atualizarHotel(@PathVariable Long id, 
                                @ModelAttribute Hotel hotel,
                                RedirectAttributes redirectAttributes) {
        hotel.setId(id);
        hotelService.atualizarHotel(hotel);
        redirectAttributes.addFlashAttribute("mensagem", "Hotel atualizado com sucesso!");
        return "redirect:/hoteis/" + id;
    }

    @GetMapping("/deletar/{id}")
    public String deletarHotel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        hotelService.deletarHotel(id);
        redirectAttributes.addFlashAttribute("mensagem", "Hotel deletado com sucesso!");
        return "redirect:/hoteis";
    }
}
