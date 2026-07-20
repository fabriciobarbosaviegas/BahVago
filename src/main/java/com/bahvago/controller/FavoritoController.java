package com.bahvago.controller;

import com.bahvago.model.Usuario;
import com.bahvago.service.FavoritoService;
import com.bahvago.service.FavoritoService;
import com.bahvago.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/favoritos")
public class FavoritoController {

    @Autowired
    private FavoritoService favoritoService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public String listarFavoritos(Authentication authentication, Model model) {
        Usuario usuario = buscarUsuarioAutenticado(authentication);
        model.addAttribute("ofertasSalvas", favoritoService.listarOfertasSalvas(usuario.getCpf()));
        return "favoritos";
    }

    @GetMapping("/usuario/{cpf}")
    public String listarFavoritosLegado(@PathVariable String cpf, Authentication authentication) {
        if (authentication != null) {
            return "redirect:/favoritos";
        }
        return "redirect:/login";
    }

    @GetMapping("/ids")
    @ResponseBody
    public ResponseEntity<List<Integer>> listarIds(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Usuario usuario = buscarUsuarioAutenticado(authentication);
        return ResponseEntity.ok(favoritoService.listarCodigosOfertaPorUsuario(usuario.getCpf()));
    }

    @PostMapping("/toggle/{codigoOferta}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> alternar(@PathVariable Integer codigoOferta,
                                                          Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Usuario usuario = buscarUsuarioAutenticado(authentication);
        boolean salvo = favoritoService.alternar(usuario.getCpf(), codigoOferta);

        Map<String, Object> resposta = new HashMap<>();
        resposta.put("salvo", salvo);
        resposta.put("codigoOferta", codigoOferta);
        return ResponseEntity.ok(resposta);
    }

    @DeleteMapping("/remover/{codigoOferta}")
    @ResponseBody
    public ResponseEntity<Void> remover(@PathVariable Integer codigoOferta,
                                        Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Usuario usuario = buscarUsuarioAutenticado(authentication);
        favoritoService.remover(usuario.getCpf(), codigoOferta);
        return ResponseEntity.noContent().build();
    }

    private Usuario buscarUsuarioAutenticado(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Usuario nao autenticado");
        }

        return usuarioService.buscarPorEmail(authentication.getName())
            .orElseThrow(() -> new RuntimeException("Usuario autenticado nao encontrado"));
    }
}
