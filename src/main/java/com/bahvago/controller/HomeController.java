package com.bahvago.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/estatisticas")
    public String estatisticas() {
        return "estatisticas";
    }

    @GetMapping("/gerenciar-hotel")
    public String gerenciarHotel() {
        return "gerenciar-hotel";
    }

    @GetMapping("/gerenciar-quartos")
    public String gerenciarQuartos() {
        return "gerenciar-quartos";
    }

    @GetMapping("/gerenciar-avaliacoes")
    public String gerenciarAvaliacoes() {
        return "gerenciar-avaliacoes";
    }

    @GetMapping("/cadastro")
    public String cadastro() {
        return "cadastro";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/login-hoteleiro")
    public String loginHoteleiro() {
        return "login-hoteleiro";
    }
}
