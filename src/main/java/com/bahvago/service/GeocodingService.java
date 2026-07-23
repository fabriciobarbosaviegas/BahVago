package com.bahvago.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Geocodifica endereços usando a API pública Nominatim do OpenStreetMap.
 */
@Service
public class GeocodingService {

    private static final Logger log = LoggerFactory.getLogger(GeocodingService.class);

    private final RestClient restClient = RestClient.builder()
            .requestFactory(new SimpleClientHttpRequestFactory())
            .build();

    @Value("${geocoding.nominatim.url}")
    private String nominatimUrl;

    @Value("${geocoding.nominatim.user-agent}")
    private String userAgent;

    public Optional<Coordenadas> buscarCoordenadas(String enderecoCompleto) {
        if (!StringUtils.hasText(enderecoCompleto)) {
            return Optional.empty();
        }

        try {
            URI uri = UriComponentsBuilder.fromUriString(nominatimUrl)
                    .queryParam("q", enderecoCompleto)
                    .queryParam("format", "jsonv2")
                    .queryParam("limit", 1)
                    .build()
                    .encode()
                    .toUri();

            List<Map<String, Object>> resposta = restClient.get()
                    .uri(uri)
                    .header("User-Agent", userAgent)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {
                    });

            if (resposta == null || resposta.isEmpty()) {
                log.warn("Nominatim não encontrou coordenadas para o endereço '{}'", enderecoCompleto);
                return Optional.empty();
            }

            Map<String, Object> primeiroResultado = resposta.get(0);
            double latitude = Double.parseDouble(String.valueOf(primeiroResultado.get("lat")));
            double longitude = Double.parseDouble(String.valueOf(primeiroResultado.get("lon")));

            // O banco guarda lat/lon como inteiro (micrograus: grau * 1.000.000),
            // igual ao dado já existente em HotelEstatisticas/Localizacao.
            int latitudeArmazenada = (int) Math.round(latitude * 1_000_000);
            int longitudeArmazenada = (int) Math.round(longitude * 1_000_000);

            return Optional.of(new Coordenadas(latitudeArmazenada, longitudeArmazenada));
        } catch (Exception e) {
            log.warn("Falha ao geocodificar endereço '{}' via Nominatim: {}", enderecoCompleto, e.getMessage());
            return Optional.empty();
        }
    }

    public record Coordenadas(int latitude, int longitude) {
    }
}
