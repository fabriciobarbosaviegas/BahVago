package com.bahvago.service;

import com.bahvago.model.Oferta;
import com.bahvago.repository.OfertaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OfertaService {

    @Autowired
    private OfertaRepository ofertaRepository;

    public Optional<Oferta> buscarPorId(Integer id) {
        return ofertaRepository.findById(id);
    }

    public List<Oferta> buscarPorHotel(Integer codigoHotel) {
        return ofertaRepository.findByCodigoHotel(codigoHotel);
    }

    public Optional<Oferta> buscarOfertaPrincipalPorHotel(Integer codigoHotel) {
        return ofertaRepository.findFirstByCodigoHotelOrderByPrecoAsc(codigoHotel);
    }

    public Map<Integer, Integer> mapOfertaPrincipalPorHotel(List<Integer> codigosHotel) {
        Map<Integer, Integer> mapa = new HashMap<>();
        for (Integer codigoHotel : codigosHotel) {
            buscarOfertaPrincipalPorHotel(codigoHotel)
                .ifPresent(oferta -> mapa.put(codigoHotel, oferta.getId()));
        }
        return mapa;
    }
}
