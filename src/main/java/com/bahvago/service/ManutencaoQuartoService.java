package com.bahvago.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;

/**
 * Guarda em memória quais quartos foram marcados como "em manutenção", distinto de
 * simplesmente indisponível. Não existe coluna para isso na tabela Quarto (regra de
 * ouro: nenhuma migration é alterada), então esse é um estado auxiliar da aplicação,
 * não do banco — some se a aplicação reiniciar, mas o quarto continua indisponível
 * de qualquer forma (Disponivel=false é sempre persistido normalmente).
 */
@Service
public class ManutencaoQuartoService {

    private final Set<String> quartosEmManutencao = ConcurrentHashMap.newKeySet();

    private String chave(Long codigoHotel, Integer numero) {
        return codigoHotel + ":" + numero;
    }

    public void marcar(Long codigoHotel, Integer numero) {
        quartosEmManutencao.add(chave(codigoHotel, numero));
    }

    public void desmarcar(Long codigoHotel, Integer numero) {
        quartosEmManutencao.remove(chave(codigoHotel, numero));
    }

    public boolean estaEmManutencao(Long codigoHotel, Integer numero) {
        return quartosEmManutencao.contains(chave(codigoHotel, numero));
    }
}
