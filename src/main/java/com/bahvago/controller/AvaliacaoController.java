package com.bahvago.controller;

import com.bahvago.service.AvaliacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/avaliacoes")
public class AvaliacaoController {

    @Autowired
    private AvaliacaoService avaliacaoService;

    @PostMapping("/{id}/responder")
    public ResponseEntity<Void> responder(
            @PathVariable Integer id,
            @RequestBody Map<String, String> body) {

        avaliacaoService.responderAvaliacao(id, body.get("resposta"));

        return ResponseEntity.ok().build();
    }
}