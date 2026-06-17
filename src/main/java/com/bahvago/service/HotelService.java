package com.bahvago.service;

import com.bahvago.model.Hotel;
import com.bahvago.repository.HotelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class HotelService {

    @Autowired
    private HotelRepository hotelRepository;

    public Hotel criarHotel(Hotel hotel) {
        return hotelRepository.save(hotel);
    }

    public Optional<Hotel> buscarPorId(Long id) {
        return hotelRepository.findById(id);
    }

    public List<Hotel> listarTodos() {
        return hotelRepository.findAll();
    }

    public List<Hotel> buscarPorCidade(String cidade) {
        return hotelRepository.findByCidade(cidade);
    }

    public List<Hotel> buscarPorEstado(String estado) {
        return hotelRepository.findByEstado(estado);
    }

    public List<Hotel> buscarPorHoteleiro(Long idHoteleiro) {
        return hotelRepository.findByIdHoteleiro(idHoteleiro);
    }

    public List<Hotel> buscarPorNomeOuCidade(String termo) {
        return hotelRepository.buscarPorNomeOuCidade(termo);
    }

    public Hotel atualizarHotel(Hotel hotel) {
        return hotelRepository.save(hotel);
    }

    public void deletarHotel(Long id) {
        hotelRepository.deleteById(id);
    }
}
