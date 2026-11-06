package utnfc.isi.back.taller.ordenes.clients;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import utnfc.isi.back.taller.commons.security.TokenUtils;
import utnfc.isi.back.taller.ordenes.dtos.NotificacionRequest;

/**
 * Colaboración con ms-notificaciones.
 * RN-09: si el aviso falla no se frena la orden; solo se registra en el log.
 */
@Component
public class NotificacionesClient {

    private static final Logger log = LoggerFactory.getLogger(NotificacionesClient.class);

    private final RestClient restClient;

    public NotificacionesClient(@Qualifier("notificacionesRestClient") RestClient notificacionesRestClient) {
        this.restClient = notificacionesRestClient;
    }

    public void enviar(NotificacionRequest request) {
        try {
            restClient.post()
                    .uri("/api/notificaciones")
                    .headers(this::autorizacion)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.warn("No se pudo solicitar la notificación de la orden {}: {}", request.ordenId(), e.getMessage());
        }
    }

    private void autorizacion(HttpHeaders headers) {
        TokenUtils.tokenActual().ifPresent(headers::setBearerAuth);
    }
}
