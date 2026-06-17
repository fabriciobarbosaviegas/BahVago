package com.bahvago.controller;

import com.bahvago.model.Favorito;
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

    @GetMapping("/usuario/{idUsuario}")
    public String meusFavoritos(@PathVariable Long idUsuario, Model model) {
        List<Favorito> favoritos = favoritoService.buscarFavoritosPorUsuario(idUsuario);
        model.addAttribute("favoritos", favoritos);
        return "favoritos";
    }

    @PostMapping("/adicionar")
    public String adicionarFavorito(@RequestParam Long idUsuario,
                                   @RequestParam Long idHotel,
                                   RedirectAttributes redirectAttributes) {
        Favorito favorito = Favorito.builder()
            .idUsuario(idUsuario)
            .idHotel(idHotel)
            .build();
        
        favoritoService.adicionarFavorito(favorito);
        redirectAttributes.addFlashAttribute("mensagem", "Hotel adicionado aos favoritos!");
        return "redirect:/hoteis/" + idHotel;
    }

    @GetMapping("/remover/{idUsuario}/{idHotel}")
    public String removerFavorito(@PathVariable Long idUsuario,
                                 @PathVariable Long idHotel,
                                 RedirectAttributes redirectAttributes) {
        favoritoService.removerFavoritoPorUsuarioEHotel(idUsuario, idHotel);
        redirectAttributes.addFlashAttribute("mensagem", "Hotel removido dos favoritos!");
        return "redirect:/hoteis/" + idHotel;
    }
}
