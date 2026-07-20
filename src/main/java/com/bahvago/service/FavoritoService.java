package com.bahvago.service;

import com.bahvago.model.Favorito;
import com.bahvago.model.FavoritoId;
import com.bahvago.repository.FavoritoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class FavoritoService {

    @Autowired
    private FavoritoRepository favoritoRepository;

    public Favorito adicionarFavorito(Favorito favorito) {
        return favoritoRepository.save(favorito);
    }

    public void removerFavorito(FavoritoId id) {
        favoritoRepository.deleteById(id);
    }

    public void removerFavoritoPorUsuarioEHotel(String cpf, Integer codigoHotel) {
        favoritoRepository.deleteById(new FavoritoId(cpf, codigoHotel));
    }

    public List<Favorito> buscarFavoritosPorUsuario(String cpf) {
        return favoritoRepository.findByIdCpf(cpf);
    }

    public boolean isFavorito(String cpf, Integer codigoHotel) {
        return favoritoRepository.existsByIdCpfAndIdCodigoHotel(cpf, codigoHotel);
    }

    public Optional<Favorito> buscarFavorito(String cpf, Integer codigoHotel) {
        return favoritoRepository.findByIdCpfAndIdCodigoHotel(cpf, codigoHotel);
    }
}