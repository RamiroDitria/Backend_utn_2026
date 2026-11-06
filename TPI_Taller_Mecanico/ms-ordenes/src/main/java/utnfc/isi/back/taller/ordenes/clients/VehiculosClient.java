package utnfc.isi.back.taller.ordenes.clients;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.databind.ObjectMapper;

import utnfc.isi.back.taller.commons.exceptions.ApiError;
import utnfc.isi.back.taller.commons.exceptions.ServiceException;
import utnfc.isi.back.taller.commons.security.TokenUtils;
import utnfc.isi.back.taller.ordenes.dtos.VehiculoDTO;
import utnfc.isi.back.taller.ordenes.dtos.VinRequest;

/** Colaboración con ms-clientes-vehiculos. Propaga el JWT del usuario que hizo la operación. */
@Component
public class VehiculosClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public VehiculosClient(@Qualifier("vehiculosRestClient") RestClient vehiculosRestClient, ObjectMapper objectMapper) {
        this.restClient = vehiculosRestClient;
        this.objectMapper = objectMapper;
    }

    public VehiculoDTO obtener(Long vehiculoId) {
        try {
            return restClient.get()
                    .uri("/api/vehiculos/{id}", vehiculoId)
                    .headers(this::autorizacion)
                    .retrieve()
                    .body(VehiculoDTO.class);
        } catch (RestClientResponseException e) {
            throw traducir(e);
        } catch (ResourceAccessException e) {
            throw noDisponible();
        }
    }

    public VehiculoDTO registrarVin(Long vehiculoId, VinRequest request) {
        try {
            return restClient.put()
                    .uri("/api/vehiculos/{id}/vin", vehiculoId)
                    .headers(this::autorizacion)
                    .body(request)
                    .retrieve()
                    .body(VehiculoDTO.class);
        } catch (RestClientResponseException e) {
            throw traducir(e);
        } catch (ResourceAccessException e) {
            throw noDisponible();
        }
    }

    private void autorizacion(HttpHeaders headers) {
        TokenUtils.tokenActual().ifPresent(headers::setBearerAuth);
    }

    /** Reenvía el error de negocio del otro servicio con su mismo código y status. */
    private ServiceException traducir(RestClientResponseException e) {
        try {
            ApiError error = objectMapper.readValue(e.getResponseBodyAsByteArray(), ApiError.class);
            return new ServiceException(error.codigo(), error.mensaje(), e.getStatusCode().value());
        } catch (Exception ignorada) {
            return new ServiceException("ERROR_MS_VEHICULOS",
                    "ms-clientes-vehiculos respondió " + e.getStatusCode().value(), e.getStatusCode().value());
        }
    }

    private ServiceException noDisponible() {
        return new ServiceException("SERVICIO_NO_DISPONIBLE", "ms-clientes-vehiculos no responde", 503);
    }
}
