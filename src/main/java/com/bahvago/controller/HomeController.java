package com.bahvago.controller;

import com.bahvago.model.Hotel;
import com.bahvago.model.Oferta;
import com.bahvago.model.Quarto;
import com.bahvago.model.Usuario;
import com.bahvago.service.AvaliacaoService;
import com.bahvago.service.HotelService;
import com.bahvago.service.ManutencaoQuartoService;
import com.bahvago.service.OfertaService;
import com.bahvago.service.QuartoService;
import com.bahvago.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Controller
public class HomeController {

    @Autowired
    private HotelService hotelService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private OfertaService ofertaService;

    @Autowired
    private AvaliacaoService avaliacaoService;

    @Autowired
    private QuartoService quartoService;

    @Autowired
    private ManutencaoQuartoService manutencaoQuartoService;

    @GetMapping("/")
    public String index(Model model) {
        List<Hotel> hoteis = hotelService.listarTodos();
        hotelService.preencherInformacaoPet(hoteis); 
        List<Integer> ids = hoteis.stream().map(Hotel::getId).collect(Collectors.toList());
        model.addAttribute("hoteis", hoteis);
        model.addAttribute("ofertasPorHotel", mapOfertasPorHotel(hoteis));
        model.addAttribute("totalAvaliacoesPorHotel", avaliacaoService.contarAvaliacoesPorHoteis(ids));
        return "index";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {

        Usuario usuario = usuarioService.buscarPorEmail(authentication.getName())
                .orElseThrow();

        Hotel hotel = hotelService.buscarPorHoteleiro(usuario.getCpf())
                .stream()
                .findFirst()
                .orElseThrow();

        model.addAttribute("hotel", hotel);
        Long codigoHotel = hotel.getId().longValue();
        long totalQuartos = quartoService.contarTotal(codigoHotel);
        long quartosDisponiveis = quartoService.contarDisponiveis(codigoHotel);
        long quartosEmManutencao = manutencaoQuartoService.contar(codigoHotel);
        long quartosOcupados = quartoService.contarIndisponiveis(codigoHotel) - quartosEmManutencao;
        double taxaOcupacao = totalQuartos == 0 ? 0 : (quartosOcupados * 100.0) / totalQuartos;

        model.addAttribute("taxaOcupacao", taxaOcupacao);
        model.addAttribute("totalQuartos", totalQuartos);
        model.addAttribute("quartosDisponiveis", quartosDisponiveis);
        model.addAttribute("quartosOcupados", quartosOcupados);
        model.addAttribute("quartosEmManutencao", quartosEmManutencao);
        return "dashboard";
    }

    @GetMapping("/gerenciar-quartos")
    public String gerenciarQuartos(Authentication authentication, Model model) {

        Usuario usuario = usuarioService.buscarPorEmail(authentication.getName())
                .orElseThrow();

        Hotel hotel = hotelService.buscarPorHoteleiro(usuario.getCpf())
                .stream()
                .findFirst()
                .orElseThrow();

        List<Quarto> quartos = quartoService.buscarPorHotel(hotel.getId().longValue());

        model.addAttribute("hotel", hotel);
        model.addAttribute("quartos", quartos);

        return "gerenciar-quartos";
    }

    @GetMapping("/gerenciar-avaliacoes")
    public String gerenciarAvaliacoes(Authentication authentication, Model model) {

        Usuario usuario = usuarioService.buscarPorEmail(authentication.getName())
                .orElseThrow();

        Hotel hotel = hotelService.buscarPorHoteleiro(usuario.getCpf())
                .stream()
                .findFirst()
                .orElseThrow();

        model.addAttribute("hotel", hotel);

        model.addAttribute("avaliacoes",
                avaliacaoService.buscarPorHotel(hotel.getId()));

        return "gerenciar-avaliacoes";
    }

    @GetMapping("/cadastro")
    public String cadastro() {
        return "redirect:/usuarios/cadastro";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/login-hoteleiro")
    public String loginHoteleiro() {
        return "login-hoteleiro";
    }

    private Map<Integer, Oferta> mapOfertasPorHotel(List<Hotel> hoteis) {
        List<Integer> ids = hoteis.stream().map(Hotel::getId).collect(Collectors.toList());
        return ofertaService.mapOfertaPrincipalPorHotel(ids);
    }
}
