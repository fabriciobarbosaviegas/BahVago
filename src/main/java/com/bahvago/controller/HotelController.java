package com.bahvago.controller;

import com.bahvago.model.Avaliacao;
import com.bahvago.model.Hotel;
import com.bahvago.model.Localizacao;
import com.bahvago.model.Oferta;
import com.bahvago.model.Usuario;
import com.bahvago.service.AvaliacaoService;
import com.bahvago.service.GeocodingService;
import com.bahvago.service.HotelService;
import com.bahvago.service.LocalizacaoService;
import com.bahvago.service.OfertaService;
import com.bahvago.service.QuartoService;
import com.bahvago.service.UsuarioService;
import com.bahvago.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/hoteis")
public class HotelController {

    @Autowired
    private HotelService hotelService;

    @Autowired
    private AvaliacaoService avaliacaoService;

    @Autowired
    private OfertaService ofertaService;

    @Autowired
    private QuartoService quartoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private LocalizacaoService localizacaoService;

    @Autowired
    private GeocodingService geocodingService;

    @GetMapping
    public String listarHoteis(Model model) {
        List<Hotel> hoteis = hotelService.listarTodos();
        hotelService.preencherInformacaoPet(hoteis);
        hoteis.sort((h1, h2) -> {
            Double r1 = h1.getAvaliacaoMedia() != null ? h1.getAvaliacaoMedia() : 0.0;
            Double r2 = h2.getAvaliacaoMedia() != null ? h2.getAvaliacaoMedia() : 0.0;
            return r2.compareTo(r1);
        });
        List<Integer> ids = hoteis.stream().map(Hotel::getId).collect(Collectors.toList());
        model.addAttribute("hoteis", hoteis);
        model.addAttribute("ofertasPorHotel", mapOfertasPorHotel(hoteis));
        model.addAttribute("totalAvaliacoesPorHotel", avaliacaoService.contarAvaliacoesPorHoteis(ids));
        model.addAttribute("termo", "Todos os Hotéis");
        return "resultados";
    }

    @GetMapping("/gerenciar-hotel")
    public String gerenciarHotel(Authentication authentication, Model model) {

        Usuario usuario = usuarioAutenticado(authentication);

        Hotel hotel = hotelService.buscarPorHoteleiro(usuario.getCpf())
                .stream()
                .findFirst()
                .orElseThrow();

        hotelService.preencherInformacaoPet(List.of(hotel));

        model.addAttribute("hotel", hotel);

        return "gerenciar-hotel";
    }

    @GetMapping("/search")
    public String buscarHoteis(@RequestParam(value = "termo", required = false) String termo, @RequestParam(required = false) String checkin,
                                @RequestParam(required = false) String checkout,
                                @RequestParam(required = false) Integer pessoas,
                                @RequestParam(required = false) Integer quartos,
                                Model model) {
        List<Hotel> hoteis;

        if (termo == null || termo.trim().isEmpty()) {
            hoteis = hotelService.listarTodos();
            model.addAttribute("termo", "");
        } else {
            hoteis = hotelService.buscarPorNomeOuCidade(termo);
            model.addAttribute("termo", termo);
        }

        hotelService.preencherInformacaoPet(hoteis); 

        model.addAttribute("hoteis", hoteis);
        model.addAttribute("ofertasPorHotel", mapOfertasPorHotel(hoteis));

        model.addAttribute("checkin", checkin);
        model.addAttribute("checkout", checkout);
        model.addAttribute("pessoas", pessoas);
        model.addAttribute("quartos", quartos);
        
        return "resultados";
    }

    @GetMapping("/cidade/{cidade}")
    public String hotelsPorCidade(@PathVariable String cidade, Model model) {
        List<Hotel> hoteis = hotelService.buscarPorCidade(cidade);
        model.addAttribute("hoteis", hoteis);
        model.addAttribute("cidade", cidade);
        model.addAttribute("ofertasPorHotel", mapOfertasPorHotel(hoteis));
        return "resultados";
    }

    @GetMapping("/{id}")
    public String detalheHotel(@PathVariable Integer id,
                                @RequestParam(required = false) String checkin,
                                @RequestParam(required = false) String checkout,
                                @RequestParam(required = false) Integer pessoas,
                                @RequestParam(required = false) Integer quartosBusca,
                                Model model, Authentication authentication) {
        Hotel hotel = hotelService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Hotel não encontrado"));
        Double mediaAvaliacoes = avaliacaoService.calcularMediaAvaliacoes(id);

        List<com.bahvago.model.Quarto> quartos = quartoService.buscarPorHotel(id.longValue());
        boolean aceitaPet = quartos.stream().anyMatch(q -> Boolean.TRUE.equals(q.getAceitaPet()));
        boolean usuarioLogado = authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName());

        model.addAttribute("hotel", hotel);
        model.addAttribute("mediaAvaliacoes", mediaAvaliacoes);
        model.addAttribute("avaliacoes", avaliacaoService.buscarPorHotel(id));
        model.addAttribute("quartos", quartos);
        model.addAttribute("aceitaPet", aceitaPet);
        model.addAttribute("usuarioLogado", usuarioLogado);
        model.addAttribute("checkin", checkin);
        model.addAttribute("checkout", checkout);
        model.addAttribute("pessoas", pessoas);
        model.addAttribute("quartosBusca", quartosBusca);

        return "hotel";
    }

    @PostMapping("/criar")
    public String criarHotel(@ModelAttribute Hotel hotel,
                              @RequestParam(value = "imagemArquivos", required = false) List<MultipartFile> imagemArquivos,
                              @RequestParam(value = "imagensUrls", required = false) String imagensUrls,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        // CPF vem do usuário autenticado, nunca do formulário: evita que alguém
        // registre um hotel em nome de outro hoteleiro forjando esse campo.
        Usuario usuario = usuarioAutenticado(authentication);
        hotel.setCpf(usuario.getCpf());

        List<String> novasUrls = fileStorageService.salvarArquivos(imagemArquivos, "hoteis");
        if (imagensUrls != null && !imagensUrls.trim().isEmpty()) {
            for (String url : imagensUrls.split("[\n,]+")) {
                String cleanUrl = url.trim();
                if (!cleanUrl.isEmpty()) {
                    novasUrls.add(cleanUrl);
                }
            }
        }
        if (!novasUrls.isEmpty()) {
            hotel.getImagens().addAll(novasUrls);
        }
        Hotel novoHotel = hotelService.criarHotel(hotel);
        redirectAttributes.addFlashAttribute("mensagem", "Hotel criado com sucesso!");
        return "redirect:/hoteis/" + novoHotel.getId();
    }

    @PostMapping("/atualizar/{id}")
    public String atualizarHotel(@PathVariable Integer id,
                                  @ModelAttribute Hotel dadosFormulario,
                                  @RequestParam(value = "imagemArquivos", required = false) List<MultipartFile> imagemArquivos,
                                  @RequestParam(value = "imagensUrls", required = false) String imagensUrls,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {

        Hotel hotel = hotelService.buscarPorId(id) .orElseThrow(() -> new RuntimeException("Hotel não encontrado"));
        verificarPropriedade(hotel, usuarioAutenticado(authentication));

        hotel.setNome(dadosFormulario.getNome());
        hotel.setDescricao(dadosFormulario.getDescricao());

        boolean enderecoLocalizado = atualizarLocalizacaoComGeocoding(hotel, dadosFormulario.getLocalizacao());

        List<String> novasUrls = fileStorageService.salvarArquivos(imagemArquivos, "hoteis");
        if (imagensUrls != null && !imagensUrls.trim().isEmpty()) {
            for (String url : imagensUrls.split("[\n,]+")) {
                String cleanUrl = url.trim();
                if (!cleanUrl.isEmpty()) {
                    novasUrls.add(cleanUrl);
                }
            }
        }
        hotel.getImagens().addAll(novasUrls);

        hotelService.atualizarHotel(hotel);

        if (enderecoLocalizado) {
            redirectAttributes.addFlashAttribute("mensagem", "Hotel atualizado com sucesso!");
        } else {
            redirectAttributes.addFlashAttribute("erro",
                    "Hotel atualizado, mas não encontramos o endereço informado no mapa — a localização (latitude/longitude) pode ter ficado desatualizada.");
        }
        return "redirect:/hoteis/gerenciar-hotel";
    }

    /**
     * Geocodifica o endereço informado no formulário via Nominatim/OpenStreetMap e associa o
     * hotel à Localizacao correspondente (existente ou nova) por coordenadas. Se a geocodificação
     * falhar, mantém as coordenadas atuais e apenas atualiza os textos (cidade/estado/endereço)
     * na Localizacao já associada, para não perder os dados digitados pelo hoteleiro.
     */
    private boolean atualizarLocalizacaoComGeocoding(Hotel hotel, Localizacao dadosLocalizacao) {
        String cidade = dadosLocalizacao.getCidade();
        String estado = dadosLocalizacao.getEstado();
        String enderecoAproximado = dadosLocalizacao.getEnderecoAproximado();

        Localizacao localizacaoAtual = hotel.getLocalizacao();
        String pais = localizacaoAtual != null && StringUtils.hasText(localizacaoAtual.getPais())
                ? localizacaoAtual.getPais() : "Brasil";
        String cep = localizacaoAtual != null && localizacaoAtual.getCep() != null
                ? localizacaoAtual.getCep() : "";

        String enderecoParaGeocodificar = Stream.of(enderecoAproximado, cidade, estado, pais)
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(", "));

        Optional<GeocodingService.Coordenadas> coordenadas = geocodingService.buscarCoordenadas(enderecoParaGeocodificar);

        if (coordenadas.isPresent()) {
            Localizacao localizacao = localizacaoService.buscarOuCriar(
                    coordenadas.get().latitude(), coordenadas.get().longitude(),
                    cidade, estado, pais, cep, enderecoAproximado);
            hotel.setLatitude(localizacao.getId().getLatitude());
            hotel.setLongitude(localizacao.getId().getLongitude());
            hotel.setLocalizacao(localizacao);
            return true;
        }

        if (localizacaoAtual != null) {
            localizacaoAtual.setCidade(cidade);
            localizacaoAtual.setEstado(estado);
            localizacaoAtual.setEnderecoAproximado(enderecoAproximado);
            localizacaoService.atualizarLocalizacao(localizacaoAtual);
        }
        return false;
    }

    @PostMapping("/atualizar/{id}/imagem/remover")
    public String removerImagemHotel(@PathVariable Integer id,
                                      @RequestParam String url,
                                      Authentication authentication,
                                      RedirectAttributes redirectAttributes) {
        Hotel hotel = hotelService.buscarPorId(id).orElseThrow(() -> new RuntimeException("Hotel não encontrado"));
        verificarPropriedade(hotel, usuarioAutenticado(authentication));

        hotel.getImagens().remove(url);
        hotelService.atualizarHotel(hotel);

        redirectAttributes.addFlashAttribute("mensagem", "Imagem removida com sucesso!");
        return "redirect:/hoteis/gerenciar-hotel";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String tratarUploadGrandeDemais(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("erro",
                "Uma ou mais imagens são grandes demais (limite de 10MB por arquivo). Tente novamente com arquivos menores.");
        return "redirect:/hoteis/gerenciar-hotel";
    }

    @GetMapping("/deletar/{id}")
    public String deletarHotel(@PathVariable Integer id, Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        Hotel hotel = hotelService.buscarPorId(id).orElseThrow(() -> new RuntimeException("Hotel não encontrado"));
        verificarPropriedade(hotel, usuarioAutenticado(authentication));

        hotelService.deletarHotel(id);
        redirectAttributes.addFlashAttribute("mensagem", "Hotel deletado com sucesso!");
        return "redirect:/hoteis";
    }

    @PostMapping("/{id}/avaliacoes")
    @ResponseBody
    public ResponseEntity<?> criarAvaliacao(@PathVariable Integer id,
                                             @RequestParam Float nota,
                                             @RequestParam(required = false) String comentario,
                                             Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("erro", "Você precisa estar logado para avaliar."));
        }

        try {
            com.bahvago.model.Usuario usuario = usuarioService.buscarPorEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            Avaliacao avaliacao = Avaliacao.builder()
                    .nota(nota)
                    .comentario(comentario)
                    .codigoHotel(id)
                    .cpf(usuario.getCpf())
                    .build();

            Avaliacao salva = avaliacaoService.criarAvaliacao(avaliacao);
            return ResponseEntity.ok(Map.of(
                    "sucesso", true,
                    "codigoAvaliacao", salva.getId(),
                    "nomeUsuario", usuario.getNome(),
                    "nota", salva.getNota(),
                    "comentario", salva.getComentario() != null ? salva.getComentario() : "",
                    "data", salva.getData().toString()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Não foi possível salvar a avaliação: " + e.getMessage()));
        }
    }

    private Map<Integer, Oferta> mapOfertasPorHotel(List<Hotel> hoteis) {
        List<Integer> ids = hoteis.stream().map(Hotel::getId).collect(Collectors.toList());
        return ofertaService.mapOfertaPrincipalPorHotel(ids);
    }

    private Usuario usuarioAutenticado(Authentication authentication) {
        return usuarioService.buscarPorEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    private void verificarPropriedade(Hotel hotel, Usuario usuario) {
        if (!hotel.getCpf().equals(usuario.getCpf())) {
            throw new RuntimeException("Você não tem permissão para gerenciar este hotel");
        }
    }
}