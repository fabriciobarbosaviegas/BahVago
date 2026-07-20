package com.bahvago.controller;

import com.bahvago.model.Favorito;
import com.bahvago.model.FavoritoId;
import com.bahvago.service.FavoritoService;
import com.bahvago.service.HotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/favoritos")
public class FavoritoController {

    @Autowired
    private FavoritoService favoritoService;

    @Autowired
    private HotelService hotelService;

    @GetMapping("/usuario/{cpf}")
    public String meusFavoritos(@PathVariable String cpf, Model model) {
        List<Favorito> favoritos = favoritoService.buscarFavoritosPorUsuario(cpf);
        model.addAttribute("favoritos", favoritos);
        return "favoritos";
    }

    @PostMapping("/adicionar")
    public String adicionarFavorito(@RequestParam String cpf,
                                     @RequestParam Integer codigoHotel,
                                     RedirectAttributes redirectAttributes) {
        Favorito favorito = Favorito.builder()
                .id(new FavoritoId(cpf, codigoHotel))
                .build();
        favoritoService.adicionarFavorito(favorito);
        redirectAttributes.addFlashAttribute("mensagem", "Hotel adicionado aos favoritos!");
        return "redirect:/hoteis/" + codigoHotel;
    }

    @GetMapping("/remover/{cpf}/{codigoHotel}")
    public String removerFavorito(@PathVariable String cpf,
                                   @PathVariable Integer codigoHotel,
                                   RedirectAttributes redirectAttributes) {
        favoritoService.removerFavoritoPorUsuarioEHotel(cpf, codigoHotel);
        redirectAttributes.addFlashAttribute("mensagem", "Hotel removido dos favoritos!");
        return "redirect:/hoteis/" + codigoHotel;
    }
}