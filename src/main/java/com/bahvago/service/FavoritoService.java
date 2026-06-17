package com.bahvago.service;

import com.bahvago.model.Favorito;
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

    public void removerFavorito(Long id) {
        favoritoRepository.deleteById(id);
    }

    public void removerFavoritoPorUsuarioEHotel(Long idUsuario, Long idHotel) {
        Optional<Favorito> favorito = favoritoRepository.findByIdUsuarioAndIdHotel(idUsuario, idHotel);
        favorito.ifPresent(f -> favoritoRepository.deleteById(f.getId()));
    }

    public List<Favorito> buscarFavoritosPorUsuario(Long idUsuario) {
        return favoritoRepository.findByIdUsuario(idUsuario);
    }

    public boolean isFavorito(Long idUsuario, Long idHotel) {
        return favoritoRepository.existsByIdUsuarioAndIdHotel(idUsuario, idHotel);
    }

    public Optional<Favorito> buscarFavorito(Long idUsuario, Long idHotel) {
        return favoritoRepository.findByIdUsuarioAndIdHotel(idUsuario, idHotel);
    }
}
